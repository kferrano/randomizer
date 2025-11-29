package com.hardrock.randomizer.data;

import net.minecraft.resources.ResourceLocation;

public record ItemEntry(
        ResourceLocation id,
        int count,
        int weight
) {}
