package joni.status4discordmc;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.MinecraftServer;

public class Placeholders {

	private final static Server s = Bukkit.getServer();

	private static String replace(String msg) {
		msg = msg.replaceAll("%tps%", String.valueOf(getTPS(0)));
		msg = msg.replaceAll("%tps_5%", String.valueOf(getTPS(1)));
		msg = msg.replaceAll("%tps_15%", String.valueOf(getTPS(2)));
		msg = msg.replaceAll("%online_players%", Integer.toString(getOnlinePlayers()));
		msg = msg.replaceAll("%max_players%", Integer.toString(getMaxPlayers()));
		msg = msg.replaceAll("%cpu%", String.valueOf(getCPU()));
		msg = msg.replaceAll("%freeram%", getFreeMemory());
		msg = msg.replaceAll("%freeram_percentage%", getFreeInPercentMemory());
		msg = msg.replaceAll("%usedram_percentage%", getUsedMemoryPercentage());
		msg = msg.replaceAll("%usedram%", getUsedMemory());
		msg = msg.replaceAll("%maxram%", getMaxMemory());
		msg = msg.replaceAll("%serverip%", getServerIP());
		msg = msg.replaceAll("%uptime%", getUptime());
		return msg;
	}

	public static String set(String msg) {
		if (Status4Discord.papi) {
			msg = PlaceholderAPI.setPlaceholders(null, msg);
			msg = replace(msg);
			return msg;
		}
		return replace(msg);
	}

	public static double getCPU() {
		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
			com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
			return Math.round(sunOsBean.getProcessCpuLoad() * 100 * 10.0) / 10.0;
		}
		return 0;
	}

	@SuppressWarnings({ "resource", "deprecation" })
	public static double getTPS(int m) {
		return Math.round(MinecraftServer.getServer().recentTps[m] * 10.0) / 10.0;
	}

	public static int getOnlinePlayers() {
		return s.getOnlinePlayers().size();
	}

	public static int getMaxPlayers() {
		return s.getMaxPlayers();
	}

	public static String getFreeMemory() {
		return String.valueOf(Runtime.getRuntime().freeMemory() / 1024L / 1024L);
	}

	public static String getFreeInPercentMemory() {
		long totalMemory = Runtime.getRuntime().totalMemory() / (1024L * 1024L);
		long freeMemory = Runtime.getRuntime().freeMemory() / (1024L * 1024L);
		int freeMemoryPercentage = (int) ((freeMemory * 100.0) / totalMemory);
		return String.valueOf(freeMemoryPercentage);
	}

	public static String getUsedMemory() {
		long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L;
		return String.valueOf(usedMemory);
	}

	public static String getUsedMemoryPercentage() {
		long totalMemory = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
		long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L;
		double usedMemoryPercentage = (double) (usedMemory * 100) / totalMemory;
		return Double.toString(Math.round(usedMemoryPercentage));
	}

	public static String getMaxMemory() {
		return String.valueOf(Runtime.getRuntime().maxMemory() / 1024L / 1024L);
	}

	public static String listAllPlayers() {
		Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
		StringBuilder playerList = new StringBuilder();

		for (Player player : onlinePlayers) {
			playerList.append(player.getName()).append("\n");
		}

		return playerList.toString().trim();
	}

	public static String getServerIP() {
		String serverIP = Status4Discord.getInstance().getConfig().getString("embed.serverIP");
		if (serverIP.equals("server.properties")) {
			String bukkitIP = Bukkit.getIp();
			if (bukkitIP.equals("")) {
				serverIP = "not set";
			} else {
				serverIP = bukkitIP;
			}
		}
		return serverIP;
	}

	public static String getUptime() {
		long currentTime = System.currentTimeMillis();
		long uptimeInSeconds = (currentTime - Status4Discord.startUp) / 1000;

		if (uptimeInSeconds < 60) {
			return uptimeInSeconds + " seconds";
		} else if (uptimeInSeconds < 3600) {
			long minutes = uptimeInSeconds / 60;
			return minutes + (minutes == 1 ? " minute" : " minutes");
		} else if (uptimeInSeconds < 86400) {
			long hours = uptimeInSeconds / 3600;
			return hours + (hours == 1 ? " hour" : " hours");
		} else {
			long days = uptimeInSeconds / 86400;
			return days + (days == 1 ? " day" : " days");
		}
	}

}
