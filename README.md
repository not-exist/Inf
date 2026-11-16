# Inf

A Minecraft 1.21.1 (NeoForge) mod about forging blades. Every hammer strike raises a blade's value and its chance to shatter. Stop at the right moment, bank the weapon, and carry its tier out of the forge.

## Loop

1. Craft a **Forge Hammer** (iron block + blaze rod) and a blade blank.
2. Put the blank and the hammer into the **Forge Table**.
3. Strike. Each strike rolls one of four outcomes: **BREAK**, **DAMAGE**
   (lose 10% value), **SUCCESS**, or **GREAT_SUCCESS** (double gain). The
   four probabilities are listed live on the right side of the GUI.
4. Stop before greed wins. The blade becomes a forged weapon named and coloured by its tier, with tier-specific attributes.
5. Each strike costs one point of hammer durability. A hammer holds 128 strikes.

Weapons: sword, pickaxe, axe, scythe (long reach, heavy sweep), dagger (fast, high damage, short reach, no knockback, no sweep).

Tiers: Crude → Common → Fine → Rare → Epic → Legendary. Thresholds are 0 / 100 / 240 / 450 / 720 / 1000 value.

Frenzy: finishing 10 weapons of the same kind charges a window of 3 unbreakable strikes.

## Recipes

- Hammer: shapeless iron block + blaze rod.
- Blanks: iron ingots + sticks (sword 2+1, pickaxe/axe/scythe 3+2, dagger 1+1).
- Forge Table: 3×3 with six iron ingots around an anvil (`III` / ` A ` / `III`).

## Build

Requires JDK 21 (Temurin 21 was used).

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew build
JAVA_HOME=/path/to/jdk-21 ./gradlew runClient
```

Output: `build/libs/inf-0.2.0.jar`. Tests: `./gradlew test`.

## Configuration

See `docs/configuration.md`.

## UI and art

The forge table opens a full-screen dark-forge scene with a 420×260 panel:
anvil and slots on the left, blade/value in the centre, four outcome
probabilities on the right, player inventory at the bottom right. All item,
block and GUI textures are generated for this mod (`tools/gen_assets.py`), not
reused from vanilla.

## Advancements

19 vanilla-style advancements cover the forge, all five weapon kinds, every
tier, break/damage/great-success/Frenzy and the full armory. See
`docs/advancement-tree.md`.

## Status

Implemented: forging loop with four outcomes, five weapon kinds, tiers and attributes, hammer durability, Frenzy, tier colours and tooltips, full-screen forge UI, advancement tree.

Not in this version: dragon tribute, relic machine, heat/timing minigames, custom dimensions, material tiers.
