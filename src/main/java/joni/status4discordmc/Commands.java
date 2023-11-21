package joni.status4discordmc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import joni.lib.ColorTranslator;
import joni.status4discordmc.discord.Discord;
import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor {

	private Discord discord;
	private JavaPlugin plugin;

	public Commands(Discord discord, JavaPlugin plugin) {
		this.discord = discord;
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

		if (args.length == 0 || args[0].equals("info")) {
			sendPluginInfo(s);
		}

		if (!s.hasPermission("status4discord.admin"))
			return false;

		if (args[0].equals("restart")) {
			doRestart(s);
			return false;
		}

		if (args[0].equals("reload")) {
			doReload(s);
			return false;
		}

		return false;
	}

	private void sendPluginInfo(CommandSender s) {
		s.sendMessage(ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6by Joni &9/help"));
	}

	private void doReload(CommandSender s) {
		s.sendMessage(ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6Status4Discord reloaded!"));
		try {
			plugin.reloadConfig();
		} catch (Exception e) {
			s.sendMessage(ChatColor.RED + "Something went wrong!");
			e.printStackTrace();
		}

	}

	private void doRestart(CommandSender s) {
		s.sendMessage(
				ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6The Discord bot will be restarted!"));
		try {
			discord.stop();
			plugin.reloadConfig();
			discord.start();
		} catch (Exception e) {
			s.sendMessage(ChatColor.RED + "Something went wrong!");
			e.printStackTrace();
		}
	}

}
