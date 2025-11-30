package it.alessiogta.simpleItemsPerms.listeners;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import it.alessiogta.simpleItemsPerms.utils.ItemPermissionUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ArmorEquipListener implements Listener {
    
    private final SimpleItemsPerms plugin;
    
    public ArmorEquipListener(SimpleItemsPerms plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onArmorEquip(InventoryClickEvent event) {
        if (!plugin.getConfigManager().isEventBlocked("equip-armor")) return;
        
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        // Controlla se sta equipaggiando un'armatura
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) return;
        
        ItemStack item = event.getCursor();
        if (item == null || item.getType().isAir()) {
            item = event.getCurrentItem();
        }
        
        if (item == null) return;
        
        String permission = ItemPermissionUtil.getPermission(item);
        if (permission == null) return;
        
        if (!player.hasPermission(permission)) {
            event.setCancelled(true);
            
            // Rimuovi il prefisso per mostrare solo il suffisso
            String suffix = permission.replace("simpleitemsperms.", "");
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{permission}", suffix);
            plugin.getMessageManager().sendMessage(player, "item-blocked.equip", placeholders);
            
            playBlockSound(player);
        }
    }
    
    private void playBlockSound(Player player) {
        if (!plugin.getConfigManager().isSoundEnabled()) return;
        
        try {
            Sound sound = Sound.valueOf(plugin.getConfigManager().getBlockSound());
            float volume = plugin.getConfigManager().getSoundVolume();
            float pitch = plugin.getConfigManager().getSoundPitch();
            
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().warning("Suono invalido nel config: " + plugin.getConfigManager().getBlockSound());
            }
        }
    }
}
