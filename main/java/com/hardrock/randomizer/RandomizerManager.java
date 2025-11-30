package com.hardrock.randomizer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import com.hardrock.randomizer.network.RandomizerNetwork;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;


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


    public enum State {
        RUNNING,
        PAUSED,
        STOPPED
    }

    public RandomizerManager() {

    }

    public void updateDelayFromConfig() {
        int seconds = RandomizerConfig.COMMON.delaySeconds.get();
        if (seconds < 1) seconds = 1;
        this.delayTicks = seconds * 20;
    }

    public void start() {
        if (state == State.RUNNING) {
            LOGGER.debug("RandomizerManager.start() called, but already RUNNING.");
            return;
        }
        state = State.RUNNING;
        tickCounter = 0;
        LOGGER.info("Randomizer started. Delay: {} seconds", delayTicks / 20);
    }

    public void pause() {
        if (state == State.RUNNING) {
            state = State.PAUSED;
            LOGGER.info("Randomizer paused at {} ticks elapsed.", elapsedTicks);

        }
    }

    public void stop() {
        state = State.STOPPED;
        tickCounter = 0;
        elapsedTicks = 0;
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
        // Nur SERVER-Ticks und nur wenn wir laufen
        if (state != State.RUNNING) {
            return;
        }

        elapsedTicks++;

        if (elapsedTicks % 20 == 0) {
            boolean running = (state == State.RUNNING);
            RandomizerNetwork.sendTimerToAllPlayers(elapsedTicks, running);
        }

        tickCounter++;
        if (tickCounter < delayTicks) {
            return;
        }

        tickCounter = 0;

        // Spieler einsammeln (z.B. alle online)
        var players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        // Einen zufälligen Spieler auswählen (oder alle, wenn du willst)
        ServerPlayer target = players.get(random.nextInt(players.size()));

        RandomPools.EventType type = pools.chooseEventType(random);
        if (type == null) return;

        String resultText = null;

        switch (type) {
            case EFFECT -> resultText = pools.applyRandomEffect(target, random);
            case MOB -> resultText = pools.spawnRandomMob(target, random);
            case ITEM -> resultText = pools.giveRandomItem(target, random);
        }

        if (resultText != null && !resultText.isEmpty()) {
            String playerName = target.getName().getString();
            Component msg = Component.empty()
                    .append(Randomizer.randomizerPrefix())
                    .append(Component.literal(playerName + " " + resultText));

            for (ServerPlayer p : players) {
                p.sendSystemMessage(msg);
            }
            LOGGER.info(
                    "Randomizer event: type={}, target={}, info={}",
                    type, playerName, resultText
            );
        }
    }


    // später: hier Wheel-Packet für GUI schicken
    // RandomEventResult eventResult = new RandomEventResult(type, resultText);
}



