package joni.status4discordmc;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import joni.status4discordmc.discord.Discord;

public class Status4Discord extends JavaPlugin {

	static boolean papi;
	public static long startUp;
	public static Status4Discord instance;

	private static Discord discord;

	@Override
	public void onLoad() {
		startUp = System.currentTimeMillis();
	}

	@Override
	public void onEnable() {
		instance = this;
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
			papi = true;
		saveDefaultConfig();
		discord = new Discord(this, getConfig());
		discord.start();

		int pluginId = 20241;
		new Metrics(this, pluginId);

		getCommand("status4mc").setExecutor(new Commands(discord));
	}

	@Override
	public void onDisable() {
		discord.stop();
	}

	public static Discord getDiscord() {
		return discord;
	}

	public static Status4Discord getInstance() {
		return instance;
	}

}
