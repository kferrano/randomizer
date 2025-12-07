package com.hardrock.randomizer.client;

import com.hardrock.randomizer.Randomizer;
import com.hardrock.randomizer.RandomizerConfig;
import com.hardrock.randomizer.network.RandomizerNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.config.ModConfig;

@EventBusSubscriber(modid = Randomizer.MOD_ID, value = Dist.CLIENT)
public class RandomizerClientConfigHandler {

    @SubscribeEvent
    public static void onClientConfigReloading(final ModConfigEvent.Reloading event) {
        ModConfig config = event.getConfig();

        // Nur unsere CLIENT-Config interessiert uns
        if (config.getType() != ModConfig.Type.CLIENT) {
            return;
        }
        if (config.getSpec() != RandomizerConfig.CLIENT_SPEC) {
            return;
        }

        // Neue Bossbar-Preference aus der gerade gespeicherten Config auslesen
        boolean disableBossbar = RandomizerConfig.CLIENT.disableBossbar.get();

        // Und erneut an den Server schicken
        RandomizerNetwork.sendBossbarPreferenceToServer(disableBossbar);
    }
}
