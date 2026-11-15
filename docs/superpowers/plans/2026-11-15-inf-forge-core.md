# Inf 锻刀核心 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 Minecraft 1.21.1 / NeoForge 上做出可反复游玩的锻刀核心：刀胚逐锤加价值、风险累积、停手结算为带阶位属性与外观的武器，锤子按锤消耗耐久。

**Architecture:** 纯 Java 的数值内核（`com.inf.forge` / `com.inf.weapon`，不依赖 Minecraft 类）+ 一层薄薄的 NeoForge 适配（数据组件、方块实体、菜单、GUI、属性组件）。所有判定在服务端，客户端只显示服务端下发的 `ContainerData` 并发送按钮 id。

**Tech Stack:** Java 21、NeoForge 21.1.x、ModDevGradle 2.x、Minecraft 1.21.1 官方映射、JUnit 5。

**Spec:** `docs/superpowers/specs/2026-11-15-inf-forge-design.md`

## Global Constraints

- Minecraft `1.21.1`，NeoForge `21.1.x`，Java 21，官方 Mojang 映射；mod id `inf`，主类 `com.inf.Inf`。
- `com.inf.forge` 与 `com.inf.weapon` 下的纯逻辑类**不得 import `net.minecraft.**`**，保证 JUnit 可在无游戏启动的情况下运行。
- 所有随机数、碎裂判定、阶位结算只在服务端执行；客户端不得决定任何数值。
- 设计文档中的数值是唯一事实来源：`ForgeBalance.DEFAULT`、阶位阈值、属性表必须逐字一致。
- 玩家可见文本必须同时提供 `zh_cn` 与 `en_us`，`zh_cn` 为默认。
- 本阶段不实现：龙贡、遗物机、抽票、控温/节奏小游戏、自定义维度、材质分级。
- 每个任务结束必须 `./gradlew build` 通过；纯逻辑任务必须 `./gradlew test` 通过。
- 版本/API 名称以 1.21.1 映射为准；若计划中的签名与 21.1.x 实际映射有出入，以编译器提示的同义 API 为准，并在提交信息里记下差异。

## Review Focus

1. **错误物品进入槽位**：把成品刀、泥土或别的模组锤子放进刀胚/锤子槽，必须被拒绝且不吞物品。→ Task 6 的槽位校验与手动检查。
2. **锤子最后 1 点耐久**：成功锻打后锤子碎裂，本次锻打必须已经结算，且不能复制刀胚或锤子。→ Task 5 的耐久测试与 Task 7 的顺序约束。
3. **产出槽已占用时停手**：必须拒绝停手，刀胚与产出都不丢失。→ Task 8 的 `stop` 前置条件测试。
4. **破坏锻刀台**：槽位里的物品与方块自身都要掉落，不能蒸发。→ Task 6 的 `onRemove` 掉落与客户端检查。
5. **连点 / 伪造按钮包**：客户端狂点或伪造 `clickMenuButton` 不能绕过冷却、耐久与槽位校验。→ Task 7 的服务端校验与冷却测试。

## Milestones

| 里程碑 | 包含任务 | 交付物 | 估算（熟练 NeoForge 开发者） |
|---|---|---|---|
| M1 内核 | 1–3 | 可启动工程 + 全部纯逻辑与单测 | 2.5 天 |
| M2 物品与工作台 | 4–6 | 11 个物品、锤子配方、可打开的锻刀台 GUI | 3 天 |
| M3 锻造闭环 | 7–8 | 锻打/碎裂/停手/成品属性全链路 | 2 天 |
| M4 表现与行为 | 9–10 | 阶位染色、Tooltip、匕首/镰刀/镐/斧差异 | 1.5 天 |
| M5 Frenzy | 11 | 充能与免碎窗口 | 1 天 |
| M6 打磨与平衡 | 12–13 | 双语文案、文档、平衡模拟报告 | 1.5 天 |
| M7 发布验证 | 14 | 可发布 jar + 验证清单 | 1 天 |

总计约 12.5 个开发日。M2 的 GUI 与 M3 的结算强耦合，按顺序做；M4 的三种行为彼此独立，可由不同执行者并行；M6 的平衡模拟只依赖纯逻辑，可在 M3 结束后随时开始。

---

### Task 1: NeoForge 1.21.1 工程骨架

**Files:**
- Create: `settings.gradle`, `gradle.properties`, `build.gradle`, `gradle/wrapper/*`, `gradlew`, `gradlew.bat`
- Create: `src/main/java/com/inf/Inf.java`
- Create: `src/main/resources/META-INF/neoforge.mods.toml`
- Create: `src/main/resources/pack.mcmeta`
- Create: `.gitignore`, `README.md`

**Interfaces:**
- Consumes: 无
- Produces: `com.inf.Inf`，常量 `Inf.MODID = "inf"`；可运行的 `runClient` / `runServer` / `build` Gradle 任务。

- [ ] **Step 1: 用官方 1.21.1 ModDevGradle MDK 作为骨架**

以 `NeoForgeMDKs/MDK-1.21.1-ModDevGradle` 的文件结构为基准，写入仓库；把 `mod_id` 改为 `inf`、`mod_group_id` 改为 `com.inf`、`mod_name` 改为 `Inf`、`mod_version` 改为 `0.1.0`。`neoForgeVersion` 取 21.1.x 最新版（例如 `21.1.209`），`minecraftVersion=1.21.1`。

- [ ] **Step 2: 写主类与元数据**

`Inf.java`：

```java
@Mod(Inf.MODID)
public final class Inf {
    public static final String MODID = "inf";
    public Inf(IEventBus modBus, ModContainer container) { }
}
```

`neoforge.mods.toml` 中 `modId="inf"`，`displayName="Inf"`，`license` 先写 `All Rights Reserved`（发布前由仓库所有者确认），依赖只保留 `neoforge [21.1.0,)` 与 `minecraft [1.21.1]`。

- [ ] **Step 3: 构建验证**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`，`build/libs/inf-0.1.0.jar` 存在。

- [ ] **Step 4: 客户端启动验证**

Run: `./gradlew runClient`
Expected: 游戏进入主菜单，模组列表可见 `Inf 0.1.0`，日志无 `Missing or unsupported mandatory dependencies`。

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "chore: bootstrap NeoForge 1.21.1 project for Inf"
```

---

### Task 2: 锻刀纯逻辑内核

**Files:**
- Create: `src/main/java/com/inf/weapon/WeaponKind.java`
- Create: `src/main/java/com/inf/weapon/WeaponTier.java`
- Create: `src/main/java/com/inf/forge/ForgeBalance.java`
- Create: `src/main/java/com/inf/forge/StrikeOutcome.java`
- Create: `src/main/java/com/inf/forge/ForgeMath.java`
- Test: `src/test/java/com/inf/forge/ForgeMathTest.java`
- Test: `src/test/java/com/inf/weapon/WeaponTierTest.java`
- Modify: `build.gradle`（JUnit 5）

**Interfaces:**
- Consumes: 无
- Produces:
  - `enum WeaponKind { SWORD, PICKAXE, AXE, SCYTHE, DAGGER }`，字段 `public final String id`，方法 `translationKey()`、`static WeaponKind byId(String id)`（未知 id 抛 `IllegalArgumentException`）
  - `enum WeaponTier { CRUDE, COMMON, FINE, RARE, EPIC, LEGENDARY }`，方法 `static WeaponTier forValue(int value)`、`translationKey()`
  - `record ForgeBalance(double baseRisk, double riskPerStrike, double riskPerValue, double maxRisk, int baseGain, int gainPerStrike, int gainVariance, int minGain)`，常量 `DEFAULT = new ForgeBalance(0.015, 0.0012, 0.00003, 0.85, 12, 1, 8, 4)`
  - `record StrikeOutcome(int value, int strikes, boolean broken, int gain, double breakChance)`
  - `ForgeMath.breakChance(int value, int strikes, ForgeBalance b) -> double`
  - `ForgeMath.valueGain(int strikes, int gainRoll0to99, ForgeBalance b) -> int`
  - `ForgeMath.strike(int value, int strikes, double breakRoll, int gainRoll, boolean frenzy, ForgeBalance b) -> StrikeOutcome`

