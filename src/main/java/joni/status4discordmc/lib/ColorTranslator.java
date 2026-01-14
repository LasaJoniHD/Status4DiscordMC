package joni.status4discordmc.lib;

import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ColorTranslator {

    Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

    static String translateColor(String textToTranslate) {

        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        String msg = ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());

        return ChatColor.translateAlternateColorCodes('&', msg);

    }
    
    static Color parseColor(String value, Color fallback) {
        if (value == null) return fallback;

        try {
            // HEX support: #ff0000 or 0xff0000
            if (value.startsWith("#") || value.startsWith("0x")) {
                return Color.decode(value);
            }

            // Named colors: GREEN, RED, BLUE, CYAN, etc.
            Field field = Color.class.getField(value.toUpperCase());
            return (Color) field.get(null);

        } catch (Exception ignored) {
            return fallback;
        }
    }

}
