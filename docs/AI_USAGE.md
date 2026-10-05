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
4. Implementação em etapas — pendente
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

## Logs
- `docs/ai-log/01-claude-md.md`: criação e revisão do CLAUDE.md.
- `docs/ai-log/02-ferramentas.md`: criação da skill `cenario-teste-csv`.
  A instalação dos plugins foi feita pelo menu `/plugin`, que não aparece nos exports.