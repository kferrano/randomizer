package com.hardrock.randomizer;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class RandomizerConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();

        // CLIENT (nur HUD / Anzeige)
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    public static class Common {

        // Delay
        public final ModConfigSpec.IntValue delaySeconds;
        public final ModConfigSpec.BooleanValue enableBossbar;
        public final ModConfigSpec.BooleanValue showHudWithBossbar;


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


        // Admin-Modus
        public final ModConfigSpec.BooleanValue allowManualWhenStopped;

        public Common(ModConfigSpec.Builder builder) {

            // ====================
            //  GENERAL
            // ====================
            builder
                    .comment("General randomizer settings.")
                    .translation("randomizer.config.category.general")
                    .push("general");

            delaySeconds = builder

                    .translation("randomizer.common.general.delaySeconds")

                    .comment("Delay in seconds between random events.")
                    .defineInRange("delaySeconds", 60, 5, 3600);

            enableEffects = builder
                    .translation("randomizer.common.general.enableEffects")
                    .comment("Allow effect events.")
                    .define("enableEffects", true);

            enableMobs = builder
                    .translation("randomizer.common.general.enableMobs")
                    .comment("Allow mob spawn events.")
                    .define("enableMobs", true);

            enableItems = builder
                    .translation("randomizer.common.general.enableItems")
                    .comment("Allow item reward events.")
                    .define("enableItems", true);

            weightEffects = builder
                    .translation("randomizer.common.general.weightEffects")
                    .comment("Relative weight of effect events.")
                    .defineInRange("weightEffects", 5, 0, 100);

            weightMobs = builder
                    .translation("randomizer.common.general.weightMobs")
                    .comment("Relative weight of mob events.")
                    .defineInRange("weightMobs", 3, 0, 100);

            weightItems = builder
                    .translation("randomizer.common.general.weightItems")
                    .comment("Relative weight of item events.")
                    .defineInRange("weightItems", 2, 0, 100);

            builder.pop();


            // ====================
            //  CONTENT (FILTER / POOLS)
            // ====================
            builder
                    .comment("Content filters and vanilla/modded settings.")
                    .translation("randomizer.config.category.content")
                    .push("content");

            onlyVanillaEffects = builder
                    .translation("randomizer.common.content.onlyVanillaEffects")
                    .comment("If true, only effects from minecraft namespace are used.")
                    .define("onlyVanillaEffects", true);

            onlyVanillaMobs = builder
                    .translation("randomizer.common.content.onlyVanillaMobs")
                    .comment("If true, only mobs from minecraft namespace are used.")
                    .define("onlyVanillaMobs", true);

            onlyVanillaItems = builder
                    .translation("randomizer.common.content.onlyVanillaItems")
                    .comment("If true, only items from minecraft namespace are used.")
                    .define("onlyVanillaItems", true);

            enableHostileMobs = builder
                    .translation("randomizer.common.content.enableHostileMobs")
                    .comment("Allow hostile mobs (MobCategory.MONSTER).")
                    .define("enableHostileMobs", true);

            enablePassiveMobs = builder
                    .translation("randomizer.common.content.enablePassiveMobs")
                    .comment("Allow non-hostile mobs (animals etc.).")
                    .define("enablePassiveMobs", true);

            effectBlacklist = builder
                    .translation("randomizer.common.content.effectBlacklist")
                    .comment("Blacklist of effect ids (e.g. \"minecraft:instant_damage\").")
                    .defineList("effectBlacklist",
                            List.of("minecraft:instant_damage"),
                            o -> o instanceof String);

            mobBlacklist = builder
                    .translation("randomizer.common.content.mobBlacklist")
                    .comment("Blacklist of mob ids (e.g. \"minecraft:wither\", \"minecraft:ender_dragon\").")
                    .defineList("mobBlacklist",
                            List.of("minecraft:wither", "minecraft:ender_dragon", "minecraft:giant"),
                            o -> o instanceof String);

            itemBlacklist = builder
                    .translation("randomizer.common.content.itemBlacklist")
                    .comment("Blacklist of item ids (e.g. \"minecraft:barrier\").")
                    .defineList("itemBlacklist",
                            List.of("minecraft:barrier"),
                            o -> o instanceof String);

            builder.pop();


            // ====================
            //  EFFECTS
            // ====================
            builder
                    .comment("Effect duration and amplifier settings.")
                    .translation("randomizer.config.category.effects")
                    .push("effects");

            durationMinFactor = builder
                    .translation("randomizer.common.effects.durationMinFactor")
                    .comment("Minimum factor for effect duration relative to baseDuration.")
                    .defineInRange("durationMinFactor", 0.5d, 0.1d, 10.0d);

            durationMaxFactor = builder
                    .translation("randomizer.common.effects.durationMaxFactor")
                    .comment("Maximum factor for effect duration relative to baseDuration.")
                    .defineInRange("durationMaxFactor", 2.0d, 0.1d, 20.0d);

            maxBuffAmplifier = builder
                    .translation("randomizer.common.effects.maxBuffAmplifier")
                    .comment("Maximum amplifier for beneficial effects (0 = level I, 1 = level II, ...).")
                    .defineInRange("maxBuffAmplifier", 3, 0, 10);

            maxDebuffAmplifier = builder
                    .translation("randomizer.common.effects.maxDebuffAmplifier")
                    .comment("Maximum amplifier for harmful effects.")
                    .defineInRange("maxDebuffAmplifier", 2, 0, 10);

            builder.pop();


            // ====================
            //  MOBS
            // ====================
            builder
                    .comment("Mob spawn settings.")
                    .translation("randomizer.config.category.mobs")
                    .push("mobs");

            baseHostileMobCount = builder
                    .translation("randomizer.common.mobs.baseHostileMobCount")
                    .comment("Base number of hostile mobs spawned per event.")
                    .defineInRange("baseHostileMobCount", 3, 1, 20);

            basePassiveMobCount = builder
                    .translation("randomizer.common.mobs.basePassiveMobCount")
                    .comment("Base number of passive mobs spawned per event.")
                    .defineInRange("basePassiveMobCount", 2, 1, 20);

            baseHostileRadius = builder
                    .translation("randomizer.common.mobs.baseHostileRadius")
                    .comment("Base radius for hostile mob spawns around player.")
                    .defineInRange("baseHostileRadius", 6, 1, 64);

            basePassiveRadius = builder
                    .translation("randomizer.common.mobs.basePassiveRadius")
                    .comment("Base radius for passive mob spawns around player.")
                    .defineInRange("basePassiveRadius", 5, 1, 64);

            builder.pop();

            builder
                    .comment("Visual settings for HUD and boss bar.")
                    .translation("randomizer.config.category.display")
                    .push("display");

            enableBossbar = builder
                    .translation("randomizer.config.display.enableBossbar")
                    .comment("If true, shows a boss bar indicating progress to the next random event. Bossbar mode is experimental and purely cosmetic for now.")
                    .define("enableBossbar", false);

            showHudWithBossbar = builder
                    .translation("randomizer.config.display.showHudWithBossbar")
                    .comment("If true, HUD timer text is shown together with the boss bar. If false, only the boss bar is used.")
                    .define("showHudWithBossbar", true);

            builder.pop();

            builder
                    .push("admin");

            allowManualWhenStopped = builder
                    .define("allowManualWhenStopped", true);

            builder.pop();
        }
    }

    public static class Client {

        public final ModConfigSpec.BooleanValue disableHud;
        public final ModConfigSpec.DoubleValue hudX;
        public final ModConfigSpec.DoubleValue hudY;
        public final ModConfigSpec.BooleanValue disableBossbar;

        Client(ModConfigSpec.Builder builder) {
            builder
                    .comment("HUD display settings for the randomizer timer.")
                    .translation("randomizer.config.category.hud")
                    .push("hud");

            disableBossbar = builder
                    .translation("randomizer.config.hud.disableBossbar")
                    .comment("If true, the Randomizer Bossbar is completely disabled on this client.")
                    .define("disableBossbar", false);
            disableHud = builder
                    .translation("randomizer.config.hud.disableHud")
                    .comment("If true, the Randomizer HUD timer is completely disabled on this client.")
                    .define("disableHud", false);

            hudX = builder
                    .translation("randomizer.config.hud.hudX")
                    .comment("Horizontal position of the HUD timer (0.0 = left, 0.5 = center, 1.0 = right)")
                    .defineInRange("hudX", 0.5d, 0.0d, 1.0d);

            hudY = builder
                    .translation("randomizer.config.hud.hudY")
                    .comment("Vertical position of the HUD timer (0.0 = top, 1.0 = just above the hotbar)")
                    .defineInRange("hudY", 0.9d, 0.0d, 1.0d);

            builder.pop();
        }


    }
}
