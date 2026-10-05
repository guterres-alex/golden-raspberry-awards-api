# Golden Raspberry Awards API

API REST que lê a lista de indicados e vencedores da categoria **Pior Filme** do Golden Raspberry Awards
a partir de um CSV, carrega os dados num banco H2 em memória ao iniciar e informa o produtor com o
**menor** e o com o **maior** intervalo entre duas vitórias consecutivas.

## Requisitos

- **Java 21** (JDK) no `PATH` ou em `JAVA_HOME`.
- Não é preciso instalar o Maven: o projeto traz o wrapper (`./mvnw` no Linux/macOS/Git Bash e `mvnw.cmd` no Windows).

## Como executar

```bash
./mvnw spring-boot:run          # Linux, macOS, Git Bash
```

```powershell
.\mvnw.cmd spring-boot:run      # Windows (PowerShell ou cmd)
```

A aplicação sobe em `http://localhost:8080`. Ao iniciar, o log mostra o resumo da carga, por exemplo:

```
CSV lido: 206 linhas válidas, 0 linhas malformadas ignoradas
CSV carregado: 206 filmes, 0 repetidos ignorados, 359 produtores, 59 estúdios
```

### Usando outro CSV

O arquivo é indicado pela propriedade `app.movies.csv-path`, que exige o prefixo `classpath:` ou `file:`.