- [ ] **Step 1: 加 JUnit 5 并写失败测试**

`build.gradle` 增加 `testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'` 与 `tasks.named('test') { useJUnitPlatform() }`。`ForgeMathTest`：

```java
private static final ForgeBalance B = ForgeBalance.DEFAULT;

@Test void breakChance_atOriginIsBaseRisk() {
    assertEquals(0.015, ForgeMath.breakChance(0, 0, B), 1e-9);
}
@Test void breakChance_growsWithStrikesAndValue() {
    double a = ForgeMath.breakChance(0, 0, B);
    double b = ForgeMath.breakChance(0, 10, B);
    double c = ForgeMath.breakChance(100, 10, B);
    assertTrue(a < b && b < c);
}
@Test void breakChance_capsAtMaxRisk() {
    assertEquals(0.85, ForgeMath.breakChance(1_000_000, 10_000, B), 1e-9);
}
@Test void valueGain_staysWithinDocumentedRange() {
    for (int roll = 0; roll < 100; roll++)
        for (int strikes = 0; strikes < 60; strikes++) {
            int gain = ForgeMath.valueGain(strikes, roll, B);
            assertTrue(gain >= 4 && gain <= 30);
        }
}
@Test void strike_breaksWhenRollIsBelowChance() {
    assertTrue(ForgeMath.strike(0, 0, 0.0, 50, false, B).broken());
}
@Test void strike_survivesWhenRollEqualsChance() {
    double risk = ForgeMath.breakChance(0, 0, B);
    assertFalse(ForgeMath.strike(0, 0, risk, 50, false, B).broken());
}
@Test void strike_frenzyNeverBreaksButStillGains() {
    StrikeOutcome o = ForgeMath.strike(0, 0, 0.0, 99, true, B);
    assertFalse(o.broken());
    assertTrue(o.value() > 0);
    assertEquals(1, o.strikes());
}
@Test void strike_incrementsStrikes() {
    assertEquals(1, ForgeMath.strike(0, 0, 0.99, 0, false, B).strikes());
}
```

`WeaponTierTest`：

```java
@Test void forValue_hitsExactThresholds() {    assertEquals(WeaponTier.CRUDE, WeaponTier.forValue(0));
    assertEquals(WeaponTier.COMMON, WeaponTier.forValue(100));
    assertEquals(WeaponTier.FINE, WeaponTier.forValue(240));
    assertEquals(WeaponTier.RARE, WeaponTier.forValue(450));
    assertEquals(WeaponTier.EPIC, WeaponTier.forValue(720));
    assertEquals(WeaponTier.LEGENDARY, WeaponTier.forValue(1000));
}
@Test void forValue_justBelowThresholdStaysLower() {
    assertEquals(WeaponTier.CRUDE, WeaponTier.forValue(99));
    assertEquals(WeaponTier.COMMON, WeaponTier.forValue(239));
}
@Test void forValue_neverExceedsLegendary() {
    assertEquals(WeaponTier.LEGENDARY, WeaponTier.forValue(Integer.MAX_VALUE));
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew test --tests 'com.inf.forge.*' --tests 'com.inf.weapon.WeaponTierTest'`
Expected: 编译失败，`ForgeMath` / `WeaponTier` 不存在。

- [ ] **Step 3: 实现内核**

- `WeaponKind`：五个枚举值，`id` 为 `sword/pickaxe/axe/scythe/dagger`；`translationKey()` 返回 `"inf.weapon." + id`；`byId` 遍历 `values()` 匹配 `id`，未命中抛 `IllegalArgumentException`。**不要实现 `StringRepresentable`**，保持纯 Java。
- `WeaponTier`：六个枚举值，字段 `minValue` 按设计文档 `0/100/240/450/720/1000`；`forValue` 返回满足 `value >= minValue` 的最高阶位；`translationKey()` 返回 `"inf.tier." + name().toLowerCase(Locale.ROOT)`。
- `ForgeBalance`：record + `DEFAULT` 常量。
- `ForgeMath`：
  - `gain = Math.max(b.minGain(), b.baseGain() + Math.min(strikes / 2 * b.gainPerStrike(), 10) + gainRoll * b.gainVariance() / 100)`
  - `risk = Math.min(b.maxRisk(), b.baseRisk() + b.riskPerStrike() * strikes + b.riskPerValue() * value)`
  - `broken = !frenzy && breakRoll < risk`
  - 成功时 `value + gain`、`strikes + 1`；破碎时价值与次数保持传入值、`gain = 0`。
  - `StrikeOutcome.breakChance` 始终返回本次判定前的 `risk`。

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew test --tests 'com.inf.forge.*' --tests 'com.inf.weapon.WeaponTierTest'`
Expected: 全部 PASS。

- [ ] **Step 5: 提交**

```bash
git add build.gradle src/main/java/com/inf/forge src/main/java/com/inf/weapon src/test/java
git commit -m "feat: add pure forging math and tier thresholds"
```

---

### Task 3: 阶位属性表

**Files:**
- Create: `src/main/java/com/inf/weapon/WeaponStats.java`
- Create: `src/main/java/com/inf/weapon/WeaponStatsTable.java`
- Test: `src/test/java/com/inf/weapon/WeaponStatsTableTest.java`

**Interfaces:**
- Consumes: `WeaponKind`, `WeaponTier`
- Produces:
  - `record WeaponStats(double attackDamage, double attackSpeedModifier, double reachModifier, double sweepRatio, double miningSpeed, int harvestLevel)`
  - `WeaponStatsTable.forTier(WeaponKind kind, WeaponTier tier) -> WeaponStats`

- [ ] **Step 1: 写失败测试**

```java
@Test void sword_endpointsMatchDesign() {
    assertEquals(4.0, WeaponStatsTable.forTier(WeaponKind.SWORD, WeaponTier.CRUDE).attackDamage(), 1e-9);
    assertEquals(11.0, WeaponStatsTable.forTier(WeaponKind.SWORD, WeaponTier.LEGENDARY).attackDamage(), 1e-9);
    assertEquals(-2.4, WeaponStatsTable.forTier(WeaponKind.SWORD, WeaponTier.CRUDE).attackSpeedModifier(), 1e-9);
    assertEquals(-2.0, WeaponStatsTable.forTier(WeaponKind.SWORD, WeaponTier.LEGENDARY).attackSpeedModifier(), 1e-9);
}
@Test void dagger_hasNoSweepAndNegativeReach() {
    WeaponStats s = WeaponStatsTable.forTier(WeaponKind.DAGGER, WeaponTier.LEGENDARY);
    assertEquals(0.0, s.sweepRatio(), 1e-9);
    assertEquals(-0.5, s.reachModifier(), 1e-9);
    assertEquals(3.0, 4.0 + s.attackSpeedModifier(), 1e-9);
}
@Test void scythe_hasLongReachAndHighSweep() {
    WeaponStats s = WeaponStatsTable.forTier(WeaponKind.SCYTHE, WeaponTier.LEGENDARY);
    assertEquals(1.5, s.reachModifier(), 1e-9);
    assertEquals(1.2, s.sweepRatio(), 1e-9);
}
@Test void pickaxe_harvestLevelRisesToDiamond() {
    assertEquals(1, WeaponStatsTable.forTier(WeaponKind.PICKAXE, WeaponTier.CRUDE).harvestLevel());
    assertEquals(4, WeaponStatsTable.forTier(WeaponKind.PICKAXE, WeaponTier.LEGENDARY).harvestLevel());
    assertEquals(15.0, WeaponStatsTable.forTier(WeaponKind.PICKAXE, WeaponTier.LEGENDARY).miningSpeed(), 1e-9);
}
@Test void everyKind_scalesDamageMonotonically() {
    for (WeaponKind kind : WeaponKind.values()) {
        double previous = -1;
        for (WeaponTier tier : WeaponTier.values()) {
            double damage = WeaponStatsTable.forTier(kind, tier).attackDamage();
            assertTrue(damage > previous, kind + " " + tier);
            previous = damage;
        }
    }
}
```

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew test --tests 'com.inf.weapon.WeaponStatsTableTest'`
Expected: 编译失败。

