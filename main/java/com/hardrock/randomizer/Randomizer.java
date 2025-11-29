package com.hardrock.randomizer;

import com.hardrock.randomizer.client.RandomizerHud;
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


@Mod(Randomizer.MOD_ID)
public class Randomizer {

    public static final String MOD_ID = "randomizer";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RandomizerManager randomizerManager = new RandomizerManager();


    public Randomizer(IEventBus modEventBus, ModContainer modContainer) {

        LOGGER.info("Randomizer mod initializing.");
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        modContainer.registerConfig(ModConfig.Type.COMMON, RandomizerConfig.COMMON_SPEC);
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


    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("randomizer")
                .requires(source -> source.hasPermission(2)) // OP-Level 2
                .then(Commands.literal("start")
                        .executes(ctx -> {
                            randomizerManager.start();
                            RandomizerHud.startOrResume();
                            ctx.getSource().sendSuccess(
                                    () -> Component.empty()
                                            .append(Randomizer.randomizerPrefix())
                                            .append(Component.literal("started.")),
                                    true
                            );
                            return 1;
                        }))
                .then(Commands.literal("pause")
                        .executes(ctx -> {
                            randomizerManager.pause();
                            RandomizerHud.pause(); // 🔹 HUD einfrieren
                            ctx.getSource().sendSuccess(
                                    () -> Component.empty()
                                            .append(Randomizer.randomizerPrefix())
                                            .append(Component.literal("paused.")),
                                    true
                            );
                            return 1;
                        }))
                .then(Commands.literal("stop")
                        .executes(ctx -> {
                            randomizerManager.stop();
                            RandomizerHud.stop();
                            ctx.getSource().sendSuccess(
                                    () -> Component.empty()
                                            .append(Randomizer.randomizerPrefix())
                                            .append(Component.literal(" stopped and timer reset.")),
                                    true
                            );
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
        );

    }

    public static Component randomizerPrefix() {
        return Component.literal("[Randomizer] ")
                .withStyle(ChatFormatting.GOLD);
    }
}

