package com.inf.registry;

import com.inf.Inf;
import com.inf.forge.FrenzyState;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Inf.MODID);

    public static final Supplier<AttachmentType<FrenzyState>> FRENZY = ATTACHMENTS.register(
            "frenzy",
            () -> AttachmentType.builder(() -> FrenzyState.EMPTY)
                    .serialize(FrenzyStateCodec.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}
