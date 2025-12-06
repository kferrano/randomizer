package com.hardrock.randomizer.network;

import com.hardrock.randomizer.Randomizer;
import com.hardrock.randomizer.RandomizerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;


@EventBusSubscriber(modid = Randomizer.MOD_ID)
public class RandomizerNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // HUD
        registrar.playToClient(
                RandomizerHudPayload.TYPE,
                RandomizerHudPayload.STREAM_CODEC
        );

        // Timer-Sync
        registrar.playToClient(
                RandomizerTimerPayload.TYPE,
                RandomizerTimerPayload.STREAM_CODEC
        );

        // Bossbar
        registrar.playToServer(RandomizerBossbarPreferencePayload.TYPE, RandomizerBossbarPreferencePayload.STREAM_CODEC, (payload, ctx) -> {
            ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer player) {
                    RandomizerManager.INSTANCE.setBossbarPreference(player, payload.disableBossbar());
                }
            });
        });
    }

    public static void sendBossbarPreferenceToServer(boolean disableBossbar) {
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new RandomizerBossbarPreferencePayload(disableBossbar));
        }
    }


    // HUD-Steuerung (hast du schon)
    public static void sendHudActionToAllPlayers(RandomizerHudPayload.Action action) {
        PacketDistributor.sendToAllPlayers(new RandomizerHudPayload(action));
    }

    // NEU: Timer an alle Clients schicken
    public static void sendTimerToAllPlayers(long elapsedTicks, boolean running) {
        PacketDistributor.sendToAllPlayers(new RandomizerTimerPayload(elapsedTicks, running));
    }

    public static void sendHudActionToPlayer(ServerPlayer player, RandomizerHudPayload.Action action) {
        PacketDistributor.sendToPlayer(player, new RandomizerHudPayload(action));
    }

    public static void sendTimerToPlayer(ServerPlayer player, long elapsedTicks, boolean running) {
        PacketDistributor.sendToPlayer(player, new RandomizerTimerPayload(elapsedTicks, running));
    }

}
