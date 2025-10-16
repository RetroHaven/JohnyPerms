package com.johnymuffin.jperms.beta.util;

import org.bukkit.ChatColor;

public class ColorUtil {

    /**
     * Translates color codes from alternate color code characters to proper ChatColor codes
     * This is for compatibility with legacy Bukkit versions that don't have translateAlternateColorCodes
     *
     * @param altColorChar The alternate color code character (usually '&')
     * @param textToTranslate The text containing color codes
     * @return The text with proper ChatColor codes
     */
    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        if (textToTranslate == null) {
            return null;
        }

        char[] chars = textToTranslate.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[i + 1]) > -1) {
                chars[i] = '\u00A7'; // Section sign (§) - the color character
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }
}