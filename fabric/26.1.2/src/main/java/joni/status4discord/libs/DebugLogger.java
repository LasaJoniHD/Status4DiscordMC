package joni.status4discord.libs;

import joni.status4discord.Status4discord;

public class DebugLogger {

    private static boolean debug = false;

    public static void setDebug(boolean debug) {
        DebugLogger.debug = debug;
    }

    public static void log(String log) {
        if (debug && log != null)
            Status4discord.LOGGER.warn(log);
    }

}
