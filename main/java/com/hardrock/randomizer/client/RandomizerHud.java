package com.hardrock.randomizer.client;

import com.hardrock.randomizer.Randomizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Randomizer.MOD_ID, value = Dist.CLIENT)
public class RandomizerHud {

    private enum HudState {
        HIDDEN,   // HUD aus, Timer auf 0
        RUNNING,  // Timer läuft
        PAUSED    // Timer steht, bleibt sichtbar
    }

    private static HudState state = HudState.HIDDEN;
    private static float elapsedTicks = 0f;

    // Wird bei /randomizer start aufgerufen
    public static void startOrResume() {
        // Nur wenn HUD ganz aus war, resetten wir auf 0
        if (state == HudState.HIDDEN) {
            elapsedTicks = 0f;
        }
        state = HudState.RUNNING;
    }

    // Wird bei /randomizer pause aufgerufen
    public static void pause() {
        if (state == HudState.RUNNING) {
            state = HudState.PAUSED; // Zeit bleibt, HUD bleibt sichtbar
        }
    }

    // Wird bei /randomizer stop aufgerufen
    public static void stop() {
        state = HudState.HIDDEN;
        elapsedTicks = 0f; // Komplett zurücksetzen
    }

    public static void syncFromServer(long serverElapsedTicks, boolean running) {
        elapsedTicks = serverElapsedTicks; // implizit cast long → float ist ok
        if (running) {
            // nur anzeigen, wenn der Timer läuft
            if (state == HudState.HIDDEN) {
                state = HudState.RUNNING;
            }
        } else {
            // wenn nicht running: Timer pausiert anzeigen
            if (elapsedTicks > 0) {
                state = HudState.PAUSED;
            } else {
                state = HudState.HIDDEN;
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Randomizer.rl("hud"),
                (GuiGraphics gfx, DeltaTracker delta) -> render(gfx, delta)
        );
    }

    private static void render(GuiGraphics gfx, DeltaTracker delta) {
        // HUD nur anzeigen, wenn nicht komplett versteckt
        if (state == HudState.HIDDEN) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Nur im RUNNING-Zustand weiter hochzählen
        if (state == HudState.RUNNING) {
            // ~20 Ticks pro Sekunde
            elapsedTicks += delta.getGameTimeDeltaTicks();
        }

        int ticks = (int) elapsedTicks;
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;

        String text = String.format("Randomizer: %02d:%02d", minutes, seconds);

        int width = gfx.guiWidth();
        int height = gfx.guiHeight();
        int x = (width - mc.font.width(text)) / 2;
        int y = height - 40;

        gfx.drawString(mc.font, text, x, y, 0xFFFFAA00, true);
    }
}
