package com.hardrock.randomizer.client;

import com.hardrock.randomizer.Randomizer;
import com.hardrock.randomizer.RandomizerConfig;
import com.hardrock.randomizer.network.RandomizerNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Randomizer.MOD_ID, value = Dist.CLIENT)
public class RandomizerClientConnectionHandler {

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        boolean disable = RandomizerConfig.CLIENT.disableBossbar.get();
        RandomizerNetwork.sendBossbarPreferenceToServer(disable);
    }


    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        // Reset HUD + Timer komplett
        RandomizerHud.stop();
    }
}