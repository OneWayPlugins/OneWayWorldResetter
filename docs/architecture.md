# Architecture

## Module graph

```
api            → contratos puros (interfaces, eventos, modelo)
core           → regras de negócio (depende SÓ de api)
platform-paper → impls Bukkit/Paper das abstrações
platform-folia → impls Folia (GlobalRegionScheduler, etc.)
integrations   → adapters soft-dep (Multiverse, PAPI, Discord)
plugin         → composition root, gera o JAR final via Shadow
```

Regras de dependência impostas pelo compilador:
- `api → (nada)`
- `core → api`
- `platform-* → core`
- `integrations → api (+ core compileOnly)`
- `plugin → todos`

Nenhum módulo abaixo de `plugin` importa de `platform-*`.

## Máquina de fases

`IDLE → PRECHECK → TELEPORT → UNLOAD → DELETE → RECREATE → VERIFY → COMPLETE`
(ou `FAILED` em qualquer ponto.)

Cada transição persiste o ResetState em JSON atômico para crash recovery.
Falhas disparam retry com backoff (até 3 tentativas).

## Componentes principais

- **`Scheduler`** (core): abstração de scheduling Bukkit vs Folia.
- **`WorldLifecycleService`** (core): create/unload/delete/regenerate por adapter.
- **`ResetService`** (core): orquestrador central com fila e maxConcurrentResets.
- **`ResetStrategy`** (core): in-place ou double-buffered, plugáveis.
- **`PreflightGate`** (core): TpsGate / OnlinePlayersGate / DiskSpaceGate.
- **`StateRepository` + `HistoryRepository`** (core): JSON atômico via Gson.
- **`EventBus`** (core, impl em platform-paper): bridge para Bukkit Event.

## API pública

```java
OwwrService api = Bukkit.getServicesManager().load(OwwrService.class);
List<ResourceWorld> worlds = api.worlds();
api.requestReset("mining", false);
```

Eventos:

```java
@EventHandler
public void onPreReset(BukkitPreResetEvent event) {
    if (myCondition()) event.setCancelled(true);
}
```
