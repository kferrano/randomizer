package com.hardrock.randomizer.data;


import net.minecraft.resources.ResourceLocation;

public record EffectEntry (
        ResourceLocation id,
        int amplifier,
        int durationTicks,
        int weight,
        EffectType type
) {
    public enum EffectType { BUFF, DEBUFF }
}
