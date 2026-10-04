# Uso de IA no desenvolvimento

## Ferramentas
- Claude Code (CLI + plugin IntelliJ), plano Pro, para entender o desafio, planejar o processo e desenvolvimento.

## Processo planejado
1. Planejamento — concluído
2. Contexto (CLAUDE.md) — concluído
3. Ferramentas (plugin de revisão e skill de testes) — pendente
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

## Logs
- `docs/ai-log/01-claude-md.md`: criação e revisão do CLAUDE.md.