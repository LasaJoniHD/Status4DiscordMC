package joni.status4discordmc;

import java.awt.Color;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import joni.status4discordmc.discord.Discord;

public class Status4Discord extends JavaPlugin {

	static boolean papi;
	public static long startUp;

	@Override
	public void onLoad() {
		Discord.setup();
		startUp = System.currentTimeMillis();
	}

	@Override
	public void onEnable() {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
			papi = true;
		Discord.sendMessangeToLogAsEmbed(":white_check_mark: **Server started!**", Color.GREEN);
		Discord.statusEmbed();
	}

	@Override
	public void onDisable() {
		Discord.sendMessangeToLogAsEmbed(":x: **Server stopped!**", Color.RED);
		Discord.shutdown();
	}

	public static @NotNull Logger logger() {
		return Bukkit.getPluginManager().getPlugin("Status4Discord").getLogger();
	}

}
