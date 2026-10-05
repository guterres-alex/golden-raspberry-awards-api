 ▐▛███▛█   Claude Code v2.1.286
▝▜██████▀  Opus 5.5 · Claude Pro
 ▝▝   ▝▝   C:\dev\projecs\golden-raspberry-awards-api

▎ Your voice can help guide AI
▎ Take 15 min to share your experiences with Anthropic Interviewer. Start now
  1 more notice hidden

❯ /clear                                                                        

❯ Com base no CLAUDE.md, proponha o plano de implementação dividido em etapas.  
  Não escreva código.                                                           
                                                                                
  Para cada etapa, informe:                                                     
  - objetivo;                                                                   
  - arquivos que serão criados ou alterados;                                    
  - como verificar que a etapa funciona antes de seguir para a próxima;         
  - nome da branch (ex.: feat/carga-csv) e mensagem de commit sugerida.         
                                                                                
  Restrições:                                                                   
  - Cada etapa deve ser pequena o suficiente para eu revisar de uma vez.        
  - Ao concluir a etapa das entidades, pare para eu decidir sobre o uso de      
  Lombok.                                                                       
  - Os cenários de teste com CSVs alternativos serão criados depois, com a      
    skill cenario-teste-csv; no plano, indique em qual etapa entram.            
  - Aponte riscos ou dúvidas que ainda precisem de decisão minha.               

  Searched for 1 pattern, read 6 files (ctrl+o to expand)

