package com.hardrock.randomizer.data;

import net.minecraft.resources.ResourceLocation;

public record MobEntry(
        ResourceLocation entityId,
        int count,
        int radius,
        int weight,
        MobType type
) {
    public enum MobType { HOSTILE, PASSIVE, NEUTRAL }
}