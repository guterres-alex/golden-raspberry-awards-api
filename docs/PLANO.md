# Plano de implementação: Golden Raspberry Awards API

## Contexto
O projeto tem só o esqueleto do Spring Boot 4.1.1: a classe `GoldenRaspberryAwardsApiApplication`, o teste `contextLoads`, o `movielist.csv` e um `application.properties` com uma única linha. O `pom.xml` já traz webmvc, data-jpa, h2, h2console e os starters de teste `*-webmvc-test` e `*-data-jpa-test`, então não é preciso adicionar dependências.
O objetivo é implementar o que o `CLAUDE.md` descreve: a carga do CSV ao iniciar e o endpoint `GET /api/producers/award-intervals`. O trabalho vai em etapas pequenas, cada uma revisada separadamente.

Pacote base: `io.github.guterresalex.goldenraspberry` (abreviado como `…` abaixo).
Toda etapa termina com `./mvnw test`, relatando executados, falhas e erros, e para para revisão. O agente não faz commits e trabalha só na branch atual. O desenvolvedor cria as branches e faz os merges.

---

## Etapa 1: Configuração, entidades e repositórios
**Objetivo:** criar o modelo de dados e o schema gerado pelo Hibernate no H2.
- `src/main/resources/application.properties`: `app.movies.csv-path=classpath:movielist.csv`, `spring.mvc.problemdetails.enabled=true`, `spring.jpa.open-in-view=false`. Sem `spring.datasource.url`. O H2 console fica desligado.
- `…/domain/Movie.java`: `id`, `year` → `@Column(name="release_year")`, `title`, `winner`. `@ManyToMany Set<Producer>` e `@ManyToMany Set<Studio>`. O `Set` garante um único vínculo quando o produtor se repete no mesmo filme.
- `…/domain/Producer.java` e `…/domain/Studio.java`: `id`, `name` (grafia exibida) e `nameKey` (único), com um método estático que gera a chave de normalização do CLAUDE.md.
- `…/repository/MovieRepository.java`, `ProducerRepository.java`, `StudioRepository.java` (`JpaRepository`).

**Verificação:** `./mvnw test` com o `contextLoads` verde e sem erros de DDL no log (principalmente em `release_year`).
**Branch:** `feat/entidades`. **Commit:** `feat: adiciona entidades Movie, Producer e Studio e repositórios`

> ⏸ **Parada obrigatória:** aqui o desenvolvedor decide sobre o Lombok. Hoje o CLAUDE.md proíbe Lombok. Se a decisão for usar, o `pom.xml` (dependência e annotation processor) e o CLAUDE.md precisam mudar antes de seguir.

---

## Etapa 2: Leitura e carga do CSV na inicialização
**Objetivo:** ler e validar o arquivo e persistir os filmes uma única vez ao iniciar, numa única transação.
- `…/config/MoviesProperties.java`: `record` anotado com `@ConfigurationProperties("app.movies")`, com o campo `csvPath`.
- `…/dto/MovieCsvRow.java`: `record(int year, String title, List<String> studios, List<String> producers, boolean winner, int lineNumber)`.
- `…/service/MovieCsvReader.java`:
  - exige o prefixo `classpath:` ou `file:` e, sem ele, falha a inicialização;
  - resolve o caminho via `ResourceLoader` e checa se o arquivo existe;
  - lê em UTF-8, remove o BOM e valida o cabeçalho (arquivo vazio é inválido);
  - em cada linha: ignora as em branco, aplica `split(";", -1)` e trim, valida e separa produtores e estúdios com a regex;
  - linha malformada gera `log.warn` com o número da linha;
  - prefixo ausente, arquivo inexistente e cabeçalho inválido lançam `IllegalStateException` com mensagem clara.
