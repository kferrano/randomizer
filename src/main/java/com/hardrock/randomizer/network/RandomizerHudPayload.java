package com.hardrock.randomizer.network;

import com.hardrock.randomizer.Randomizer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RandomizerHudPayload(Action action) implements CustomPacketPayload {

    public enum Action {
        START,
        PAUSE,
        STOP
    }

    public static final Type<RandomizerHudPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Randomizer.MOD_ID, "hud_control"));

    public static final StreamCodec<ByteBuf, RandomizerHudPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    payload -> payload.action().ordinal(),
                    RandomizerHudPayload::fromOrdinal
            );

    private static RandomizerHudPayload fromOrdinal(int ordinal) {
        Action[] values = Action.values();
        Action action = (ordinal < 0 || ordinal >= values.length)
                ? Action.STOP
                : values[ordinal];
        return new RandomizerHudPayload(action);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
