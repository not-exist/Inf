package com.inf.item.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inf.weapon.WeaponKind;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

class BladeDataCodecTest {
    @Test
    void jsonRoundTrips() {
        BladeData original = new BladeData(WeaponKind.SCYTHE, 720, 33, 123456789L);
        var encoded = BladeData.CODEC.encodeStart(JsonOps.INSTANCE, original);
        assertTrue(encoded.result().isPresent(), encoded.toString());
        BladeData decoded =
                BladeData.CODEC.parse(JsonOps.INSTANCE, encoded.result().orElseThrow()).result().orElseThrow();
        assertEquals(original, decoded);
    }

    @Test
    void jsonDefaultsMissingOptionalFields() {
        BladeData decoded = BladeData.CODEC
                .parse(JsonOps.INSTANCE, com.google.gson.JsonParser.parseString("{\"kind\":\"dagger\"}"))
                .result()
                .orElseThrow();
        assertEquals(new BladeData(WeaponKind.DAGGER, 0, 0, 0L), decoded);
    }

    @Test
    void streamRoundTrips() {
        BladeData original = new BladeData(WeaponKind.PICKAXE, 1234, 56, Long.MIN_VALUE);
        ByteBuf buffer = Unpooled.buffer();
        BladeData.STREAM_CODEC.encode(buffer, original);
        BladeData decoded = BladeData.STREAM_CODEC.decode(buffer);
        assertEquals(original, decoded);
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void unknownKind_isRejectedByThePersistentCodec() {
        var json = com.google.gson.JsonParser.parseString("{\"kind\":\"warpaxe\"}");
        try {
            assertTrue(BladeData.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent(),
                    "unknown kind should not decode");
        } catch (RuntimeException expected) {
            // DFU may surface the mapping failure as an exception instead of an error result.
        }
    }

    @Test
    void unknownKind_isRejectedByTheNetworkCodec() {
        ByteBuf buffer = Unpooled.buffer();
        net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.encode(buffer, "warpaxe");
        assertThrows(RuntimeException.class, () -> BladeData.STREAM_CODEC.decode(buffer));
    }
}
