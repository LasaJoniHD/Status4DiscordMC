package joni.status4discordmc.libs;

import joni.status4discordmc.Status4Discord;

public class DebugLogger {

    private static boolean debug = false;

    public static void setDebug(boolean debug) {
        DebugLogger.debug = debug;
    }

    public static void log(String log) {
        if (debug && log != null)
            Status4Discord.getInstance().getLogger().warning(log);
    }

}
