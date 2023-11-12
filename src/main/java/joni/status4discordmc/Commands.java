package joni.status4discordmc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

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

		if (!s.hasPermission("status4mc.admin") || !s.isOp())
			return false;

		if (args[0].equals("restart")) {
			s.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&f[&9Status&f4&9Discord&f] &6The Discord bot will be restarted!"));
			try {
				discord.stop();
				plugin.reloadConfig();
				discord.start();
			} catch (Exception e) {
				s.sendMessage(ChatColor.RED + "Something went wrong!");
				e.printStackTrace();
			}
		}

		if (args[0].equals("reload")) {
			s.sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&f[&9Status&f4&9Discord&f] &6Status4MC reloaded!"));
			try {
				plugin.reloadConfig();
			} catch (Exception e) {
				s.sendMessage(ChatColor.RED + "Something went wrong!");
				e.printStackTrace();
			}
		}

		return false;
	}

}
