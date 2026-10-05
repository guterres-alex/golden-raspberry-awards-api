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
4. Implementação em etapas — em andamento
5. Testes de integração — pendente
6. Documentação — pendente

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


## Logs
- `docs/ai-log/01-claude-md.md`: criação e revisão do CLAUDE.md.
- `docs/ai-log/02-ferramentas.md`: criação da skill `cenario-teste-csv`.
  A instalação dos plugins foi feita pelo menu `/plugin`, que não aparece nos exports.
- `docs/ai-log/03-plano-implementacao.md`: plano de implementação (salvo em `docs/PLANO.md`).
- `docs/ai-log/04-entidades.md`: etapa 1, entidades e repositórios; decisão sobre o Lombok e revisão de código.