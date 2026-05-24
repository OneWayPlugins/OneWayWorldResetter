# Config reference

A configuração vive em `plugins/OneWayWorldResetter/config.yml`. Ver
`core/src/test/resources/config-sample.yml` para um exemplo completo testado.

## settings

| chave | tipo | default | descrição |
|---|---|---|---|
| locale | string | "en" | locale do messages.yml |
| max-concurrent-resets | int | 1 | resets simultâneos máximos da fila global |
| update-checker | bool | true | checa releases no GitHub |
| metrics | bool | true | bStats |

## resource-worlds.<id>

Cada chave é um id lógico. Campos:
- `world-name` (string, obrigatório): nome do diretório do mundo.
- `environment` (NORMAL | NETHER | END).
- `enabled` (bool): se desabilitado, é ignorado pelo scheduler.
- `auto-create` (bool): cria o mundo se não existir no start.
- `seed.strategy`: FIXED | RANDOM | CYCLING.
- `seed.values`: lista de longs (usados em cycling/fixed).
- `reset.strategy`: "in-place" | "double-buffered".
- `reset.schedule.type`: daily | weekly | monthly | cron.
- `reset.schedule.time`: "HH:MM".
- `reset.schedule.day`: 1-7 (weekly) ou 1-31 (monthly).
- `reset.schedule.cron`: expressão cron (Fase 9).
- `reset.warnings-minutes`: list[int].
- `reset.gates.<nome>`: { value, on-fail }, on-fail ∈ delay | abort | force.
- `reset.pause-autosave`: bool.
- `reset.grace-warning`: bool — avisa antes do unload sobre itens largados.
- `teleport.mode`: "spawn" | "rtp".
- `teleport.safe-location`: bool — RTP tenta achar local seguro.
- `teleport.destination-on-reset`: mundo destino durante reset.
- `world-settings.difficulty`: PEACEFUL | EASY | NORMAL | HARD.
- `world-settings.gamerules`: map<string,string>.
- `world-settings.border`: { center: [x,z], size }.
- `regions.enabled`, `regions.list`: reset por região (Fase 6).
- `copy-regions`: cópia opcional de proteções (WG/Residence/GP).
- `notifications`: bossbar/title/actionbar/discord-webhook.

## resource-worlds.<id>.reset.chunky (v1.1)

Bloco opcional. Quando ausente OU `enabled: false`, a fase PREGEN é skipada.

| chave | tipo | default | descrição |
|---|---|---|---|
| enabled | bool | false | Habilita pre-gen pós-reset |
| shape | string | (required) | "square" \| "circle" \| "star" \| "diamond" \| "triangle" |
| center | [int, int] | (required) | [x, z] em blocos |
| radius | double | (required, > 0) | Raio em blocos |
| max-duration-minutes | int | 60 | Limite de tempo; expirar = sucesso parcial |
| block-teleport-during-pregen | bool | true | Bloqueia /resource enquanto pre-gen roda |
| failure-behavior | string | "critical" | "critical" (retry) \| "warning" (sucesso parcial) |
| notifications.bossbar | bool | false | Mostra bossbar durante pre-gen |
| notifications.actionbar | bool | false | Mostra actionbar durante pre-gen |
| notifications.discord | bool | false | (Reservado para webhook Discord futuro) |
