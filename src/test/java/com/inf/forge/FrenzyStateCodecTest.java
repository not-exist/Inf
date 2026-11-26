package com.inf.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inf.registry.FrenzyStateCodec;
import com.inf.weapon.WeaponKind;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

class FrenzyStateCodecTest {
    @Test
    void roundTripsEveryKindAndReadyFlag() {
        FrenzyState state = FrenzyState.EMPTY.onWeaponCompleted(WeaponKind.SCYTHE, 4);
        FrenzyState ready = FrenzyState.EMPTY.onWeaponCompleted(WeaponKind.DAGGER, 1);
        for (FrenzyState original : new FrenzyState[] {state, ready, FrenzyState.EMPTY}) {
            var encoded = FrenzyStateCodec.CODEC.encodeStart(JsonOps.INSTANCE, original);
            assertTrue(encoded.result().isPresent(), encoded.toString());
            FrenzyState decoded = FrenzyStateCodec.CODEC
                    .parse(JsonOps.INSTANCE, encoded.result().orElseThrow())
                    .result()
                    .orElseThrow();
            assertEquals(original, decoded);
        }
    }

    @Test
    void missingOptionalFields_defaultToEmpty() {
        FrenzyState decoded = FrenzyStateCodec.CODEC
                .parse(JsonOps.INSTANCE, com.google.gson.JsonParser.parseString("{}"))
                .result()
                .orElseThrow();
        assertEquals(FrenzyState.EMPTY, decoded);
        assertFalse(decoded.ready());
    }

    @Test
    void unknownKind_isRejected() {
        var input = com.google.gson.JsonParser.parseString("{\"progress\":{\"warpaxe\":3}}");
        try {
            assertTrue(FrenzyStateCodec.CODEC.parse(JsonOps.INSTANCE, input).error().isPresent(),
                    "unknown kind should not decode");
        } catch (RuntimeException expected) {
            // DFU may surface the mapping failure as an exception instead of an error result.
        }
    }
}