● Updated plan
  ⎿  /plan to preview
  ⎿  User rejected Claude's plan:
     ╭─────────────────────────────────────────────────────────────────────────╮
     │ Plano de implementação: Golden Raspberry Awards API                     │
     │                                                                         │
     │ Contexto                                                                │
     │                                                                         │
     │ O projeto tem só o esqueleto do Spring Boot 4.1.1: a classe             │
     │ GoldenRaspberryAwardsApiApplication, o teste contextLoads, o            │
     │ movielist.csv e um application.properties com uma única linha. O        │
     │ pom.xml já traz webmvc, data-jpa, h2, h2console e os starters de teste  │
     │ *-webmvc-test e *-data-jpa-test. Não é preciso adicionar dependências.  │
     │ O objetivo é implementar o que o CLAUDE.md descreve: carga do CSV ao    │
     │ iniciar e o GET /api/producers/award-intervals. O trabalho vai em       │
     │ etapas pequenas, cada uma revisada separadamente.                       │
     │                                                                         │
     │ Pacote base: io.github.guterresalex.goldenraspberry (abreviado como …   │
     │ abaixo).                                                                │
     │ Toda etapa termina com ./mvnw test (relato: executados, falhas, erros)  │
     │ e para para revisão. Eu não faço commits.                               │
     │                                                                         │
     │ ---                                                                     │
     │                                                                         │
     │ Etapa 1: Configuração, entidades e repositórios                         │
     │                                                                         │
     │ Objetivo: criar o modelo de dados e o schema gerado pelo Hibernate no   │
     │ H2.                                                                     │
     │ - src/main/resources/application.properties:                            │
     │   app.movies.csv-path=classpath:movielist.csv,                          │
     │   spring.mvc.problemdetails.enabled=true,                               │
     │   spring.jpa.open-in-view=false. Sem spring.datasource.url.             │
     │ - …/domain/Movie.java: id, year → @Column(name="release_year"), title,  │
     │   winner. @ManyToMany Set<Producer> e @ManyToMany Set<Studio>. Usar Set │
     │   garante um único vínculo quando o produtor se repete no mesmo filme.  │
     │ - …/domain/Producer.java e …/domain/Studio.java: id, name (grafia       │
     │   exibida) e nameKey (único), com um método estático que gera a chave   │
     │   de normalização do CLAUDE.md.                                         │
     │ - …/repository/MovieRepository.java, ProducerRepository.java,           │
     │   StudioRepository.java (JpaRepository).                                │
     │                                                                         │
     │ Verificação: ./mvnw test. O contextLoads deve passar e o log não pode   │
     │ ter erros de DDL, principalmente com release_year.                      │
     │ Branch: feat/entidades. Commit: feat: adiciona entidades Movie,         │
     │ Producer e Studio e repositórios                                        │
     │                                                                         │
     │ ▎ ⏸ Parada obrigatória: aqui você decide sobre o Lombok. O CLAUDE.md    │
     │ ▎ hoje proíbe Lombok. Se a decisão for usar, é preciso mudar o pom.xml  │
     │ ▎ (dependência e annotation processor) e o CLAUDE.md antes de seguir.   │
     │                                                                         │
     │ ---                                                                     │
     │                                                                         │
     │ Etapa 2: Leitura e validação do CSV (sem persistência)                  │
     │                                                                         │
     │ Objetivo: transformar o arquivo em linhas válidas seguindo as regras do │
     │ CLAUDE.md.                                                              │
     │ - …/config/MoviesProperties.java: record com                            │
     │   @ConfigurationProperties("app.movies") e o campo csvPath, habilitado  │
     │   com @ConfigurationPropertiesScan ou @EnableConfigurationProperties.   │
     │ - …/dto/MovieCsvRow.java: record(int year, String title, List<String>   │
     │   studios, List<String> producers, boolean winner, int lineNumber).     │
     │ - …/service/MovieCsvReader.java:                                        │
     │   - valida o prefixo classpath: ou file:, resolve o arquivo via         │
     │     ResourceLoader e checa se ele existe;                               │
     │   - lê em UTF-8, remove o BOM e valida o cabeçalho (arquivo vazio é     │
     │     inválido);                                                          │
     │   - para cada linha: ignora se estiver em branco, faz split(";", -1),   │
     │     aplica trim, valida, separa com a regex e emite log.warn com o      │
     │     número da linha quando for malformada.                              │
     │   - Arquivo inexistente ou cabeçalho inválido lançam                    │
     │     IllegalStateException com mensagem clara.                           │
     │                                                                         │
     │ Verificação: ./mvnw test continua verde. Nada chama o leitor ainda,     │
     │ então nesta etapa só se revisa o código. Os testes do leitor entram nas │
     │ etapas 3 e 5.                                                           │
     │ Branch: feat/leitura-csv. Commit: feat: adiciona leitura e validação do │
     │ CSV de filmes                                                           │
     │                                                                         │
     │ ---                                                                     │
     │                                                                         │
     │ Etapa 3: Carga no H2 ao iniciar                                         │
     │                                                                         │
     │ Objetivo: persistir os filmes uma única vez, na inicialização, numa     │
     │ única transação.                                                        │
     │ - …/service/MovieImportService.java: método @Transactional              │
     │   import(List<MovieCsvRow>).                                            │
     │   - Ignora filme repetido (título sem diferenciar caixa + ano) com      │
     │     log.warn.                                                           │
     │   - Mantém um Map<nameKey, Producer> e outro Map<nameKey, Studio> em    │
     │     memória, para que a primeira grafia prevaleça e para evitar         │
     │     consultas por linha.                                                │
     │   - Ao final, registra log.info com o total carregado.                  │
     │ - …/config/MovieDataLoader.java: ApplicationRunner que chama o leitor e │
     │   depois import. Ele roda dentro de SpringApplication.run, então uma    │
     │   exceção derruba a inicialização, como o CLAUDE.md exige para os       │
     │   testes com assertThrows.                                              │
     │                                                                         │
     │ Verificação: ./mvnw test verde. O contextLoads agora carrega o CSV      │
     │ original. Rodar ./mvnw spring-boot:run e conferir no log o total de     │
     │ filmes e a ausência de warns inesperados no CSV original.               │
     │ Opcionalmente, conferir no H2 console (ver dúvida 4).                   │
     │ Branch: feat/carga-csv. Commit: feat: carrega o CSV de filmes no H2 na  │
     │ inicialização                                                           │
     │                                                                         │
     │ ---                                                                     │
     │                                                                         │
     │ Etapa 4: Cálculo dos intervalos e endpoint                              │
     │                                                                         │
     │ Objetivo: expor GET /api/producers/award-intervals.                     │
     │ - …/dto/ProducerWinYear.java: record(Long producerId, String producer,  │
     │   int year), a projeção da query.                                       │
     │ - ProducerRepository: uma query JPQL com projeção de construtor: select │
     │   new …(p.id, p.name, m.year) from Movie m join m.producers p where     │
     │   m.winner = true order by p.id, m.year.                                │
     │ - …/dto/ProducerInterval.java (producer, interval, previousWin,         │
     │   followingWin) e …/dto/AwardIntervalsResponse.java (min, max).         │
     │ - …/service/AwardIntervalService.java: uma única passada sobre a lista  │
     │   ordenada. Mantém o min/max corrente e a lista dos empatados, e ordena │
     │   no fim por previousWin e depois por producer.                         │
     │ - …/controller/ProducerController.java:                                 │
     │   @GetMapping("/api/producers/award-intervals").                        │
     │ - Teste: trocar o contextLoads por AwardIntervalsMovieListOriginalTest  │
     │   (nome a confirmar). Ele usa @SpringBootTest + @AutoConfigureMockMvc e │
     │   compara com STRICT o oráculo (Joel Silver 1, 1990→1991 / Matthew      │
     │   Vaughn 13, 2002→2015).                                                │
     │                                                                         │
     │ Verificação: ./mvnw test verde com o teste do oráculo. Os imports do    │
     │ Boot 4 (AutoConfigureMockMvc no pacote                                  │
     │ org.springframework.boot.webmvc.test.autoconfigure, JsonCompareMode)    │
     │ são confirmados na compilação. Também curl                              │
     │ localhost:8080/api/producers/award-intervals com a aplicação rodando.   │
     │ Branch: feat/intervalos-premios. Commit: feat: expõe intervalos mínimo  │
     │ e máximo entre prêmios de produtores                                    │
     │                                                                         │
     │ ---                                                                     │
     │                                                                         │
     │ Etapa 5: Cenários de teste com CSVs alternativos (skill                 │
     │ cenario-teste-csv)                                                      │
     │                                                                         │
     │ Objetivo: cobrir os casos de borda do CLAUDE.md. Cada cenário é um par  │
     │ (CSV em src/test/resources/datasets/ + classe *Test), criado pela       │
     │ skill. Cada um é aprovado antes de ser criado e para ao final, conforme │
     │ o fluxo da skill.                                                       │
     │ Ordem sugerida:                                                         │
     │ 1. resultado vazio                                                      │
     │ 2. intervalo 0                                                          │
     │ 3. empates no min/max                                                   │
     │ 4. vários pares do mesmo produtor                                       │
     │ 5. nomes com caixa e espaços diferentes                                 │
     │ 6. separadores , / and / , and                                          │
     │ 7. produtor repetido no mesmo filme                                     │
     │ 8. filme repetido                                                       │
     │ 9. linhas malformadas ignoradas                                         │
     │ 10. falha de inicialização: arquivo inexistente e cabeçalho inválido    │
     │     (assertThrows com SpringApplicationBuilder)                         │
     │                                                                         │
     │ Verificação: a cada cenário, ./mvnw test com o total de testes          │
     │ aumentando. Se um cenário falhar com o cálculo correto, ele achou um    │
     │ bug: paro e relato.                                                     │
     │ Branch: test/cenarios-csv. Um commit por cenário, ex.: test: adiciona   │
     │ cenário de intervalo zero.                                              │
     │                                                                         │
     │ ---                                                                     │
     │                                                                         │
     │ Etapa 6 (opcional): Documentação                                        │
     │                                                                         │
     │ README com requisitos, como rodar, como testar e exemplo de resposta, e │
     │ atualização do docs/AI_USAGE.md se você quiser.                         │
     │ Branch: docs/readme. Commit: docs: adiciona README com instruções de    │
     │ execução e testes                                                       │
     │                                                                         │
     │ ---                                                                     │
     │                                                                         │
     │ Riscos e dúvidas que dependem de você                                   │
     │                                                                         │
     │ 1. Lombok (fim da etapa 1): hoje ele é proibido pelo CLAUDE.md. Usar    │
     │    exige mudar o pom e o CLAUDE.md.                                     │
     │ 2. Caminho sem prefixo classpath:/file:: o CLAUDE.md exige o prefixo,   │
     │    mas diz que a inicialização só falha com arquivo inexistente ou      │
     │    cabeçalho inválido. Proposta: tratar a falta de prefixo como erro de │
     │    configuração e falhar a inicialização também. Confirma?              │
     │ 3. Filme repetido com dados diferentes (ex.: um yes e outro não): a     │
     │    primeira ocorrência prevalece e a repetição é ignorada, mesmo que só │
     │    a segunda seja vencedora. Confirma?                                  │
     │ 4. H2 console: a dependência spring-boot-h2console já está no pom.      │
     │    Habilitar spring.h2.console.enabled=true para inspecionar a carga ou │
     │    deixar desligado?                                                    │
     │ 5. Teste contextLoads: ele não segue o padrão MockMvc do CLAUDE.md.     │
     │    Proposta: substituí-lo pelo teste do oráculo na etapa 4.             │
     │ 6. Branches: quem cria e faz o merge? Criar uma branch (git switch -c)  │
     │    não reescreve o histórico, mas o merge sim. Proposta: você cria as   │
     │    branches e faz os merges; eu só trabalho na branch em que você       │
     │    estiver.                                                             │
     │ 7. Testes da carga antes da etapa 4: como só são permitidos testes via  │
     │    MockMvc, as etapas 2 e 3 não têm um teste dedicado. A verificação    │
     │    delas é o contextLoads, o log e uma inspeção manual. A cobertura     │
     │    real vem nas etapas 4 e 5.                                           │
     │ 8. Regex de separação: um nome que contenha and (ex.: um estúdio "X and │
     │    Y Films") será dividido em dois. Isso é aceito, já que o CLAUDE.md   │
     │    proíbe correções de nomes.                                           │
     │ 9. Spring Boot 4 / Jackson 3: pacotes de teste e de serialização        │
     │    mudaram em relação ao Boot 3. Risco de imports errados, mitigado com │
     │    a compilação a cada etapa.                                           │
     ╰─────────────────────────────────────────────────────────────────────────╯

✻ Crunched for 1m 18s · done 21:23

❯ Ajustes no plano:                                                             
  - Juntar as etapas 2 e 3 numa só (leitura e carga do CSV), para que a etapa   
    tenha uma verificação concreta: aplicação sobe e o log mostra o total       
    carregado. Branch: feat/carga-csv.                                          
  - A etapa de documentação (README) é obrigatória pela especificação, não      
  opcional.                                                                     
                                                                                
  Respostas às dúvidas:                                                         
  1. Lombok: decido ao fim da etapa 1.                                          
  2. Caminho sem prefixo: falhar a inicialização. Confirmado.                   
  3. Filme repetido com dados diferentes: vale a primeira ocorrência.           
  Confirmado.                                                                   
  4. H2 console: manter desligado.                                              
  5. Substituir o contextLoads pelo teste do oráculo na etapa de endpoint.      
  Confirmado.                                                                   
  6. Branches: eu crio as branches e faço os merges; você trabalha apenas na    
     branch atual. Confirmado.                                                  
                                                                                
  Não inicie a implementação. Ao aprovar, a única ação deve ser salvar o plano  
  final em docs/PLANO.md.                                                       

● Updated plan
  ⎿  /plan to preview

● User approved Claude's plan
  ⎿  Plan saved to: 
     ~\.claude\plans\pasted-content-id-c44c-com-base-ethereal-bunny.md · /plan 
     to edit
     Plano de implementação: Golden Raspberry Awards API

     ▎ Ação ao aprovar: a única ação é salvar este plano em docs/PLANO.md. A 
     ▎ implementação não começa sem um pedido explícito.

     Contexto

     O projeto tem só o esqueleto do Spring Boot 4.1.1: a classe
     GoldenRaspberryAwardsApiApplication, o teste contextLoads, o movielist.csv
     e um application.properties com uma única linha. O pom.xml já traz webmvc,
     data-jpa, h2, h2console e os starters de teste *-webmvc-test e
     *-data-jpa-test, então não é preciso adicionar dependências.
     O objetivo é implementar o que o CLAUDE.md descreve: a carga do CSV ao
     iniciar e o endpoint GET /api/producers/award-intervals. O trabalho vai em
     etapas pequenas, cada uma revisada separadamente.

     Pacote base: io.github.guterresalex.goldenraspberry (abreviado como …
     abaixo).
     Toda etapa termina com ./mvnw test, relatando executados, falhas e erros, e
     para para revisão. Eu não faço commits e trabalho só na branch atual. Você
     cria as branches e faz os merges.

     ---

     Etapa 1: Configuração, entidades e repositórios

     Objetivo: criar o modelo de dados e o schema gerado pelo Hibernate no H2.
     - src/main/resources/application.properties:
       app.movies.csv-path=classpath:movielist.csv,
       spring.mvc.problemdetails.enabled=true, spring.jpa.open-in-view=false.
       Sem spring.datasource.url. O H2 console fica desligado.
     - …/domain/Movie.java: id, year → @Column(name="release_year"), title,
       winner. @ManyToMany Set<Producer> e @ManyToMany Set<Studio>. O Set
       garante um único vínculo quando o produtor se repete no mesmo filme.
     - …/domain/Producer.java e …/domain/Studio.java: id, name (grafia exibida)
       e nameKey (único), com um método estático que gera a chave de
       normalização do CLAUDE.md.
     - …/repository/MovieRepository.java, ProducerRepository.java,
       StudioRepository.java (JpaRepository).

     Verificação: ./mvnw test com o contextLoads verde e sem erros de DDL no log
     (principalmente em release_year).
     Branch: feat/entidades. Commit: feat: adiciona entidades Movie, Producer e 
     Studio e repositórios

     ▎ ⏸ Parada obrigatória: aqui você decide sobre o Lombok. Hoje o CLAUDE.md 
     ▎ proíbe Lombok. Se a decisão for usar, o pom.xml (dependência e annotation
     ▎ processor) e o CLAUDE.md precisam mudar antes de seguir.

     ---

     Etapa 2: Leitura e carga do CSV na inicialização

     Objetivo: ler e validar o arquivo e persistir os filmes uma única vez ao
     iniciar, numa única transação.
     - …/config/MoviesProperties.java: record anotado com
       @ConfigurationProperties("app.movies"), com o campo csvPath.
     - …/dto/MovieCsvRow.java: record(int year, String title, List<String> 
       studios, List<String> producers, boolean winner, int lineNumber).
     - …/service/MovieCsvReader.java:
       - exige o prefixo classpath: ou file: e, sem ele, falha a inicialização;
       - resolve o caminho via ResourceLoader e checa se o arquivo existe;
       - lê em UTF-8, remove o BOM e valida o cabeçalho (arquivo vazio é
         inválido);
       - em cada linha: ignora as em branco, aplica split(";", -1) e trim,
         valida e separa produtores e estúdios com a regex;
       - linha malformada gera log.warn com o número da linha;
       - prefixo ausente, arquivo inexistente e cabeçalho inválido lançam
         IllegalStateException com mensagem clara.
     - …/service/MovieImportService.java: @Transactional 
       import(List<MovieCsvRow>).
       - Filme repetido (título sem diferenciar caixa + ano): vale a primeira
         ocorrência, e a repetição gera log.warn.
       - Mantém em memória um Map<nameKey, Producer> e um Map<nameKey, Studio>,
         para que a primeira grafia prevaleça e para não consultar o banco a
         cada linha.
       - Ao final, registra o total carregado com log.info.
     - …/config/MovieDataLoader.java: ApplicationRunner que chama o leitor e
       depois o import. Como roda dentro de SpringApplication.run, uma exceção
       derruba a inicialização.

     Verificação: ./mvnw test verde, com o contextLoads já carregando o CSV
     original. Depois, ./mvnw spring-boot:run: a aplicação deve subir e o log
     deve mostrar o total de filmes carregados, sem warns inesperados no CSV
     original.
     Branch: feat/carga-csv. Commit: feat: lê e carrega o CSV de filmes no H2 na
     inicialização

     ---

     Etapa 3: Cálculo dos intervalos e endpoint

     Objetivo: expor GET /api/producers/award-intervals.
     - …/dto/ProducerWinYear.java: record(Long producerId, String producer, int 
       year), a projeção da query.
     - ProducerRepository: uma query JPQL com projeção de construtor: select new
       …(p.id, p.name, m.year) from Movie m join m.producers p where m.winner =
       true order by p.id, m.year.
     - …/dto/ProducerInterval.java (producer, interval, previousWin,
       followingWin) e …/dto/AwardIntervalsResponse.java (min, max).
     - …/service/AwardIntervalService.java: uma única passada sobre a lista
       ordenada. Guarda o min e o max correntes com seus empatados e, no fim,
       ordena por previousWin e depois por producer.
     - …/controller/ProducerController.java:
       @GetMapping("/api/producers/award-intervals").
     - Teste: o contextLoads sai e entra AwardIntervalsOriginalCsvTest, com
       @SpringBootTest + @AutoConfigureMockMvc, que compara em modo STRICT o
       oráculo (Joel Silver 1, 1990→1991 / Matthew Vaughn 13, 2002→2015).

     Verificação: ./mvnw test verde com o teste do oráculo. Os imports do Boot 4
     (AutoConfigureMockMvc em
     org.springframework.boot.webmvc.test.autoconfigure, JsonCompareMode) são
     confirmados na compilação. Com a aplicação rodando, conferir também curl 
     localhost:8080/api/producers/award-intervals.
     Branch: feat/intervalos-premios. Commit: feat: expõe intervalos mínimo e 
     máximo entre prêmios de produtores

     ---

     Etapa 4: Cenários de teste com CSVs alternativos (skill cenario-teste-csv)

     Objetivo: cobrir os casos de borda do CLAUDE.md. Cada cenário é um par
     criado pela skill: um CSV em src/test/resources/datasets/ e uma classe
     *Test. Cada um é aprovado antes de ser criado e para ao final, como a skill
     define.
     Ordem sugerida:
     1. resultado vazio
     2. intervalo 0
     3. empates no min/max
     4. vários pares do mesmo produtor
     5. nomes com caixa e espaços diferentes
     6. separadores , / and / , and 
     7. produtor repetido no mesmo filme
     8. filme repetido (vale a primeira ocorrência)
     9. linhas malformadas ignoradas
     10. falha de inicialização: arquivo inexistente, cabeçalho inválido e
         caminho sem prefixo (assertThrows com SpringApplicationBuilder)

     Verificação: ./mvnw test a cada cenário, com o total de testes aumentando.
     Se um cenário falhar com o cálculo correto, ele achou um bug: paro e
     relato.
     Branch: test/cenarios-csv. Um commit por cenário, ex.: test: adiciona 
     cenário de intervalo zero.

     ---

     Etapa 5: Documentação (obrigatória)

     Objetivo: criar o README.md exigido pela especificação, com requisitos
     (Java 21), como rodar (./mvnw spring-boot:run, mvnw.cmd no Windows), como
     testar (./mvnw test), a propriedade app.movies.csv-path, o endpoint com
     exemplo de resposta e as regras de carga do CSV.
     Verificação: seguir o README do zero (subir a aplicação, chamar o endpoint,
     rodar os testes) e conferir que os comandos funcionam.
     Branch: docs/readme. Commit: docs: adiciona README com instruções de 
     execução e testes

     ---

     Decisões registradas

     1. Lombok: a decisão fica para o fim da etapa 1.
     2. Caminho sem prefixo: falha a inicialização.
     3. Filme repetido com dados diferentes: vale a primeira ocorrência.
     4. H2 console: desligado.
     5. contextLoads: é substituído pelo teste do oráculo na etapa 3.
     6. Branches: você cria as branches e faz os merges; eu trabalho só na
        branch atual.

     Riscos remanescentes

     - Regex de separação: um nome que contenha and será dividido em dois. Isso
       é aceito, porque o CLAUDE.md proíbe corrigir nomes.
     - Etapas 1 e 2 sem teste dedicado: só são permitidos testes via MockMvc,
       então essas etapas são verificadas pelo contextLoads e pelo log. A
       cobertura real vem nas etapas 3 e 4.
     - Spring Boot 4 / Jackson 3: os pacotes mudaram em relação ao Boot 3. O
       risco de imports errados é mitigado compilando a cada etapa.

  Ran 1 shell command (ctrl+o to expand)

● Salvei o plano final em docs/PLANO.md sem nenhuma alteração. Não mexi em
  nenhum outro arquivo e não comecei a implementação.

  O plano tem 5 etapas:
  1. Entidades (feat/entidades): para no fim, para você decidir sobre o Lombok.
  2. Leitura e carga do CSV (feat/carga-csv): agora numa etapa só. A verificação
     é a aplicação subir e o log mostrar o total de filmes carregados.
  3. Endpoint (feat/intervalos-premios): o teste do oráculo substitui o
     contextLoads.
  4. Cenários de CSV com a skill cenario-teste-csv (test/cenarios-csv): os casos
     de falha na inicialização agora incluem o caminho sem prefixo.
  5. README (docs/readme): agora obrigatório.

  Suas respostas às dúvidas estão registradas na seção "Decisões registradas" do
  plano.

  Mensagem de commit sugerida para este arquivo:
  docs: adiciona plano de implementação em etapas

  Quando você criar a branch feat/entidades e pedir, começo a etapa 1.

✻ Crunched for 40s · done 21:26