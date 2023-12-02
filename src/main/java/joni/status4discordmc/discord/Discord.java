package joni.status4discordmc.discord;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;

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

		try {
			bot = builder.build();
		} catch (InvalidTokenException e) {
			plugin.getLogger().severe("Invalid Token Exception: The provided token is invalid!");
			Bukkit.getPluginManager().disablePlugin(plugin);
			return;
		}

		try {
			bot.awaitReady();
		} catch (InterruptedException e) {
			plugin.getLogger().severe("JDA could not initialize!");
			return;
		}

		plugin.getLogger().info("Logged in as " + bot.getSelfUser().getName());

		bot.addEventListener(new Commands(plugin, plugin.getLogger(), config));

		if (!isInGuilds()) {
			plugin.getLogger().info("The Discord bot is not on any guild! Maybe you would like to invite him:");
			plugin.getLogger().info(getInvitationLink());
		}

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

	public void setFileConfiguration(FileConfiguration conf) {
		if (conf == null)
			return;
		config = conf;
	}

	public void stop() {
		if (bot == null)
			return;

		stopModules();

		bot.shutdown();

	}

	public Boolean isInGuilds() {
		if (bot == null)
			return null;

		if (bot.getGuilds().size() > 0)
			return true;

		return false;
	}

	public String getInvitationLink() {
		if (bot == null)
			return null;

		return bot.getInviteUrl(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.MESSAGE_MANAGE,
				Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY,
				Permission.MESSAGE_ADD_REACTION);
	}

}
