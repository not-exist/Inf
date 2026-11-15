# Configuration

Config file: `config/inf-common.toml` (server-side COMMON config).

| Key | Default | Range | Effect |
|---|---:|---|---|
| `baseRisk` | `0.015` | 0–1 | Shatter chance of the first strike. |
| `riskPerStrike` | `0.0015` | 0–1 | Added shatter chance per completed strike on the same blade. |
| `riskPerValue` | `0.000035` | 0–1 | Added shatter chance per point of blade value. |
| `maxRisk` | `0.85` | 0–1 | Hard cap on shatter chance. |
| `baseGain` | `12` | 0–1000 | Base value gained by a successful strike. |
| `gainPerStrike` | `1` | 0–1000 | Progress bonus; the formula adds `min(strikes / 2 * gainPerStrike, 10)`. |
| `gainVariance` | `8` | 0–1000 | Random bonus range: `roll(0..99) * gainVariance / 100`. |
| `minGain` | `4` | 0–1000 | Minimum value gained by a successful strike. |
| `frenzyRequired` | `10` | 1–1000 | Completed weapons of one kind needed to charge Frenzy. |
| `frenzyFreeStrikes` | `3` | 1–100 | Unbreakable strikes granted by one Frenzy activation. |
| `strikeCooldownTicks` | `4` | 0–200 | Minimum ticks between two strikes. `0` disables the cooldown. |
| `uiAnimations` | `true` | boolean | Enables client bar easing and other GUI animations. |

The GUI never reads the risk formula from the client config: the server sends
value, risk permille, tier, gain range and Frenzy state through `ContainerData`.
Changing the config only affects newly opened or currently synced menus, and
only the server's values are authoritative.
