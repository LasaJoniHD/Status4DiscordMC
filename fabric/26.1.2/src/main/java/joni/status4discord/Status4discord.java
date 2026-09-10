package joni.status4discord;

import joni.status4discord.config.ConfigManager;
import joni.status4discord.discord.Discord;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Status4discord implements ModInitializer {
    public static final String MOD_ID = "status4discord";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ConfigManager configManager;
    private static Discord discord;
    private static long startUp;
    private static MinecraftServer serverInstance;


    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {

            serverInstance = server;

            // Config
            try {
                configManager = new ConfigManager();
            } catch (IOException e) {
                LOGGER.error("Failed to load/create config file! Please check if access to the file is granted!");
                LOGGER.error("Disabling!");
                return;
            }

            startUp = System.currentTimeMillis();

            startDiscord();

            LOGGER.info("Status4Discord initialized successfully!");

        });

        Commands.register();

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (discord != null)
                discord.stop();
        });

    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void startDiscord() {
        discord = new Discord();
        discord.start();
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static Discord getDiscord() {
        return discord;
    }

    public static long getStartUp() {
        return startUp;
    }

    public static MinecraftServer getServerInstance() {
        return serverInstance;
    }

    public static String getVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
}