- [ ] **Step 3: 按设计文档逐行落表**

每个 `(kind, tier)` 一个 `WeaponStats`，数值严格取设计文档第 5 节：

| kind | 伤害 | 攻速修正 | 距离修正 | 横扫 | 挖掘速度 | 挖掘等级 |
|---|---|---|---|---|---|---|
| sword | 4,5,6,7,9,11 | -2.4,-2.4,-2.3,-2.2,-2.1,-2.0 | 0 | .50,.55,.60,.65,.70,.80 | 0 | 0 |
| pickaxe | 2,3,4,5,6,8 | -2.8,-2.7,-2.6,-2.5,-2.4,-2.3 | 0 | 0 | 4,6,8,10,12,15 | 1,2,2,3,3,4 |
| axe | 6,7,8,10,12,15 | -3.0,-3.0,-2.9,-2.8,-2.7,-2.6 | 0 | 0 | 4,6,8,10,12,15 | 1,2,2,3,3,4 |
| scythe | 5,6,7,9,11,13 | -2.6,-2.6,-2.5,-2.4,-2.3,-2.2 | +.50,+.50,+.75,+1.0,+1.25,+1.5 | .80,.85,.90,.95,1.0,1.2 | 0 | 0 |
| dagger | 3,4,5,6,8,10 | -1.5,-1.4,-1.3,-1.2,-1.1,-1.0 | -1.0,-.9,-.8,-.7,-.6,-.5 | 0 | 0 | 0 |

- [ ] **Step 4: 运行确认通过**

Run: `./gradlew test --tests 'com.inf.weapon.WeaponStatsTableTest'`
Expected: 全部 PASS。

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/inf/weapon src/test/java/com/inf/weapon
git commit -m "feat: add per-tier weapon stat table"
```

---

### Task 4: 配置、物品、数据组件与创造模式标签页

**Files:**
- Create: `src/main/java/com/inf/InfConfig.java`
- Create: `src/main/java/com/inf/item/component/BladeData.java`
- Create: `src/main/java/com/inf/item/BladeBlankItem.java`
- Create: `src/main/java/com/inf/item/forged/ForgedSwordItem.java`
- Create: `src/main/java/com/inf/item/forged/ForgedPickaxeItem.java`
- Create: `src/main/java/com/inf/item/forged/ForgedAxeItem.java`
- Create: `src/main/java/com/inf/item/forged/ForgedScytheItem.java`
- Create: `src/main/java/com/inf/item/forged/ForgedDaggerItem.java`
- Create: `src/main/java/com/inf/registry/ModDataComponents.java`
- Create: `src/main/java/com/inf/registry/ModItems.java`
- Create: `src/main/java/com/inf/registry/ModCreativeTabs.java`
- Create: `src/main/resources/assets/inf/lang/zh_cn.json`
- Create: `src/main/resources/assets/inf/lang/en_us.json`
- Create: `src/main/resources/assets/inf/models/item/sword_blank.json`（其余 4 个刀胚、`hammer`、5 个成品同目录，共 11 个）
- Modify: `src/main/java/com/inf/Inf.java`
- Test: `src/test/java/com/inf/forge/ForgeBalanceConfigTest.java`

**Interfaces:**
- Consumes: Task 2、Task 3
- Produces:
  - `BladeData(WeaponKind kind, int value, int strikes, long seed)` + `BladeData.CODEC` / `BladeData.STREAM_CODEC`
  - `ModDataComponents.BLADE_DATA : Supplier<DataComponentType<BladeData>>`
  - `ModItems`：`SWORD_BLANK, PICKAXE_BLANK, AXE_BLANK, SCYTHE_BLANK, DAGGER_BLANK, HAMMER, FORGED_SWORD, FORGED_PICKAXE, FORGED_AXE, FORGED_SCYTHE, FORGED_DAGGER`，方法 `static Item forged(WeaponKind kind)`
  - `InfConfig.forgeBalance() -> ForgeBalance`
  - `InfConfig.frenzyRequired()/frenzyFreeStrikes()/strikeCooldownTicks()` 均为 `int`，`InfConfig.uiAnimations() -> boolean`（默认 `true`）

- [ ] **Step 1: 写配置映射的失败测试**

`ForgeBalanceConfigTest` 只测纯映射：给一组显式数值构造 `ForgeBalance`，断言字段顺序没有被写错（防止 toml 读反）：

```java
@Test void of_preservesArgumentOrder() {
    ForgeBalance b = ForgeBalance.of(0.02, 0.003, 0.00004, 0.9, 10, 2, 6, 3);
    assertEquals(0.02, b.baseRisk(), 1e-9);
    assertEquals(0.003, b.riskPerStrike(), 1e-9);
    assertEquals(0.00004, b.riskPerValue(), 1e-9);
    assertEquals(0.9, b.maxRisk(), 1e-9);
    assertEquals(10, b.baseGain());
    assertEquals(2, b.gainPerStrike());
    assertEquals(6, b.gainVariance());
    assertEquals(3, b.minGain());
}
```

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew test --tests 'com.inf.forge.ForgeBalanceConfigTest'`
Expected: 编译失败，`ForgeBalance.of` 不存在。

- [ ] **Step 3: 实现配置、组件、物品、语言文件**

- `ForgeBalance` 增加 `static of(...)`，参数顺序与 record 一致。
- `InfConfig`：`ModConfigSpec` 的 COMMON 配置，字段名与设计文档一致；`forgeBalance()` 读配置返回 `ForgeBalance`；默认值必须等于 `ForgeBalance.DEFAULT` 的八个数，另有 `frenzyRequired=10`、`frenzyFreeStrikes=3`、`strikeCooldownTicks=4`、`uiAnimations=true`。
- `BladeData`：`RecordCodecBuilder` 持久化（`kind/value/strikes/seed`；`kind` 用 `Codec.STRING.xmap(WeaponKind::byId, WeaponKind::id)`），`StreamCodec.composite` 网络同步（`kind` 用 `ByteBufCodecs.STRING_UTF8.map(WeaponKind::byId, WeaponKind::id)`）。
- `ModDataComponents`：`DeferredRegister.DataComponents.createDataComponents(Inf.MODID)`，注册 `blade_data`，`persistent(...).networkSynchronized(...)`。
- `BladeBlankItem extends Item`：构造参数 `(WeaponKind kind, Properties)`，暴露 `kind()`。
- 五种成品物品类：剑/匕首/镰刀继承 `SwordItem`，镐/斧继承 `DiggerItem`；构造时传 `Tiers.IRON` 与对应 `BlockTags.MINEABLE_WITH_*`，并**重写 `createAttributes()` 返回空 builder**（属性由 Task 8 写入组件，避免双重加成）。`ForgedPickaxeItem` 额外重写 `getDestroySpeed` / `isCorrectToolForDrops` 读取 `BladeData`（Task 10 完成具体逻辑，这里先返回默认值）。
- `ModItems`：注册 5 种刀胚、`hammer`（属性 `durability(128).stacksTo(1)`，Task 5 换类）、5 种成品；`forged(kind)` 用 switch 返回对应 `DeferredItem`。
- 11 个物品模型 JSON 全部先用原版贴图占位（父模型 `item/handheld` 或 `item/generated`）：成品剑/匕首 `minecraft:item/iron_sword`、镐 `iron_pickaxe`、斧 `iron_axe`、镰刀 `iron_hoe`、锤子 `minecraft:item/iron_block`、刀胚 `minecraft:item/iron_ingot`。
- `ModCreativeTabs`：一个 `inf` 标签页，按“锤子 → 刀胚 → 成品”顺序展示。
- `zh_cn.json` / `en_us.json`：`inf.tier.*`、`inf.weapon.*`、`item.inf.*`、`block.inf.forge_table`、`inf.weapon.name`（`"%s · %s"` / `"%s %s"`）。
- `Inf.java` 构造函数中注册所有 `DeferredRegister` 与 `container.registerConfig(ModConfig.Type.COMMON, InfConfig.SPEC)`。

