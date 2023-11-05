package joni.status4discordmc;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import me.clip.placeholderapi.PlaceholderAPI;

public class Placeholders {

	final static Server s = Bukkit.getServer();

	private static String replace(String msg) {
		msg = msg.replaceAll("%tps%", String.valueOf(getTPS(0)));
		msg = msg.replaceAll("%tps_5%", String.valueOf(getTPS(1)));
		msg = msg.replaceAll("%tps_15%", String.valueOf(getTPS(2)));
		msg = msg.replaceAll("%online_players%", s.getOnlinePlayers().size() + "");
		msg = msg.replaceAll("%max_players%", s.getMaxPlayers() + "");
		msg = msg.replaceAll("%cpu_system%", String.valueOf(getCPU()));
		msg = msg.replaceAll("%freeram%", String.valueOf(Runtime.getRuntime().freeMemory() / 1024L / 1024L));
		msg = msg.replaceAll("%usedram%",
				String.valueOf((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L));
		msg = msg.replaceAll("%maxram%", String.valueOf(Runtime.getRuntime().maxMemory() / 1024L / 1024L));
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

	private static double getTPS(int m) {
		return Math.round(s.getTPS()[m] * 10.0) / 10.0;
	}

}
