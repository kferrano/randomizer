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
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RandomizerManager randomizerManager = new RandomizerManager();
    public static RandomizerManager MANAGER;


    public Randomizer(IEventBus modEventBus, ModContainer modContainer) {
        MANAGER = new RandomizerManager();

        LOGGER.info("Randomizer mod initializing.");
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        modContainer.registerConfig(ModConfig.Type.COMMON, RandomizerConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, RandomizerConfig.CLIENT_SPEC);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onServerTick(ServerTickEvent.Post event) {
        // ServerTickEvent hat in 1.21.x getServer()
        MinecraftServer server = event.getServer();
        randomizerManager.onServerTick(server);

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
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("randomizer")
                .requires(source -> source.hasPermission(2)) // OP-Level 2
                .then(Commands.literal("start")
                        .executes(ctx -> {
                            RandomizerManager.State state = randomizerManager.getState();
                            if (state == RandomizerManager.State.RUNNING) {
                                ctx.getSource().sendFailure(
                                        Component.empty()
                                                .append(Randomizer.randomizerPrefix())
                                                .append(Component.literal("is already running."))
                                );
                                return 0;
                            }
                            randomizerManager.start();
                            RandomizerNetwork.sendHudActionToAllPlayers(RandomizerHudPayload.Action.START);
                            broadcastToAll(ctx.getSource(), "Randomizer started.");
                            return 1;
                        }))
                .then(Commands.literal("pause")
                        .executes(ctx -> {
                            RandomizerManager.State state = randomizerManager.getState();
                            if (state != RandomizerManager.State.RUNNING) {
                                ctx.getSource().sendFailure(
                                        Component.empty()
                                                .append(Randomizer.randomizerPrefix())
                                                .append(Component.literal("cannot be paused because it is not running."))
                                );
                                return 0;
                            }
                            randomizerManager.pause();
                            RandomizerNetwork.sendHudActionToAllPlayers(RandomizerHudPayload.Action.PAUSE);
                            broadcastToAll(ctx.getSource(), "Randomizer paused.");
                            return 1;
                        }))

                .then(Commands.literal("stop")
                        .executes(ctx -> {
                            RandomizerManager.State state = randomizerManager.getState();
                            if (state == RandomizerManager.State.STOPPED) {
                                ctx.getSource().sendFailure(
                                        Component.empty()
                                                .append(Randomizer.randomizerPrefix())
                                                .append(Component.literal("is already stopped."))
                                );
                                return 0;
                            }
                            randomizerManager.stop();
                            RandomizerNetwork.sendHudActionToAllPlayers(RandomizerHudPayload.Action.STOP);
                            broadcastToAll(ctx.getSource(), "Randomizer stopped and timer reset.");

                            return 1;
                        }))
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            String status = randomizerManager.getStatusString();
                            ctx.getSource().sendSuccess(
                                    () -> Component.empty()
                                            .append(Randomizer.randomizerPrefix())
                                            .append(Component.literal(status)),
                                    false
                            );

                            return 1;
                        }))

                .then(Commands.literal("manual")
                        // /randomizer manual -> self
                        .executes(ctx -> {
                            ServerPlayer self = ctx.getSource().getPlayerOrException();
                            Randomizer.MANAGER.triggerManualEventForPlayer(self);

                            ctx.getSource().sendSuccess(
                                    () -> Component.empty()
                                            .append(Randomizer.randomizerPrefix())
                                            .append(Component.literal("Triggered a manual event for you.")),
                                    false
                            );
                            return 1;
                        })
                        // /randomizer manual <targets> -> Name, @a, @p, etc.
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> {
                                    Collection<ServerPlayer> targets;
                                    try {
                                        targets = EntityArgument.getPlayers(ctx, "targets");
                                    } catch (Exception e) {
                                        ctx.getSource().sendFailure(
                                                Component.empty()
                                                        .append(Randomizer.randomizerPrefix())
                                                        .append(Component.literal("No valid player targets found."))
                                        );
                                        return 0;
                                    }

                                    if (targets.isEmpty()) {
                                        ctx.getSource().sendFailure(
                                                Component.empty()
                                                        .append(Randomizer.randomizerPrefix())
                                                        .append(Component.literal("No players matched the given selector."))
                                        );
                                        return 0;
                                    }

                                    int count = 0;
                                    for (ServerPlayer p : targets) {
                                        try {
                                            Randomizer.MANAGER.triggerManualEventForPlayer(p);
                                            count++;
                                        } catch (Exception e) {
                                            LOGGER.error("Manual event failed for player {}", p.getName().getString(), e);
                                            ctx.getSource().sendFailure(
                                                    Component.empty()
                                                            .append(Randomizer.randomizerPrefix())
                                                            .append(Component.literal("Manual event failed for " + p.getName().getString()))
                                            );
                                        }
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
}