- [ ] **Step 4: 构建并手动验证注册**

Run: `./gradlew build && ./gradlew runClient`
Expected: `BUILD SUCCESSFUL`；创造模式 `Inf` 页可见 11 个物品；`/give @s inf:sword_blank` 得到刀胚且不崩溃。

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "feat: register blade items, data component and config"
```

---

### Task 5: 锤子与合成配方

**Files:**
- Create: `src/main/java/com/inf/item/HammerItem.java`
- Create: `src/main/java/com/inf/forge/HammerMath.java`
- Create: `src/main/resources/data/inf/recipe/hammer.json`
- Create: `src/main/resources/data/inf/recipe/sword_blank.json`
- Create: `src/main/resources/data/inf/recipe/pickaxe_blank.json`
- Create: `src/main/resources/data/inf/recipe/axe_blank.json`
- Create: `src/main/resources/data/inf/recipe/scythe_blank.json`
- Create: `src/main/resources/data/inf/recipe/dagger_blank.json`
- Modify: `src/main/java/com/inf/registry/ModItems.java`
- Test: `src/test/java/com/inf/forge/HammerDurabilityTest.java`

**Interfaces:**
- Consumes: `ModItems.HAMMER`
- Produces: `HammerItem extends Item`，`public static final int DURABILITY = 128`；`HammerItem.canStrike(ItemStack) -> boolean`（委托 `HammerMath.canStrike`）
  - `HammerMath.remainingAfterStrikes(int maxDurability, int strikes) -> int`
  - `HammerMath.canStrike(int maxDurability, int damageValue) -> boolean`（`maxDurability - damageValue > 0`）

- [ ] **Step 1: 写失败测试**

```java
@Test void durabilityFloorIsZero() {
    assertEquals(0, HammerMath.remainingAfterStrikes(128, 128));
    assertEquals(1, HammerMath.remainingAfterStrikes(128, 127));
}
@Test void emptyHammerCannotStrike() {
    assertFalse(HammerMath.canStrike(128, 128));
    assertTrue(HammerMath.canStrike(128, 127));
}
```

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew test --tests 'com.inf.forge.HammerDurabilityTest'`
Expected: 编译失败。

- [ ] **Step 3: 实现锤子与配方**

- `HammerItem`：属性在 `ModItems` 中设为 `new Item.Properties().durability(128).stacksTo(1)`；`canStrike(ItemStack)` 委托 `HammerMath.canStrike(stack.getMaxDamage(), stack.getDamageValue())`。
- `hammer.json`：`minecraft:crafting_shapeless`，`iron_block` + `blaze_rod` → `inf:hammer`（数量 1）。
- 刀胚配方（shaped，`category: "equipment"`）：
  - `sword_blank`：`[" I ", " I ", " S "]`（2 铁锭 + 1 木棍）
  - `pickaxe_blank`：`["III", " S ", " S "]`（3 铁锭 + 2 木棍）
  - `axe_blank`：`["II ", "IS ", " S "]`（3 铁锭 + 2 木棍）
  - `scythe_blank`：`["III", "  S", "  S"]`（3 铁锭 + 2 木棍）
  - `dagger_blank`：`[" I ", " S "]`（1 铁锭 + 1 木棍）
  - key：`I = minecraft:iron_ingot`，`S = minecraft:stick`
- `ModItems.HAMMER` 改为 `registerItem("hammer", HammerItem::new, props)`。

- [ ] **Step 4: 运行测试与客户端验证**

Run: `./gradlew test --tests 'com.inf.forge.HammerDurabilityTest' && ./gradlew runClient`
Expected: 测试 PASS；工作台可用铁块 + 烈焰棒合成锤子；锤子耐久条显示 128；`/give @s inf:hammer` 正常。

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "feat: add forging hammer and crafting recipes"
```

---

### Task 6: 锻刀台方块、方块实体、菜单与 GUI 壳

**Files:**
- Create: `src/main/java/com/inf/block/ForgeBlock.java`
- Create: `src/main/java/com/inf/block/ForgeBlockEntity.java`
- Create: `src/main/java/com/inf/block/ForgeData.java`
- Create: `src/main/java/com/inf/menu/ForgeMenu.java`
- Create: `src/main/java/com/inf/menu/ForgeResultSlot.java`
- Create: `src/main/java/com/inf/registry/ModBlocks.java`
- Create: `src/main/java/com/inf/registry/ModBlockEntities.java`
- Create: `src/main/java/com/inf/registry/ModMenus.java`
- Create: `src/main/java/com/inf/client/ForgeScreen.java`
- Create: `src/main/java/com/inf/client/ClientSetup.java`
- Create: `src/main/resources/assets/inf/blockstates/forge_table.json`
- Create: `src/main/resources/assets/inf/models/block/forge_table.json`
- Create: `src/main/resources/assets/inf/models/item/forge_table.json`
- Create: `src/main/resources/assets/inf/textures/gui/forge_table.png`（256×256 GUI 图集，按设计 §6.1 绘制）
- Create: `src/main/resources/data/inf/loot_table/blocks/forge_table.json`
- Create: `src/main/resources/data/inf/recipe/forge_table.json`
- Create: `src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json`
- Create: `src/main/resources/data/minecraft/tags/block/needs_iron_tool.json`
- Modify: `src/main/java/com/inf/Inf.java`、`ModItems`、`ModCreativeTabs`、两个 lang 文件

**Interfaces:**
- Consumes: `BladeData`, `ModItems`, `InfConfig`
- Produces:
  - `ForgeBlockEntity`：槽位常量 `SLOT_BLADE=0, SLOT_HAMMER=1, SLOT_OUTPUT=2`；`inventory() -> ItemStackHandler`；`lastStrikeGameTime()` / `markStrike(long)`；`frenzyStrikesLeft()` / `setFrenzyStrikesLeft(int)` / `consumeFrenzyStrike()`；`dropContents(ServerLevel)`
  - `ForgeData implements ContainerData`：构造 `ForgeData(ForgeBlockEntity be, Player player, ForgeBalance balance)`；索引 `VALUE=0, STRIKES=1, RISK_PERMILLE=2, TIER=3, FRENZY_READY=4, GAIN_MIN=5, GAIN_MAX=6, SIZE=7`
  - `ForgeMenu`：`(int id, Inventory inv, ForgeBlockEntity be, ContainerData data)` 与 `(int id, Inventory inv, FriendlyByteBuf buf)` 两个构造；按钮 id `0=锻打, 1=停手, 2=Frenzy`；读取方法 `value()/strikes()/riskPermille()/tier()/frenzyReady()/gainMin()/gainMax()`
  - `ModBlocks.FORGE_TABLE`、`ModBlockEntities.FORGE_TABLE`、`ModMenus.FORGE_TABLE`

- [ ] **Step 1: 先写可编译的空壳与资源**

方块属性：`strength(3.5F).requiresCorrectToolForDrops()`，`BaseEntityBlock`，`getRenderShape → MODEL`，`useWithoutItem` 打开菜单。方块实体实现 `MenuProvider`，内部 `ItemStackHandler(3)`（重写 `onContentsChanged` 调 `setChanged()`），`saveAdditional/loadAdditional` 序列化槽位、`lastStrikeGameTime`、`frenzyStrikesLeft`。`ForgeBlock.onRemove` 在服务端调用 `be.dropContents(serverLevel)`（逐个 `Block.popResource` 后清空），确保破坏/替换方块不吞物品。`ForgeData.get` 最终按 Task 2 的公式计算；本任务先返回 0，Task 7 实现。

`ForgeMenu` 三个槽位坐标：刀胚 `(16,26)`、锤子 `(16,52)`、产出 `(16,78)`；刀胚槽 `mayPlace` 仅接受 `BladeBlankItem`（锻打后仍是该物品，只是多了 `BLADE_DATA`），锤子槽仅接受 `HammerItem`，产出槽 `mayPlace=false`。再追加玩家背包 3×9（`x=8+列×18`，`y=140+行×18`）与快捷栏 9 格（`y=198`），否则玩家无法把物品放进锻刀台。`stillValid` 用玩家与方块距离平方 ≤ 64。`ForgeScreen` 严格按设计文档 §6.1 实现 176×222 面板：标题栏、三个 18×18 内凹槽、价值条 `(44,50,116,6)`、风险条 `(44,72,116,4)`、三个按钮 `y=118`、空槽提示 `y=108`；空状态显示幽灵图标与提示，按钮禁用。GUI 图集 `forge_table.png` 的图集坐标在 `ForgeScreen` 里用常量命名（`PANEL`、`SLOT`、`BUTTON` 等），不要散落魔法数字。

`forge_table.png` 的固定分区（256×256）：面板 `(0,0,176,222)`（含玩家背包槽底）；槽位三态 `(176,0)/(196,0)/(216,0)` 各 18×18；图标 8×8 放在 `(176,20)/(188,20)/(200,20)`（铁砧/锤/火焰）；按钮三态各占三行：锻打 `x=176,y=32/54/76` 72×20、停手 `x=176,y=98/120/142` 36×20、狂热 `x=176,y=164/186/208` 28×20；价值条框 `(0,230,116,6)`、风险条框 `(0,242,116,4)`。条形填充与风险三档颜色由 `GuiGraphics.fill` 用设计 §6.1 的语义色绘制，不占图集。

资源：方块模型用 `minecraft:block/anvil` 贴图占位（`{"parent":"minecraft:block/cube_all","textures":{"all":"minecraft:block/anvil"}}`），方块状态与物品模型指向它；战利品表掉落自身；两个标签文件把 `inf:forge_table` 加入 `minecraft:mineable/pickaxe` 与 `minecraft:needs_iron_tool`；`forge_table.json` 配方为 `["III", " A ", "III"]`（`I = iron_ingot`，`A = minecraft:anvil`）。

- [ ] **Step 2: 构建与客户端验证空壳**

Run: `./gradlew build && ./gradlew runClient`
Expected: `BUILD SUCCESSFUL`；`/give @s inf:forge_table` 后放置，右键打开 GUI；面板、槽位、价值/风险条、三个按钮与玩家背包的位置与设计 §6.1 一致；空状态显示“放入刀胚与锤子”，锻打/停手/狂热均禁用。

- [ ] **Step 3: 验证错误物品被拒绝与破坏掉落**

手动步骤（创造模式）：
1. 往刀胚槽放泥土 → 放不进去，物品回到光标。
2. 往锤子槽放成品剑 → 放不进去。
3. 刀胚槽放刀胚、锤子槽放锤子（产出槽玩家放不进去），`/setblock` 或创造模式破坏方块 → 两个物品与方块自身都以掉落物形式出现。

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "feat: add forge table block, entity, menu and screen shell"
```