- `…/service/MovieImportService.java`: `@Transactional import(List<MovieCsvRow>)`.
  - Filme repetido (título sem diferenciar caixa + ano): vale a primeira ocorrência, e a repetição gera `log.warn`.
  - Mantém em memória um `Map<nameKey, Producer>` e um `Map<nameKey, Studio>`, para que a primeira grafia prevaleça e para não consultar o banco a cada linha.
  - Ao final, registra o total carregado com `log.info`.
- `…/config/MovieDataLoader.java`: `SmartInitializingSingleton` que, em `afterSingletonsInstantiated()`, chama o leitor e depois o `import`. Roda durante o refresh, antes de o servidor web aceitar requisições, e uma exceção derruba a inicialização.
  (Na primeira versão era um `ApplicationRunner`, que roda com o servidor já no ar; trocado na revisão final.)

**Verificação:** `./mvnw test` verde, com o `contextLoads` já carregando o CSV original. Depois, `./mvnw spring-boot:run`: a aplicação deve subir e o log deve mostrar o total de filmes carregados, sem warns inesperados no CSV original.
**Branch:** `feat/carga-csv`. **Commit:** `feat: lê e carrega o CSV de filmes no H2 na inicialização`

---

## Etapa 3: Cálculo dos intervalos e endpoint
**Objetivo:** expor `GET /api/producers/award-intervals`.
- `…/dto/ProducerWinYear.java`: `record(Long producerId, String producer, int year)`, a projeção da query.
- `ProducerRepository`: uma query JPQL com projeção de construtor: `select new …(p.id, p.name, m.year) from Movie m join m.producers p where m.winner = true order by p.id, m.year`.
- `…/dto/ProducerInterval.java` (`producer`, `interval`, `previousWin`, `followingWin`) e `…/dto/AwardIntervalsResponse.java` (`min`, `max`).
- `…/service/AwardIntervalService.java`: uma única passada sobre a lista ordenada. Guarda o min e o max correntes com seus empatados e, no fim, ordena por `previousWin` e depois por `producer`.
- `…/controller/ProducerController.java`: `@GetMapping("/api/producers/award-intervals")`.
- Teste: o `contextLoads` sai e entra `AwardIntervalsOriginalCsvTest`, com `@SpringBootTest` + `@AutoConfigureMockMvc`, que compara em modo STRICT o oráculo (Joel Silver 1, 1990→1991 / Matthew Vaughn 13, 2002→2015).

**Verificação:** `./mvnw test` verde com o teste do oráculo. Os imports do Boot 4 (`AutoConfigureMockMvc` em `org.springframework.boot.webmvc.test.autoconfigure`, `JsonCompareMode`) são confirmados na compilação. Com a aplicação rodando, conferir também `curl localhost:8080/api/producers/award-intervals`.
**Branch:** `feat/intervalos-premios`. **Commit:** `feat: expõe intervalos mínimo e máximo entre prêmios de produtores`

---

## Etapa 4: Cenários de teste com CSVs alternativos (skill `cenario-teste-csv`)
**Objetivo:** cobrir os casos de borda do CLAUDE.md. Cada cenário é um par criado pela skill: um CSV em `src/test/resources/datasets/` e uma classe `*Test`. Cada um é aprovado antes de ser criado e para ao final, como a skill define.
Ordem sugerida:
1. resultado vazio
2. intervalo 0
3. empates no min/max. A ordem de aparição no CSV deve ser diferente das chaves de ordenação, senão o teste passa mesmo sem o sort:
   - dois produtores empatados com o mesmo `previousWin`, o de nome maior aparecendo primeiro (ex.: "Zed" antes de "Amy"), para testar o desempate por `producer`;
   - um produtor que aparece primeiro no CSV mas tem `previousWin` maior, para testar a chave principal.
