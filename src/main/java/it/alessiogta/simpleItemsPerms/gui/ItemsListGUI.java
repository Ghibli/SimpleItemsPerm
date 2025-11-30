package it.alessiogta.simpleItemsPerms.gui;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
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
import java.util.Set;

public class ItemsListGUI implements Listener {
    
    private final SimpleItemsPerms plugin;
    private final Player player;
    private Inventory inventory;
    private List<String> itemUUIDs;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 45;
    
    private static final int PREV_SLOT = 45;
    private static final int INFO_SLOT = 48;
    private static final int NEXT_SLOT = 53;
    private static final int CLOSE_SLOT = 49;
    
    public ItemsListGUI(SimpleItemsPerms plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.itemUUIDs = new ArrayList<>(plugin.getItemRegistry().getAllItemUUIDs());
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        createInventory();
    }
    
    private void createInventory() {
        String title = plugin.getMessageManager().getMessage("gui-list.title");
        inventory = Bukkit.createInventory(null, 54, title);
        updateInventory();
    }
    
    private void updateInventory() {
        inventory.clear();
        
        if (itemUUIDs.isEmpty()) {
            ItemStack noItems = new ItemStack(Material.BARRIER);
            ItemMeta meta = noItems.getItemMeta();
            meta.setDisplayName(plugin.getMessageManager().getMessage("gui-list.no-items-name"));
            meta.setLore(plugin.getMessageManager().getMessageList("gui-list.no-items-lore"));
            noItems.setItemMeta(meta);
            inventory.setItem(22, noItems);
        } else {
            int start = currentPage * ITEMS_PER_PAGE;
            int end = Math.min(start + ITEMS_PER_PAGE, itemUUIDs.size());
            
            int slot = 0;
            for (int i = start; i < end; i++) {
                String uuid = itemUUIDs.get(i);
                ItemStack displayItem = plugin.getItemRegistry().createDisplayItem(uuid);
                if (displayItem != null) {
                    inventory.setItem(slot, displayItem);
                    slot++;
                }
            }
        }
        
        setupNavigationButtons();
    }
    
    private void setupNavigationButtons() {
        if (currentPage > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            meta.setDisplayName(plugin.getMessageManager().getMessage("gui-list.prev-page"));
            prev.setItemMeta(meta);
            inventory.setItem(PREV_SLOT, prev);
        }
        
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        int totalPages = (int) Math.ceil((double) itemUUIDs.size() / ITEMS_PER_PAGE);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{current}", String.valueOf(currentPage + 1));
        placeholders.put("{total}", String.valueOf(Math.max(1, totalPages)));
        placeholders.put("{amount}", String.valueOf(itemUUIDs.size()));
        
        infoMeta.setDisplayName(plugin.getMessageManager().getMessage("gui-list.page-info", placeholders));
        List<String> lore = new ArrayList<>();
        lore.add(plugin.getMessageManager().getMessage("gui-list.total-items", placeholders));
        infoMeta.setLore(lore);
        info.setItemMeta(infoMeta);
        inventory.setItem(INFO_SLOT, info);
        
        if ((currentPage + 1) * ITEMS_PER_PAGE < itemUUIDs.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName(plugin.getMessageManager().getMessage("gui-list.next-page"));
            next.setItemMeta(meta);
            inventory.setItem(NEXT_SLOT, next);
        }
        
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(plugin.getMessageManager().getMessage("gui-list.close"));
        close.setItemMeta(closeMeta);
        inventory.setItem(CLOSE_SLOT, close);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public void refresh() {
        this.itemUUIDs = new ArrayList<>(plugin.getItemRegistry().getAllItemUUIDs());
        int maxPage = Math.max(0, (itemUUIDs.size() - 1) / ITEMS_PER_PAGE);
        if (currentPage > maxPage) {
            currentPage = maxPage;
        }
        updateInventory();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (slot == PREV_SLOT && currentPage > 0) {
            currentPage--;
            updateInventory();
            return;
        }
        
        if (slot == NEXT_SLOT && (currentPage + 1) * ITEMS_PER_PAGE < itemUUIDs.size()) {
            currentPage++;
            updateInventory();
            return;
        }
        
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        
        if (slot == INFO_SLOT) {
            return;
        }
        
        if (slot < ITEMS_PER_PAGE) {
            int itemIndex = (currentPage * ITEMS_PER_PAGE) + slot;
            if (itemIndex < itemUUIDs.size()) {
                String uuid = itemUUIDs.get(itemIndex);
                player.closeInventory();
                new ItemDetailsGUI(plugin, player, uuid, this).open();
            }
        }
    }
}
