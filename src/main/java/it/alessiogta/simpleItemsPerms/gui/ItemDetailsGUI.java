package it.alessiogta.simpleItemsPerms.gui;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import it.alessiogta.simpleItemsPerms.utils.ColorUtil;
import it.alessiogta.simpleItemsPerms.utils.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GUI per visualizzare i dettagli di un item specifico
 */
public class ItemDetailsGUI implements Listener {
    
    private final SimpleItemsPerms plugin;
    private final Player player;
    private final String itemUuid;
    private final ItemsListGUI parentGUI;
    private Inventory inventory;
    
    private static final int ITEM_DISPLAY_SLOT = 13;
    private static final int DELETE_SLOT = 30;
    private static final int BACK_SLOT = 32;
    
    public ItemDetailsGUI(SimpleItemsPerms plugin, Player player, String itemUuid, ItemsListGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.itemUuid = itemUuid;
        this.parentGUI = parentGUI;
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        createInventory();
    }
    
    private void createInventory() {
        String title = plugin.getMessageManager().getMessage("gui-details.title");
        inventory = Bukkit.createInventory(null, 45, title);
        
        ItemRegistry.ItemInfo info = plugin.getItemRegistry().getItemInfo(itemUuid);
        if (info == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("gui-details.error-not-found"));
            return;
        }
        
        // Item display
        ItemStack displayItem = new ItemStack(info.material);
        ItemMeta displayMeta = displayItem.getItemMeta();
        
        if (info.displayName != null) {
            displayMeta.setDisplayName(ColorUtil.color(info.displayName));
        } else {
            displayMeta.setDisplayName(ColorUtil.color("&e" + info.material.name()));
        }
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{permission}", info.permission);
        placeholders.put("{creator}", info.createdBy);
        placeholders.put("{date}", info.createdAt);
        placeholders.put("{times}", String.valueOf(info.timesGiven));
        
        lore.add(plugin.getMessageManager().getMessage("gui-details.lore-permission", placeholders));
        lore.add("");
        lore.add(plugin.getMessageManager().getMessage("gui-details.lore-creator", placeholders));
        lore.add(plugin.getMessageManager().getMessage("gui-details.lore-date", placeholders));
        lore.add(plugin.getMessageManager().getMessage("gui-details.lore-times", placeholders));
        
        if (!info.enchantments.isEmpty()) {
            lore.add("");
            lore.add(plugin.getMessageManager().getMessage("gui-details.lore-enchants-title"));
            for (String ench : info.enchantments) {
                Map<String, String> enchPlaceholder = new HashMap<>();
                enchPlaceholder.put("{enchant}", ench);
                lore.add(plugin.getMessageManager().getMessage("gui-details.lore-enchant-line", enchPlaceholder));
            }
        }
        
        if (!info.customLore.isEmpty()) {
            lore.add("");
            lore.add(plugin.getMessageManager().getMessage("gui-details.lore-custom-title"));
            for (String loreLine : info.customLore) {
                Map<String, String> lorePlaceholder = new HashMap<>();
                lorePlaceholder.put("{lore}", loreLine);
                lore.add(plugin.getMessageManager().getMessage("gui-details.lore-custom-line", lorePlaceholder));
            }
        }
        
        lore.add("");
        
        displayMeta.setLore(lore);
        displayItem.setItemMeta(displayMeta);
        inventory.setItem(ITEM_DISPLAY_SLOT, displayItem);
        
        // Pulsante Elimina
        ItemStack deleteButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta deleteMeta = deleteButton.getItemMeta();
        deleteMeta.setDisplayName(plugin.getMessageManager().getMessage("gui-details.delete-button-name"));
        deleteMeta.setLore(plugin.getMessageManager().getMessageList("gui-details.delete-button-lore"));
        deleteButton.setItemMeta(deleteMeta);
        inventory.setItem(DELETE_SLOT, deleteButton);
        
        // Pulsante Indietro
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(plugin.getMessageManager().getMessage("gui-details.back-button-name"));
        List<String> backLore = new ArrayList<>();
        backLore.add(plugin.getMessageManager().getMessage("gui-details.back-button-lore"));
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        inventory.setItem(BACK_SLOT, backButton);
        
        // Bordo decorativo
        fillBorder();
    }
    
    private void fillBorder() {
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(36 + i, borderItem);
        }
        
        for (int i = 1; i < 4; i++) {
            inventory.setItem(i * 9, borderItem);
            inventory.setItem(i * 9 + 8, borderItem);
        }
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        if (slot == DELETE_SLOT) {
            handleDelete();
        } else if (slot == BACK_SLOT) {
            player.closeInventory();
            parentGUI.refresh();
            parentGUI.open();
        }
    }
    
    private void handleDelete() {
        ItemRegistry.ItemInfo info = plugin.getItemRegistry().getItemInfo(itemUuid);
        if (info == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("gui-details.error-not-found"));
            player.closeInventory();
            return;
        }
        
        plugin.getItemRegistry().removeItem(itemUuid);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{permission}", info.permission);
        placeholders.put("{material}", info.material.name());
        
        player.sendMessage(plugin.getMessageManager().getMessage("gui-details.delete-success"));
        player.sendMessage(plugin.getMessageManager().getMessage("gui-details.delete-info-permission", placeholders));
        player.sendMessage(plugin.getMessageManager().getMessage("gui-details.delete-info-material", placeholders));
        
        player.closeInventory();
        parentGUI.refresh();
        parentGUI.open();
    }
}
