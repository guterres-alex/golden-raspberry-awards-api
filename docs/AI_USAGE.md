# Uso de IA no desenvolvimento

## Ferramentas
- Claude Code (CLI + plugin IntelliJ), plano Pro, para entender o desafio, planejar o processo e desenvolvimento.
- Plugins oficiais da Anthropic, instalados no escopo do projeto e declarados em `.claude/settings.json`:
  - `pr-review-toolkit`: agentes de revisão de código, via `/plugin install pr-review-toolkit@claude-plugins-official`
  - `skill-creator`: criação de skills, via marketplace `anthropics/skills`
- Skill própria `cenario-teste-csv` (`.claude/skills/cenario-teste-csv/SKILL.md`), criada com o skill-creator,
  para gerar cenários de teste de integração com CSVs alternativos.

## Processo planejado
1. Planejamento — concluído
2. Contexto (CLAUDE.md) — concluído
3. Ferramentas (plugin de revisão e skill de testes) — concluído
4. Implementação em etapas — concluído
5. Testes de integração — concluído
6. Documentação — concluído

## Configurações do agente
- Commits e push bloqueados para o agente em `.claude/settings.json`;
  todo commit passa pela minha revisão.
- Plan mode para análise e planejamento; aprovação manual de edições.


## Decisões e correções
| Etapa | O que a IA propôs | O que decidi | Motivo |
|---|---|---|---|
| CLAUDE.md | Comparação exata de nomes | Case-insensitive (`Locale.ROOT`) | Maiúsculas não mudam a identidade do produtor |
| CLAUDE.md | Decidir as ambiguidades por mim | Respondi item a item | As decisões de regra de negócio são minhas |
| Revisão | Studios divididos só por `,` | Mesma regex de producers | Consistência e evitar "and X" em studios |
| Revisão | Remover a regra "menos de 2 vitórias" | Mantida | Regra crítica; explícita reduz erro do agente |
| Revisão | Bug: `, and` gerava "and Tom Hooper" (Cats) | Aceito: nova regex | Afetaria o resultado do CSV oficial |
| Revisão | — | Agente validou a regex no CSV real | Verificação antes de fixar a regra |
| Projeto | Leitura do CSV com `split` | Aceito, sem biblioteca | Formato simples; evita dependência extra |
| Projeto | — | Lombok adiado | Reavaliar após as entidades |
| Projeto | — | Renomeei `Movielist.csv` para `movielist.csv` | Nomes de arquivo diferenciam maiúsculas no jar e em Linux |
| Ferramentas | Plugin oficial de revisão | Instalado no escopo do projeto | A configuração fica versionada no repositório |
| Ferramentas | Custo de ~2,1k tokens/turno do plugin | Um único plugin de revisão | Controle do consumo de contexto |
| Skill | Alertou que classes `*IT` seriam ignoradas pelo Surefire | Sufixo `Test` | Todos os testes já são de integração; evita alterar o build |
| Skill | Pacote base fixo no corpo da skill | Editei manualmente para referenciar a classe principal | Evitar duplicar informação visível no código |
| Plano | Leitura do CSV numa etapa sem verificação possível | Juntei leitura e carga numa etapa | Cada etapa precisa de uma verificação concreta |
| Plano | README como etapa opcional | Obrigatório | Exigido pela especificação |
| Plano | Caminho sem prefixo | Falha a inicialização | Erro de configuração deve falhar cedo |
| Plano | Filme repetido com dados diferentes | Vale a primeira ocorrência | Simples e previsível |
| Plano | Habilitar o H2 console | Desligado | Desnecessário; o log basta para verificar a carga |
| Plano | Substituir o `contextLoads` | Pelo teste do oráculo na etapa do endpoint | Redundante e fora do padrão MockMvc |
| Plano | Quem cria branches e faz merges | Eu | Controle do histórico do repositório |
| Entidades | Parada para decidir sobre o Lombok | Não usar | Ganho de ~40 linhas; o design das entidades exigiria exceções e cuidados com o `@Builder` |
| Entidades | IntelliJ sugeriu `final` nas coleções | Não aplicado | A especificação JPA proíbe campos persistentes `final` |
| Entidades | Testes com SQL salvos em arquivo temporário e filtrados | Aceito | Economia de tokens, conforme o CLAUDE.md |
| Revisão | `keyOf` duplicado em Producer e Studio | Mantido | Extrair um utilitário seria abstração sem necessidade concreta |
| Revisão | Arquivos sem quebra de linha final | Corrigido e regra adicionada ao CLAUDE.md | Evitar ruído no diff e repetição nas próximas etapas |
| Carga CSV | Remoção do BOM com o caractere literal | Pedi o escape `\uFEFF`; o agente verificou e corrigiu a própria ferramenta, que tinha gravado o literal | Caractere invisível no código pode se perder conforme a codificação |
| Carga CSV | Chave de filme repetido concatenando título e ano | Record (título, ano) | Evitar colisões como "Filme1"+999 e "Filme"+1999 |
| Revisão | Falhas silenciosas: log sem total de linhas ignoradas | Aplicado | Uma carga toda rejeitada subiria sem aviso no resumo |
| Revisão | Título longo (>255) derruba a inicialização; sugeriu só registrar | Título ou nome acima de 255 caracteres = linha malformada, ignorada com aviso | Coerente com a regra de não derrubar a aplicação por uma linha; limite em constante única |
| Revisão | CSV em outra codificação lido sem erro | Registrado como risco | O CLAUDE.md define UTF-8; detectar codificação seria complexidade desnecessária |
| Revisão | Espaço não separável passa pelo trim | Mantido | Caso improvável; mudaria a regra de normalização |
| Revisão | pr-test-analyzer: CSV oficial em ordem cronológica não detecta falta de ordenação | Cenário com linhas fora de ordem na etapa 4 | O teste precisa falhar se a ordenação for removida |
| Revisão | Lacunas: winner com caixa variada, BOM, limites de 255 | Viraram requisitos e cenários da etapa 4 | Cobrir as regras do CLAUDE.md, não só o CSV oficial |
| Revisão | Teste de Problem Details como opcional | Incluído: 405 e 404 | Demonstra o nível 2 de Richardson exigido pela especificação |
| Revisão | Manter a verificação exata do content type | `contentTypeCompatibleWith` | Evita quebra caso a resposta inclua `;charset=UTF-8` |
| Cenários | Arquivos gerados sem quebra de linha final, mesmo com a regra no CLAUDE.md | Correção em commit `style` separado; o agente salvou uma memória para conferir sempre | Limitação da ferramenta de escrita; manter o histórico limpo |
| Cenários | Instrução ambígua minha na retomada fez o agente criar o cenário 5 sem apresentar a proposta | Conferi o cálculo depois da criação e reforcei o passo 0 da skill | Instruções explícitas evitam que o agente interprete "seguir" como aprovação |
| Cenários | Padrão de teste de falha do CLAUDE.md usava `.properties()`, de prioridade menor que o `application.properties`; os testes falharam | Caminho passado como argumento de linha de comando; contexto fechado se subir inesperadamente; CLAUDE.md corrigido | O teste revelou um erro na própria regra; corrigir a regra, não o esperado |
| Cenários | Corpo do erro com STRICT completo dependeria de textos do Spring | LENIENT nos campos estáveis do RFC 9457, como exceção explicada | Testar o comportamento do projeto, não o texto do framework |
| README | Usar outro CSV só via `spring-boot:run` | Incluí a execução pelo jar, caminho no Windows e aspas no PowerShell | É o comando que o avaliador usará com os próprios dados |
| README | Uso de IA apenas como link para `docs/` | Seção própria no README | Registro de interações exigido pela especificação |
| README | Limitações sem os erros de digitação do CSV | Acrescentada | Decisão consciente desde o CLAUDE.md |
| README | Verificação só com o CSV original | Verificação também com CSVs de teste, pelas três formas de execução | Garantir que o comando de outro CSV funciona de verdade |
| Revisão final | Comentários com cálculos imprecisos e um teste que passaria pelo motivo errado | Corrigidos | O comentário com o cálculo à mão é a documentação do teste |
| Revisão final | Carga do CSV depois de o servidor aceitar requisições | Carga antes de o servidor subir | Evitar resposta vazia indistinguível do caso "sem dados" |
| Revisão final | LENIENT nos testes de erro contrariava a regra STRICT | Exceção registrada no CLAUDE.md | Manter regra e código coerentes |
| Revisão final | CSV fora de UTF-8, `keyOf` duplicado, invariantes nos tipos | Mantidos | Já decididos ou defensivos demais para o escopo |

