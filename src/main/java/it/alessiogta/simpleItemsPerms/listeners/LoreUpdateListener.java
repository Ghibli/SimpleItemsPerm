package it.alessiogta.simpleItemsPerms.listeners;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import it.alessiogta.simpleItemsPerms.utils.ItemPermissionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener per aggiornare dinamicamente il lore degli items
 * in base ai permessi del player.
 *
 * Questo listener risolve il bug dove il lore "✘ Non hai i permessi per usarlo"
 * viene mostrato anche ai player che HANNO il permesso.
 */
public class LoreUpdateListener implements Listener {

    private final SimpleItemsPerms plugin;

    public LoreUpdateListener(SimpleItemsPerms plugin) {
        this.plugin = plugin;
    }

    /**
     * Quando un player entra nel server, aggiorna tutti gli items nel suo inventario
     * per nascondere/mostrare il lore in base ai suoi permessi.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Esegui l'aggiornamento 1 secondo dopo il join (per dare tempo ai permessi di caricarsi)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updatePlayerInventory(player);
        }, 20L); // 20 ticks = 1 secondo
    }

    /**
     * Quando un player raccoglie un item, aggiorna il suo lore
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        // Aggiorna il lore dell'item raccolto
        ItemPermissionUtil.updateLore(item, player);
    }

    /**
     * Aggiorna tutti gli items nell'inventario di un player
     */
    private void updatePlayerInventory(Player player) {
        // Aggiorna items nell'inventario principale
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir()) {
                ItemStack updated = ItemPermissionUtil.updateLore(item, player);
                player.getInventory().setItem(i, updated);
            }
        }

        // Aggiorna armor
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null && !armor[i].getType().isAir()) {
                armor[i] = ItemPermissionUtil.updateLore(armor[i], player);
            }
        }
        player.getInventory().setArmorContents(armor);

        // Aggiorna off-hand
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && !offHand.getType().isAir()) {
            player.getInventory().setItemInOffHand(ItemPermissionUtil.updateLore(offHand, player));
        }

        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("[LoreUpdate] Aggiornato inventario di " + player.getName());
        }
    }
}