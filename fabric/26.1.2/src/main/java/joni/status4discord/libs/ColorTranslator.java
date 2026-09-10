package joni.status4discord.libs;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ColorTranslator {

    Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    static Component translateColor(String text) {
        Component result = Component.empty();

        Matcher matcher = HEX_PATTERN.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            result = result.copy().append(
                    parseLegacyText(text.substring(lastEnd, matcher.start()), null)
            );

            int rgb = Integer.parseInt(matcher.group(1), 16);

            int nextColor = findNextHexColor(text, matcher.end());

            result = result.copy().append(
                    parseLegacyText(text.substring(matcher.end(), nextColor), rgb)
            );

            lastEnd = nextColor;
        }

        result = result.copy().append(
                parseLegacyText(text.substring(lastEnd), null)
        );

        return result;
    }

    static Component parseLegacyText(String text, Integer initialColor) {
        Component result = Component.empty();

        Style style = Style.EMPTY;

        if (initialColor != null) {
            style = style.withColor(initialColor);
        }

        StringBuilder currentText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            if (character == '&' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));

                if (isColorCode(code)) {
                    if (!currentText.isEmpty()) {
                        result = result.copy().append(
                                Component.literal(currentText.toString())
                                        .withStyle(style)
                        );

                        currentText.setLength(0);
                    }

                    style = applyCode(style, code);

                    i++;
                    continue;
                }
            }

            currentText.append(character);
        }

        if (!currentText.isEmpty()) {
            result = result.copy().append(
                    Component.literal(currentText.toString())
                            .withStyle(style)
            );
        }

        return result;
    }

    static Style applyCode(Style style, char code) {
        return switch (code) {
            case '0' -> style.withColor(0x000000);
            case '1' -> style.withColor(0x0000AA);
            case '2' -> style.withColor(0x00AA00);
            case '3' -> style.withColor(0x00AAAA);
            case '4' -> style.withColor(0xAA0000);
            case '5' -> style.withColor(0xAA00AA);
            case '6' -> style.withColor(0xFFAA00);
            case '7' -> style.withColor(0xAAAAAA);
            case '8' -> style.withColor(0x555555);
            case '9' -> style.withColor(0x5555FF);
            case 'a' -> style.withColor(0x55FF55);
            case 'b' -> style.withColor(0x55FFFF);
            case 'c' -> style.withColor(0xFF5555);
            case 'd' -> style.withColor(0xFF55FF);
            case 'e' -> style.withColor(0xFFFF55);
            case 'f' -> style.withColor(0xFFFFFF);

            case 'k' -> style.withObfuscated(true);
            case 'l' -> style.withBold(true);
            case 'm' -> style.withStrikethrough(true);
            case 'n' -> style.withUnderlined(true);
            case 'o' -> style.withItalic(true);

            case 'r' -> Style.EMPTY;

            default -> style;
        };
    }

    static boolean isColorCode(char character) {
        return character >= '0' && character <= '9'
                || character >= 'a' && character <= 'f'
                || character >= 'k' && character <= 'o'
                || character == 'r';
    }

    static int findNextHexColor(String text, int start) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        matcher.region(start, text.length());

        return matcher.find() ? matcher.start() : text.length();
    }

    static Color parseColor(String value, Color fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            if (value.startsWith("#") || value.startsWith("0x")) {
                return Color.decode(value);
            }

            Field field = Color.class.getField(value.toUpperCase());
            return (Color) field.get(null);

        } catch (Exception ignored) {
            return fallback;
        }
    }
}