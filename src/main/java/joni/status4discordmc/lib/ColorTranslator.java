package joni.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

public interface ColorTranslator {

	public static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

	public static String translateColor(String textToTranslate) {

		Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
		StringBuffer buffer = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
		}

		String msg = ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());

		return ChatColor.translateAlternateColorCodes('&', msg);

	}

}
