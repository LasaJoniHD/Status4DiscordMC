package joni.lib;

import org.bukkit.plugin.java.JavaPlugin;

public class DebugLogger {

	private JavaPlugin plugin;

	public DebugLogger(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void debug(String log) {
		if (log == null)
			return;
		if (plugin.getConfig().getBoolean("debug"))
			plugin.getLogger().warning(log);
	}

}