---

### Task 7: 锻打动作（价值、风险、耐久、碎裂）

**Files:**
- Create: `src/main/java/com/inf/forge/ForgeService.java`
- Create: `src/main/java/com/inf/forge/ForgeTiming.java`
- Modify: `src/main/java/com/inf/block/ForgeBlockEntity.java`
- Modify: `src/main/java/com/inf/menu/ForgeMenu.java`
- Modify: `src/main/java/com/inf/block/ForgeData.java`
- Modify: `src/main/java/com/inf/client/ForgeScreen.java`
- Test: `src/test/java/com/inf/forge/ForgeTimingTest.java`

**Interfaces:**
- Consumes: Task 2、4、5、6
- Produces:
  - `ForgeService.strike(ForgeBlockEntity be, ServerPlayer player) -> boolean`
  - `ForgeService.stop(ForgeBlockEntity be, ServerPlayer player) -> boolean`
  - `ForgeService.activateFrenzy(ForgeBlockEntity be, ServerPlayer player) -> boolean`
  - `ForgeTiming.canStrike(long gameTime, long lastStrikeGameTime, int cooldownTicks) -> boolean`

- [ ] **Step 1: 写冷却纯函数测试**

```java
@Test void cooldownBlocksUntilElapsed() {
    assertFalse(ForgeTiming.canStrike(100, 100, 4));
    assertFalse(ForgeTiming.canStrike(103, 100, 4));
    assertTrue(ForgeTiming.canStrike(104, 100, 4));
}
@Test void firstStrikeAlwaysAllowed() {
    assertTrue(ForgeTiming.canStrike(5, Long.MIN_VALUE, 4));
}
```

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew test --tests 'com.inf.forge.ForgeTimingTest'`
Expected: 编译失败。

- [ ] **Step 3: 实现锻打**

先给 `ForgeService.stop` 与 `ForgeService.activateFrenzy` 写返回 `false` 的占位（分别在 Task 8 / Task 11 实现），让菜单能编译。`ForgeService.strike` 顺序固定：

1. `ItemStack blade = be.inventory().getStackInSlot(SLOT_BLADE)`，`ItemStack hammer = ...SLOT_HAMMER`；校验刀胚有 `BladeData`（无则用 `BladeBlankItem.kind()` + `player.getRandom().nextLong()` 创建）且 `HammerItem.canStrike(hammer)` 为真。
2. `ForgeTiming.canStrike(be.getLevel().getGameTime(), be.lastStrikeGameTime(), InfConfig.strikeCooldownTicks())`，失败返回 false。
3. `double breakRoll = player.getRandom().nextDouble()`，`int gainRoll = player.getRandom().nextInt(100)`，`boolean frenzy = be.frenzyStrikesLeft() > 0`。
4. `StrikeOutcome out = ForgeMath.strike(data.value(), data.strikes(), breakRoll, gainRoll, frenzy, InfConfig.forgeBalance())`。
5. 破碎：`inventory.setStackInSlot(SLOT_BLADE, ItemStack.EMPTY)`，播放 `SoundEvents.GLASS_BREAK`，`ServerLevel.sendParticles(ParticleTypes.CRIT, ...)`。
   未破碎：`blade.set(ModDataComponents.BLADE_DATA.get(), new BladeData(data.kind(), out.value(), out.strikes(), data.seed()))`。
6. **在结算之后**直接修改槽内锤子的伤害值：`hammer.setDamageValue(hammer.getDamageValue() + 1)`；若 `getDamageValue() >= getMaxDamage()` 则清空锤子槽并播放 `SoundEvents.ITEM_BREAK`。**不要用 `hurtAndBreak(..., MAINHAND)`**——锤子在容器槽里，不在玩家主手。
7. Frenzy 生效时 `be.consumeFrenzyStrike()`；`be.markStrike(gameTime)`；`be.setChanged()`；返回 true。

`ForgeMenu.clickMenuButton(Player player, int id)`（1.21.1 的父类签名）只做分发：`0 → strike`、`1 → stop`、`2 → activateFrenzy`，把 `player` 转为 `ServerPlayer` 后调用；其余 id 直接返回 false。客户端 `ForgeScreen` 三个按钮调用 `minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id)`。

`ForgeData.get` 按 Task 2 公式计算 `VALUE/STRIKES/RISK_PERMILLE/TIER/GAIN_MIN/GAIN_MAX`；`FRENZY_READY` 读玩家附件（Task 11 前先返回 0）。`ForgeMenu` 构造时传入 `new ForgeData(be, player, InfConfig.forgeBalance())`。

`ForgeScreen` 显示：价值与阶位名、风险千分比转百分比（一位小数）、已锻打次数、下一锤增益范围、Frenzy 状态；每 20 tick 或 `containerTick` 刷新。

- [ ] **Step 4: 测试与手动验证**

Run: `./gradlew test --tests 'com.inf.forge.ForgeTimingTest' && ./gradlew build && ./gradlew runClient`

手动：
1. 放入刀胚与锤子，连点“锻打” 20 次 → 价值与次数增长，风险百分比增长，锤子耐久相应下降。
2. 快速连点（间隔 < 4 tick）→ 数值不会跳变，耐久不会多扣。
3. 把 `config/inf-common.toml` 的 `maxRisk` 临时改为 `1.0` → 下一次锻打刀胚碎裂，锤子仍扣 1 耐久，GUI 中刀胚槽清空。
4. 用 `/give @s inf:hammer[minecraft:damage=127]` 给一把只剩 1 点耐久的锤子 → 成功锻打一次后锤子消失，本次价值已增加。

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "feat: implement forge strike with risk, breakage and hammer wear"
```

