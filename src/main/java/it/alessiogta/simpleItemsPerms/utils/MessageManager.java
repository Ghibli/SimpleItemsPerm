package it.alessiogta.simpleItemsPerms.utils;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {
    
    private final SimpleItemsPerms plugin;
    private FileConfiguration messages;
    private File messagesFile;
    private String currentLanguage;
    
    public MessageManager(SimpleItemsPerms plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    private void loadMessages() {
        // Leggi la lingua dal config
        currentLanguage = plugin.getConfig().getString("language", "en_US");
        
        // Crea cartella lang se non esiste
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        
        // File della lingua corrente
        messagesFile = new File(langFolder, "messages_" + currentLanguage + ".yml");
        
        // Se non esiste, copia dalla risorsa
        if (!messagesFile.exists()) {
            try {
                InputStream resource = plugin.getResource("lang/messages_" + currentLanguage + ".yml");
                if (resource != null) {
                    Files.copy(resource, messagesFile.toPath());
                    plugin.getLogger().info("Created language file: messages_" + currentLanguage + ".yml");
                } else {
                    // Fallback a inglese
                    plugin.getLogger().warning("Language file not found: " + currentLanguage);
                    plugin.getLogger().warning("Falling back to en_US");
                    currentLanguage = "en_US";
                    messagesFile = new File(langFolder, "messages_en_US.yml");
                    resource = plugin.getResource("lang/messages_en_US.yml");
                    if (resource != null) {
                        Files.copy(resource, messagesFile.toPath());
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create language file!");
                e.printStackTrace();
            }
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getLogger().info("✓ Loaded language: " + currentLanguage);
    }
    
    public void reload() {
        loadMessages();
    }
    
    public String getMessage(String path) {
        return ColorUtil.color(messages.getString(path, "&cMessaggio non trovato: " + path));
    }
    
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        
        // Aggiungi sempre il prefix
        if (!placeholders.containsKey("{prefix}")) {
            placeholders.put("{prefix}", plugin.getConfigManager().getPrefix());
        }
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        
        return message;
    }
    
    public List<String> getMessageList(String path) {
        List<String> messageList = messages.getStringList(path);
        List<String> coloredList = new ArrayList<>();
        for (String line : messageList) {
            coloredList.add(ColorUtil.color(line));
        }
        return coloredList;
    }
    
    public List<String> getMessageList(String path, Map<String, String> placeholders) {
        List<String> messageList = getMessageList(path);
        List<String> replacedList = new ArrayList<>();
        
        for (String line : messageList) {
            String replaced = line;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                replaced = replaced.replace(entry.getKey(), entry.getValue());
            }
            replacedList.add(replaced);
        }
        
        return replacedList;
    }
    
    public void sendMessage(CommandSender sender, String path) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{prefix}", plugin.getConfigManager().getPrefix());
        sender.sendMessage(getMessage(path, placeholders));
    }
    
    public void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(getMessage(path, placeholders));
    }
    
    public void sendInfo(CommandSender sender) {
        List<String> info = messages.getStringList("info");
        for (String line : info) {
            sender.sendMessage(ColorUtil.color(line));
        }
    }
}
