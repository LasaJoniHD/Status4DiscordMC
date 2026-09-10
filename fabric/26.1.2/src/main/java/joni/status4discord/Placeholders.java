package joni.status4discord;


import joni.status4discord.placeholders.CpuMonitor;
import joni.status4discord.placeholders.FabricTPS;

public class Placeholders {

    private static final CpuMonitor CPU_MONITOR = new CpuMonitor();

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
        msg = replace(msg);
        return msg;
    }

    public static double getCPU() {
        return CPU_MONITOR.getCPU();
    }

    public static double getTPS(int m) {
        return FabricTPS.getTPS(m);
    }

    public static int getOnlinePlayers() {
        return Status4discord.getServerInstance().getPlayerCount();
    }

    public static int getMaxPlayers() {
        return Status4discord.getServerInstance().getMaxPlayers();
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
        String[] onlinePlayers = Status4discord.getServerInstance().getPlayerNames();
        return String.join(", ", onlinePlayers);
    }


    public static String getServerIP() {
        String serverIP = Status4discord.getConfigManager().getConfig().getString("serverIP");
        if (serverIP != null && serverIP.equalsIgnoreCase("server.properties")) {
            String bukkitIP = Status4discord.getServerInstance().getLocalIp();
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
        long uptimeInSeconds = (currentTime - Status4discord.getStartUp()) / 1000;

        dev.dejvokep.boostedyaml.YamlDocument config = Status4discord.getConfigManager().getConfig();
        String secondsStr = config.getString("messages.seconds", "seconds");
        String minuteStr = config.getString("messages.minute", "minute");
        String minutesStr = config.getString("messages.minutes", "minutes");
        String hourStr = config.getString("messages.hour", "hour");
        String hoursStr = config.getString("messages.hours", "hours");
        String dayStr = config.getString("messages.day", "day");
        String daysStr = config.getString("messages.days", "days");

        if (uptimeInSeconds < 60) {
            return uptimeInSeconds + " " + secondsStr;
        } else if (uptimeInSeconds < 3600) {
            long minutes = uptimeInSeconds / 60;
            return minutes + " " + (minutes == 1 ? minuteStr : minutesStr);
        } else if (uptimeInSeconds < 86400) {
            long hours = uptimeInSeconds / 3600;
            return hours + " " + (hours == 1 ? hourStr : hoursStr);
        } else {
            long days = uptimeInSeconds / 86400;
            return days + " " + (days == 1 ? dayStr : daysStr);
        }
    }

}
