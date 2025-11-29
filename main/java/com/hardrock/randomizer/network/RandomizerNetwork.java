package com.hardrock.randomizer.network;

import com.hardrock.randomizer.Randomizer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Randomizer.MOD_ID)
public class RandomizerNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // Schon vorhanden:
        registrar.playToClient(
                RandomizerHudPayload.TYPE,
                RandomizerHudPayload.STREAM_CODEC
        );

        // NEU: Timer-Sync
        registrar.playToClient(
                RandomizerTimerPayload.TYPE,
                RandomizerTimerPayload.STREAM_CODEC
        );
    }

    // HUD-Steuerung (hast du schon)
    public static void sendHudActionToAllPlayers(RandomizerHudPayload.Action action) {
        PacketDistributor.sendToAllPlayers(new RandomizerHudPayload(action));
    }

    // NEU: Timer an alle Clients schicken
    public static void sendTimerToAllPlayers(long elapsedTicks, boolean running) {
        PacketDistributor.sendToAllPlayers(new RandomizerTimerPayload(elapsedTicks, running));
    }
}