---

### Task 8: 停手、成品生成、阶位属性

**Files:**
- Create: `src/main/java/com/inf/forge/WeaponFactory.java`
- Create: `src/main/java/com/inf/forge/WeaponAttributeApplier.java`
- Create: `src/main/java/com/inf/forge/WeaponSettlement.java`
- Modify: `src/main/java/com/inf/forge/ForgeService.java`
- Modify: `src/main/java/com/inf/client/ForgeScreen.java`
- Test: `src/test/java/com/inf/forge/WeaponSettlementTest.java`

**Interfaces:**
- Consumes: `WeaponStatsTable`, `BladeData`, `ModItems`
- Produces:
  - `WeaponSettlement.canStop(int bladeValue, boolean outputEmpty) -> boolean`
  - `WeaponFactory.create(WeaponKind kind, int value, int strikes, long seed) -> ItemStack`
  - `WeaponAttributeApplier.apply(ItemStack stack, WeaponKind kind, WeaponTier tier)`

- [ ] **Step 1: 写停手前置条件测试**

```java
@Test void cannotStopWhenOutputOccupied() {
    assertFalse(WeaponSettlement.canStop(500, false));
}
@Test void cannotStopWithZeroValue() {
    assertFalse(WeaponSettlement.canStop(0, true));
}
@Test void canStopWithValueAndEmptyOutput() {
    assertTrue(WeaponSettlement.canStop(1, true));
}
```

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew test --tests 'com.inf.forge.WeaponSettlementTest'`
Expected: 编译失败。

- [ ] **Step 3: 实现停手与成品**

`ForgeService.stop`：
1. 读刀胚数据；`WeaponSettlement.canStop(value, outputSlot.isEmpty())` 为假则返回 false。
2. `ItemStack weapon = WeaponFactory.create(kind, value, strikes, seed)`。
3. 产出槽放入 weapon，刀胚槽清空，播放 `SoundEvents.ANVIL_USE`，`setChanged`。
4. 同类完成计数 +1（Task 11 接入；本任务留一个 `onWeaponCompleted(player, kind)` 调用点，方法体先空）。

`WeaponFactory.create`：
- 阶位 = `WeaponTier.forValue(value)`。
- `new ItemStack(ModItems.forged(kind))`；写入 `BLADE_DATA`。
- 名字：`stack.set(DataComponents.ITEM_NAME, Component.translatable("inf.weapon.name", Component.translatable(tier.translationKey()), Component.translatable(kind.translationKey())))`。
- 稀有度：`0,1 → COMMON`，`2 → UNCOMMON`，`3 → RARE`，`4,5 → EPIC`。
- 调 `WeaponAttributeApplier.apply`。

`WeaponAttributeApplier.apply`：取 `WeaponStatsTable.forTier(kind, tier)`，用 `ItemAttributeModifiers.builder()` 添加：
- 所有武器：`ATTACK_DAMAGE`（`ADD_VALUE`，值 = `stats.attackDamage()`）、`ATTACK_SPEED`（`ADD_VALUE`，值 = `stats.attackSpeedModifier()`）。
- `sword/scythe`：`SWEEP_DAMAGE_RATIO` = `stats.sweepRatio()`；`scythe` 再加 `ENTITY_INTERACTION_RANGE` = `stats.reachModifier()`。
- `dagger`：`ENTITY_INTERACTION_RANGE` = `stats.reachModifier()`，`SWEEP_DAMAGE_RATIO` = 0。
- 修饰符 id：`ResourceLocation.fromNamespaceAndPath("inf", "forged/" + kind.id + "/damage")` 等，同一物品内各属性 id 唯一。
- 槽位组 `EquipmentSlotGroup.MAINHAND`；最后 `stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build())`。

- [ ] **Step 4: 测试与手动验证**

Run: `./gradlew test --tests 'com.inf.forge.WeaponSettlementTest' && ./gradlew build && ./gradlew runClient`

手动：
1. 锻到 100+ 价值后停手 → 产出“寻常 · 长剑”等对应名字、稀有度颜色、Tooltip 可读。
2. 先停手一次让产出槽有成品，再放入新刀胚点停手 → 不结算，刀胚与已产出成品都保留。
3. `F3+H` 打开高级 Tooltip 或攻击生物，确认攻击力/攻速与属性表一致。
4. `/give @s inf:forged_sword[inf:blade_data={kind:"sword",value:1000,strikes:47,seed:1}]` → 名字为“传说 · 长剑”。

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "feat: settle forged weapons with tier names and attributes"
```

---

### Task 9: 阶位外观与 Tooltip

**Files:**
- Create: `src/main/java/com/inf/client/TierColors.java`
- Create: `src/main/java/com/inf/client/TooltipEvents.java`
- Modify: `src/main/java/com/inf/client/ClientSetup.java`
- Modify: `src/main/java/com/inf/item/forged/*`（如需统一 tooltip 钩子）

**Interfaces:**
- Consumes: `BladeData`, `WeaponTier`
- Produces: `TierColors.of(WeaponTier) -> int`（ARGB）

- [ ] **Step 1: 注册颜色**

- 物品模型沿用 Task 4 的原版贴图占位（父模型已带 `tintindex 0`），无需新增模型文件。
- `ClientSetup` 监听 `RegisterColorHandlersEvent.Item`，注册全部 5 个成品物品的 `ItemColor`：读 `BLADE_DATA`，返回 `TierColors.of(WeaponTier.forValue(value))`。
- `TierColors`：`CRUDE=0xFF8A8A8A`、`COMMON=0xFFD8D8D8`、`FINE=0xFF7FB4E8`、`RARE=0xFFB07CFF`、`EPIC=0xFFFFB33E`、`LEGENDARY=0xFFFF4D5E`。
- 未带 `BLADE_DATA` 时返回 `-1`（不染色）。

- [ ] **Step 2: Tooltip**

`TooltipEvents` 监听 `ItemTooltipEvent`，对带 `BLADE_DATA` 的成品追加两行：`inf.tooltip.value`（价值 + 锻打次数）与 `inf.tooltip.tier`（阶位）。语言文件补 key。

- [ ] **Step 3: 客户端验证**

Run: `./gradlew runClient`

