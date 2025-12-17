package com.hardrock.randomizer;

import net.minecraft.resources.ResourceLocation;
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

        // Display
        public final ModConfigSpec.BooleanValue enableBossbar;
        public final ModConfigSpec.BooleanValue showHudWithBossbar;
        public final ModConfigSpec.BooleanValue showTargetedActionbar;
        public final ModConfigSpec.BooleanValue broadcastEventsToAll;
        public final ModConfigSpec.BooleanValue broadcastToOpsOnly;

        // Event-Typen
        public final ModConfigSpec.BooleanValue enableEffects;
        public final ModConfigSpec.BooleanValue enableMobs;
        public final ModConfigSpec.BooleanValue enableItems;

        public final ModConfigSpec.IntValue weightEffects;
        public final ModConfigSpec.IntValue weightMobs;
        public final ModConfigSpec.IntValue weightItems;

        // === Tier Weights: ITEMS ===
        public final ModConfigSpec.IntValue itemTierCommon;
        public final ModConfigSpec.IntValue itemTierRare;
        public final ModConfigSpec.IntValue itemTierExtreme;

        // === Tier Weights: EFFECTS ===
        public final ModConfigSpec.IntValue effectTierCommon;
        public final ModConfigSpec.IntValue effectTierRare;
        public final ModConfigSpec.IntValue effectTierExtreme;

        // === Tier Weights: MOBS ===
        public final ModConfigSpec.IntValue mobTierCommon;
        public final ModConfigSpec.IntValue mobTierRare;
        public final ModConfigSpec.IntValue mobTierExtreme;

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

        // Whitelists (String-Liste "namespace:path")
        public final ModConfigSpec.ConfigValue<List<? extends String>> effectWhitelist;
        public final ModConfigSpec.ConfigValue<List<? extends String>> mobWhitelist;
        public final ModConfigSpec.ConfigValue<List<? extends String>> itemWhitelist;

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
        public final ModConfigSpec.BooleanValue enableDebugLogging;

        public Common(ModConfigSpec.Builder builder) {

            // ====================
            //  GENERAL
            // ====================
            builder
                    .comment("General randomizer settings.")
                    .translation("randomizer.config.category.general")
                    .push("general");

            delaySeconds = builder
                    .translation("randomizer.config.general.delaySeconds")
                    .comment("Delay in seconds between random events.")
                    .defineInRange("delaySeconds", 60, 5, 3600);

            enableEffects = builder
                    .translation("randomizer.config.general.enableEffects")
                    .comment("Allow effect events.")
                    .define("enableEffects", true);

            enableMobs = builder
                    .translation("randomizer.config.general.enableMobs")
                    .comment("Allow mob spawn events.")
                    .define("enableMobs", true);

            enableItems = builder
                    .translation("randomizer.config.general.enableItems")
                    .comment("Allow item reward events.")
                    .define("enableItems", true);

            weightEffects = builder
                    .translation("randomizer.config.general.weightEffects")
                    .comment("Relative weight of effect events.")
                    .defineInRange("weightEffects", 5, 0, 100);

            weightMobs = builder
                    .translation("randomizer.config.general.weightMobs")
                    .comment("Relative weight of mob events.")
                    .defineInRange("weightMobs", 3, 0, 100);

            weightItems = builder
                    .translation("randomizer.config.general.weightItems")
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
                    .translation("randomizer.config.content.onlyVanillaEffects")
                    .comment("If true, only effects from minecraft namespace are used.")
                    .define("onlyVanillaEffects", true);

            onlyVanillaMobs = builder
                    .translation("randomizer.config.content.onlyVanillaMobs")
                    .comment("If true, only mobs from minecraft namespace are used.")
                    .define("onlyVanillaMobs", true);

            onlyVanillaItems = builder
                    .translation("randomizer.config.content.onlyVanillaItems")
                    .comment("If true, only items from minecraft namespace are used.")
                    .define("onlyVanillaItems", true);

            enableHostileMobs = builder
                    .translation("randomizer.config.content.enableHostileMobs")
                    .comment("Allow hostile mobs (MobCategory.MONSTER).")
                    .define("enableHostileMobs", true);

            enablePassiveMobs = builder
                    .translation("randomizer.config.content.enablePassiveMobs")
                    .comment("Allow non-hostile mobs (animals etc.).")
                    .define("enablePassiveMobs", true);

            effectWhitelist = builder
                    .translation("randomizer.config.content.effectWhitelist")
                    .comment(
                            "Whitelist of effect ids (e.g. \"minecraft:speed\").",
                            "If not empty: ONLY whitelisted effects are allowed. Blacklist is ignored.")
                    .defineList("effectWhitelist",
                            List.of(),
                            o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                    );

            mobWhitelist = builder
                    .translation("randomizer.config.content.mobWhitelist")
                    .comment(
                            "Whitelist of mob ids (e.g. \"minecraft:zombie\").",
                            "If not empty: ONLY whitelisted mobs are allowed. Blacklist is ignored.")
                    .defineList("mobWhitelist",
                            List.of(),
                            o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                    );

            itemWhitelist = builder
                    .translation("randomizer.config.content.itemWhitelist")
                    .comment(
                            "Whitelist of item ids (e.g. \"minecraft:diamond\").",
                            "If not empty: ONLY whitelisted items are allowed. Blacklist is ignored.")
                    .defineList("itemWhitelist",
                            List.of(),
                            o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                    );


            effectBlacklist = builder
                    .translation("randomizer.config.content.effectBlacklist")
                    .comment("Blacklist of effect ids (e.g. \"minecraft:instant_damage\").")
                    .defineList("effectBlacklist",
                            List.of("minecraft:instant_damage"),
                            o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                    );

            mobBlacklist = builder
                    .translation("randomizer.config.content.mobBlacklist")
                    .comment("Blacklist of mob ids (e.g. \"minecraft:wither\", \"minecraft:ender_dragon\").")
                    .defineList("mobBlacklist",
                            List.of("minecraft:wither", "minecraft:ender_dragon", "minecraft:giant"),
                            o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                    );

            itemBlacklist = builder
                    .translation("randomizer.config.content.itemBlacklist")
                    .comment("Blacklist of item ids (e.g. \"minecraft:barrier\").")
                    .defineList("itemBlacklist",
                            List.of("minecraft:barrier"),
                            o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                    );

            builder.pop();


            // ====================
            //  EFFECTS
            // ====================
            builder
                    .comment("Effect duration and amplifier settings.")
                    .translation("randomizer.config.category.effects")
                    .push("effects");

            durationMinFactor = builder
                    .translation("randomizer.config.effects.durationMinFactor")
                    .comment("Minimum factor for effect duration relative to baseDuration.")
                    .defineInRange("durationMinFactor", 0.5d, 0.1d, 10.0d);

            durationMaxFactor = builder
                    .translation("randomizer.config.effects.durationMaxFactor")
                    .comment("Maximum factor for effect duration relative to baseDuration.")
                    .defineInRange("durationMaxFactor", 2.0d, 0.1d, 20.0d);

            maxBuffAmplifier = builder
                    .translation("randomizer.config.effects.maxBuffAmplifier")
                    .comment("Maximum amplifier for beneficial effects (0 = level I, 1 = level II, ...).")
                    .defineInRange("maxBuffAmplifier", 3, 0, 10);

            maxDebuffAmplifier = builder
                    .translation("randomizer.config.effects.maxDebuffAmplifier")
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
                    .translation("randomizer.config.mobs.baseHostileMobCount")
                    .comment("Base number of hostile mobs spawned per event.")
                    .defineInRange("baseHostileMobCount", 3, 1, 20);

            basePassiveMobCount = builder
                    .translation("randomizer.config.mobs.basePassiveMobCount")
                    .comment("Base number of passive mobs spawned per event.")
                    .defineInRange("basePassiveMobCount", 2, 1, 20);

            baseHostileRadius = builder
                    .translation("randomizer.config.mobs.baseHostileRadius")
                    .comment("Base radius for hostile mob spawns around player.")
                    .defineInRange("baseHostileRadius", 6, 1, 64);

            basePassiveRadius = builder
                    .translation("randomizer.config.mobs.basePassiveRadius")
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

            showTargetedActionbar = builder
                    .translation("randomizer.config.display.showTargetedActionbar")
                    .comment("If true, targeted players receive a short actionbar notice when an event triggers.")
                    .define("showTargetedActionbar", true);

            broadcastEventsToAll = builder
                    .translation("randomizer.config.display.broadcastEventsToAll")
                    .comment("If true, non-target players also receive the event broadcast message.")
                    .define("broadcastEventsToAll", true);

            broadcastToOpsOnly = builder
                    .translation("randomizer.config.display.broadcastToOpsOnly")
                    .comment("If true, broadcast messages are only sent to server operators. Requires broadcastEventsToAll=true.")
                    .define("broadcastToOpsOnly", false);

            builder.pop();

            builder
                    .push("admin");

            allowManualWhenStopped = builder
                    .define("allowManualWhenStopped", true);
            enableDebugLogging = builder
                    .translation("randomizer.config.logging.enableDebugLogging")
                    .comment("If true, enables verbose debug logging for pool selection, fallbacks and spawn retries.")
                    .define("enableDebugLogging", false);
            builder.pop();

            builder.push("tiers");

        // ---------- ITEM TIERS ----------
            builder.push("items");

            itemTierCommon = builder
                    .translation("randomizer.config.tiers.items.common")
                    .comment(
                            "Relative chance for COMMON items within the item pool.",
                            "Higher values make common items more likely.",
                            "If set to 0, common items will never be selected."
                    )
                    .defineInRange("common", 70, 0, 1000);

            itemTierRare = builder
                    .translation("randomizer.config.tiers.items.rare")
                    .comment(
                            "Relative chance for RARE items within the item pool.",
                            "Higher values increase the chance of rare items.",
                            "If set to 0, rare items will never be selected."
                    )
                    .defineInRange("rare", 25, 0, 1000);

            itemTierExtreme = builder
                    .translation("randomizer.config.tiers.items.extreme")
                    .comment(
                            "Relative chance for EXTREME items within the item pool.",
                            "Extreme items are powerful or special rewards.",
                            "If set to 0, extreme items will never be selected."
                    )
                    .defineInRange("extreme", 5, 0, 1000);

            builder.pop();

        // ---------- EFFECT TIERS ----------
            builder.push("effects");

            effectTierCommon = builder
                    .translation("randomizer.config.tiers.effects.common")
                    .comment(
                            "Relative chance for COMMON effects.",
                            "Common effects are usually mild or beneficial."
                    )
                    .defineInRange("common", 65, 0, 1000);

            effectTierRare = builder
                    .translation("randomizer.config.tiers.effects.rare")
                    .comment(
                            "Relative chance for RARE effects.",
                            "Rare effects are stronger or more impactful."
                    )
                    .defineInRange("rare", 30, 0, 1000);

            effectTierExtreme = builder
                    .translation("randomizer.config.tiers.effects.extreme")
                    .comment(
                            "Relative chance for EXTREME effects.",
                            "Extreme effects can be very strong or dangerous."
                    )
                    .defineInRange("extreme", 5, 0, 1000);

            builder.pop();


        // ---------- MOB TIERS ----------
            builder.push("mobs");

            mobTierCommon = builder
                    .translation("randomizer.config.tiers.mobs.common")
                    .comment(
                            "Relative chance for COMMON mobs.",
                            "Usually passive or low-threat mobs."
                    )
                    .defineInRange("common", 60, 0, 1000);

            mobTierRare = builder
                    .translation("randomizer.config.tiers.mobs.rare")
                    .comment(
                            "Relative chance for RARE mobs.",
                            "Typically hostile or stronger mobs."
                    )
                    .defineInRange("rare", 30, 0, 1000);

            mobTierExtreme = builder
                    .translation("randomizer.config.tiers.mobs.extreme")
                    .comment(
                            "Relative chance for EXTREME mobs.",
                            "Boss-like or very dangerous mobs.",
                            "Boss mobs are always limited to a spawn count of 1."
                    )
                    .defineInRange("extreme", 10, 0, 1000);

            builder.pop();


            builder.pop(); // tiers
        }
    }


    public static class Client {

        public final ModConfigSpec.BooleanValue disableTimer;
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
                    .comment("If true, the Randomizer Bossbar is completely disabled on this client. Needed Reconnect to be effective.")
                    .define("disableBossbar", false);
            disableTimer = builder
                    .translation("randomizer.config.hud.disableTimer")
                    .comment("If true, the Randomizer HUD timer is completely disabled on this client.")
                    .define("disableTimer", false);

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
