package com.hardrock.randomizer;

import com.hardrock.randomizer.data.EffectEntry;
import com.hardrock.randomizer.data.ItemEntry;
import com.hardrock.randomizer.data.MobEntry;
import com.hardrock.randomizer.data.RarityTier;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.monster.warden.Warden;
import org.slf4j.Logger;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomPools {

    public enum EventType {
        EFFECT,
        MOB,
        ITEM
    }

    private final List<EffectEntry> effects = new ArrayList<>();
    private final List<MobEntry> mobs = new ArrayList<>();
    private final List<ItemEntry> items = new ArrayList<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    private boolean enabled = true;
    private boolean initialized = false;


    public RandomPools() {

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    private void ensureInitialized() {
        if (initialized) return;

        initDefaultEntries();
        initialized = true;
    }

    // =====================================================================================
    // Default-Einträge (später durch Config ersetzbar)
    // =====================================================================================

    private void initDefaultEntries() {
        initEffectsFromRegistry();
        initMobsFromRegistry();
        initItemsFromRegistry();

        /*
        mobs.add(new MobEntry(
                ResourceLocation.parse("minecraft:warden"),
                5,
                10,
                10,
                MobEntry.MobType.HOSTILE,
                RarityTier.EXTREME
        ));
        */
    }

    private static boolean isAllowed(ResourceLocation id, Set<ResourceLocation> whitelist, Set<ResourceLocation> blacklist) {
        if (!whitelist.isEmpty()) {
            return whitelist.contains(id);
        }
        return !blacklist.contains(id);
    }

    private static Set<ResourceLocation> buildValidatedIdSet(
            List<? extends String> raw,
            Set<ResourceLocation> registryIds,
            String label
    ) {
        Set<ResourceLocation> out = new java.util.HashSet<>();
        for (String s : raw) {
            if (s == null || s.isBlank()) continue;

            ResourceLocation rl = ResourceLocation.tryParse(s);
            if (rl == null) {
                LOGGER.warn("[Randomizer] Invalid {} id '{}' (not a valid ResourceLocation). Ignoring.", label, s);
                continue;
            }

            if (!registryIds.contains(rl)) {
                LOGGER.warn("[Randomizer] Unknown {} id '{}' (not registered). Ignoring.", label, s);
                continue;
            }

            out.add(rl);
        }
        return java.util.Set.copyOf(out);
    }

    private void initEffectsFromRegistry() {
        boolean onlyVanilla = RandomizerConfig.COMMON.onlyVanillaEffects.get();
        Set<ResourceLocation> effectRegistry = BuiltInRegistries.MOB_EFFECT.keySet();

        Set<ResourceLocation> whitelist = buildValidatedIdSet(
                RandomizerConfig.COMMON.effectWhitelist.get(),
                effectRegistry,
                "effectWhitelist"
        );

        Set<ResourceLocation> blacklist = buildValidatedIdSet(
                RandomizerConfig.COMMON.effectBlacklist.get(),
                effectRegistry,
                "effectBlacklist"
        );

        int maxBuffAmp = RandomizerConfig.COMMON.maxBuffAmplifier.get();
        int maxDebuffAmp = RandomizerConfig.COMMON.maxDebuffAmplifier.get();

        for (ResourceLocation id : BuiltInRegistries.MOB_EFFECT.keySet()) {

            if (onlyVanilla && !id.getNamespace().equals("minecraft")) continue;
            if (!isAllowed(id, whitelist, blacklist)) continue;

            var optHolder = BuiltInRegistries.MOB_EFFECT.get(id);
            if (optHolder.isEmpty()) continue;

            Holder<MobEffect> holder = optHolder.get();
            MobEffect effect = holder.value();

            MobEffectCategory cat = effect.getCategory();
            boolean beneficial = (cat == MobEffectCategory.BENEFICIAL);

            int baseDuration = beneficial ? 20 * 60 : 20 * 30;  // 60s / 30s Basis
            int maxAmplifier = beneficial ? maxBuffAmp : maxDebuffAmp;
            int weight = beneficial ? 5 : 4;

            RarityTier tier;
            if (beneficial) {
                tier = RarityTier.COMMON;
            } else {
                // debuffs grundsätzlich seltener
                tier = (maxDebuffAmp >= 2) ? RarityTier.EXTREME : RarityTier.RARE;
            }


            effects.add(new EffectEntry(
                    id,
                    maxAmplifier,
                    baseDuration,
                    weight,
                    beneficial ? EffectEntry.EffectType.BUFF : EffectEntry.EffectType.DEBUFF,
                    tier
            ));
        }
    }

    private void initMobsFromRegistry() {
        boolean onlyVanilla = RandomizerConfig.COMMON.onlyVanillaMobs.get();
        boolean allowHostile = RandomizerConfig.COMMON.enableHostileMobs.get();
        boolean allowPassive = RandomizerConfig.COMMON.enablePassiveMobs.get();
        Set<ResourceLocation> mobRegistry = BuiltInRegistries.ENTITY_TYPE.keySet();

        Set<ResourceLocation> whitelist = buildValidatedIdSet(
                RandomizerConfig.COMMON.mobWhitelist.get(),
                mobRegistry,
                "mobWhitelist"
        );

        Set<ResourceLocation> blacklist = buildValidatedIdSet(
                RandomizerConfig.COMMON.mobBlacklist.get(),
                mobRegistry,
                "mobBlacklist"
        );

        for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {

            if (onlyVanilla && !id.getNamespace().equals("minecraft")) continue;
            if (!isAllowed(id, whitelist, blacklist)) continue;

            var optTypeHolder = BuiltInRegistries.ENTITY_TYPE.get(id);
            if (optTypeHolder.isEmpty()) continue;

            Holder<EntityType<?>> holder = optTypeHolder.get();
            EntityType<?> type = holder.value();

            MobCategory cat = type.getCategory();
            if (cat == MobCategory.MISC) continue;

            boolean isHostile = (cat == MobCategory.MONSTER);
            boolean isBossLike = id.getNamespace().equals("minecraft") && (
                    id.getPath().equals("wither")
                            || id.getPath().equals("ender_dragon")
                            || id.getPath().equals("warden")
            );
            if (isHostile && !allowHostile) continue;
            if (!isHostile && !allowPassive) continue;

            MobEntry.MobType poolType = isHostile
                    ? MobEntry.MobType.HOSTILE
                    : MobEntry.MobType.PASSIVE;

            int count = isBossLike ? 1 : (isHostile
                    ? RandomizerConfig.COMMON.baseHostileMobCount.get()
                    : RandomizerConfig.COMMON.basePassiveMobCount.get());
            int radius = isHostile
                    ? RandomizerConfig.COMMON.baseHostileRadius.get()
                    : RandomizerConfig.COMMON.basePassiveRadius.get();
            int weight = isHostile ? 4 : 2;
            RarityTier tier = isBossLike
                    ? RarityTier.EXTREME
                    : (isHostile ? RarityTier.RARE : RarityTier.COMMON);

            mobs.add(new MobEntry(
                    id,
                    count,
                    radius,
                    weight,
                    poolType,
                    tier
            ));
        }
    }

    private static RarityTier determineItemTier(ResourceLocation id, Item item) {
        int maxStack = item.getDefaultMaxStackSize();

        if (id.getNamespace().equals("minecraft") && (
                id.getPath().equals("nether_star")
                        || id.getPath().equals("elytra")
                        || id.getPath().equals("dragon_egg")
                        || id.getPath().equals("totem_of_undying")
        )) {
            return RarityTier.EXTREME;
        }

        if (maxStack == 1) {
            return RarityTier.RARE;
        }

        return RarityTier.COMMON;
    }


    private void initItemsFromRegistry() {
        boolean onlyVanilla = RandomizerConfig.COMMON.onlyVanillaItems.get();
        Set<ResourceLocation> itemRegistry = BuiltInRegistries.ITEM.keySet();

        Set<ResourceLocation> whitelist = buildValidatedIdSet(
                RandomizerConfig.COMMON.itemWhitelist.get(),
                itemRegistry,
                "itemWhitelist"
        );

        Set<ResourceLocation> blacklist = buildValidatedIdSet(
                RandomizerConfig.COMMON.itemBlacklist.get(),
                itemRegistry,
                "itemBlacklist"
        );

        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {

            if (onlyVanilla && !id.getNamespace().equals("minecraft")) continue;
            if (!isAllowed(id, whitelist, blacklist)) continue;

            var optItemHolder = BuiltInRegistries.ITEM.get(id);
            if (optItemHolder.isEmpty()) continue;

            Holder<Item> holder = optItemHolder.get();
            Item item = holder.value();

            int maxStack = item.getDefaultMaxStackSize();
            if (maxStack <= 0) continue;

            int count;
            if (maxStack >= 64) {
                count = 16;
            } else if (maxStack >= 16) {
                count = 8;
            } else {
                count = 1;
            }

            int weight = 3;

            // Tier-Zuordnung (wichtig!)
            RarityTier tier = determineItemTier(id, item);

            items.add(new ItemEntry(
                    id,
                    count,
                    weight,
                    tier

            ));
        }
    }


    // =====================================================================================
    // Event-Typ Auswahl
    // =====================================================================================

    public EventType chooseEventType(Random random) {
        ensureInitialized();

        boolean effEnabled = RandomizerConfig.COMMON.enableEffects.get() && !effects.isEmpty();
        boolean mobsEnabled = RandomizerConfig.COMMON.enableMobs.get() && !mobs.isEmpty();
        boolean itemEnabled = RandomizerConfig.COMMON.enableItems.get() && !items.isEmpty();

        int weightEffect = effEnabled ? RandomizerConfig.COMMON.weightEffects.get() : 0;
        int weightMob = mobsEnabled ? RandomizerConfig.COMMON.weightMobs.get() : 0;
        int weightItem = itemEnabled ? RandomizerConfig.COMMON.weightItems.get() : 0;

        int total = weightEffect + weightMob + weightItem;
        if (total <= 0) return null;

        int roll = random.nextInt(total);

        if (roll < weightEffect) return EventType.EFFECT;
        roll -= weightEffect;

        if (roll < weightMob) return EventType.MOB;
        roll -= weightMob;

        if (roll < weightItem) return EventType.ITEM;

        return null;
    }


    // =====================================================================================
    // Effekte
    // =====================================================================================
    private static RarityTier chooseEffectTier(Random random) {
        int common = RandomizerConfig.COMMON.effectTierCommon.get();
        int rare = RandomizerConfig.COMMON.effectTierRare.get();
        int extreme = RandomizerConfig.COMMON.effectTierExtreme.get();

        int total = common + rare + extreme;
        if (total <= 0) return RarityTier.COMMON;

        int roll = random.nextInt(total);
        if (roll < common) return RarityTier.COMMON;
        roll -= common;
        if (roll < rare) return RarityTier.RARE;
        return RarityTier.EXTREME;
    }

    public String applyRandomEffect(ServerPlayer player, Random random) {
        ensureInitialized();

        if (effects.isEmpty()) return null;
        RarityTier tier = chooseEffectTier(random);
        EffectEntry entry = weightedEffect(random, tier);
        if (entry == null && tier != RarityTier.COMMON) {
            entry = weightedEffect(random, RarityTier.COMMON);
        }
        if (entry == null) return null;

        var optHolder = BuiltInRegistries.MOB_EFFECT.get(entry.id());
        if (optHolder.isEmpty()) return null;

        Holder<MobEffect> effectHolder = optHolder.get();

        // --- Random Faktoren aus Config ---
        double minFactor = RandomizerConfig.COMMON.durationMinFactor.get();
        double maxFactor = RandomizerConfig.COMMON.durationMaxFactor.get();

        if (minFactor <= 0) minFactor = 0.1;
        if (maxFactor < minFactor) maxFactor = minFactor;

        int baseDuration = entry.durationTicks();
        int minDuration = (int) Math.max(40, baseDuration * minFactor);            // min. 2s
        int maxDuration = (int) Math.max(minDuration, baseDuration * maxFactor);

        int duration = random.nextInt(maxDuration - minDuration + 1) + minDuration;

        int maxAmpFromEntry = Math.max(0, entry.amplifier());
        int amplifier = 0;
        if (maxAmpFromEntry > 0) {
            amplifier = random.nextInt(maxAmpFromEntry + 1); // 0..maxAmp inkl.
        }

        MobEffectInstance instance = new MobEffectInstance(
                effectHolder,
                duration,
                amplifier
        );

        player.addEffect(instance);

        String effectName = effectHolder.value().getDisplayName().getString();
        int seconds = duration / 20;
        int level = amplifier + 1;

        return "received " + effectName + " " + level + " for " + seconds + "s";
    }


    private EffectEntry weightedEffect(Random random, RarityTier tier) {
        int totalWeight = 0;
        for (EffectEntry e : effects) {
            if (e.tier() != tier) continue;
            totalWeight += e.weight();
        }
        if (totalWeight <= 0) return null;

        int roll = random.nextInt(totalWeight);
        for (EffectEntry e : effects) {
            if (e.tier() != tier) continue;
            roll -= e.weight();
            if (roll < 0) {
                return e;
            }
        }
        return null;
    }

    // =====================================================================================
    // Mobs
    // =====================================================================================

    private RarityTier chooseMobTier(Random random) {
        // Diese 3 Config-Werte musst du in RandomizerConfig ergänzen (pro Pool).
        int wCommon = RandomizerConfig.COMMON.mobTierCommon.get();
        int wRare = RandomizerConfig.COMMON.mobTierRare.get();
        int wExtreme = RandomizerConfig.COMMON.mobTierExtreme.get();

        int total = wCommon + wRare + wExtreme;
        if (total <= 0) return RarityTier.COMMON;

        int roll = random.nextInt(total);
        if (roll < wCommon) return RarityTier.COMMON;
        roll -= wCommon;

        if (roll < wRare) return RarityTier.RARE;
        return RarityTier.EXTREME;
    }

    public String spawnRandomMob(ServerPlayer player, Random random) {
        ensureInitialized();

        if (mobs.isEmpty()) return null;

        RarityTier tier = chooseMobTier(random);
        MobEntry entry = weightedMob(random, tier);
        if (entry == null && tier != RarityTier.COMMON) {
            entry = weightedMob(random, RarityTier.COMMON);
        }
        if (entry == null) return null;

        if (!(player.level() instanceof ServerLevel level)) return null;

        BlockPos base = player.blockPosition();
        int spawned = 0;
        String mobName = "mob";

        // Basiswert aus Entry
        int baseCount = entry.count();

        // z.B. zwischen 50% und 150% des Basiswerts
        int min = Math.max(1, baseCount / 2);
        int max = Math.max(min, (int) Math.round(baseCount * 1.5));

        int totalToSpawn = random.nextInt(max - min + 1) + min;

        for (int i = 0; i < totalToSpawn; i++) {
            int dx = random.nextInt(entry.radius() * 2 + 1) - entry.radius();
            int dz = random.nextInt(entry.radius() * 2 + 1) - entry.radius();
            BlockPos spawnPos = base.offset(dx, 0, dz);

            var optTypeHolder = BuiltInRegistries.ENTITY_TYPE.get(entry.entityId());
            if (optTypeHolder.isEmpty()) continue;

            Holder<EntityType<?>> typeHolder = optTypeHolder.get();
            EntityType<?> type = typeHolder.value();

            // Entity EINMAL erzeugen
            var entity = type.create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            if (!(entity instanceof Mob mob)) {
                // Keine echte Mob-Entity → überspringen
                continue;
            }

            // Position & Rotation setzen
            double x = spawnPos.getX() + 0.5D;
            double y = spawnPos.getY();
            double z = spawnPos.getZ() + 0.5D;
            float yaw = level.random.nextFloat() * 360.0F;

            mob.setPos(x, y, z);
            mob.setYRot(yaw);
            mob.yHeadRot = yaw;
            mob.yBodyRot = yaw;

            // Vanilla-Spawn-Initialisierung (AI, Ausrüstung, etc.)
            mob.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(spawnPos),
                    net.minecraft.world.entity.EntitySpawnReason.TRIGGERED,
                    null
            );

            // Spezieller Fix: Pillager ohne Waffe → Crossbow geben
            if (mob instanceof Pillager pillager && pillager.getMainHandItem().isEmpty()) {
                pillager.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CROSSBOW));
            }

            // (Optional) Warden persistent machen – wenn du ihn nicht default blacklistest
            if (mob instanceof Warden warden) {
                warden.setPersistenceRequired();
                warden.increaseAngerAt(player, 150, true);
            }

            level.addFreshEntity(mob);
            spawned++;

            mobName = mob.getType().getDescription().getString();
        }

        if (spawned <= 0) return null;

        return "spawned " + spawned + "x " + mobName + " nearby";
    }


    private MobEntry weightedMob(Random random, RarityTier tier) {
        int totalWeight = 0;
        for (MobEntry e : mobs) totalWeight += e.weight();

        if (totalWeight <= 0) return null;

        int roll = random.nextInt(totalWeight);
        for (MobEntry e : mobs) {
            roll -= e.weight();
            if (roll < 0) return e;
        }
        return null;
    }

    // =====================================================================================
    // Items
    // =====================================================================================

    private static RarityTier chooseItemTier(Random random) {
        int common = RandomizerConfig.COMMON.itemTierCommon.get();
        int rare = RandomizerConfig.COMMON.itemTierRare.get();
        int extreme = RandomizerConfig.COMMON.itemTierExtreme.get();

        int total = common + rare + extreme;
        if (total <= 0) return RarityTier.COMMON;

        int roll = random.nextInt(total);
        if (roll < common) return RarityTier.COMMON;
        roll -= common;
        if (roll < rare) return RarityTier.RARE;
        return RarityTier.EXTREME;
    }


    public String giveRandomItem(ServerPlayer player, Random random) {
        ensureInitialized();

        if (items.isEmpty()) return null;
        RarityTier tier = chooseItemTier(random);

        ItemEntry entry = weightedItem(random, tier);
        if (entry == null && tier != RarityTier.COMMON) {
            entry = weightedItem(random, RarityTier.COMMON);
        }
        if (entry == null) return null;

        var optItemHolder = BuiltInRegistries.ITEM.get(entry.id());
        if (optItemHolder.isEmpty()) return null;

        Holder<Item> itemHolder = optItemHolder.get();
        Item item = itemHolder.value();

        int maxStack = item.getDefaultMaxStackSize();

        if (maxStack <= 0) {
            maxStack = 1;
        }

        // Basis aus deinem Pool (16 / 8 / 1)
        int count;
        if (maxStack == 1) {
            count = 1;
        } else {
            count = 1 + random.nextInt(maxStack);
        }

        ItemStack stack = new ItemStack(itemHolder, count);

        // WICHTIG: Name VOR dem Inventar-Einfügen lesen
        String itemName = stack.getHoverName().getString();

        player.getInventory().placeItemBackInInventory(stack);

        return "received " + count + "x " + itemName;
    }


    private ItemEntry weightedItem(Random random, RarityTier tier) {
        int totalWeight = 0;
        for (ItemEntry e : items) {
            if (e.tier() != tier) continue;
            totalWeight += e.weight();
        }
        if (totalWeight <= 0) return null;

        int roll = random.nextInt(totalWeight);
        for (ItemEntry e : items) {
            if (e.tier() != tier) continue;
            roll -= e.weight();
            if (roll < 0) {
                return e;
            }
        }
        return null;
    }
}
