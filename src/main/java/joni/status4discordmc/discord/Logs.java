package joni.status4discordmc.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discordmc.Status4Discord;
import joni.status4discordmc.lib.ColorTranslator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.Instant;
import java.util.logging.Logger;

public class Logs {

    private final Logger logger;
    private final JDA bot;
    private final YamlDocument config;

    public Logs(JDA bot, Logger logger) {
        this.logger = logger;
        this.bot = bot;
        this.config = Status4Discord.getInstance().getConfigManager().getConfig();
    }

    public void sendMessageToLogAsEmbed(String msg, Color c) {

        String id = Status4Discord.getInstance().getConfig().getString("logs.textChannelID");
        if (id == null || id.isEmpty() || id.equals("0")) {
            logger.severe("Please provide an id for the log channel!");
            return;
        }

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
        }
    }

    public void sendStart() {
        if (!isEnabled())
            return;
        sendMessageToLogAsEmbed(config.getString("logs.start.message", ":white_check_mark: **Server started!**"),
                ColorTranslator.parseColor(config.getString("logs.start.color", "GREEN").toUpperCase(), Color.GREEN));
    }

    public void sendStop() {
        if (!isEnabled())
            return;
        sendMessageToLogAsEmbed(config.getString("logs.stop.message", ":x: **Server stopped!**"),
                ColorTranslator.parseColor(config.getString("logs.stop.color", "RED").toUpperCase(), Color.RED));
    }

    private Boolean isEnabled() {
        return Status4Discord.getInstance().getConfigManager().getConfig().getBoolean("logs.enabled");
    }

}
