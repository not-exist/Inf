# Release Checklist — v0.1.0

Verified in a headless environment (no display, client GUI not launched).

## Automated

- [x] `./gradlew clean test build` — BUILD SUCCESSFUL.
- [x] 31 unit tests, 0 failures (`build/test-results/test/*.xml`).
- [x] `./gradlew runServer` — `Done (0.318s)`, no registry or data-pack errors.
- [x] Jar produced: `build/libs/inf-0.1.0.jar`.

## Manual (requires a display)

- [ ] Craft hammer, five blanks, forge table.
- [ ] Insert blank + hammer; 10 strikes update value, risk, durability, gain range.
- [ ] Bank a blade; check name, tier colour, tooltip and attributes.
- [ ] Break a blade; confirm the blank is destroyed and the hammer still wears.
- [ ] Use the last hammer durability; confirm the strike resolves and the hammer breaks.
- [ ] Fill the output slot; confirm banking is rejected without item loss.
- [ ] Break the forge table; confirm slot contents and the block drop.
- [ ] Charge Frenzy (10 same-kind weapons), activate it, confirm 3 unbreakable strikes.
- [ ] Dagger: no knockback, no sweep, short reach, fast attacks.
- [ ] Scythe: long reach and wide sweep.
- [ ] Pickaxe: obsidian at legendary, no drop at crude.
- [ ] GUI scale 1/2/3, zh_cn/en_us: no clipping, no blur, all states legible.
- [ ] `uiAnimations=false`: information remains complete.
- [ ] Two clients: values stay in sync; server owns the rolls.

## Release

- [ ] Create `not-exist/Inf` on GitHub or replace `origin` with the real URL.
- [ ] `git push -u origin feat/forge-core --tags` (after merge decision).
