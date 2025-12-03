package com.hardrock.randomizer.network;

import com.hardrock.randomizer.Randomizer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Synchronisiert den Timer-Wert vom Server zu allen Clients.
 */
public record RandomizerTimerPayload(long elapsedTicks, boolean running) implements CustomPacketPayload {

    public static final Type<RandomizerTimerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Randomizer.MOD_ID, "timer_sync"));

    public static final StreamCodec<ByteBuf, RandomizerTimerPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG,
                    RandomizerTimerPayload::elapsedTicks,
                    ByteBufCodecs.BOOL,
                    RandomizerTimerPayload::running,
                    RandomizerTimerPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
