package joni.status4discordmc.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discordmc.Placeholders;
import joni.status4discordmc.Status4Discord;
import joni.status4discordmc.libs.ColorTranslator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class EmbedStatus {

    private final JDA bot;
    private final Logger logger;
    private final YamlDocument config;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;

    public EmbedStatus(JDA bot, Logger logger, ScheduledExecutorService scheduler) {
        this.bot = bot;
        this.logger = logger;
        this.config = Status4Discord.getInstance().getConfigManager().getConfig();
        this.scheduler = scheduler;
    }

    public void start() {
        if (!config.getBoolean("embed.enabled"))
            return;

        String id = config.getString("embed.textChannelID");

        if (id == null || id.isEmpty()) {
            logger.severe("Please provide an id for the embed channel!");
            return;
        }

        try {
            TextChannel textChannel = bot.getTextChannelById(id);
            if (textChannel == null) {
                logger.severe("Please provide an id for the embed channel!");
                return;
            }
            String mId = config.getString("embedMessageID");
            if (mId == null || mId.isEmpty()) {
                send(textChannel);
            } else {
                schedule(textChannel);
            }
        } catch (IllegalArgumentException e) {
            logger.severe("IllegalArgumentException: ID is invalid!");
            logger.severe("Check if embed is correct setup!");
        }

    }

    private void schedule(TextChannel textChannel) {
        int delay = config.getInt("embed.update", 30);
        if (delay < 10) {
            logger.severe(
                    "Please keep the update interval above 10s to avoid problems with Discord."
            );
            delay = 30;
        }

        // Cancel a previously scheduled task so repeated start() calls don't stack updaters
        if (task != null)
            task.cancel(false);

        task = scheduler.scheduleWithFixedDelay(() -> {
            try {
                String embedMessageID = config.getString("embedMessageID");

                if (embedMessageID == null || embedMessageID.isEmpty()) {
                    TextChannel t = bot.getTextChannelById(config.getString("embed.textChannelID"));
                    if (t == null) return;
                    send(t);
                    return;
                }

                textChannel
                        .editMessageEmbedsById(embedMessageID, embed().build())
                        .queue(null, this::handleUpdateFailure);

            } catch (Exception e) {
                logger.severe("Updating the Embed failed: " + e.getMessage());
            }

        }, 1, delay, TimeUnit.SECONDS);
    }

    private void handleUpdateFailure(Throwable failure) {
        // 10008: Unknown Message -> the stored embed message was deleted on Discord's side
        if (failure instanceof ErrorResponseException ex && ex.getErrorCode() == 10008) {

            logger.warning(
                    "The status embed message no longer exists (deleted?). Posting a new one..."
            );

            config.set("embedMessageID", "");
            try {
                config.save();
                config.reload();
            } catch (IOException e) {
                logger.severe("Failed to clear embedMessageID: " + e.getMessage());
            }

            TextChannel t = bot.getTextChannelById(config.getString("embed.textChannelID"));
            if (t != null)
                send(t);
            return;
        }

        logger.severe("Updating the Embed failed: " + failure.getMessage());
    }

    private void send(TextChannel textChannel) {
        if (!textChannel.canTalk()) {
            logger.severe("Bot can't talk in specified channel!");
        }
        textChannel.sendMessageEmbeds(embed().build()).queue(msg -> {
            config.set("embedMessageID", msg.getId());
            try {
                config.save();
                config.reload();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            schedule(textChannel);
        });
    }

    private EmbedBuilder embed() {
        return buildEmbed("embed.online");
    }

    private EmbedBuilder buildEmbed(String path) {
        EmbedBuilder e = new EmbedBuilder();

        // Title
        e.setTitle(config.getString(path + ".title", "Status"));

        // Color
        try {
            e.setColor(ColorTranslator.parseColor(
                    config.getString(path + ".color", "GREEN").toUpperCase()
                    , Color.GREEN));
        } catch (IllegalArgumentException ignored) {
            e.setColor(Color.GREEN);
        }

        // Fields
        List<Map<?, ?>> fields = config.getMapList(path + ".fields");
        for (Map<?, ?> field : fields) {
            String name = String.valueOf(field.get("name"));
            String value = String.valueOf(field.get("value"));
            boolean inline = Boolean.parseBoolean(String.valueOf(field.get("inline")));

            e.addField(
                    Placeholders.set(name),
                    Placeholders.set(value),
                    inline
            );
        }

        // Footer (optional)
        String footer = config.getString(path + ".footer.text", null);
        if (footer != null && !footer.isEmpty()) {
            e.setFooter(Placeholders.set(footer));
        }

        // Timestamp (global toggle)
        if (config.getBoolean("embed.timestamp", true)) {
            e.setTimestamp(Instant.now());
        }

        return e;
    }


    public void stop() {
        if (!config.getBoolean("embed.enabled"))
            return;

        if (task != null)
            task.cancel(false);

        EmbedBuilder e = buildEmbed("embed.offline");

        String mId = config.getString("embed.textChannelID");
        if (mId == null || mId.isEmpty()) return;
        try {
            TextChannel textChannel = bot.getTextChannelById(mId);
            if (textChannel == null) return;
            String embedMessageID = config.getString("embedMessageID");
            if (embedMessageID == null || embedMessageID.isEmpty()) return;
            textChannel.editMessageEmbedsById(embedMessageID, e.build()).complete();
        } catch (ErrorResponseException err) {
            // Ignore 10008 (message already gone) and similar REST failures on shutdown
        } catch (Exception ignored) {
        }
    }

}
