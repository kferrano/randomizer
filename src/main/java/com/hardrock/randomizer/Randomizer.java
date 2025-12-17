package com.hardrock.randomizer;

import com.hardrock.randomizer.network.RandomizerHudPayload;
import com.hardrock.randomizer.network.RandomizerNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;


@Mod(Randomizer.MOD_ID)
public class Randomizer {

    public static final String MOD_ID = "randomizer";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static RandomizerManager MANAGER;
    private static final String CONFIG_DIR = "randomizer";

    public Randomizer(IEventBus modEventBus, ModContainer modContainer) {
        RandomizerManager manager = RandomizerManager.INSTANCE;


        LOGGER.info("Randomizer mod initializing.");
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        modContainer.registerConfig(ModConfig.Type.COMMON, RandomizerConfig.COMMON_SPEC, CONFIG_DIR + "/common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, RandomizerConfig.CLIENT_SPEC, CONFIG_DIR + "/client.toml");
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
    }

    private void onServerTick(ServerTickEvent.Post event) {
        // ServerTickEvent hat in 1.21.x getServer()
        RandomizerManager.INSTANCE.onServerTick(event.getServer());
    }

    public static ResourceLocation rl(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    private static void broadcastToAll(CommandSourceStack source, String message) {
        var server = source.getServer();
        if (server == null) {
            return;
        }

        Component full = Component.empty()
                .append(Randomizer.randomizerPrefix())
                .append(Component.literal(message));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(full);
        }
    }


    private void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("randomizer")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("start")
                                .executes(ctx -> {
                                    RandomizerManager manager = RandomizerManager.INSTANCE;
                                    var state = manager.getState();
                                    if (state == RandomizerManager.State.RUNNING) {
                                        ctx.getSource().sendFailure(
                                                Component.empty()
                                                        .append(Randomizer.randomizerPrefix())
                                                        .append(Component.literal("is already running."))
                                        );
                                        return 0;
                                    }
                                    manager.start();
                                    ctx.getSource().getServer().getPlayerList().getPlayers()
                                            .forEach(p -> p.sendSystemMessage(
                                                    Component.empty()
                                                            .append(Randomizer.randomizerPrefix())
                                                            .append(Component.literal("started."))
                                            ));
                                    RandomizerNetwork.sendHudActionToAllPlayers(RandomizerHudPayload.Action.START);
                                    return 1;
                                }))

                        .then(Commands.literal("pause")
                                .executes(ctx -> {
                                    RandomizerManager manager = RandomizerManager.INSTANCE;
                                    manager.pause();
                                    ctx.getSource().getServer().getPlayerList().getPlayers()
                                            .forEach(p -> p.sendSystemMessage(
                                                    Component.empty()
                                                            .append(Randomizer.randomizerPrefix())
                                                            .append(Component.literal("paused."))
                                            ));
                                    RandomizerNetwork.sendHudActionToAllPlayers(RandomizerHudPayload.Action.PAUSE);
                                    return 1;
                                }))

                        .then(Commands.literal("stop")
                                .executes(ctx -> {
                                    RandomizerManager manager = RandomizerManager.INSTANCE;
                                    manager.stop();
                                    ctx.getSource().getServer().getPlayerList().getPlayers()
                                            .forEach(p -> p.sendSystemMessage(
                                                    Component.empty()
                                                            .append(Randomizer.randomizerPrefix())
                                                            .append(Component.literal("stopped."))
                                            ));
                                    RandomizerNetwork.sendHudActionToAllPlayers(RandomizerHudPayload.Action.STOP);
                                    return 1;
                                }))

                        .then(Commands.literal("manual")
                                .executes(ctx -> {
                                    ServerPlayer self = ctx.getSource().getPlayerOrException();
                                    RandomizerManager.INSTANCE.triggerManualEventForPlayer(self);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.empty()
                                                    .append(Randomizer.randomizerPrefix())
                                                    .append(Component.literal("Triggered a manual event for you.")),
                                            false
                                    );
                                    return 1;
                                })
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ctx -> {
                                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
                                            int count = 0;
                                            for (ServerPlayer p : targets) {
                                                RandomizerManager.INSTANCE.triggerManualEventForPlayer(p);
                                                count++;
                                            }
                                            final int resultCount = count;
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.empty()
                                                            .append(Randomizer.randomizerPrefix())
                                                            .append(Component.literal("Triggered manual events for " + resultCount + " player(s).")),
                                                    false
                                            );
                                            return resultCount;
                                        })
                                )
                        )
        );
    }

    public static Component randomizerPrefix() {
        return Component.literal("[Randomizer] ")
                .withStyle(ChatFormatting.GOLD);
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Gleicher Manager wie bei Tick/Commands
        RandomizerManager manager = RandomizerManager.INSTANCE;
        RandomizerManager.State state = manager.getState();

        if (manager == null) {
            RandomizerNetwork.sendHudActionToPlayer(player, RandomizerHudPayload.Action.STOP);
            return;
        }
        // Wenn Randomizer nicht läuft → HUD aus
        if (state == RandomizerManager.State.STOPPED) {
            RandomizerNetwork.sendHudActionToPlayer(player, RandomizerHudPayload.Action.STOP);
            return;
        }

        boolean running = (state == RandomizerManager.State.RUNNING);

        // 1) HUD-Zustand setzen (Start / Pause)
        RandomizerNetwork.sendHudActionToPlayer(
                player,
                running ? RandomizerHudPayload.Action.START : RandomizerHudPayload.Action.PAUSE
        );

        // 2) Timer-Daten schicken – gleiche Logik wie dein Timer-Payload: elapsedTicks + running
        long elapsedTicks = manager.getElapsedTicks();
        RandomizerNetwork.sendTimerToPlayer(player, elapsedTicks, running);
    }
}

