# Inf

A Minecraft 1.21.1 (NeoForge) mod about forging blades. Every hammer strike raises a blade's value and its chance to shatter. Stop at the right moment, bank the weapon, and carry its tier out of the forge.

## Loop

1. Craft a **Forge Hammer** (iron block + blaze rod) and a blade blank.
2. Put the blank and the hammer into the **Forge Table**.
3. Strike. A strike adds value and raises the shatter risk; a failed strike destroys the blank.
4. Stop before greed wins. The blade becomes a forged weapon named and coloured by its tier, with tier-specific attributes.
5. Each strike costs one point of hammer durability. A hammer holds 128 strikes.

Weapons: sword, pickaxe, axe, scythe (long reach, heavy sweep), dagger (fast, high damage, short reach, no knockback, no sweep).

Tiers: Crude → Common → Fine → Rare → Epic → Legendary. Thresholds are 0 / 100 / 240 / 450 / 720 / 1000 value.

Frenzy: finishing 10 weapons of the same kind charges a window of 3 unbreakable strikes.

## Recipes

- Hammer: shapeless iron block + blaze rod.
- Blanks: iron ingots + sticks (sword 2+1, pickaxe/axe/scythe 3+2, dagger 1+1).
- Forge Table: 3×3 iron ingot ring around an anvil.

## Build

Requires JDK 21 (Temurin 21 was used).

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew build
JAVA_HOME=/path/to/jdk-21 ./gradlew runClient
```

Output: `build/libs/inf-0.1.0.jar`. Tests: `./gradlew test`.

## Configuration

See `docs/configuration.md`.

## Status

Implemented: forging loop, five weapon kinds, tiers and attributes, hammer durability, Frenzy, tier colours and tooltips, forge table GUI.

Not in this version: dragon tribute, relic machine, heat/timing minigames, custom dimensions, material tiers.
