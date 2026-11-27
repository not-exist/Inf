package com.inf.registry;

import com.inf.forge.FrenzyState;
import com.inf.weapon.WeaponKind;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;

public final class FrenzyStateCodec {
    public static final Codec<FrenzyState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(Codec.STRING.xmap(WeaponKind::byId, kind -> kind.id), Codec.INT)
                            .optionalFieldOf("progress", Map.of())
                            .forGetter(FrenzyState::progress),
                    Codec.BOOL.optionalFieldOf("ready", false).forGetter(FrenzyState::ready))
            .apply(instance, FrenzyState::new));

    private FrenzyStateCodec() {}
}
