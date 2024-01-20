package joni.status4discordmc.discord;

import java.awt.Color;
import java.time.Instant;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import joni.status4discordmc.Placeholders;
import joni.status4discordmc.Status4Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class EmbedStatus {

	private JDA bot;
	private FileConfiguration config;
	private Logger logger;
	@SuppressWarnings("unused")
	private JavaPlugin plugin;

	private Boolean updateEmbed = true;

	public EmbedStatus(JDA bot, Logger logger, FileConfiguration config, JavaPlugin plugin) {
		this.bot = bot;
		this.logger = logger;
		this.config = config;
		this.plugin = plugin;
	}

	public void start() {

		if (!isEnabled())
			return;

		String id = config.getString("embed.textChannelID");

		if (id == null) {
			logger.severe("Please provide an id for the embed channel!");
			return;
		}

		try {
			TextChannel textChannel = bot.getTextChannelById(id);
			setup(textChannel);
		} catch (IllegalArgumentException e) {
			logger.severe("IllegalArgumentException: ID is invalid!");
			logger.severe("Check if embed is correct setup!");
			return;
		}

	}

	private void setup(TextChannel textChannel) {
		String mId = Status4Discord.getInstance().getConfig().getString("embedMessageID");
		if (mId.equals("") || mId == null) {
			send(textChannel);
		}
		scheduler(textChannel);
	}

	public void update() {
		String mId = config.getString("embed.textChannelID");
		if (mId == null) {
			return;
		}
		try {
			TextChannel textChannel = bot.getTextChannelById(config.getString(mId));
			String embedMessageID = config.getString("embedMessageID");
			textChannel.editMessageEmbedsById(embedMessageID, embed().build()).queue();
		} catch (IllegalArgumentException e) {
			return;
		}
	}

	public void update(EmbedBuilder embed) {
		String mId = config.getString("embed.textChannelID");
		if (mId == null) {
			return;
		}
		try {
			TextChannel textChannel = bot.getTextChannelById(mId);
			String embedMessageID = config.getString("embedMessageID");
			textChannel.editMessageEmbedsById(embedMessageID, embed.build()).queue();
		} catch (IllegalArgumentException e) {
			return;
		}
	}

	private void scheduler(TextChannel textChannel) {
		new Thread() {
			public void run() {
				while (updateEmbed) {
					try {
						int sleep = config.getInt("embed.update");
						if (sleep < 10000) {
							logger.severe(
									"Please keep the update interval above 10000 ms to avoid problems with discord.");
							sleep = 30000;
						}
						sleep(sleep);
					} catch (InterruptedException e) {
						logger.severe("Updating the Embed failed! Thread interrupted!");
					}
					String embedMessageID = config.getString("embedMessageID");
					if (embedMessageID.equals("0")) {
						send(bot.getTextChannelById(config.getString("embed.textChannelID")));
						try {
							sleep(30000);
						} catch (InterruptedException e) {
							logger.severe("Updating the Embed failed! Thread interrupted!");
						}
					}
					if (updateEmbed) {
						textChannel.editMessageEmbedsById(embedMessageID, embed().build()).queue();
					}
				}
			}
		}.start();
	}

	private void send(TextChannel textChannel) {
		if (!textChannel.canTalk()) {
			logger.severe("Bot can't talk in specified channel!");
		}
		textChannel.sendMessageEmbeds(embed().build()).queue(msg -> {
			Status4Discord.getInstance().getConfig().set("embedMessageID", msg.getId());
			Status4Discord.getInstance().saveConfig();
			Status4Discord.getInstance().reloadConfig();
		});
	}

	private Boolean isEnabled() {
		return config.getBoolean("embed.enabled");
	}

	private EmbedBuilder embed() {
		EmbedBuilder e = new EmbedBuilder();

		e.setTitle("Online");
		e.setColor(Color.GREEN);

		e.addField("Server IP", addFormat(Placeholders.getServerIP()), true);
		e.addField("Player Count", addFormat(Placeholders.getOnlinePlayers() + " / " + Placeholders.getMaxPlayers()),
				true);
		e.addField("RAM", addFormat("Used: " + Placeholders.getUsedMemoryPercentage() + " % ("
				+ Placeholders.getUsedMemory() + " mb / " + Placeholders.getMaxMemory() + " mb)"), false);
		e.addField("Uptime", addFormat(Placeholders.getUptime()), true);
		e.addField("TPS", addFormat(String.valueOf(Placeholders.getTPS(0))), true);
		e.addField("CPU", addFormat(Placeholders.getCPU() + " %"), true);

		e.setTimestamp(Instant.now());
		return e;
	}

	public void reset() {
		config.set("embedMessageID", "0");
	}

	public void stop() {

		updateEmbed = false;

		EmbedBuilder e = new EmbedBuilder();
		e.setTitle("Offline");
		e.setColor(Color.RED);

		e.addField("Server IP", addFormat(Placeholders.getServerIP()), true);

		e.setTimestamp(Instant.now());

		update(e);
	}

	private String addFormat(String string) {
		return "`" + string + "`";
	}

}