依次 `/give` 六种价值（0/100/240/450/720/1000）的同一把剑：
```
/give @s inf:forged_sword[inf:blade_data={kind:"sword",value:1000,strikes:47,seed:1}]
```
Expected: 六把剑名字与颜色依次为粗制灰、寻常白、精良蓝、稀有紫、史诗金、传说红；Tooltip 显示价值与锻打次数。

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "feat: tint forged weapons by tier and add tooltips"
```

---

### Task 10: 匕首无击退、镰刀范围、镐/斧采集

**Files:**
- Create: `src/main/java/com/inf/combat/CombatEvents.java`
- Create: `src/main/java/com/inf/weapon/HarvestRules.java`
- Modify: `src/main/java/com/inf/item/forged/ForgedPickaxeItem.java`
- Modify: `src/main/java/com/inf/item/forged/ForgedAxeItem.java`
- Modify: `src/main/java/com/inf/item/forged/ForgedDaggerItem.java`
- Test: `src/test/java/com/inf/weapon/HarvestRulesTest.java`

**Interfaces:**
- Consumes: `WeaponStats`, `BladeData`
- Produces:
  - `HarvestRules.requiredLevel(boolean needsStone, boolean needsIron, boolean needsDiamond) -> int`
  - `HarvestRules.canHarvest(int harvestLevel, boolean requiresCorrectTool, int requiredLevel) -> boolean`
  - `CombatEvents.onKnockback(LivingKnockBackEvent)`

- [ ] **Step 1: 写采集规则纯测试**

```java
@Test void requiredLevel_mapsTags() {
    assertEquals(0, HarvestRules.requiredLevel(false, false, false));
    assertEquals(1, HarvestRules.requiredLevel(true, false, false));
    assertEquals(2, HarvestRules.requiredLevel(true, true, false));
    assertEquals(4, HarvestRules.requiredLevel(true, true, true));
}
@Test void canHarvest_respectsLevelAndToolRequirement() {
    assertTrue(HarvestRules.canHarvest(0, false, 0));
    assertFalse(HarvestRules.canHarvest(1, true, 2));
    assertTrue(HarvestRules.canHarvest(2, true, 2));
}
```

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew test --tests 'com.inf.weapon.HarvestRulesTest'`
Expected: 编译失败。

- [ ] **Step 3: 实现行为**

- `HarvestRules`：纯函数，规则如上；`Pickaxe/Axe` 的 `isCorrectToolForDrops` 调用 `state.is(BlockTags.NEEDS_STONE_TOOL/IRON/DIAMOND)` 后交给 `requiredLevel`，再与 `BladeData` 的 `WeaponTier.ordinal()` 映射出的 `harvestLevel` 比较（`WeaponStatsTable.forTier(...).harvestLevel()`）。
- `getDestroySpeed` 返回 `WeaponStatsTable.forTier(kind, tier).miningSpeed()`；无 `BLADE_DATA` 时返回 1.0。
- `CombatEvents`：`@EventBusSubscriber(modid = Inf.MODID)`，`onKnockback(LivingKnockBackEvent)` 中若被击退实体的 `getLastHurtByMob()` 是玩家且主手是 `ForgedDaggerItem`，`event.setStrength(0.0F)`。
- 匕首的 `sweep_damage_ratio` 已在 Task 8 设为 0；镰刀的 `entity_interaction_range` 已由属性组件生效。

- [ ] **Step 4: 测试与手动验证**

Run: `./gradlew test --tests 'com.inf.weapon.HarvestRulesTest' && ./gradlew build && ./gradlew runClient`

手动：
1. 传说匕首攻击牛 → 牛不后退，攻速明显快于剑。
2. 传说镰刀攻击 4 格外的羊 → 能打到；普通剑打不到。
3. 粗制镐挖铁矿掉矿，传说镐挖黑曜石掉黑曜石，粗制镐挖黑曜石不掉落。

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "feat: add dagger knockback, scythe reach and mining tiers"
```

---

### Task 11: Frenzy 充能与免碎窗口

**Files:**
- Create: `src/main/java/com/inf/forge/FrenzyState.java`
- Create: `src/main/java/com/inf/registry/ModAttachments.java`
- Modify: `src/main/java/com/inf/Inf.java`
- Modify: `src/main/java/com/inf/forge/ForgeService.java`
- Modify: `src/main/java/com/inf/block/ForgeData.java`
- Modify: `src/main/java/com/inf/client/ForgeScreen.java`
- Test: `src/test/java/com/inf/forge/FrenzyStateTest.java`

**Interfaces:**
- Consumes: `WeaponKind`
- Produces:
  - `record FrenzyState(Map<WeaponKind,Integer> progress, boolean ready)`，`EMPTY`、`onWeaponCompleted(WeaponKind, int required)`、`activated()`、`progress(WeaponKind)`
  - `ModAttachments.FRENZY : Supplier<AttachmentType<FrenzyState>>`
  - `ForgeService.onWeaponCompleted(ServerPlayer, WeaponKind)`（替换 Task 8 的空方法体）

- [ ] **Step 1: 写状态机测试**

```java
@Test void chargesAfterRequiredCompletions() {
    FrenzyState s = FrenzyState.EMPTY;
    for (int i = 0; i < 9; i++) s = s.onWeaponCompleted(WeaponKind.SWORD, 10);
    assertFalse(s.ready());
    s = s.onWeaponCompleted(WeaponKind.SWORD, 10);
    assertTrue(s.ready());
}
@Test void differentKindsDoNotShareProgress() {
    FrenzyState s = FrenzyState.EMPTY;
    for (int i = 0; i < 9; i++) s = s.onWeaponCompleted(WeaponKind.SWORD, 10);
    s = s.onWeaponCompleted(WeaponKind.AXE, 10);
    assertFalse(s.ready());
}
@Test void activationClearsProgressAndReady() {
    FrenzyState s = FrenzyState.EMPTY.onWeaponCompleted(WeaponKind.SWORD, 1);
    FrenzyState used = s.activated();
    assertFalse(used.ready());
    assertEquals(0, used.progress(WeaponKind.SWORD));
}
```

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew test --tests 'com.inf.forge.FrenzyStateTest'`
Expected: 编译失败。

- [ ] **Step 3: 实现**

- `FrenzyState`：不可变；`onWeaponCompleted` 在未 ready 时把对应 kind 计数 +1，达到 `required` 时打包新 map 并 `ready = true`。`CODEC` 用 `RecordCodecBuilder`：`progress` 为 `Codec.unboundedMap(Codec.STRING.xmap(WeaponKind::byId, WeaponKind::id), Codec.INT)`，`ready` 为 `Codec.BOOL`。
- `ModAttachments`：`DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Inf.MODID)`，注册 `frenzy`，serialize 用 `FrenzyState.CODEC`，`copyOnDeath()`；`Inf` 注册。
- `ForgeService.onWeaponCompleted`：`player.setData(ModAttachments.FRENZY, player.getData(...).onWeaponCompleted(kind, InfConfig.frenzyRequired()))`。
- `activateFrenzy`：要求 ready、刀胚存在且 `value > 0`；`be.setFrenzyStrikesLeft(InfConfig.frenzyFreeStrikes())`，`player.setData(..., state.activated())`，返回 true。
- `ForgeData.FRENZY_READY` 返回 `player.getData(...).ready() ? 1 : 0`；GUI 显示 `x/required` 与窗口剩余锤数，Frenzy 按钮在不可用时禁用。

- [ ] **Step 4: 测试与手动验证**

Run: `./gradlew test --tests 'com.inf.forge.FrenzyStateTest' && ./gradlew build && ./gradlew runClient`

