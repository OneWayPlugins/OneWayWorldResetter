# OneWayWorldResetter

Reset agendado de resource worlds para servidores Paper e Folia (MC 1.21+).

## Desenvolvimento

```bash
./gradlew build              # compila tudo + roda testes
./gradlew :plugin:runServer  # sobe um Paper de teste com o plugin
doxygen Doxyfile             # gera documentação em docs/generated/html
```

Plugin descritor: `plugin/src/main/resources/paper-plugin.yml`.
Composition root: `plugin/.../OneWayWorldResetterPlugin.java`.