4. vários pares do mesmo produtor. As linhas ficam fora de ordem cronológica no CSV (ex.: 2015, 2002, 2008), para que o teste falhe se o `m.year` sair do `order by` da query.
5. nomes com caixa e espaços diferentes
6. separadores `,` / ` and ` / `, and `
7. produtor repetido no mesmo filme
8. filme repetido (vale a primeira ocorrência)
9. linhas malformadas ignoradas. Os dois lados do limite de 255 são testados:
   - título com exatamente 255 caracteres, numa vitória que precisa contar;
   - título, produtor e estúdio com 256 caracteres, cada um numa linha vencedora que precisa ser ignorada.
   O JSON esperado só fica certo se as regras de limite estiverem todas corretas.
10. `winner` sem diferenciar maiúsculas e com trim: `YES`, `Yes` e ` yes ` contam como vitória; `y`, `true` e `no` não contam.
    Um vencedor falso fica entre duas vitórias reais do mesmo produtor, de modo que contá-lo mudaria o `min`.
11. BOM e cabeçalho válido com variações: o arquivo começa com os bytes `EF BB BF` e o cabeçalho é ` Year ; TITLE;Studios;producers ;WINNER`.
    A aplicação deve subir e devolver um resultado conhecido. Conferir os bytes do arquivo com `od -c` depois de criá-lo.
12. falha de inicialização: arquivo inexistente, cabeçalho inválido e caminho sem prefixo (`assertThrows` com `SpringApplicationBuilder`).
    A exceção do leitor sai do `refresh()` sem embrulho, mas os testes conferem a causa mais específica (tipo e mensagem),
    para não depender de como o Spring propaga a falha.
13. verbos e status HTTP (nível 2 de Richardson), com o CSV original e sem dataset próprio:
    - `POST /api/producers/award-intervals` → `405` com content type compatível com `application/problem+json`;
    - `GET` numa URL inexistente → `404` com content type compatível com `application/problem+json`.

**Verificação:** `./mvnw test` a cada cenário, com o total de testes aumentando. Se um cenário falhar com o cálculo correto, ele achou um bug: o agente para e relata.
**Branch:** `test/cenarios-csv`. Um commit por cenário, ex.: `test: adiciona cenário de intervalo zero`.

---

## Etapa 5: Documentação (obrigatória)
**Objetivo:** criar o `README.md` exigido pela especificação, com requisitos (Java 21), como rodar (`./mvnw spring-boot:run`, `mvnw.cmd` no Windows), como testar (`./mvnw test`), a propriedade `app.movies.csv-path`, o endpoint com exemplo de resposta e as regras de carga do CSV.
**Verificação:** seguir o README do zero (subir a aplicação, chamar o endpoint, rodar os testes) e conferir que os comandos funcionam.
**Branch:** `docs/readme`. **Commit:** `docs: adiciona README com instruções de execução e testes`

---

## Decisões registradas
1. **Lombok:** a decisão fica para o fim da etapa 1.
2. **Caminho sem prefixo:** falha a inicialização.
3. **Filme repetido com dados diferentes:** vale a primeira ocorrência.
4. **H2 console:** desligado.
5. **`contextLoads`:** é substituído pelo teste do oráculo na etapa 3.
6. **Branches:** o desenvolvedor cria as branches e faz os merges; O agente trabalha só na branch atual.

## Riscos remanescentes
- **Regex de separação:** um nome que contenha ` and ` será dividido em dois. Isso é aceito, porque o CLAUDE.md proíbe corrigir nomes.
- **Etapas 1 e 2 sem teste dedicado:** só são permitidos testes via MockMvc, então essas etapas são verificadas pelo `contextLoads` e pelo log. A cobertura real vem nas etapas 3 e 4.
- **Codificação do CSV:** o arquivo é lido em UTF-8 com o decodificador padrão, que troca bytes inválidos por U+FFFD sem erro nem aviso. Um CSV salvo em Windows-1252 carrega "Almodóvar" como "Almod�var", e o mesmo produtor pode virar dois, alterando os intervalos em silêncio. Aceito: falhar a inicialização por isso contraria o CLAUDE.md.
- **Spring Boot 4 / Jackson 3:** os pacotes mudaram em relação ao Boot 3. O risco de imports errados é mitigado compilando a cada etapa.
