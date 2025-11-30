package it.alessiogta.simpleItemsPerms.utils;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final SimpleItemsPerms plugin;
    private FileConfiguration config;
    
    public ConfigManager(SimpleItemsPerms plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }
    
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public String getPrefix() {
        return ColorUtil.color(config.getString("prefix", "&8[&6SimpleItemsPerms&8]&r"));
    }
    
    public String getGuiTitle() {
        return ColorUtil.color(config.getString("gui.title", "&6Assegna Permesso agli Items"));
    }
    
    public int getGuiSize() {
        return config.getInt("gui.size", 54);
    }
    
    public String getLoreText() {
        return plugin.getMessageManager().getMessage("lore-no-permission");
    }
    
    public boolean shouldHideLoreForPermitted() {
        return config.getBoolean("lore.hide-for-permitted-players", true);
    }
    
    public String getLorePosition() {
        return config.getString("lore.position", "end");
    }
    
    public boolean isEventBlocked(String eventType) {
        return config.getBoolean("blocked-events." + eventType, true);
    }
    
    public boolean isSoundEnabled() {
        return config.getBoolean("block-sound.enabled", true);
    }
    
    public String getBlockSound() {
        return config.getString("block-sound.sound", "ENTITY_VILLAGER_NO");
    }
    
    public float getSoundVolume() {
        return (float) config.getDouble("block-sound.volume", 1.0);
    }
    
    public float getSoundPitch() {
        return (float) config.getDouble("block-sound.pitch", 1.0);
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
    }
}
