package joni.status4discord.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discord.Placeholders;
import joni.status4discord.Status4discord;
import joni.status4discord.libs.ColorTranslator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmbedStatus {

    private final JDA bot;
    private final YamlDocument config;
    private final ScheduledExecutorService scheduler;

    public EmbedStatus(JDA bot, ScheduledExecutorService scheduler) {
        this.bot = bot;
        this.config = Status4discord.getConfigManager().getConfig();
        this.scheduler = scheduler;
    }

    public void start() {
        if (!config.getBoolean("embed.enabled"))
            return;

        String id = config.getString("embed.textChannelID");

        if (id == null || id.isEmpty()) {
            Status4discord.LOGGER.error("Please provide an id for the embed channel!");
            return;
        }

        try {
            TextChannel textChannel = bot.getTextChannelById(id);
            if (textChannel == null) {
                Status4discord.LOGGER.error("Please provide an id for the embed channel!");
                return;
            }
            String mId = config.getString("embedMessageID");
            if (mId == null || mId.isEmpty()) {
                send(textChannel);
            } else {
                schedule(textChannel);
            }
        } catch (IllegalArgumentException e) {
            Status4discord.LOGGER.error("IllegalArgumentException: ID is invalid!");
            Status4discord.LOGGER.error("Check if embed is correct setup!");
        }

    }

    private void schedule(TextChannel textChannel) {
        int delay = config.getInt("embed.update", 30);
        if (delay < 10) {
            Status4discord.LOGGER.error(
                    "Please keep the update interval above 10s to avoid problems with Discord."
            );
            delay = 30;
        }

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                String embedMessageID = config.getString("embedMessageID");

                if (embedMessageID.isEmpty()) {
                    TextChannel t = bot.getTextChannelById(config.getString("embed.textChannelID"));
                    if (t == null) return;
                    send(t);
                    return;
                }

                textChannel
                        .editMessageEmbedsById(embedMessageID, embed().build())
                        .queue();

            } catch (Exception e) {
                Status4discord.LOGGER.error("Updating the Embed failed: " + e.getMessage());
            }

        }, 1, delay, TimeUnit.SECONDS);
    }


    private void send(TextChannel textChannel) {
        if (!textChannel.canTalk()) {
            Status4discord.LOGGER.error("Bot can't talk in specified channel!");
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

        EmbedBuilder e = buildEmbed("embed.offline");

        String mId = config.getString("embed.textChannelID");
        if (mId == null || mId.isEmpty()) return;
        try {
            TextChannel textChannel = bot.getTextChannelById(mId);
            if (textChannel == null) return;
            String embedMessageID = config.getString("embedMessageID");
            textChannel.editMessageEmbedsById(embedMessageID, e.build()).complete();
        } catch (Exception ignored) {
        }
    }

}
