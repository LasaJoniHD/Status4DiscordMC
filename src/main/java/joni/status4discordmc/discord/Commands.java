package joni.status4discordmc.discord;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import joni.lib.DebugLogger;
import joni.status4discordmc.Status4Discord;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {

	@SuppressWarnings("unused")
	private FileConfiguration config;
	private JavaPlugin plugin;
	private Logger logger;
	private Discord discord;

	private DebugLogger dlog;

	public Commands(JavaPlugin plugin, Logger logger, FileConfiguration config, Discord discord) {
		this.config = config;
		this.plugin = plugin;
		this.logger = logger;
		this.discord = discord;
		dlog = new DebugLogger(plugin);
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
			Bukkit.getScheduler().runTask(plugin, new Runnable() {

				@Override
				public void run() {
					dlog.debug("Arg1 is setembed");
					Status4Discord.getInstance().getConfig().set("embed.textChannelID", e.getChannel().getId());
					dlog.debug("Updated embed.textChannelID to " + e.getChannel().getId());
					Status4Discord.getInstance().getConfig().set("embedMessageID", "");
					dlog.debug("embedMessageID is nothing");
					Status4Discord.getInstance().getConfig().options().copyDefaults(true);
					Status4Discord.getInstance().saveConfig();
					dlog.debug("Saving config");
					Status4Discord.getInstance().reloadConfig();
					dlog.debug("Reloading config");
					e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
						deleteMessage(e);
						Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

							@Override
							public void run() {
								discord.getEmbedStatus().start();
							}
						}, 40);

					});
					dlog.debug("Reaction");
				}
			});
			return;
		}

		if (arg1.equals("setlogs")) {
			Bukkit.getScheduler().runTask(plugin, new Runnable() {

				@Override
				public void run() {
					dlog.debug("Arg1 is setlogs");
					Status4Discord.getInstance().getConfig().set("logs.textChannelID", e.getChannel().getId());
					dlog.debug("Updated logs.textChannelID to " + e.getChannel().getId());
					Status4Discord.getInstance().getConfig().options().copyDefaults(true);
					Status4Discord.getInstance().saveConfig();
					dlog.debug("Saving config");
					Status4Discord.getInstance().reloadConfig();
					dlog.debug("Reloading config");
					e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
						deleteMessage(e);
					});
					dlog.debug("Reaction");
				}
			});
			return;
		}
	}

	private void deleteMessage(MessageReceivedEvent e) {
		new Thread() {
			public void run() {
				try {
					sleep(500);
				} catch (InterruptedException e) {
					logger.severe("The Thread Interrupted!");
					e.printStackTrace();
				}

				dlog.debug("deleteMessage");
				e.getMessage().delete().queue();

			}
		}.start();
	}

}
