package joni.status4discordmc;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collection;
import java.util.stream.Collectors;

public class Placeholders {

    private final static Server s = Bukkit.getServer();

    private static String replace(String msg) {
        msg = msg.replace("%tps%", String.valueOf(getTPS(0)));
        msg = msg.replace("%tps_5%", String.valueOf(getTPS(1)));
        msg = msg.replace("%tps_15%", String.valueOf(getTPS(2)));
        msg = msg.replace("%online_players%", Integer.toString(getOnlinePlayers()));
        msg = msg.replace("%max_players%", Integer.toString(getMaxPlayers()));
        msg = msg.replace("%cpu%", String.valueOf(getCPU()));
        msg = msg.replace("%freeram%", getFreeMemory());
        msg = msg.replace("%freeram_percentage%", getFreeInPercentMemory());
        msg = msg.replace("%usedram_percentage%", getUsedMemoryPercentage());
        msg = msg.replace("%usedram%", getUsedMemory());
        msg = msg.replace("%maxram%", getMaxMemory());
        msg = msg.replace("%serverip%", getServerIP());
        msg = msg.replace("%uptime%", getUptime());
        msg = msg.replace("%players%", listAllPlayers());
        return msg;
    }

    public static String set(String msg) {
        if (Status4Discord.getInstance().getPapi()) {
            msg = PlaceholderAPI.setPlaceholders(null, msg);
        }
        msg = replace(msg);
        return msg;
    }

    public static double getCPU() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            return Math.round(sunOsBean.getProcessCpuLoad() * 100 * 10.0) / 10.0;
        }
        return 0;
    }

    public static double getTPS(int m) {
        if (Status4Discord.isPaper())
            return Math.round(Bukkit.getServer().getTPS()[m] * 10.0) / 10.0;
        else
            return 0.0;
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
        return onlinePlayers.stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));
    }


    public static String getServerIP() {
        String serverIP = Status4Discord.getInstance().getConfigManager().getConfig().getString("serverIP");
        if (serverIP != null && serverIP.equalsIgnoreCase("server.properties")) {
            String bukkitIP = Bukkit.getIp();
            if (bukkitIP.isEmpty()) {
                serverIP = "not set";
            } else {
                serverIP = bukkitIP;
            }
        }
        return serverIP;
    }

    public static String getUptime() {
        long currentTime = System.currentTimeMillis();
        long uptimeInSeconds = (currentTime - Status4Discord.getInstance().getStartUp()) / 1000;

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
