package com.hardrock.randomizer.network;

import com.hardrock.randomizer.Randomizer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RandomizerBossbarPreferencePayload(boolean disableBossbar)
        implements CustomPacketPayload {

    public static final Type<RandomizerBossbarPreferencePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Randomizer.MOD_ID, "bossbar_pref"));

    public static final StreamCodec<ByteBuf, RandomizerBossbarPreferencePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    RandomizerBossbarPreferencePayload::disableBossbar,
                    RandomizerBossbarPreferencePayload::new
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
