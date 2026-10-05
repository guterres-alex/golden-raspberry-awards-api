---
name: cenario-teste-csv
description: Cria um cenário de teste de integração para GET /api/producers/award-intervals a partir de um caso de borda descrito pelo usuário. Gera um CSV mínimo em src/test/resources/datasets/, calcula à mão o resultado esperado e escreve a classe de teste com MockMvc. Use sempre que o usuário pedir para testar um caso de borda (empate, intervalo 0, produtor repetido, separadores, linha malformada, filme duplicado, resultado vazio etc.), pedir um novo conjunto de dados ou dataset de teste, ou perguntar "o que acontece se o CSV tiver...", mesmo que não diga "cenário" nem "CSV".
---

# Cenário de teste a partir de CSV

Cada cenário é um par: **um CSV** em `src/test/resources/datasets/` e **uma classe de teste**.
Um par cobre um único caso de borda. Assim, quando um teste falha, o nome da classe já diz
qual regra quebrou.

As regras de CSV, de cálculo e de testes estão no `CLAUDE.md`, nas seções **CSV**,
**Regra de cálculo**, **Testes** e **Forma de trabalhar**. Leia essas seções antes de começar
e siga-as. Esta skill só define o fluxo e não repete essas regras.

## 0. Entender o caso e propor

1. Releia o caso descrito. Se ele misturar dois comportamentos (ex.: "empate e intervalo 0"),
   proponha dividir em dois cenários.
2. Liste `src/test/resources/datasets/` e as classes de teste já existentes para não duplicar
   um cenário que já existe.
3. Escolha os nomes:
   - CSV: kebab-case descritivo, ex.: `empate-no-intervalo-maximo.csv`.
   - Classe: PascalCase em português, descrevendo o caso, com sufixo `Test`,
     ex.: `EmpateNoIntervaloMaximoTest`. Use `Test` e não `IT`: o surefire, como está no
     `pom.xml`, só executa `*Test`/`*Tests`, e uma classe `*IT` seria ignorada sem aviso.
4. Apresente ao usuário o nome do cenário, as linhas do CSV (passo 1) e o cálculo (passo 2).
   Aguarde aprovação antes de criar arquivos, conforme a seção **Forma de trabalhar**.

## 1. CSV mínimo

- Copie o cabeçalho da primeira linha de `src/main/resources/movielist.csv`.
- Use o menor número de linhas que ainda mostre o caso. Com poucas linhas, quem revisa consegue
  conferir o resultado de cabeça. Linhas extras só servem para descartar uma
  implementação errada que passaria por acaso. Nesse caso, explique no comentário por que a
  linha existe.
- Use dados fictícios e óbvios (`Produtor A`, `Filme 1`, `Studio X`) quando o caso não depender
  de nomes reais. Quando o caso for sobre grafia (caixa, espaços, separadores), escreva
  exatamente a grafia que está sendo testada.
- Inclua filmes não vencedores apenas se o caso precisar deles (ex.: provar que um
  não vencedor não conta).

## 2. Resultado esperado calculado à mão

Calcule o resultado só a partir do CSV e das regras do `CLAUDE.md`. Não rode a aplicação
para descobrir a resposta: um esperado copiado da saída real apenas confirma o
comportamento atual, mesmo que esteja errado.

Escreva o cálculo num comentário de bloco no topo da classe de teste, nesta ordem:

1. **Caso**: uma frase sobre o que o cenário prova.
2. **Linhas consideradas**: quais linhas entram, quais são ignoradas e por qual regra.
3. **Vitórias por produtor**: produtor (grafia exibida) → anos das vitórias, em ordem.
4. **Intervalos**: cada par consecutivo `anterior → seguinte = intervalo`.
5. **min / max**: o valor, os empatados e a ordenação final.

Exemplo:

```java
/*
 * Caso: dois produtores empatam no intervalo máximo.
 *
 * Linhas consideradas: todas as 4 são vencedoras e válidas.
 *
 * Vitórias por produtor:
 *   Produtor A: 2000, 2005
 *   Produtor B: 2010, 2015
 *
 * Intervalos:
 *   Produtor A: 2000 → 2005 = 5
 *   Produtor B: 2010 → 2015 = 5
 *
 * min = max = 5 → os dois produtores aparecem em ambas as listas,
 * ordenados por previousWin (2000 antes de 2010).
 */
```

## 3. Classe de teste

- Siga a seção **Testes** do `CLAUDE.md`: anotações, seleção do CSV por
  `@TestPropertySource`, comparação estrita do JSON completo e imports do Spring Boot 4
  confirmados na compilação.
- Coloque a classe no mesmo pacote da classe principal da aplicação, ou no subpacote onde
  já estiverem os outros cenários, para manter tudo junto.
- Escreva o JSON esperado como text block (`"""`), formatado para leitura, igual ao cálculo
  do comentário.
- Um método de teste por classe é o normal. Use mais de um só se o mesmo CSV provar coisas
  diferentes do mesmo caso.
- Se o caso for falha de inicialização (arquivo inexistente ou cabeçalho inválido), use o padrão
  com `assertThrows` descrito no `CLAUDE.md` em vez de MockMvc.

## 4. Executar e relatar

1. Rode `./mvnw test`.
2. Confira que a nova classe aparece na execução. Se o total de testes não aumentou, a classe
   não rodou: verifique o nome e o pacote antes de seguir.
3. Relate como a seção **Testes** pede: executados, falhas, erros e só o detalhe das falhas.
4. Se o novo teste falhar, refaça o cálculo à mão antes de mexer em qualquer coisa:
   - Se o cálculo estava errado, corrija o comentário e o JSON esperado e explique o erro.
   - Se o cálculo está certo, a aplicação diverge da regra. Não ajuste o esperado para fazer
     o teste passar. Relate a divergência e pare, porque o teste acabou de achar um bug.
5. Sugira a mensagem de commit conforme a seção **Forma de trabalhar**, por exemplo
   `test: adiciona cenário de empate no intervalo máximo`, e pare para revisão.
