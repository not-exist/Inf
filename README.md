# Inf

A Minecraft 1.21.1 (NeoForge) mod about forging blades: every hammer strike raises a blade's value and its chance to shatter. Stop at the right moment, bank the weapon, and carry its tier out of the forge.

The design and implementation plan live in `docs/superpowers/`.

## Build

Requires JDK 21. This repository was developed with Temurin 21.

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew build
JAVA_HOME=/path/to/jdk-21 ./gradlew runClient
```

The output jar is `build/libs/inf-0.1.0.jar`.
