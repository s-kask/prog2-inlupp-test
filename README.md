# Template för PROG2 Inlupp 1 och 2

## Bygg och testa Inlupp 1 och kör Inlupp 2

```bash
mvn package
```

För att testa JavaFX innan Inlupp 1 är klar:

```bash
mvn package -Dmaven.test.skip
```

## Bygg och testa enbart Inlupp 1

```bash
mvn compile test -pl backend
```
