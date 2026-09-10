package joni.status4discord.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discord.Status4discord;
import joni.status4discord.libs.ColorTranslator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.Instant;

public class Logs {

    private final JDA bot;
    private final YamlDocument config;

    public Logs(JDA bot) {
        this.bot = bot;
        this.config = Status4discord.getConfigManager().getConfig();
    }

    /**
     * Sends a message to the configured log channel, either as embed or as
     * plain text depending on the {@code embed} flag (logs.start.embed /
     * logs.stop.embed in the config).
     */
    public void sendMessageToLog(String msg, Color c, boolean sync, boolean embed) {

        String id = config.getString("logs.textChannelID");
        if (id == null || id.isEmpty()) {
            Status4discord.LOGGER.error("Please provide an id for the log channel!");
            return;
        }

        try {
            TextChannel textChannel = bot.getTextChannelById(id);
            if (textChannel == null) {
                Status4discord.LOGGER.error("Please provide an id for the log channel!");
                return;
            }
            if (textChannel.canTalk()) {
                if (embed) {
                    EmbedBuilder e = new EmbedBuilder();
                    e.setDescription(msg);
                    e.setColor(c);
                    e.setTimestamp(Instant.now());
                    if (sync) {
                        try {
                            textChannel.sendMessageEmbeds(e.build()).complete();
                        } catch (Exception ex) {
                            Status4discord.LOGGER.error("Failed to send stop log message: {}", ex.getMessage());
                        }
                    } else {
                        textChannel.sendMessageEmbeds(e.build()).queue();
                    }
                } else {
                    if (sync) {
                        try {
                            textChannel.sendMessage(msg).complete();
                        } catch (Exception ex) {
                            Status4discord.LOGGER.error("Failed to send stop log message (msg): {}", ex.getMessage());
                        }
                    } else {
                        textChannel.sendMessage(msg).queue();
                    }
                }
            } else {
                Status4discord.LOGGER.error("The bot cannot talk in this channel, check your permissions!");
            }

        } catch (IllegalArgumentException ex) {
            Status4discord.LOGGER.error("IllegalArgumentException: ID is invalid!");
            Status4discord.LOGGER.error("Check if logs is correct setup!");
        }
    }

    public void sendStart() {
        if (!isEnabled())
            return;
        sendMessageToLog(config.getString("logs.start.message", ":white_check_mark: **Server started!**"),
                ColorTranslator.parseColor(config.getString("logs.start.color", "GREEN").toUpperCase(), Color.GREEN),
                false,
                config.getBoolean("logs.start.embed", true));
    }

    public void sendStop() {
        if (!isEnabled())
            return;
        sendMessageToLog(config.getString("logs.stop.message", ":x: **Server stopped!**"),
                ColorTranslator.parseColor(config.getString("logs.stop.color", "RED").toUpperCase(), Color.RED),
                true,
                config.getBoolean("logs.stop.embed", true));
    }

    private Boolean isEnabled() {
        return config.getBoolean("logs.enabled");
    }

}
