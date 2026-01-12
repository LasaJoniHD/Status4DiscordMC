package joni.status4discordmc.lib;

import joni.status4discordmc.Status4Discord;
import org.bukkit.plugin.java.JavaPlugin;

public class DebugLogger {

    private final JavaPlugin plugin;

    public DebugLogger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void debug(String log) {
        if (log == null)
            return;
        if (Status4Discord.getInstance().getConfigManager().getConfig().getBoolean("debug"))
            plugin.getLogger().warning(log);
    }

}
