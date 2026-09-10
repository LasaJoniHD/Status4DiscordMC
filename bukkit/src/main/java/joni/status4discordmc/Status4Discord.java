package joni.status4discordmc;

import joni.status4discordmc.config.ConfigManager;
import joni.status4discordmc.discord.Discord;
import joni.status4discordmc.libs.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;

public class Status4Discord extends JavaPlugin {

    private boolean papi = false;
    private long startUp;
    private static Status4Discord instance;

    private final String ver = getDescription().getVersion();

    private Discord discord;
    private ConfigManager configManager;

    private static boolean isPaper = false;


    @Override
    public void onLoad() {
        startUp = System.currentTimeMillis();
    }

    @Override
    public void onEnable() {
        instance = this;

        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            isPaper = true;
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Status4Discord.getInstance().getLogger()
                    .warning("TPS Placeholder is not supported on this server type yet.");
        } catch (ClassNotFoundException ignored) {
        }

        if (!isPaper()) {
            getLogger().warning("This server is not running Paper! Some features may not work properly!");
            getLogger().warning("TPS Placeholder will not work properly!");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            papi = true;

        // Config
        try {
            configManager = new ConfigManager(this);
        } catch (IOException e) {
            getLogger().severe("Failed to load/create config file! Please check if access to the file is granted!");
            getLogger().severe("Disabling plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
        }


        startDiscord();

        int pluginId = 20241;
        new Metrics(this, pluginId);

        Commands commands = new Commands(this);
        getCommand("status4discord").setExecutor(commands);
        getCommand("status4discord").setTabCompleter(commands);

        new UpdateChecker(this, "status4discord", getVersion(), List.of("paper", "spigot", "bukkit", "purpur"), null);

    }

    @Override
    public void onDisable() {
        if (discord != null)
            discord.stop();
    }

    public void startDiscord() {
        discord = new Discord(this);
        discord.start();
    }

    public Discord getDiscord() {
        return discord;
    }

    public static Status4Discord getInstance() {
        return instance;
    }

    public long getStartUp() {
        return startUp;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static boolean isPaper() {
        return isPaper;
    }

    public String getVersion() {
        return ver;
    }

    public boolean getPapi() {
        return papi;
    }

}