## Logs
- `docs/ai-log/01-claude-md.md`: criação e revisão do CLAUDE.md.
- `docs/ai-log/02-ferramentas.md`: criação da skill `cenario-teste-csv`. A instalação dos plugins foi feita pelo menu `/plugin`, que não aparece nos exports.
- `docs/ai-log/03-plano-implementacao.md`: plano de implementação (salvo em `docs/PLANO.md`).
- `docs/ai-log/04-entidades.md`: etapa 1, entidades e repositórios; decisão sobre o Lombok e revisão de código.
- `docs/ai-log/05-carga-csv.md`: etapa 2, leitura e carga do CSV; ajustes do BOM, da chave de filme repetido e do limite de 255 caracteres, revisão de código e de falhas silenciosas.
- `docs/ai-log/06-intervalos-premios.md`: etapa 3, cálculo dos intervalos e endpoint; revisão de código e de testes, com novos cenários incorporados ao plano.
- `docs/ai-log/07-cenarios-parte1.md`: etapa 4, cenários 1 a 3 (resultado vazio, intervalo 0 e empates).
- `docs/ai-log/07-cenarios-parte2.md`: etapa 4, cenários 4 a 8 (pares fora de ordem, nomes, separadores e repetições).
- `docs/ai-log/07-cenarios-parte3.md`: etapa 4, cenários 9 a 13 (linhas malformadas, winner, BOM, falha de inicialização e status HTTP).
- `docs/ai-log/08-readme.md`: etapa 5, README; verificação seguindo o README do zero, inclusive com outros CSVs.
- `docs/ai-log/09-revisao-final.md`: revisão final com os 6 agentes do pr-review-toolkit e correção dos comentários (passo 1).
- `docs/ai-log/09b-revisao-final-continuacao.md`: continuação da revisão final: dataset, regra do LENIENT, carga antes do servidor subir e quatro cenários novos.