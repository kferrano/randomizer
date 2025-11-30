package com.hardrock.randomizer.client;

import com.hardrock.randomizer.Randomizer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Randomizer.MOD_ID, value = Dist.CLIENT)
public class RandomizerClientConnectionHandler {

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        // Reset HUD + Timer komplett
        RandomizerHud.stop();
    }
}