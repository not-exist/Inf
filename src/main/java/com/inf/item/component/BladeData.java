package com.inf.item.component;

import com.inf.weapon.WeaponKind;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BladeData(WeaponKind kind, int value, int strikes, long seed) {
    public static final Codec<BladeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING
                            .xmap(WeaponKind::byId, kind -> kind.id)
                            .fieldOf("kind")
                            .forGetter(BladeData::kind),
                    Codec.INT.optionalFieldOf("value", 0).forGetter(BladeData::value),
                    Codec.INT.optionalFieldOf("strikes", 0).forGetter(BladeData::strikes),
                    Codec.LONG.optionalFieldOf("seed", 0L).forGetter(BladeData::seed))
            .apply(instance, BladeData::new));

    public static final StreamCodec<ByteBuf, BladeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(WeaponKind::byId, kind -> kind.id),
            BladeData::kind,
            ByteBufCodecs.VAR_INT,
            BladeData::value,
            ByteBufCodecs.VAR_INT,
            BladeData::strikes,
            ByteBufCodecs.VAR_LONG,
            BladeData::seed,
            BladeData::new);
}
