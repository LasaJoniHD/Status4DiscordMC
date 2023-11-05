package joni.status4discordmc;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import joni.status4discordmc.discord.Discord;

public class Status4Discord extends JavaPlugin {

	static boolean papi;
	public static long startUp;

	private static Discord discord;

	@Override
	public void onLoad() {
		startUp = System.currentTimeMillis();
		discord = new Discord(this, getConfig());
		discord.start();
	}

	@Override
	public void onEnable() {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
			papi = true;
	}

	@Override
	public void onDisable() {
		discord.stop();
	}

	public static Discord getDiscord() {
		return discord;
	}

}
