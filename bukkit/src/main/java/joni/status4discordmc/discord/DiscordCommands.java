package joni.status4discordmc.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discordmc.Status4Discord;
import joni.status4discordmc.libs.DebugLogger;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DiscordCommands extends ListenerAdapter {

    private final Discord discord;

    private final YamlDocument config;

    public DiscordCommands(Discord discord) {
        this.discord = discord;
        this.config = Status4Discord.getInstance().getConfigManager().getConfig();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

        // Message Stuff

        String raw = e.getMessage().getContentRaw();
        DebugLogger.log("MessageReceived: " + raw);

        if (!raw.startsWith("<@" + e.getJDA().getSelfUser().getId() + ">"))
            return;

        DebugLogger.log("I got mentioned!");

        if (!e.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR))
            return;

        DebugLogger.log("Sender got Permission.ADMINISTRATOR");

        String[] split = raw.split(" ", 2);
        if (split.length < 2)
            return;
        String arg1 = split[1];

        // Handle Commands

        if (arg1.equals("setembed")) {
            setEmbed(e.getChannel().getId());
            e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
                deleteMessage(e);
                CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
                        .execute(() -> discord.getEmbedStatus().start());
            }, null);
            DebugLogger.log("Reaction U+2705 added");
            return;
        }

        if (arg1.equals("setlogs")) {
            setLogs(e.getChannel().getId());
            e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
                deleteMessage(e);
            }, null);
            DebugLogger.log("Reaction U+2705 added");
        }
    }

    private void deleteMessage(MessageReceivedEvent e) {
        CompletableFuture
                .delayedExecutor(500, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    DebugLogger.log("deleteMessage");
                    e.getMessage().delete().queue(null, failure -> {
                        if (failure instanceof ErrorResponseException ex && ex.getErrorCode() == 10008) {
                            DebugLogger.log("Message already deleted...");
                        } else {
                            Status4Discord.getInstance().getLogger().severe("Failed to delete message: " + failure.getMessage());
                        }
                    });
                });
    }

    private void saveConfig() {
        try {
            config.save();
            config.reload();
            DebugLogger.log("Saving config");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setEmbed(String channelId) {
        DebugLogger.log("Setting Embed...");

        config.set("embed.textChannelID", channelId);
        DebugLogger.log("Updated embed.textChannelID to " + channelId);

        config.set("embedMessageID", "");
        DebugLogger.log("Updated embedMessageID to ''");

        saveConfig();
    }

    private void setLogs(String channelId) {
        DebugLogger.log("Setting Logs...");

        config.set("logs.textChannelID", channelId);
        DebugLogger.log("Updated logs.textChannelID to " + channelId);

        saveConfig();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "setembed" -> {
                setEmbed(e.getChannel().getId());
                e.reply("Embed channel set!").setEphemeral(true).queue();
            }
            case "setlogs" -> {
                setLogs(e.getChannel().getId());
                e.reply("Log channel set!").setEphemeral(true).queue();
            }
        }
    }

}
