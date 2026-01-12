package joni.status4discordmc.discord;

import dev.dejvokep.boostedyaml.YamlDocument;
import joni.status4discordmc.Status4Discord;
import joni.status4discordmc.lib.DebugLogger;
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

    private final DebugLogger dlog;

    public DiscordCommands(JavaPlugin plugin, Discord discord, DebugLogger dlog) {
        this.plugin = plugin;
        this.discord = discord;
        this.dlog = dlog;
        this.config = Status4Discord.getInstance().getConfigManager().getConfig();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

        String raw = e.getMessage().getContentRaw();
        dlog.debug("MessageReceived: " + raw);

        if (!raw.startsWith("<@" + e.getJDA().getSelfUser().getId() + ">"))
            return;

        dlog.debug("Message starts with mention of bot");

        if (!e.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR))
            return;

        dlog.debug("Member has permission Permission.ADMINISTRATOR");

        String arg1 = raw.split(" ", 2)[1];

        if (arg1.equals("setembed")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                dlog.debug("Doing setembed cmd");

                config.set("embed.textChannelID", e.getChannel().getId());
                dlog.debug("Updated embed.textChannelID to " + e.getChannel().getId());

                config.set("embedMessageID", "");
                dlog.debug("embedMessageID set to ''");

                try {
                    config.save();
                    config.reload();
                    dlog.debug("Saving config");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
                    deleteMessage(e);
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

                        @Override
                        public void run() {
                            discord.getEmbedStatus().start();
                        }
                    }, 40);

                }, null);
                dlog.debug("Reaction U+2705 added");
            });
            return;
        }

        if (arg1.equals("setlogs")) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                dlog.debug("Doing setlogs");

                config.set("logs.textChannelID", e.getChannel().getId());
                dlog.debug("Updated logs.textChannelID to " + e.getChannel().getId());

                try {
                    config.save();
                    config.reload();
                    dlog.debug("Saving config");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
                    deleteMessage(e);
                }, null);
                dlog.debug("Reaction U+2705 added");
            });
        }
    }

    private void deleteMessage(MessageReceivedEvent e) {
        CompletableFuture
                .delayedExecutor(500, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    dlog.debug("deleteMessage");
                    e.getMessage().delete().queue(null, failure -> {
                        if (failure instanceof ErrorResponseException ex && ex.getErrorCode() == 10008) {
                            dlog.debug("Message already deleted");
                        } else {
                            failure.printStackTrace();
                        }
                    });
                });
    }

}
