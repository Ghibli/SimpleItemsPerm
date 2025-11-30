package it.alessiogta.simpleItemsPerms.listeners;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import it.alessiogta.simpleItemsPerms.utils.ItemPermissionUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemPickupListener implements Listener {
    
    private final SimpleItemsPerms plugin;
    
    public ItemPickupListener(SimpleItemsPerms plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!plugin.getConfigManager().isEventBlocked("pickup")) return;
        
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        
        ItemStack item = event.getItem().getItemStack();
        
        String permission = ItemPermissionUtil.getPermission(item);
        if (permission == null) return;
        
        if (!player.hasPermission(permission)) {
            event.setCancelled(true);
            
            // Rimuovi il prefisso per mostrare solo il suffisso
            String suffix = permission.replace("simpleitemsperms.", "");
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{permission}", suffix);
            plugin.getMessageManager().sendMessage(player, "item-blocked.pickup", placeholders);
            
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
