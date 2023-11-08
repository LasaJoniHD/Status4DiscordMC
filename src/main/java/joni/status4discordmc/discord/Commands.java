package joni.status4discordmc.discord;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {

	private FileConfiguration config;
	private JavaPlugin plugin;

	public Commands(JavaPlugin plugin, FileConfiguration config) {
		this.config = config;
		this.plugin = plugin;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		String raw = e.getMessage().getContentRaw();
		if (!raw.startsWith("@<" + e.getJDA().getSelfUser().getId() + ">"))
			return;

		if (!e.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR))
			return;

		String arg1 = raw.split(" ", 2)[1];

		if (arg1.equals("setembed")) {
			config.set("embed.textChannelID", e.getChannel().getId());
			plugin.saveConfig();
			plugin.reloadConfig();
			return;
		}

		if (arg1.equals("setlogs")) {
			config.set("logs.textChannelID", e.getChannel().getId());
			plugin.saveConfig();
			plugin.reloadConfig();
			return;
		}
	}

}
