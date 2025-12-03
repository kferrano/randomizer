package com.hardrock.randomizer.client;

import com.hardrock.randomizer.Randomizer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.bus.api.IEventBus;

@Mod(value = Randomizer.MOD_ID, dist = Dist.CLIENT)
public class RandomizerClient {

    public RandomizerClient(IEventBus modEventBus, ModContainer container) {
        // NeoForge eingebaute Config-GUI aktivieren
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
