package joni.status4discordmc.discord;

import java.awt.Color;
import java.time.Instant;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;

import joni.status4discordmc.Status4Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Logs {

	private FileConfiguration config;
	private Logger logger;
	private JDA bot;

	public Logs(JDA bot, Logger logger, FileConfiguration config) {
		this.config = config;
		this.logger = logger;
		this.bot = bot;
	}

	public void sendMessangeToLogAsEmbed(String msg, Color c) {

		String id = Status4Discord.getInstance().getConfig().getString("logs.textChannelID");

		try {
			TextChannel textChannel = bot.getTextChannelById(id);
			if (textChannel == null) {
				logger.severe("Please provide an id for the log channel!");
				return;
			}
			if (textChannel.canTalk()) {
				EmbedBuilder embed = new EmbedBuilder();
				embed.setDescription(msg);
				embed.setColor(c);
				embed.setTimestamp(Instant.now());
				textChannel.sendMessageEmbeds(embed.build()).queue();
			} else {
				logger.severe("The bot cannot talk in this channel, check your permissions!");
			}

		} catch (IllegalArgumentException e) {
			logger.severe("IllegalArgumentException: ID is invalid!");
			logger.severe("Check if logs is correct setup!");
			return;
		}
	}

	public void sendStart() {
		if (!isEnabled())
			return;
		sendMessangeToLogAsEmbed(":white_check_mark: **Server started!**", Color.GREEN);
	}

	public void sendStop() {
		if (!isEnabled())
			return;
		sendMessangeToLogAsEmbed(":x: **Server stopped!**", Color.RED);
	}

	private Boolean isEnabled() {
		return config.getBoolean("logs.enabled");
	}

}
