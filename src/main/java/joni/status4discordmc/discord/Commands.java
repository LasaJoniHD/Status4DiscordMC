package joni.status4discordmc.discord;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {

	private FileConfiguration config;
	private JavaPlugin plugin;
	private Logger logger;

	public Commands(JavaPlugin plugin, Logger logger, FileConfiguration config) {
		this.config = config;
		this.plugin = plugin;
		this.logger = logger;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		String raw = e.getMessage().getContentRaw();
		if (!raw.startsWith("<@" + e.getJDA().getSelfUser().getId() + ">"))
			return;

		if (!e.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR))
			return;

		String arg1 = raw.split(" ", 2)[1];

		if (arg1.equals("setembed")) {
			config.set("embed.textChannelID", e.getChannel().getId());
			config.set("embedMessageID", "0");
			plugin.saveConfig();
			plugin.reloadConfig();
			e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
				deleteMessage(e);
			});
			return;
		}

		if (arg1.equals("setlogs")) {
			config.set("logs.textChannelID", e.getChannel().getId());
			plugin.saveConfig();
			plugin.reloadConfig();
			e.getMessage().addReaction(Emoji.fromUnicode("U+2705")).queue(msg -> {
				deleteMessage(e);
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

				e.getMessage().delete().queue();

			}
		}.start();
	}

}
