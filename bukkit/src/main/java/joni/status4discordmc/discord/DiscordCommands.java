package joni.status4discordmc.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discordmc.Status4Discord;
import joni.status4discordmc.libs.DebugLogger;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DiscordCommands extends ListenerAdapter {

    private final JavaPlugin plugin;
    private final Discord discord;

    private final YamlDocument config;

    public DiscordCommands(JavaPlugin plugin, Discord discord) {
        this.plugin = plugin;
        this.discord = discord;
        this.config = Status4Discord.getInstance().getConfigManager().getConfig();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

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

        if (arg1.equals("setembed")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                DebugLogger.log("Setting Embed...");

                config.set("embed.textChannelID", e.getChannel().getId());
                DebugLogger.log("Updated embed.textChannelID to " + e.getChannel().getId());

                config.set("embedMessageID", "");
                DebugLogger.log("Updated embedMessageID to ''");

                saveConfig();

                e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
                    deleteMessage(e);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> discord.getEmbedStatus().start(), 40);
                }, null);
                DebugLogger.log("Reaction U+2705 added");
            });
            return;
        }

        if (arg1.equals("setlogs")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                DebugLogger.log("Setting Logs...");

                config.set("logs.textChannelID", e.getChannel().getId());
                DebugLogger.log("Updated logs.textChannelID to " + e.getChannel().getId());

                saveConfig();

                e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
                    deleteMessage(e);
                }, null);
                DebugLogger.log("Reaction U+2705 added");
            });
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
                            failure.printStackTrace();
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

}
