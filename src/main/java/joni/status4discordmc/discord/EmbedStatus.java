package joni.status4discordmc.discord;

import java.awt.Color;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import joni.status4discordmc.Placeholders;
import joni.status4discordmc.Status4Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class EmbedStatus {

	private JDA bot;
	private FileConfiguration config;
	private Logger logger;

	private Boolean updateEmbed;

	public EmbedStatus(JDA bot, Logger logger, FileConfiguration config) {
		this.bot = bot;
		this.logger = logger;
		this.config = config;
	}

	public void start() {

		if (!isEnabled())
			return;

		TextChannel textChannel = bot.getTextChannelById(config.getString("embed.textChannelID"));
		setup(textChannel);

	}

	private void setup(TextChannel textChannel) {
		if (config.getString("embedMessageID").equals("0")) {
			send(textChannel);
		}
		scheduler(textChannel);
	}

	public void update(TextChannel textChannel) {
		String embedMessageID = config.getString("embedMessageID");
		textChannel.editMessageEmbedsById(embedMessageID, embed().build()).queue();
	}

	private void scheduler(TextChannel textChannel) {
		new Thread() {
			public void run() {
				while (updateEmbed) {
					try {
						sleep(30000);
					} catch (InterruptedException e) {
						logger.fine("Updating the Embed failed! Thread interrupted!");
					}
					String embedMessageID = config.getString("embedMessageID");
					if (embedMessageID.equals("0")) {
						send(textChannel);
						try {
							sleep(30000);
						} catch (InterruptedException e) {
							logger.fine("Updating the Embed failed! Thread interrupted!");
						}
					}
					textChannel.editMessageEmbedsById(embedMessageID, embed().build()).queue();
				}
			}
		}.start();
	}

	private void send(TextChannel textChannel) {
		if (!textChannel.canTalk()) {
			logger.fine("Bot can't talk in specified channel!");
		}
		textChannel.sendMessageEmbeds(embed().build()).queue(msg -> {
			config.set("embedMessageID", msg.getId());
		});
	}

	private Boolean isEnabled() {
		return config.getBoolean("embed.enabled");
	}

	private EmbedBuilder embed() {
		EmbedBuilder e = new EmbedBuilder();

		e.setTitle("Online");

		e.addField("Server IP", Bukkit.getIp(), true);
		e.addField("Player Count", Integer.toString(Bukkit.getOnlinePlayers().size()), true);
		e.addField("RAM",
				"Free: " + Runtime.getRuntime().freeMemory() / 1024L / 1024L + "("
						+ (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory() / 1024L / 1024L)
						+ "mb / " + String.valueOf(Runtime.getRuntime().maxMemory() / 1024L / 1024L) + " mb)",
				false);
		e.addField("Uptime", String.valueOf(System.currentTimeMillis() - Status4Discord.startUp), true);
		e.addField("TPS", String.valueOf(Math.round(Bukkit.getServer().getTPS()[0] * 10.0) / 10.0), true);
		e.addField("CPU", String.valueOf(Placeholders.getCPU()), false);
		e.addField("Players", Bukkit.getOnlinePlayers().toString(), true);

		e.setColor(Color.GREEN);
		return e;
	}

	public void reset() {
		config.set("embedMessageID", "0");
	}

	public void stop() {
		updateEmbed = false;
	}

}
