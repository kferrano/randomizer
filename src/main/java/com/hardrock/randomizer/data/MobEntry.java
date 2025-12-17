package com.hardrock.randomizer.data;

import net.minecraft.resources.ResourceLocation;
import com.hardrock.randomizer.data.RarityTier;

public record MobEntry(
        ResourceLocation entityId,
        int count,
        int radius,
        int weight,
        MobType type,
        RarityTier tier
) {
    public enum MobType { HOSTILE, PASSIVE, NEUTRAL }
}