package com.hardrock.randomizer.client;

import com.hardrock.randomizer.Randomizer;
import com.hardrock.randomizer.network.RandomizerHudPayload;
import com.hardrock.randomizer.network.RandomizerTimerPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;

@EventBusSubscriber(modid = Randomizer.MOD_ID, value = Dist.CLIENT)
public class RandomizerClientPayloadHandler {

    @SubscribeEvent
    public static void register(final RegisterClientPayloadHandlersEvent event) {
        // HUD-Steuerung
        event.register(
                RandomizerHudPayload.TYPE,
                HandlerThread.MAIN,
                RandomizerClientPayloadHandler::handleHudPayload
        );

        // NEU: Timer-Sync
        event.register(
                RandomizerTimerPayload.TYPE,
                HandlerThread.MAIN,
                RandomizerClientPayloadHandler::handleTimerPayload
        );
    }

    private static void handleHudPayload(final RandomizerHudPayload payload, final IPayloadContext context) {
        switch (payload.action()) {
            case START -> RandomizerHud.startOrResume();
            case PAUSE -> RandomizerHud.pause();
            case STOP  -> RandomizerHud.stop();
        }
    }

    private static void handleTimerPayload(final RandomizerTimerPayload payload, final IPayloadContext context) {
        RandomizerHud.syncFromServer(payload.elapsedTicks(), payload.running());
    }
}
