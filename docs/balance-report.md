# Balance Report

Command: `./gradlew -q balanceSim`

Parameters: `ForgeBalance.DEFAULT` = baseRisk `0.015`, riskPerStrike `0.0014`,
riskPerValue `0.00003`, maxRisk `0.85`, baseGain `12`, gainPerStrike `1`,
gainVariance `8`, minGain `4`. Outcome bands: damage `8%` of the non-break
probability, great success `15%` of the remaining probability. Trials
`100000`, seed `42`.

| Outcome | Rate |
|---|---:|
| Crude or better | 100.00% |
| Common or better | 87.58% |
| Fine or better | 68.68% |
| Rare or better | 42.28% |
| Epic or better | 17.43% |
| Legendary | 5.19% |
| Average strikes per blade | 22.02 |

## Reading

- Legendary lands in the designed 4–6% band (5.19%).
- Damage results lower value without ending the blade; great successes
  double the gain. The two roughly offset, but greed still dominates.
- The median blade (Common through Fine) takes roughly 10–22 strikes, about
  one hammer's worth of durability for two to three blades.
- The `higherRiskLowersLegendaryRate` test pins the direction: doubling
  `riskPerStrike` must reduce the legendary rate.

## Tuning rule

If future tuning is needed, change `ForgeBalance.DEFAULT` and the matching
`InfConfig` defaults, then re-run this report and the simulation tests. Do not
change the formula to make a target rate fit.
