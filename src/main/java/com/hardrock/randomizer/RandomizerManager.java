package com.hardrock.randomizer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import com.hardrock.randomizer.network.RandomizerNetwork;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import java.util.List;
import java.util.Random;

public class RandomizerManager {
    private static final Logger LOGGER = LogUtils.getLogger();


    private int lastDelaySeconds = -1;
    private int delayTicks = 20 * 60;
    private int countdown = delayTicks;

    private final Random random = new Random();
    private final RandomPools pools = new RandomPools();
    private boolean delayInitialized = false;

    private State state = State.STOPPED;
    private int tickCounter = 0;
    private long elapsedTicks = 0;
    private final ServerBossEvent bossBar;
    private final Set<UUID> bossbarHiddenPlayers = new HashSet<>();


    public enum State {
        RUNNING,
        PAUSED,
        STOPPED
    }

    public static RandomizerManager INSTANCE;

    public RandomizerManager() {
        INSTANCE = this;
        this.bossBar = new ServerBossEvent(
                Component.literal("Randomizer"),
                BossEvent.BossBarColor.BLUE,
                BossEvent.BossBarOverlay.PROGRESS
        );
        this.bossBar.setVisible(false);
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    private void updateBossbarPlayers(MinecraftServer server) {
        bossBar.removeAllPlayers();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            bossBar.addPlayer(player);
        }
    }

    public void setBossbarPreference(ServerPlayer player, boolean disableBossbar) {
        UUID id = player.getUUID();
        if (disableBossbar) {
            bossbarHiddenPlayers.add(id);
            bossBar.removePlayer(player);
        } else {
            bossbarHiddenPlayers.remove(id);
            // nicht sofort adden – passiert im Tick, damit alles zentral ist
        }
    }


    public void updateDelayFromConfig() {
        int seconds = RandomizerConfig.COMMON.delaySeconds.get();
        if (seconds < 5) seconds = 5;
        if (seconds > 3600) seconds = 3600;
        this.delayTicks = seconds * 20;
    }

    public void start() {
        if (state == State.RUNNING) {
            return;
        }
        if (state == State.STOPPED) {
            tickCounter = 0;
            elapsedTicks = 0;
        }
        state = State.RUNNING;
        if (RandomizerConfig.COMMON.enableBossbar.get()) {
            bossBar.setVisible(true);
        }
        LOGGER.info("Randomizer started. Delay: {} seconds", delayTicks / 20);
    }

    public void pause() {
        if (state != State.RUNNING) {
            return;
        }
        state = State.PAUSED;
        if (RandomizerConfig.COMMON.enableBossbar.get()) {
            bossBar.setVisible(false);
        }
    }

    public void stop() {
        if (state == State.STOPPED) {
            return;
        }
        state = State.STOPPED;
        tickCounter = 0;
        elapsedTicks = 0;
        if (RandomizerConfig.COMMON.enableBossbar.get()) {
            bossBar.setVisible(false);
            bossBar.setProgress(0.0f);
            bossBar.removeAllPlayers();
        }
        LOGGER.info("Randomizer stopped and timer reset.");
    }

    public State getState() {
        return state;
    }

    public long getElapsedTicks() {
        return elapsedTicks;
    }

    public String getStatusString() {
        int ticks = (int) elapsedTicks;
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;

        String time = String.format("%02d:%02d", minutes, seconds);

        return "State: " + state + ", Time: " + time + ", Delay: " + (delayTicks / 20) + "s";
    }

    public static Component randomizerPrefix() {
        return Component.literal("[Randomizer] ")
                .withStyle(ChatFormatting.GOLD);
    }


    public void onServerTick(MinecraftServer server) {

        if (!delayInitialized) {
            updateDelayFromConfig();
            delayInitialized = true;
        }

        // Wenn nicht RUNNING → Bossbar verstecken und raus
        if (state != State.RUNNING) {
            if (RandomizerConfig.COMMON.enableBossbar.get()) {
                bossBar.setVisible(false);
                bossBar.setProgress(0f);
                bossBar.removeAllPlayers();
            }
            return;
        }

        // ========== RUNNING ==========

        // Bossbar: TickCounter zeigt Fortschritt
        if (RandomizerConfig.COMMON.enableBossbar.get()) {
            bossBar.removeAllPlayers();
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (!bossbarHiddenPlayers.contains(p.getUUID())) {
                    bossBar.addPlayer(p);
                }
            }

            float progress = tickCounter / (float) delayTicks;
            if (progress < 0f) progress = 0f;
            if (progress > 1f) progress = 1f;

            bossBar.setVisible(true);
            bossBar.setProgress(progress);

            if (progress < 0.5f) bossBar.setColor(BossEvent.BossBarColor.GREEN);
            else if (progress < 0.8f) bossBar.setColor(BossEvent.BossBarColor.YELLOW);
            else bossBar.setColor(BossEvent.BossBarColor.RED);
        }

