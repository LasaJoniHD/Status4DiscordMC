package joni.status4discordmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import joni.status4discordmc.discord.Discord;

public class Status4Discord extends JavaPlugin {

	boolean papi = false;
	private long startUp;
	private static Status4Discord instance;

	private String ver = "1.0";

	private Discord discord;

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

		startDiscord();

		int pluginId = 20241;
		new Metrics(this, pluginId);

		getCommand("status4discord").setExecutor(new Commands(discord, this));
		getCommand("status4discord").setTabCompleter(new Commands(discord, this));

		updateChecker();
	}

	@Override
	public void onDisable() {
		if (discord != null)
			discord.stop();
	}

	public Discord getDiscord() {
		return discord;
	}

	public void startDiscord() {
		discord = new Discord(this, getConfig());
		discord.start();
	}

	public static Status4Discord getInstance() {
		return instance;
	}

	public long getStartUp() {
		return startUp;
	}

	public String getVersion() {
		return ver;
	}

	public boolean getPapi() {
		return papi;
	}

	private void updateChecker() {
		new Thread() {
			public void run() {
				try {
					sleep(10000);
				} catch (InterruptedException e) {
					getLogger().info("Can't check for updates? Server might be unavailable...");
				}
				try {
					StringBuilder content = new StringBuilder();

					URL url = new URL(
							"https://raw.githubusercontent.com/LasaJoniHD/Status4DiscordMC/main/assests/version.txt");
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

					String line;
					while ((line = reader.readLine()) != null) {
						content.append(line);
					}

					reader.close();

					if (content.toString().equals(ver)) {
						getLogger().info("You are running the latest version!");
						return;
					}

					getLogger().info("There is an update avaible for Status4Discord!");
					getLogger().info("https://modrinth.com/plugins/status4discord");

				} catch (IOException e) {
					getLogger().info("Can't check for updates? Server might be unavailable...");
				}
			}
		}.start();

	}

}