手动（临时把 `frenzyRequired` 配成 1）：
1. 完成 1 把剑 → GUI 显示 Frenzy 已充能。
2. 放新刀胚，点 Frenzy → 后续 3 锤即使 `breakRoll` 极低也不碎（把 `maxRisk=1.0` 验证更直观），价值仍增长。
3. 3 锤后恢复普通判定，附件回到 0/1。

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "feat: add frenzy charge and unbreakable window"
```

---

### Task 12: 音效、粒子、配置文档与 README

**Files:**
- Modify: `src/main/java/com/inf/forge/ForgeService.java`
- Create: `docs/configuration.md`
- Modify: `README.md`
- Modify: `src/main/resources/assets/inf/lang/zh_cn.json`
- Modify: `src/main/resources/assets/inf/lang/en_us.json`

**Interfaces:**
- Consumes: 全部前序任务
- Produces: 完整的中英文文本与可复制的配置说明

- [ ] **Step 1: 补齐表现**

- 锻打成功：`SoundEvents.ANVIL_USE`（音量 0.8，音高 0.9–1.1 随机）+ 8 个 `ParticleTypes.CRIT`。
- 碎裂：`SoundEvents.GLASS_BREAK` + 12 个 `ParticleTypes.CRIT`，额外 `SoundEvents.ITEM_BREAK`。
- 停手：`SoundEvents.ANVIL_LAND`（音量 0.6）。
- Frenzy 激活：`SoundEvents.BLAZE_SHOOT`（音量 0.5）。
- GUI 三态与动效（按设计 §6.1）：按钮 hover/pressed/disabled；价值条 150ms ease-out 追平；风险条三档配色且百分比常显；风险 >50% 时 1.2s 脉动；Frenzy 充能发光/激活呼吸；碎裂 200ms 红闪；空状态幽灵图标与提示。`InfConfig.uiAnimations=false` 时关闭全部动效，只保留静态状态色。
- 所有 GUI 图集坐标用常量命名，禁止在 `ForgeScreen` 里散落魔法数字。

- [ ] **Step 2: 文本与文档**

- `zh_cn.json` / `en_us.json` 覆盖所有 key：物品名、方块名、阶位名、类型名、Tooltip、GUI 按钮与状态。
- `README.md`：模组定位、安装、玩法循环、配方、配置项表、已知限制（无龙贡/遗物）。
- `docs/configuration.md`：逐项列出 `config/inf-common.toml` 的 key、默认值、取值范围与影响。

- [ ] **Step 3: 验证**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`；用 `python3 -c "import json;json.load(open('src/main/resources/assets/inf/lang/zh_cn.json'))"` 与 en_us 同样命令确认 JSON 合法。

- [ ] **Step 3.5: 视觉验收（GUI scale 1/2/3，中英文）**

Run: `./gradlew runClient`

逐项检查并记录在 `docs/release-checklist.md`：
1. GUI scale 1/2/3 下无半像素线、无模糊、无文字截断。
2. 中英文切换后按钮文字（`锻打/停手/狂热` 与 `FORGE/BANK/FRENZY`）都不溢出。
3. 空状态、正常、禁用、碎裂、Frenzy 充能、Frenzy 激活六种状态都有清晰区别，且不只靠颜色。
4. 文字与背景对比度 ≥4.5:1（用取色器抽查 `text`、`text.dim`、`risk.*`）。
5. `uiAnimations=false` 时信息完整、状态仍可辨。

- [ ] **Step 4: 提交**

```bash
git add -A
git commit -m "docs: document configuration and complete localization"
```

---

### Task 13: 平衡模拟与调参报告

**Files:**
- Create: `src/main/java/com/inf/tools/BalanceSimulator.java`
- Create: `src/main/java/com/inf/tools/BalanceSim.java`
- Test: `src/test/java/com/inf/tools/BalanceSimulationTest.java`
- Create: `docs/balance-report.md`
- Modify: `build.gradle`（`balanceSim` JavaExec 任务）

**Interfaces:**
- Consumes: `ForgeMath`, `WeaponTier`, `ForgeBalance`
- Produces:
  - `record BalanceReport(double crudeOrBetter, double commonOrBetter, double fineOrBetter, double rareOrBetter, double epicOrBetter, double legendary, double averageStrikes)`
  - `BalanceSimulator.run(int trials, long seed, ForgeBalance b) -> BalanceReport`
  - `BalanceSim.main(String[])` 打印表格

- [ ] **Step 1: 写模拟测试**

```java
@Test void defaultBalance_landsInDocumentedBand() {
    BalanceReport r = BalanceSimulator.run(100_000, 42L, ForgeBalance.DEFAULT);
    assertTrue(r.fineOrBetter() > 0.20, "fine rate " + r.fineOrBetter());
    assertTrue(r.legendary() > 0.005 && r.legendary() < 0.20, "legendary " + r.legendary());
    assertTrue(r.averageStrikes() > 20 && r.averageStrikes() < 45, "strikes " + r.averageStrikes());
}
@Test void higherRiskLowersLegendaryRate() {
    ForgeBalance harsh = ForgeBalance.of(0.015, 0.0024, 0.00003, 0.85, 12, 1, 8, 4);
    assertTrue(BalanceSimulator.run(100_000, 42L, harsh).legendary()
        < BalanceSimulator.run(100_000, 42L, ForgeBalance.DEFAULT).legendary());
}
```

`BalanceSimulator.run`：每把刀从 `value=0, strikes=0` 开始，循环到 `value >= 1000` 或碎裂；用 `java.util.Random(seed)` 每锤生成 `breakRoll` 与 `gainRoll`；统计每档最终达到率与该轮锻打数。

- [ ] **Step 2: 运行确认失败**

Run: `./gradlew test --tests 'com.inf.tools.BalanceSimulationTest'`
Expected: 编译失败。

- [ ] **Step 3: 实现模拟器与 Gradle 任务**

- `BalanceSimulator` 只依赖纯类。
- `build.gradle` 增加：
```groovy
tasks.register('balanceSim', JavaExec) {
    dependsOn tasks.named('classes')
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.inf.tools.BalanceSim'
}
```
- 运行 `./gradlew -q balanceSim` 生成表格；把输出整理进 `docs/balance-report.md`（含默认参数、六档到达率、平均锤数、结论）。

- [ ] **Step 4: 运行并确认**

Run: `./gradlew test --tests 'com.inf.tools.BalanceSimulationTest' && ./gradlew -q balanceSim`
Expected: 测试 PASS；控制台输出六档百分比与平均锤数，数值与设计文档“传说约 4–6%”不冲突。若冲突，只调整 `ForgeBalance.DEFAULT` 与 `InfConfig` 默认值，并同步设计文档，不改公式。

- [ ] **Step 5: 提交**

```bash
git add -A
git commit -m "feat: add balance simulator and tuning report"
```

---

### Task 14: 发布前验证

**Files:**
- Modify: `README.md`（版本、已知问题）
- Create: `docs/release-checklist.md`

**Interfaces:**
- Consumes: 全部
- Produces: 可发布 jar

- [ ] **Step 1: 全量测试与构建**

Run: `./gradlew clean test build`
Expected: `BUILD SUCCESSFUL`；所有 JUnit 测试 PASS；`build/libs/inf-0.1.0.jar` 生成。

- [ ] **Step 2: 服务端启动验证**

Run: `./gradlew runServer`
Expected: 服务器启动到 `Done`，无注册/数据组件错误；执行 `stop` 正常退出。

- [ ] **Step 3: 按清单走完端到端流程**

`docs/release-checklist.md` 记录并逐项勾选：
1. 合成锤子、五种刀胚、锻刀台。
2. 放入刀胚与锤子，连锤 10 次，价值/风险/耐久/增益范围同步正确。
3. 停手得到成品，名字、颜色、Tooltip、属性正确。
4. 锤碎/刀碎/产出槽占用三种边界行为正确。
5. Frenzy 充能、激活、3 锤免碎、状态清空正确。
6. 匕首无击退、镰刀远距、镐挖掘等级、斧去皮正确。
7. 破坏锻刀台掉落全部内容物。
8. 双人联机：一个客户端操作、另一个观察，数值与服务端一致，无不同步。
9. 视觉验收：GUI scale 1/2/3 + 中英文下无截断、无模糊；空/正常/禁用/碎裂/狂热六种状态清晰可辨；`uiAnimations=false` 时信息完整。

- [ ] **Step 4: 打标签**

```bash
git tag -a v0.1.0 -m "Inf 0.1.0 - forging core"
```

- [ ] **Step 5: 推送前确认远程**

Run: `git remote -v`
Expected: `origin git@github.com:not-exist/Inf.git`。该仓库尚不存在；先由仓库所有者创建 `not-exist/Inf` 或替换为真实地址，再执行 `git push -u origin main --tags`。
