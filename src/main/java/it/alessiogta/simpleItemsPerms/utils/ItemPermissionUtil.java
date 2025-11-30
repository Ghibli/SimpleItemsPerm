package it.alessiogta.simpleItemsPerms.utils;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemPermissionUtil {

    private static final String PERMISSION_KEY = "simpleitemsperms_permission";

    /**
     * Assegna un permesso ad un item
     */
    public static ItemStack setPermission(ItemStack item, String permission) {
        if (item == null) return item;

        SimpleItemsPerms plugin = SimpleItemsPerms.getInstance();

        // CREA il meta se non esiste! (FIX per items vanilla senza meta)
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());

        if (meta == null) {
            plugin.getLogger().warning("Impossibile creare ItemMeta per " + item.getType());
            return item;
        }

        // Salva il permesso nel PersistentDataContainer
        NamespacedKey key = new NamespacedKey(plugin, PERMISSION_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, permission);

        // Aggiungi il lore
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        String loreText = plugin.getConfigManager().getLoreText();

        // Rimuovi eventuali lore precedenti
        lore.removeIf(line -> line.equals(loreText));

        // Aggiungi il nuovo lore
        if (plugin.getConfigManager().getLorePosition().equalsIgnoreCase("start")) {
            lore.add(0, loreText);
        } else {
            lore.add(loreText);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Rimuove il permesso da un item
     */
    public static ItemStack removePermission(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return item;

        SimpleItemsPerms plugin = SimpleItemsPerms.getInstance();
        ItemMeta meta = item.getItemMeta();

        // Rimuovi dal PersistentDataContainer
        NamespacedKey key = new NamespacedKey(plugin, PERMISSION_KEY);
        meta.getPersistentDataContainer().remove(key);

        // Rimuovi il lore
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            String loreText = plugin.getConfigManager().getLoreText();
            lore.removeIf(line -> line.equals(loreText));
            meta.setLore(lore.isEmpty() ? null : lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Ottiene il permesso richiesto da un item
     */
    public static String getPermission(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        SimpleItemsPerms plugin = SimpleItemsPerms.getInstance();
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, PERMISSION_KEY);

        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    /**
     * Controlla se un item ha un permesso assegnato
     */
    public static boolean hasPermission(ItemStack item) {
        return getPermission(item) != null;
    }

    /**
     * Controlla se un giocatore può usare un item
     */
    public static boolean canUse(Player player, ItemStack item) {
        String permission = getPermission(item);
        if (permission == null) return true; // Nessun permesso richiesto

        return player.hasPermission(permission);
    }

    /**
     * Aggiorna il lore di un item in base ai permessi del giocatore
     */
    public static ItemStack updateLore(ItemStack item, Player player) {
        if (item == null || !item.hasItemMeta()) return item;

        SimpleItemsPerms plugin = SimpleItemsPerms.getInstance();
        String permission = getPermission(item);

        if (permission == null) return item;

        ItemMeta meta = item.getItemMeta();
        boolean hasPermission = player.hasPermission(permission);
        boolean shouldHide = plugin.getConfigManager().shouldHideLoreForPermitted();
        String loreText = plugin.getConfigManager().getLoreText();
        
        // Strip color codes per il confronto affidabile
        String strippedLoreText = stripColorCodes(loreText);

        if (hasPermission && shouldHide) {
            // Nascondi il lore se il giocatore ha il permesso
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                // Rimuovi qualsiasi linea che, strippata, corrisponde al lore del permesso
                lore.removeIf(line -> stripColorCodes(line).equals(strippedLoreText));
                meta.setLore(lore.isEmpty() ? null : lore);
                item.setItemMeta(meta);
            }
        } else if (!hasPermission) {
            // Assicurati che il lore ci sia se il giocatore NON ha il permesso
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

            // Controlla se il lore è già presente (confronto senza color codes)
            boolean loreExists = false;
            for (String line : lore) {
                if (stripColorCodes(line).equals(strippedLoreText)) {
                    loreExists = true;
                    break;
                }
            }

            // Aggiungi il lore se non c'è
            if (!loreExists) {
                if (plugin.getConfigManager().getLorePosition().equalsIgnoreCase("start")) {
                    lore.add(0, loreText);
                } else {
                    lore.add(loreText);
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
        }

        return item;
    }
    
    /**
     * Rimuove tutti i color codes da una stringa (sia legacy che hex)
     * per permettere confronti affidabili
     */
    private static String stripColorCodes(String text) {
        if (text == null) return "";
        
        // Rimuovi codici hex &#RRGGBB
        text = text.replaceAll("&#[A-Fa-f0-9]{6}", "");
        
        // Rimuovi codici legacy (&[0-9a-fk-or])
        text = text.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        
        // Rimuovi anche i color codes già tradotti (§)
        text = text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
        
        // Rimuovi codici hex Minecraft nativi §x§R§R§G§G§B§B
        text = text.replaceAll("§x(§[0-9a-fA-F]){6}", "");
        
        return text;
    }

    /**
     * Valida un nome di permesso
     */
    public static boolean isValidPermission(String permission) {
        if (permission == null || permission.isEmpty()) return false;

        // Permette solo lettere, numeri, punti e underscore
        return permission.matches("^[a-zA-Z0-9._]+$");
    }
}