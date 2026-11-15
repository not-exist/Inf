package com.inf.weapon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HarvestRulesTest {
    @Test
    void requiredLevel_mapsTags() {
        assertEquals(0, HarvestRules.requiredLevel(false, false, false));
        assertEquals(1, HarvestRules.requiredLevel(true, false, false));
        assertEquals(2, HarvestRules.requiredLevel(true, true, false));
        assertEquals(4, HarvestRules.requiredLevel(true, true, true));
    }

    @Test
    void canHarvest_respectsLevelAndToolRequirement() {
        assertTrue(HarvestRules.canHarvest(0, false, 0));
        assertFalse(HarvestRules.canHarvest(1, true, 2));
        assertTrue(HarvestRules.canHarvest(2, true, 2));
    }
}