Com o Maven:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--app.movies.csv-path=file:/dados/lista.csv
```

```powershell
# No PowerShell, as aspas em volta do -D são obrigatórias
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.movies.csv-path=file:C:/dados/lista.csv"
```

Pelo jar:

```bash
./mvnw package
java -jar target/golden-raspberry-awards-api-0.0.1-SNAPSHOT.jar --app.movies.csv-path=file:/dados/lista.csv
```

```powershell
.\mvnw.cmd package
java -jar target\golden-raspberry-awards-api-0.0.1-SNAPSHOT.jar --app.movies.csv-path=file:C:/dados/lista.csv
```

No Windows, use barras normais no caminho: `file:C:/dados/lista.csv`. O `package` também roda os testes.

## Configuração

| Propriedade            | Default                   | Descrição                                                        |
|------------------------|---------------------------|------------------------------------------------------------------|
| `app.movies.csv-path`  | `classpath:movielist.csv` | CSV carregado ao iniciar. Exige o prefixo `classpath:` ou `file:`. |

O banco é um H2 em memória, recriado a cada execução.

## Endpoint

### `GET /api/producers/award-intervals`

```bash
curl http://localhost:8080/api/producers/award-intervals
```

Resposta `200 OK` (`application/json`) com o `movielist.csv` original:

```json
{
  "min": [
    { "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
  ],
  "max": [
    { "producer": "Matthew Vaughn", "interval": 13, "previousWin": 2002, "followingWin": 2015 }
  ]
}
```

| Campo          | Significado                                    |
|----------------|------------------------------------------------|
| `producer`     | Nome do produtor, na grafia da primeira ocorrência no CSV |
| `interval`     | Anos entre as duas vitórias (`followingWin − previousWin`) |
| `previousWin`  | Ano da vitória anterior                        |
| `followingWin` | Ano da vitória seguinte                        |

Regras de cálculo:

- Só entram produtores com pelo menos 2 vitórias. Cada par de vitórias consecutivas de um produtor é um intervalo.
- Intervalo `0` é válido: o mesmo produtor vencendo com filmes diferentes no mesmo ano.
- `min` e `max` trazem todos os empatados, inclusive vários pares do mesmo produtor. Um produtor pode aparecer nas duas listas.
- Cada lista é ordenada por `previousWin` e depois por `producer`.
- Sem dados suficientes, a resposta é `200` com `{"min":[],"max":[]}`.

### Erros

Os erros seguem o formato Problem Details (RFC 9457), com `Content-Type: application/problem+json`:

- `404 Not Found` para URL inexistente;
- `405 Method Not Allowed` para verbo não suportado (ex.: `POST /api/producers/award-intervals`).

## Regras de carga do CSV

**Formato**

- Codificação UTF-8, com ou sem BOM. Separador `;`.
- Cabeçalho obrigatório: `year;title;studios;producers;winner`, nessa ordem. A comparação ignora maiúsculas e espaços nas pontas de cada coluna.
- Linhas em branco são ignoradas. Todos os campos passam por trim.

**Campos**

- `winner`: o filme é vencedor apenas se o valor for `yes` (sem diferenciar maiúsculas). Qualquer outro valor, inclusive vazio, conta como não vencedor.
- `producers` e `studios`: separados por `,`, ` and ` ou `, and `. Nomes vazios são descartados. Estúdios vazios são permitidos.
- Produtores e estúdios são identificados pelo nome normalizado (trim, espaços repetidos reduzidos a um, minúsculas).
  `Joel Silver` e ` joel  SILVER ` são o mesmo produtor, exibido com a grafia da primeira ocorrência.
- Um produtor repetido no mesmo filme gera um único vínculo.
- Filme repetido (mesmo título, sem diferenciar maiúsculas, e mesmo ano): vale a primeira ocorrência. A repetição é ignorada com aviso no log.

**Linhas malformadas** são ignoradas com aviso no log indicando o número da linha:

- número de colunas diferente de 5;
- ano que não é inteiro;
- título vazio ou nenhum produtor;
- título, nome de produtor ou nome de estúdio com mais de 255 caracteres.

**Erros de configuração que impedem a inicialização:**

- `app.movies.csv-path` não começa com `classpath:` ou `file:`;
- o arquivo não existe;
- o cabeçalho é inválido ou o arquivo está vazio.
- falhas de leitura do arquivo ou de persistência também impedem a inicialização.

## Testes

```bash
./mvnw test          # Linux, macOS, Git Bash
.\mvnw.cmd test      # Windows
```

Todos os testes são de integração. Os testes do endpoint sobem a aplicação com `@SpringBootTest` e chamam a API via MockMvc,
comparando o JSON completo; os testes de falha de inicialização sobem a aplicação com `SpringApplicationBuilder`.
Cada cenário usa um CSV próprio em `src/test/resources/datasets/`.

| Classe de teste                   | Cenário                                                                 |
|-----------------------------------|-------------------------------------------------------------------------|
| `AwardIntervalsOriginalCsvTest`   | `movielist.csv` original                                                |
| `ResultadoVazioTest`              | nenhum produtor com 2 vitórias: listas vazias                           |
| `IntervaloZeroTest`               | mesmo produtor vencendo duas vezes no mesmo ano                         |
| `EmpatesMinMaxTest`               | empates no `min` e no `max` e a ordenação                               |
| `VariosParesMesmoProdutorTest`    | vários pares do mesmo produtor, com linhas fora de ordem                |
| `NomesCaixaEspacosTest`           | nomes com caixa e espaços diferentes                                    |
| `SeparadoresProdutoresTest`       | separadores `,`, ` and ` e `, and `                                     |
| `ProdutorRepetidoMesmoFilmeTest`  | produtor repetido no mesmo filme                                        |
| `FilmeRepetidoTest`               | filme repetido: vale a primeira ocorrência                              |
| `LinhasMalformadasTest`           | linhas malformadas ignoradas, incluindo o limite de 255 caracteres      |
| `WinnerVariacoesTest`             | variações de `winner` (`YES`, ` yes `, `y`, `true`...)                  |
| `BomCabecalhoVariacoesTest`       | BOM e cabeçalho com caixa e espaços diferentes                          |
| `FalhaInicializacaoTest`          | caminho sem prefixo, arquivo inexistente, cabeçalho inválido e arquivo vazio |
| `VerbosStatusHttpTest`            | `405` e `404` com Problem Details                                       |
| `NomeComAndSemEspacosTest`        | "and" dentro do nome não separa produtores                              |
| `CaminhoPrefixoFileTest`          | CSV carregado por caminho absoluto com `file:`                          |
| `Limite255ProdutorEstudioTest`    | produtor e estúdio com exatamente 255 caracteres                        |
| `EstudioVazioTest`                | filme sem estúdio conta no resultado                                    |

## Estrutura do projeto

Pacote base `io.github.guterresalex.goldenraspberry`:

- `controller`: endpoint REST.
- `service`: leitura do CSV, importação e cálculo dos intervalos.
- `repository`: repositórios Spring Data JPA e a query dos vencedores.
- `domain`: entidades `Movie`, `Producer` e `Studio`.
- `dto`: records de entrada (linha do CSV) e de saída (resposta da API).
- `config`: propriedades da aplicação e a carga do CSV na inicialização.

## Limitações conhecidas

- Um nome que contém ` and ` é dividido em dois produtores.
- Erros de digitação no CSV não são corrigidos. Por exemplo, `Brian Robbinsand Sharla Sumpter Bridgett`
  vira um único produtor com esse nome.
- O CSV precisa estar em UTF-8. Um arquivo salvo em outra codificação (ex.: Windows-1252) é lido sem erro,
  mas os caracteres acentuados são corrompidos, e o mesmo produtor pode virar dois.
- O limite de 255 caracteres vale para o nome como está no CSV. Um nome com caracteres que crescem ao virar minúsculas (ex.: `İ`) 
  pode gerar uma chave acima do limite e impedir a inicialização.
- O ano não tem faixa de validação: valores absurdos (negativos ou muito grandes) são aceitos.
- Espaços Unicode (como o espaço não separável) e o separador ` AND ` em maiúsculas não são tratados.
- Campos entre aspas no padrão RFC 4180 não são suportados: um `;` dentro do título invalida a linha.
- A carga faz um insert por entidade, adequado ao volume do desafio.

## Uso de IA

O projeto foi desenvolvido com apoio do Claude Code. O processo está documentado em:

- [`docs/AI_USAGE.md`](docs/AI_USAGE.md): ferramentas, processo e decisões;
- [`docs/ai-log/`](docs/ai-log/): registro das sessões de cada etapa;
- [`docs/PLANO.md`](docs/PLANO.md): plano de implementação em etapas.
