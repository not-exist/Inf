# Balance Report

Command: `./gradlew -q balanceSim`

Parameters: `ForgeBalance.DEFAULT` = baseRisk `0.015`, riskPerStrike `0.0015`,
riskPerValue `0.000035`, maxRisk `0.85`, baseGain `12`, gainPerStrike `1`,
gainVariance `8`, minGain `4`. Trials `100000`, seed `42`.

| Outcome | Rate |
|---|---:|
| Crude or better | 100.00% |
| Common or better | 87.09% |
| Fine or better | 67.74% |
| Rare or better | 41.74% |
| Epic or better | 17.89% |
| Legendary | 5.55% |
| Average strikes per blade | 21.20 |

## Reading

- Legendary lands in the designed 4–6% band (5.55%).
- A player who always stops at the first tier still gets a weapon 87% of the time; the loss is concentrated in greed, not in early turns.
- The median blade (Common through Fine) takes roughly 10–20 strikes, which is about one hammer's worth of durability for two to three blades.
- The `higherRiskLowersLegendaryRate` test pins the direction: doubling `riskPerStrike` must reduce the legendary rate.

## Tuning rule

If future tuning is needed, change `ForgeBalance.DEFAULT` and the matching
`InfConfig` defaults, then re-run this report and the simulation tests. Do not
change the formula to make a target rate fit.
