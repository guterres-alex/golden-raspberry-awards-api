# Golden Raspberry Awards API

API REST que carrega o CSV de indicados e vencedores de Pior Filme do Golden Raspberry Awards
num H2 em memória ao iniciar e expõe o intervalo mínimo e máximo entre vitórias consecutivas
de produtores.

## Stack
- Java 21, Spring Boot 4.1.1, Maven (`./mvnw` / `mvnw.cmd`), Spring Web MVC, Spring Data JPA, H2 em memória.
- Não adicionar dependências sem perguntar. Não usar Lombok.
- Camadas: `controller`, `service`, `repository`, `domain`, `dto`, `config`.
  Não criar interfaces ou abstrações sem necessidade concreta.
- DTOs são `record`s.

## CSV
- Caminho: propriedade `app.movies.csv-path`, default `classpath:movielist.csv`. Exigir prefixo `classpath:` ou `file:` (via `ResourceLoader`).
- Ler em UTF-8 e remover o BOM. Separador `;`. Ler com `split(";", -1)`, nunca `split(";")`.
- Cabeçalho válido: exatamente `year;title;studios;producers;winner`, nessa ordem, comparando cada coluna com trim e sem diferenciar maiúsculas. Arquivo vazio é inválido.
- Ignorar linhas em branco. Aplicar trim a todos os campos antes de validar.
- `winner`: vencedor apenas se `equalsIgnoreCase("yes")`.
- `producers` e `studios`: dividir com a regex `,\s*and\s+|,|\s+and\s+` aplicada ao campo inteiro, aplicar trim, descartar vazios. Sem correções de nomes. Studios vazios são permitidos.
- Linha malformada (número de colunas ≠ 5, ano não inteiro, título vazio, nenhum produtor): ignorar com `log.warn` indicando o número da linha.
- Filme repetido (mesmo título sem diferenciar maiúsculas + mesmo ano): ignorar a repetição com `log.warn`.
- Falhar a inicialização apenas se o arquivo não existir ou o cabeçalho for inválido.
- A carga roda uma única vez ao iniciar, numa única transação.

## Modelo de dados
- `Movie` (year, title, winner) N:N `Producer` e N:N `Studio`. Mapear `year` para a coluna `release_year` (`YEAR` é palavra-chave do H2).
- `Producer` e `Studio`: identidade pela chave `name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT)` (coluna única).
  Guardar e exibir a grafia da primeira ocorrência no CSV.
- Um produtor repetido no mesmo filme gera um único vínculo.

## Regra de cálculo: intervalo entre prêmios
- Uma única query que retorna apenas os pares (produtor, ano) de filmes vencedores, ordenados por `producer.id` e ano.
- Calcular em Java numa única passada: para cada produtor, intervalo = ano atual − ano da vitória anterior desse produtor.
- Produtores com menos de 2 vitórias não entram no cálculo.
- Intervalo 0 é válido: o mesmo produtor vencendo com filmes diferentes no mesmo ano.
- `min` e `max` incluem todos os empatados, inclusive vários pares do mesmo produtor. Um produtor pode estar nos dois.
- Ordenar cada lista por `previousWin`, depois `producer` (`String.compareTo` sobre o nome exibido).
- Sem dados suficientes: `200` com `{"min":[],"max":[]}`.

## API (Richardson nível 2)
- `GET /api/producers/award-intervals` → `200`, `application/json`:
  `{"min":[{"producer":"...","interval":1,"previousWin":2008,"followingWin":2009}],"max":[...]}`
- Erros: `spring.mvc.problemdetails.enabled=true`. Não criar `@RestControllerAdvice` sem um erro concreto para tratar.

## Testes
- Somente testes de integração: `@SpringBootTest` + `@AutoConfigureMockMvc` + `MockMvc`. Proibido usar testes unitários, mocks, `@MockitoBean` e slices (`@WebMvcTest`, `@DataJpaTest`).
- Confirmar os imports do Spring Boot 4 na compilação; não presumir os pacotes do Boot 3.
- Comparar o JSON completo com `content().json(expected, JsonCompareMode.STRICT)`.
- Oráculo do `movielist.csv` original:
  min = Joel Silver, 1, 1990 → 1991; max = Matthew Vaughn, 13, 2002 → 2015.
- Cenários em `src/test/resources/datasets/*.csv`, selecionados com
  `@TestPropertySource(properties = "app.movies.csv-path=classpath:datasets/<arquivo>.csv")`:
  empates, intervalo 0, resultado vazio, vários pares do mesmo produtor, nomes com caixa e espaços diferentes,
  separadores `,` / ` and ` / `, and `, produtor repetido no mesmo filme, filme repetido, linhas malformadas ignoradas.
- Não fixar `spring.datasource.url`: cada contexto de teste precisa de um H2 próprio.
- Arquivo inexistente e cabeçalho inválido: `assertThrows` em
  `new SpringApplicationBuilder(GoldenRaspberryAwardsApiApplication.class).properties("spring.main.web-application-type=none", "app.movies.csv-path=...").run()`.
- Rodar `./mvnw test` ao fim de cada etapa. Relatar só o resumo (executados, falhas, erros) e o detalhe das falhas, nunca o log completo.

## Forma de trabalhar
- Antes de escrever código, apresentar um plano e aguardar aprovação.
- Trabalhar em passos pequenos e parar ao fim de cada etapa para revisão.
- Nunca executar `git commit`, `git push` nem comandos que alterem o histórico git.
  Ao fim de cada etapa, sugerir a mensagem de commit em Conventional Commits, em português.