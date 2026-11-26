package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.inf.item.component.BladeData;
import com.inf.weapon.WeaponKind;
import com.inf.weapon.WeaponTier;
import org.junit.jupiter.api.Test;

class ForgeViewModelTest {
    private static final ForgeBalance B = ForgeBalance.DEFAULT;

    @Test
    void emptyBlade_reportsAZeroedView() {
        ForgeViewModel view = ForgeViewModel.of(null, false, 0, 0, 10, B);
        assertEquals(0, view.get(ForgeViewModel.VALUE));
        assertEquals(0, view.get(ForgeViewModel.STRIKES));
        assertEquals(0, view.get(ForgeViewModel.RISK_PERMILLE));
        assertEquals(WeaponTier.CRUDE.ordinal(), view.get(ForgeViewModel.TIER));
        assertEquals(0, view.get(ForgeViewModel.GAIN_MIN));
        assertEquals(0, view.get(ForgeViewModel.GAIN_MAX));
        assertEquals(0, view.get(ForgeViewModel.BREAK_PERMILLE));
        assertEquals(0, view.get(ForgeViewModel.FRENZY_PROGRESS));
        assertEquals(10, view.get(ForgeViewModel.FRENZY_REQUIRED));
    }

    @Test
    void mapsValueStrikesAndTierOrdinal() {
        BladeData blade = new BladeData(WeaponKind.SWORD, 450, 21, 7L);
        ForgeViewModel view = ForgeViewModel.of(blade, false, 3, 0, 10, B);
        assertEquals(450, view.get(ForgeViewModel.VALUE));
        assertEquals(21, view.get(ForgeViewModel.STRIKES));
        assertEquals(WeaponTier.RARE.ordinal(), view.get(ForgeViewModel.TIER));
        assertEquals(3, view.get(ForgeViewModel.FRENZY_PROGRESS));
    }

    @Test
    void riskAndBreakPermille_areTheSameValue() {
        BladeData blade = new BladeData(WeaponKind.AXE, 240, 12, 1L);
        ForgeViewModel view = ForgeViewModel.of(blade, false, 0, 0, 10, B);
        assertEquals(view.get(ForgeViewModel.BREAK_PERMILLE), view.get(ForgeViewModel.RISK_PERMILLE));
        assertEquals(
                ForgeViewModel.permille(ForgeRates.of(240, 12, false, B).breakChance()),
                view.get(ForgeViewModel.BREAK_PERMILLE));
    }

    @Test
    void gainRange_comesFromZeroAndNinetyNineRolls() {
        BladeData blade = new BladeData(WeaponKind.DAGGER, 100, 4, 1L);
        ForgeViewModel view = ForgeViewModel.of(blade, false, 0, 0, 10, B);
        assertEquals(ForgeMath.valueGain(4, 0, B), view.get(ForgeViewModel.GAIN_MIN));
        assertEquals(ForgeMath.valueGain(4, 99, B), view.get(ForgeViewModel.GAIN_MAX));
    }

    @Test
    void frenzyFields_areCopiedThrough() {
        ForgeViewModel view = ForgeViewModel.of(new BladeData(WeaponKind.SWORD, 10, 1, 0L), true, 9, 3, 10, B);
        assertEquals(1, view.get(ForgeViewModel.FRENZY_READY));
        assertEquals(3, view.get(ForgeViewModel.FRENZY_ACTIVE));
        assertEquals(9, view.get(ForgeViewModel.FRENZY_PROGRESS));
        assertEquals(10, view.get(ForgeViewModel.FRENZY_REQUIRED));
    }

    @Test
    void frenzyActive_forcesFrenzyRates() {
        BladeData blade = new BladeData(WeaponKind.SWORD, 500, 20, 0L);
        ForgeViewModel view = ForgeViewModel.of(blade, true, 0, 2, 10, B);
        assertEquals(0, view.get(ForgeViewModel.BREAK_PERMILLE));
        assertEquals(0, view.get(ForgeViewModel.DAMAGE_PERMILLE));
        assertEquals(750, view.get(ForgeViewModel.SUCCESS_PERMILLE));
        assertEquals(250, view.get(ForgeViewModel.GREAT_PERMILLE));
    }

    @Test
    void permille_roundsHalfUp() {
        assertEquals(1, ForgeViewModel.permille(0.0005));
        assertEquals(0, ForgeViewModel.permille(0.0004));
        assertEquals(1000, ForgeViewModel.permille(1.0));
    }

    @Test
    void outOfRangeIndex_returnsZero() {
        ForgeViewModel view = ForgeViewModel.of(null, false, 0, 0, 10, B);
        assertEquals(0, view.get(-1));
        assertEquals(0, view.get(ForgeViewModel.SIZE));
    }
}
