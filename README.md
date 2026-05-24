# OneWayWorldResetter

Reset agendado de **resource worlds** para servidores Paper e Folia (MC 1.21+).

Gerencia múltiplos mundos de coleta com reset automático, gates de preflight, recuperação
de crash, GUI de teleporte, suporte a Folia, e API pública para outros plugins.

## Features (v1.0)

- **Reset agendado:** daily, weekly, monthly e cron, com avisos progressivos.
- **Duas estratégias:** in-place (rápido, requer downtime curto) e double-buffered
  (gera staging em paralelo, swap por rename, preserva nome do mundo).
- **Preflight gates:** TPS, jogadores online, espaço em disco — com ações delay/abort/force.
- **Crash recovery:** estado persistido em JSON atômico, retoma em ~60s.
- **Retry automático:** até 3 tentativas com backoff.
- **Path-safety:** delete só sob o worldContainer canonical; reset por região valida regex `r.x.z.mca`.
- **Comandos via Cloud:** `/owwr` (admin) e `/resource` (jogador, com cooldown e GUI Triumph).
- **Eventos canceláveis:** `PreResetEvent`, `PostResetEvent` (e versões de região).
- **API pública:** `Bukkit.getServicesManager().load(OwwrService.class)`.
- **Suporte Folia:** scheduler regionalizado + WorldManager via GlobalRegionScheduler.
- **Soft-deps:** Multiverse-Core, PlaceholderAPI, Discord webhook.

## Instalação

1. Coloque `OneWayWorldResetter-X.Y.Z.jar` em `plugins/`.
2. Inicie o servidor; serão criados `plugins/OneWayWorldResetter/config.yml` e `messages.yml`.
3. Edite o `config.yml` (ver [docs/config-reference.md](docs/config-reference.md)).
4. `/owwr help` para os comandos disponíveis.

## Desenvolvimento

```bash
./gradlew build              # compila tudo + roda testes
./gradlew :plugin:runServer  # sobe um Paper de teste com o plugin
doxygen Doxyfile             # gera documentação em docs/generated/html
```

Plugin descritor: `plugin/src/main/resources/paper-plugin.yml`.
Composition root: `plugin/src/main/java/.../OneWayWorldResetterPlugin.java`.

## Arquitetura

Gradle multi-módulo:

| Módulo | Responsabilidade |
|---|---|
| `api` | Contratos públicos (interfaces, eventos, modelo de domínio). Zero Bukkit. |
| `core` | Regras de negócio (máquina de fases, fila, serviços, persistência). |
| `platform-paper` | Implementações Bukkit/Paper (scheduler, adapters de mundo, GUI). |
| `platform-folia` | Implementações Folia (region scheduler, world manager). |
| `integrations` | Adapters soft-dep (Multiverse, PAPI, Discord). |
| `plugin` | Composition root + Cloud commands + shaded jar. |

Ver [docs/architecture.md](docs/architecture.md) para o detalhamento.

## License

MIT — ver [LICENSE](LICENSE).
