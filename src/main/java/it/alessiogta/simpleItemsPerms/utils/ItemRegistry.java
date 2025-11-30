package it.alessiogta.simpleItemsPerms.utils;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Gestisce il registro globale di tutti gli items con permessi creati
 */
public class ItemRegistry {
    
    private final SimpleItemsPerms plugin;
    private File itemsFile;
    private FileConfiguration itemsConfig;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    public ItemRegistry(SimpleItemsPerms plugin) {
        this.plugin = plugin;
        loadItemsFile();
    }
    
    private void loadItemsFile() {
        itemsFile = new File(plugin.getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            try {
                itemsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossibile creare items.yml!");
                e.printStackTrace();
            }
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }
    
    /**
     * Registra un nuovo item nella lista globale
     */
    public String registerItem(ItemStack item, String permission, String createdBy) {
        String uuid = UUID.randomUUID().toString();
        String path = "items." + uuid;
        
        // Salva le informazioni base
        itemsConfig.set(path + ".material", item.getType().name());
        itemsConfig.set(path + ".permission", permission);
        itemsConfig.set(path + ".created-by", createdBy);
        itemsConfig.set(path + ".created-at", DATE_FORMAT.format(new Date()));
        itemsConfig.set(path + ".times-given", 1);
        
        // Salva display name se presente
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            itemsConfig.set(path + ".display-name", item.getItemMeta().getDisplayName());
        }
        
        // Salva incantesimi se presenti
        if (item.hasItemMeta() && !item.getItemMeta().getEnchants().isEmpty()) {
            List<String> enchants = new ArrayList<>();
            for (Map.Entry<Enchantment, Integer> entry : item.getItemMeta().getEnchants().entrySet()) {
                enchants.add(entry.getKey().getKey().getKey() + ":" + entry.getValue());
            }
            itemsConfig.set(path + ".enchantments", enchants);
        }
        
        // Salva lore personalizzato (escludendo il lore del permesso)
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = new ArrayList<>();
            String permLore = plugin.getConfigManager().getLoreText();
            for (String line : item.getItemMeta().getLore()) {
                if (!line.equals(permLore)) {
                    lore.add(line);
                }
            }
            if (!lore.isEmpty()) {
                itemsConfig.set(path + ".custom-lore", lore);
            }
        }
        
        save();
        return uuid;
    }
    
    /**
     * Incrementa il contatore di volte che un item è stato dato
     */
    public void incrementTimesGiven(String uuid) {
        String path = "items." + uuid + ".times-given";
        int current = itemsConfig.getInt(path, 0);
        itemsConfig.set(path, current + 1);
        save();
    }
    
    /**
     * Rimuove un item dal registro
     */
    public void removeItem(String uuid) {
        itemsConfig.set("items." + uuid, null);
        save();
    }
    
    /**
     * Ottiene tutti gli UUID degli items registrati
     */
    public Set<String> getAllItemUUIDs() {
        if (!itemsConfig.contains("items")) {
            return new HashSet<>();
        }
        return itemsConfig.getConfigurationSection("items").getKeys(false);
    }
    
    /**
     * Ottiene le informazioni di un item
     */
    public ItemInfo getItemInfo(String uuid) {
        String path = "items." + uuid;
        if (!itemsConfig.contains(path)) {
            return null;
        }
        
        return new ItemInfo(
            uuid,
            Material.valueOf(itemsConfig.getString(path + ".material")),
            itemsConfig.getString(path + ".permission"),
            itemsConfig.getString(path + ".created-by"),
            itemsConfig.getString(path + ".created-at"),
            itemsConfig.getInt(path + ".times-given", 0),
            itemsConfig.getString(path + ".display-name"),
            itemsConfig.getStringList(path + ".enchantments"),
            itemsConfig.getStringList(path + ".custom-lore")
        );
    }
    
    /**
     * Crea un ItemStack visivo per la GUI
     */
    public ItemStack createDisplayItem(String uuid) {
        ItemInfo info = getItemInfo(uuid);
        if (info == null) return null;
        
        ItemStack item = new ItemStack(info.material);
        ItemMeta meta = item.getItemMeta();
        
        // Display name
        if (info.displayName != null) {
            meta.setDisplayName(info.displayName);
        }
        
        // Lore con informazioni
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getMessage("gui-list.item-lore-separator"));
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{permission}", info.permission);
        placeholders.put("{creator}", info.createdBy);
        placeholders.put("{date}", info.createdAt);
        placeholders.put("{times}", String.valueOf(info.timesGiven));
        
        lore.add(plugin.getMessageManager().getMessage("gui-list.item-lore-permission", placeholders));
        lore.add("");
        lore.add(plugin.getMessageManager().getMessage("gui-list.item-lore-creator", placeholders));
        lore.add(plugin.getMessageManager().getMessage("gui-list.item-lore-date", placeholders));
        lore.add(plugin.getMessageManager().getMessage("gui-list.item-lore-times", placeholders));
        
        if (!info.enchantments.isEmpty()) {
            lore.add("");
            lore.add(plugin.getMessageManager().getMessage("gui-list.item-lore-enchants"));
            for (String ench : info.enchantments) {
                Map<String, String> enchPlaceholder = new HashMap<>();
                enchPlaceholder.put("{enchant}", ench);
                lore.add(plugin.getMessageManager().getMessage("gui-list.item-lore-enchant-line", enchPlaceholder));
            }
        }
        
        lore.add(plugin.getMessageManager().getMessage("gui-list.item-lore-separator"));
        lore.add(plugin.getMessageManager().getMessage("gui-list.item-lore-click"));
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    public void reload() {
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }
    
    private void save() {
        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossibile salvare items.yml!");
            e.printStackTrace();
        }
    }
    
    /**
     * Classe per contenere le informazioni di un item
     */
    public static class ItemInfo {
        public final String uuid;
        public final Material material;
        public final String permission;
        public final String createdBy;
        public final String createdAt;
        public final int timesGiven;
        public final String displayName;
        public final List<String> enchantments;
        public final List<String> customLore;
        
        public ItemInfo(String uuid, Material material, String permission, String createdBy, 
                       String createdAt, int timesGiven, String displayName, 
                       List<String> enchantments, List<String> customLore) {
            this.uuid = uuid;
            this.material = material;
            this.permission = permission;
            this.createdBy = createdBy;
            this.createdAt = createdAt;
            this.timesGiven = timesGiven;
            this.displayName = displayName;
            this.enchantments = enchantments != null ? enchantments : new ArrayList<>();
            this.customLore = customLore != null ? customLore : new ArrayList<>();
        }
    }
}
