# Weather2-Remixed
Forge 1.20.1 migration branch for Weather2.

## Target Stack
- Minecraft: **1.20.1**
- Forge: **47.x**
- Java: **17**

## Current Migration Layout
Because 1.12.2 -> 1.20.1 is a major API rewrite, the repository now compiles from the 1.20 entrypoint sources under:

- `src/forge20/java`

Legacy 1.12-era sources remain in `src/main/java` for iterative porting and are not part of the active compile source set.

## Coroutil
Weather2 depends on Coroutil at runtime. During migration, place the Forge 1.20.1 Coroutil jar at:

- `libs/CoroUtil-forge-1.20.1.jar`

## Build
```bash
JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2 ./gradlew build
```

If wrapper download is blocked in your environment, use an installed Gradle with Java 17 and ensure Forge/Maven endpoints are reachable.
