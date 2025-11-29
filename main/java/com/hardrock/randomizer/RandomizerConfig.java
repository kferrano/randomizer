package com.hardrock.randomizer;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class RandomizerConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    public static class Common {

        // Delay
        public final ModConfigSpec.IntValue delaySeconds;

        // Event-Typen
        public final ModConfigSpec.BooleanValue enableEffects;
        public final ModConfigSpec.BooleanValue enableMobs;
        public final ModConfigSpec.BooleanValue enableItems;

        public final ModConfigSpec.IntValue weightEffects;
        public final ModConfigSpec.IntValue weightMobs;
        public final ModConfigSpec.IntValue weightItems;

        // --- Balancing / Filter ---

        // Nur Vanilla-Content?
        public final ModConfigSpec.BooleanValue onlyVanillaEffects;
        public final ModConfigSpec.BooleanValue onlyVanillaMobs;
        public final ModConfigSpec.BooleanValue onlyVanillaItems;

        // Hostile / Passive Mobs getrennt steuerbar
        public final ModConfigSpec.BooleanValue enableHostileMobs;
        public final ModConfigSpec.BooleanValue enablePassiveMobs;

        // Blacklists (String-Liste "namespace:path")
        public final ModConfigSpec.ConfigValue<List<? extends String>> effectBlacklist;
        public final ModConfigSpec.ConfigValue<List<? extends String>> mobBlacklist;
        public final ModConfigSpec.ConfigValue<List<? extends String>> itemBlacklist;

        // --- Random-Ranges für Effekte ---

        // Faktor auf Basisdauer (z.B. 0.5..2.0)
        public final ModConfigSpec.DoubleValue durationMinFactor;
        public final ModConfigSpec.DoubleValue durationMaxFactor;

        // Maximaler Verstärker (global) für Buffs/Debuffs
        public final ModConfigSpec.IntValue maxBuffAmplifier;
        public final ModConfigSpec.IntValue maxDebuffAmplifier;

        public final ModConfigSpec.IntValue baseHostileMobCount;
        public final ModConfigSpec.IntValue basePassiveMobCount;
        public final ModConfigSpec.IntValue baseHostileRadius;
        public final ModConfigSpec.IntValue basePassiveRadius;


        public Common(ModConfigSpec.Builder builder) {
            builder.push("general");

            delaySeconds = builder
                    .comment("Delay in seconds between random events.")
                    .defineInRange("delaySeconds", 60, 5, 3600);

            enableEffects = builder
                    .comment("Allow effect events.")
                    .define("enableEffects", true);

            enableMobs = builder
                    .comment("Allow mob spawn events.")
                    .define("enableMobs", true);

            enableItems = builder
                    .comment("Allow item reward events.")
                    .define("enableItems", true);

            weightEffects = builder
                    .comment("Relative weight of effect events.")
                    .defineInRange("weightEffects", 5, 0, 100);

            weightMobs = builder
                    .comment("Relative weight of mob events.")
                    .defineInRange("weightMobs", 3, 0, 100);

            weightItems = builder
                    .comment("Relative weight of item events.")
                    .defineInRange("weightItems", 2, 0, 100);

            // --- Balancing / Filter ---

            onlyVanillaEffects = builder
                    .comment("If true, only effects from minecraft namespace are used.")
                    .define("onlyVanillaEffects", true);

            onlyVanillaMobs = builder
                    .comment("If true, only mobs from minecraft namespace are used.")
                    .define("onlyVanillaMobs", true);

            onlyVanillaItems = builder
                    .comment("If true, only items from minecraft namespace are used.")
                    .define("onlyVanillaItems", true);

            enableHostileMobs = builder
                    .comment("Allow hostile mobs (MobCategory.MONSTER).")
                    .define("enableHostileMobs", true);

            enablePassiveMobs = builder
                    .comment("Allow non-hostile mobs (animals etc.).")
                    .define("enablePassiveMobs", true);

            baseHostileMobCount = builder
                    .comment("Base number of hostile mobs spawned per event.")
                    .defineInRange("baseHostileMobCount", 3, 1, 20);

            basePassiveMobCount = builder
                    .comment("Base number of passive mobs spawned per event.")
                    .defineInRange("basePassiveMobCount", 2, 1, 20);

            baseHostileRadius = builder
                    .comment("Base radius for hostile mob spawns around player.")
                    .defineInRange("baseHostileRadius", 6, 1, 64);

            basePassiveRadius = builder
                    .comment("Base radius for passive mob spawns around player.")
                    .defineInRange("basePassiveRadius", 5, 1, 64);


            effectBlacklist = builder
                    .comment("Blacklist of effect ids (e.g. \"minecraft:instant_damage\").")
                    .defineList("effectBlacklist",
                            List.of("minecraft:instant_damage"),
                            o -> o instanceof String);

            mobBlacklist = builder
                    .comment("Blacklist of mob ids (e.g. \"minecraft:wither\", \"minecraft:ender_dragon\").")
                    .defineList("mobBlacklist",
                            List.of("minecraft:wither", "minecraft:ender_dragon"),
                            o -> o instanceof String);

            itemBlacklist = builder
                    .comment("Blacklist of item ids (e.g. \"minecraft:barrier\").")
                    .defineList("itemBlacklist",
                            List.of("minecraft:barrier"),
                            o -> o instanceof String);

            // --- Random-Ranges ---

            durationMinFactor = builder
                    .comment("Minimum factor for effect duration relative to baseDuration.")
                    .defineInRange("durationMinFactor", 0.5d, 0.1d, 10.0d);

            durationMaxFactor = builder
                    .comment("Maximum factor for effect duration relative to baseDuration.")
                    .defineInRange("durationMaxFactor", 2.0d, 0.1d, 20.0d);

            maxBuffAmplifier = builder
                    .comment("Maximum amplifier for beneficial effects (0 = level I, 1 = level II, ...).")
                    .defineInRange("maxBuffAmplifier", 3, 0, 10);

            maxDebuffAmplifier = builder
                    .comment("Maximum amplifier for harmful effects.")
                    .defineInRange("maxDebuffAmplifier", 2, 0, 10);

            builder.pop();
        }
    }
}
