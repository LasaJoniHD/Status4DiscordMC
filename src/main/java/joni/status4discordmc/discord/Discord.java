package joni.status4discordmc.discord;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Discord {

	private ActivityStatus activityStatus;
	private Logs logs;
	private EmbedStatus embedStatus;

	private JavaPlugin plugin;
	private FileConfiguration config;
	private JDA bot;

	public Discord(JavaPlugin plugin, FileConfiguration config) {
		this.plugin = plugin;
		this.config = config;
	}

	public void start() {

		String token = config.getString("token");

		if (token.equals("")) {
			plugin.getLogger().warning("Please setup Status4Discord and restart/reload the server!");
			return;
		}

		JDABuilder builder = JDABuilder.createDefault(token);
		builder.setActivity(Activity.customStatus("Server is starting..."));
		builder.setStatus(OnlineStatus.IDLE);

		bot = builder.build();

		try {
			bot.awaitReady();
		} catch (InterruptedException e) {
			plugin.getLogger().fine("JDA could not initialize!");
		}

		plugin.getLogger().info("Logged in as " + bot.getSelfUser().getName());

		bot.addEventListener(new Commands(plugin, plugin.getLogger(), config));

		plugin.reloadConfig();

		createModules();
		startModules();
	}

	private void startModules() {
		logs.sendStart();
		activityStatus.start();
		embedStatus.start();
	}

	private void stopModules() {
		logs.sendStop();
		activityStatus.stop();
		embedStatus.stop();
	}

	private void createModules() {
		Logger log = plugin.getLogger();
		logs = new Logs(bot, log, config);
		activityStatus = new ActivityStatus(bot, log, config);
		embedStatus = new EmbedStatus(bot, log, config, plugin);
	}

	public ActivityStatus getActivityStatus() {
		return activityStatus;
	}

	public EmbedStatus getEmbedStatus() {
		return embedStatus;
	}

	public JDA getJDA() {
		return bot;
	}

	public FileConfiguration getFileConfiguration() {
		return config;
	}

	public Logs getLogs() {
		return logs;
	}

	public void stop() {
		if (bot == null)
			return;

		stopModules();

		bot.shutdown();

	}

}
