package joni.status4discordmc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import joni.lib.ColorTranslator;
import joni.status4discordmc.discord.Discord;
import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor, TabExecutor {

	private final Discord discord;
	private final JavaPlugin plugin;

	public Commands(Discord discord, JavaPlugin plugin) {
		this.discord = discord;
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

		if (args.length == 0 || args[0].equals("info")) {
			sendPluginInfo(s);
			return false;
		}

		if (!s.hasPermission("status4discord.admin"))
			return false;

		if (args[0].equals("reload")) {
			doReload(s);
			return false;
		}

		if (args[0].equals("invite")) {
			doInvite(s);
			return false;
		}

		if (args[0].equals("help")) {
			doHelp(s);
			return false;
		}

		return false;
	}

	private void sendPluginInfo(CommandSender s) {
		s.sendMessage(ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6by Joni &9/help"));
		s.sendMessage(ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6Version ")
				+ Status4Discord.getInstance().getVersion());
	}

	private void doReload(CommandSender s) {

		try {
			discord.stop();
			plugin.reloadConfig();
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

				@Override
				public void run() {
					s.sendMessage(
							ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6Status4Discord reloaded!"));
					Status4Discord.getInstance().startDiscord();
				}
			}, 20);
		} catch (Exception e) {
			s.sendMessage(ChatColor.RED + "Something went wrong!");
		}
	}

	private void doInvite(CommandSender s) {
		s.sendMessage(ColorTranslator.translateColor("&f[&9Status&f4&9Discord&f] &6") + discord.getInvitationLink());
	}

	@Override
	public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
		ArrayList<String> list = new ArrayList<>();
		ArrayList<String> flist = new ArrayList<>();
		if (args.length == 1) {
			list.add("help");
			list.add("reload");
			list.add("invite");
			list.add("info");
		}
		String current = args[args.length - 1].toLowerCase();
		for (String string : list) {
			if (string.toLowerCase().startsWith(current))
				flist.add(string);
		}
		return flist;
	}

	private void doHelp(CommandSender s) {
		s.sendMessage(ColorTranslator
				.translateColor("&f[&9Status&f4&9Discord&f] &6Use /status4discord [reload | invite | info | help]"));
	}

}
