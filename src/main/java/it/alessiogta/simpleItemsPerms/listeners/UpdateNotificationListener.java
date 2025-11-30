package it.alessiogta.simpleItemsPerms.listeners;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener per notificare gli admin quando c'è un aggiornamento disponibile
 */
public class UpdateNotificationListener implements Listener {

    private final SimpleItemsPerms plugin;

    public UpdateNotificationListener(SimpleItemsPerms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Controlla se il player è admin
        if (!player.hasPermission("simpleitemsperms.admin")) {
            return;
        }

        // Controlla se le notifiche sono abilitate
        if (!plugin.getConfig().getBoolean("update-checker.notify-admins-on-join", true)) {
            return;
        }

        // Notifica con un piccolo delay per evitare spam al login
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && plugin.getUpdateChecker() != null) {
                plugin.getUpdateChecker().notifyPlayer(player);
            }
        }, 40L); // 2 secondi di delay
    }
}
