package joni.status4discordmc.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discordmc.Placeholders;
import joni.status4discordmc.Status4Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class EmbedStatus {

    private final JDA bot;
    private final Logger logger;
    private final YamlDocument config;
    private final ScheduledExecutorService scheduler;

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

        if (id == null || id.isEmpty() || id.equals("0")) {
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
            if (mId == null || mId.isEmpty() || mId.equals("0")) {
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
        int delay = config.getInt("embed.update", 30_000);
        if (delay < 10_000) {
            logger.severe(
                    "Please keep the update interval above 10000 ms to avoid problems with Discord."
            );
            delay = 30_000;
        }

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                String embedMessageID = config.getString("embedMessageID");

                if (embedMessageID.equalsIgnoreCase("0")) {
                    TextChannel t = bot.getTextChannelById(config.getString("embed.textChannelID"));
                    if (t == null) return;
                    send(t);
                    return;
                }

                textChannel
                        .editMessageEmbedsById(embedMessageID, embed().build())
                        .queue();

            } catch (Exception e) {
                logger.severe("Updating the Embed failed: " + e.getMessage());
            }

        }, 10, delay, TimeUnit.MILLISECONDS);
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

    public void stop() {
        EmbedBuilder e = new EmbedBuilder();
        e.setTitle("Offline");
        e.setColor(Color.RED);

        e.addField("Server IP", addFormat(Placeholders.getServerIP()), true);

        e.setTimestamp(Instant.now());

        String mId = config.getString("embed.textChannelID");
        if (mId == null || mId.isEmpty() || mId.equals("0")) return;
        try {
            TextChannel textChannel = bot.getTextChannelById(mId);
            if (textChannel == null) return;
            String embedMessageID = config.getString("embedMessageID");
            textChannel.editMessageEmbedsById(embedMessageID, e.build()).queue();
        } catch (IllegalArgumentException ignored) {
        }
    }

    private String addFormat(String string) {
        return "`" + string + "`";
    }

}
