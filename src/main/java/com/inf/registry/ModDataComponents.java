package com.inf.registry;

import com.inf.Inf;
import com.inf.item.component.BladeData;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Inf.MODID);

    public static final Supplier<DataComponentType<BladeData>> BLADE_DATA = DATA_COMPONENTS.registerComponentType(
            "blade_data", builder -> builder.persistent(BladeData.CODEC).networkSynchronized(BladeData.STREAM_CODEC));

    private ModDataComponents() {}
}