        // ========== TickCounter hochzählen ==========
        tickCounter++;
        elapsedTicks++;


        // Noch nicht genug Zeit für ein Event
        if (tickCounter < delayTicks) return;

        // Genau jetzt: Event!
        tickCounter = 0;

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;

        ServerPlayer target = players.get(random.nextInt(players.size()));
        if (target == null || !target.isAlive() || target.isRemoved()) return;

        RandomPools.EventType type = pools.chooseEventType(random);
        if (type == null) return;

        String resultText = null;
        try {
            switch (type) {
                case EFFECT -> resultText = pools.applyRandomEffect(target, random);
                case MOB -> resultText = pools.spawnRandomMob(target, random);
                case ITEM -> resultText = pools.giveRandomItem(target, random);
            }
        } catch (Exception e) {
            LOGGER.error("Randomizer event failed: {}", e.getMessage(), e);
            return;
        }

        if (resultText == null || resultText.isEmpty()) return;
        final String text = resultText;

        Component icon = switch (type) {
            case ITEM -> Component.literal("[Item] ").withStyle(ChatFormatting.GOLD);
            case EFFECT -> Component.literal("[Effect] ").withStyle(ChatFormatting.LIGHT_PURPLE);
            case MOB -> Component.literal("[Mob] ").withStyle(ChatFormatting.RED);
            default -> Component.empty();
        };

        Component msg = Component.empty()
                .append(Randomizer.randomizerPrefix())
                .append(icon)
                .append(Component.literal("Player "))
                .append(target.getDisplayName().copy().withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" "))
                .append(Component.literal(text));

        for (ServerPlayer p : players) {
            p.sendSystemMessage(msg);
        }

        LOGGER.info(
                "Randomizer event triggered: type={}, player={}, info={}",
                type, target.getName().getString(), text
        );
    }

    public void triggerManualEventForPlayer(ServerPlayer target) {
        MinecraftServer server = ((ServerLevel) target.level()).getServer(); // <-- FIX HIER

        if (server == null) {
            LOGGER.warn("Manual event aborted: server was null for player {}", target.getName().getString());
            return;
        }

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            LOGGER.warn("Manual event aborted: no players online.");
            return;
        }

        RandomPools.EventType type = pools.chooseEventType(random);
        if (type == null) {
            LOGGER.warn("Manual event aborted: no event type available.");
            return;
        }

        String resultText = null;

        try {
            switch (type) {
                case EFFECT -> resultText = pools.applyRandomEffect(target, random);
                case MOB -> resultText = pools.spawnRandomMob(target, random);
                case ITEM -> resultText = pools.giveRandomItem(target, random);
            }
        } catch (Exception e) {
            LOGGER.error("Manual event failed: type={}, target={}",
                    type, target.getName().getString(), e);
            return;
        }

        if (resultText == null || resultText.isEmpty()) {
            LOGGER.warn("Manual event produced empty result for {}", target.getName().getString());
            return;
        }

        final String text = resultText;

        Component icon = switch (type) {
            case ITEM -> Component.literal("[Item] ").withStyle(ChatFormatting.GOLD);
            case EFFECT -> Component.literal("[Effect] ").withStyle(ChatFormatting.LIGHT_PURPLE);
            case MOB -> Component.literal("[Mob] ").withStyle(ChatFormatting.RED);
            default -> Component.empty();
        };

        Component msg = Component.empty()
                .append(Randomizer.randomizerPrefix())
                .append(Component.literal("[Manual] ").withStyle(ChatFormatting.GRAY))
                .append(icon)
                .append(Component.literal("Player "))
                .append(target.getDisplayName().copy().withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" "))
                .append(Component.literal(text));

        for (ServerPlayer p : players) {
            p.sendSystemMessage(msg);
        }

        LOGGER.info(
                "Manual randomizer event: type={}, target={}, info={}",
                type, target.getName().getString(), text
        );
    }

}



