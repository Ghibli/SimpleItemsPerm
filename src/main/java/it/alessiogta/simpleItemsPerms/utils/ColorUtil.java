package it.alessiogta.simpleItemsPerms.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Colora una stringa supportando sia i codici legacy (&) che hex (&#RRGGBB)
     */
    public static String color(String message) {
        if (message == null) return "";
        
        // Supporto per codici hex
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        matcher.appendTail(buffer);
        
        // Supporto per codici legacy
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
