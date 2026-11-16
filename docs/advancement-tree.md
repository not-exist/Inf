# Advancement Tree

19 advancements under `data/inf/advancement/`, all using
`minecraft:impossible` except the root's inventory trigger. The server awards
them from `ForgeService`; clients cannot fake them.

```
root (obtain an Inf forge hammer)
└─ first_strike
   ├─ first_weapon (goal)
   │  ├─ frenzy (goal)
   │  ├─ tier_common → tier_fine → tier_rare → tier_epic → tier_legendary (challenge)
   │  └─ kind_sword / kind_pickaxe / kind_axe / kind_scythe / kind_dagger
   │     └─ all_kinds (challenge, awarded when all five kind nodes are done)
   ├─ break_blade
   ├─ hammer_break
   ├─ damage_blade
   └─ great_success (goal)
```

## Triggers

| Node | Awarded when |
|---|---|
| `root` | The player has `inf:hammer` in their inventory (`inventory_changed`). |
| `first_strike` | Any strike resolves, including break/damage. |
| `first_weapon` | `stop` successfully banks a weapon. |
| `break_blade` | A strike returns `BREAK`. |
| `hammer_break` | The last hammer durability is consumed. |
| `damage_blade` | A strike returns `DAMAGE`. |
| `great_success` | A strike returns `GREAT_SUCCESS`. |
| `frenzy` | `activateFrenzy` succeeds. |
| `tier_common` … `tier_legendary` | Banking a weapon at that tier (crude has no node). |
| `kind_*` | Banking a weapon of that kind. |
| `all_kinds` | All five `kind_*` nodes are done. |

## Data layout

- Display backgrounds: `inf:textures/gui/advancement_background.png`.
- Titles/descriptions: `advancements.inf.<node>.title` / `.description` in
  `zh_cn` and `en_us`.
- Frames: `task` for early nodes, `goal` for first weapon / frenzy / great
  success, `challenge` for legendary and all kinds.
