package it.alessiogta.simpleItemsPerms.commands;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import it.alessiogta.simpleItemsPerms.gui.PermissionGUI;
import it.alessiogta.simpleItemsPerms.utils.ItemPermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SipCommand implements CommandExecutor {
    
    private final SimpleItemsPerms plugin;
    private final Map<UUID, PermissionGUI> activeGUIs;
    
    public SipCommand(SimpleItemsPerms plugin) {
        this.plugin = plugin;
        this.activeGUIs = new HashMap<>();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        // Comando /sip senza argomenti mostra l'help
        if (args.length == 0) {
            plugin.getMessageManager().sendInfo(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        // Gestisci comandi con token
        if (subCommand.startsWith("confirm-")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String token = subCommand.substring(8); // Rimuovi "confirm-"
                if (activeGUIs.containsKey(player.getUniqueId())) {
                    activeGUIs.get(player.getUniqueId()).handleConfirmCommand(player, token);
                    activeGUIs.remove(player.getUniqueId());
                }
            }
            return true;
        }
        
        if (subCommand.startsWith("cancel-")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String token = subCommand.substring(7); // Rimuovi "cancel-"
                if (activeGUIs.containsKey(player.getUniqueId())) {
                    activeGUIs.get(player.getUniqueId()).handleCancelCommand(player, token);
                    activeGUIs.remove(player.getUniqueId());
                }
            }
            return true;
        }
        
        if (subCommand.startsWith("cancelinput-")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String token = subCommand.substring(12); // Rimuovi "cancelinput-"
                if (activeGUIs.containsKey(player.getUniqueId())) {
                    activeGUIs.get(player.getUniqueId()).handleCancelInputCommand(player, token);
                    activeGUIs.remove(player.getUniqueId());
                }
            }
            return true;
        }
        
        switch (subCommand) {
            case "gui":
                return handleGui(sender);
                
            case "give":
                return handleGive(sender, args);
                
            case "remove":
                return handleRemove(sender);
                
            case "check":
                return handleCheck(sender);
                
            case "list":
                return handleList(sender);
                
            case "info":
                plugin.getMessageManager().sendInfo(sender);
                return true;
                
            case "reload":
                return handleReload(sender);

            case "update":
                return handleUpdate(sender);

            default:
                plugin.getMessageManager().sendInfo(sender);
                return true;
        }
    }
    
    private boolean handleGui(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("simpleitemsperms.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }
        
        PermissionGUI gui = new PermissionGUI(plugin, player);
        activeGUIs.put(player.getUniqueId(), gui);
        gui.open();
        plugin.getMessageManager().sendMessage(sender, "gui-opened");
        
        return true;
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("simpleitemsperms.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only");
            return true;
        }
        
        if (args.length < 3) {
            plugin.getMessageManager().sendMessage(sender, "give-usage");
            return true;
        }
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().sendMessage(sender, "give-no-item");
            return true;
        }
        
        String targetName = args[1];
        String permissionInput = args[2];
        
        // Valida il permesso (solo lettere, numeri, underscore e punti)
        if (!permissionInput.matches("^[a-zA-Z0-9_.]+$")) {
            plugin.getMessageManager().sendMessage(sender, "gui-invalid-permission");
            return true;
        }
        
        // Crea il permesso completo - CONTROLLA SE INIZIA GIÀ CON IL PREFISSO
        String fullPermission;
        if (permissionInput.toLowerCase().startsWith("simpleitemsperms.")) {
            // L'utente ha già scritto il prefisso completo
            fullPermission = permissionInput.toLowerCase();
        } else {
            // Aggiungi il prefisso
            fullPermission = "simpleitemsperms." + permissionInput.toLowerCase();
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{player}", targetName);
            plugin.getMessageManager().sendMessage(sender, "player-not-found", placeholders);
            return true;
        }
        
        ItemStack itemToGive = item.clone();
        ItemPermissionUtil.setPermission(itemToGive, fullPermission);
        
        // Registra l'item nel registro globale
        plugin.getItemRegistry().registerItem(itemToGive, fullPermission, player.getName());
        
        target.getInventory().addItem(itemToGive);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{player}", target.getName());
        placeholders.put("{permission}", fullPermission);
        
        plugin.getMessageManager().sendMessage(sender, "give-success", placeholders);
        plugin.getMessageManager().sendMessage(target, "give-success-receiver", placeholders);
        
        return true;
    }
    
    private boolean handleRemove(CommandSender sender) {
        if (!sender.hasPermission("simpleitemsperms.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().sendMessage(sender, "remove-no-item");
            return true;
        }
        
        String permission = ItemPermissionUtil.getPermission(item);
        
        if (permission == null) {
            plugin.getMessageManager().sendMessage(sender, "remove-no-permission-found");
            return true;
        }
        
        ItemPermissionUtil.removePermission(item);
        
        // Rimuovi il prefisso per mostrare solo il suffisso
        String suffix = permission.replace("simpleitemsperms.", "");
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{permission}", suffix);
        plugin.getMessageManager().sendMessage(sender, "remove-success", placeholders);
        
        return true;
    }
    
    private boolean handleCheck(CommandSender sender) {
        if (!sender.hasPermission("simpleitemsperms.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            plugin.getMessageManager().sendMessage(sender, "check-no-item");
            return true;
        }
        
        String permission = ItemPermissionUtil.getPermission(item);
        
        if (permission == null) {
            plugin.getMessageManager().sendMessage(sender, "check-no-permission");
        } else {
            // Rimuovi il prefisso per mostrare solo il suffisso
            String suffix = permission.replace("simpleitemsperms.", "");
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{permission}", suffix);
            plugin.getMessageManager().sendMessage(sender, "check-has-permission", placeholders);
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("simpleitemsperms.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        new it.alessiogta.simpleItemsPerms.gui.ItemsListGUI(plugin, player).open();
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("simpleitemsperms.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }

        plugin.reload();
        plugin.getMessageManager().sendMessage(sender, "reload-success");

        return true;
    }

    private boolean handleUpdate(CommandSender sender) {
        if (!sender.hasPermission("simpleitemsperms.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return true;
        }

        // Controlla se l'update checker è abilitato
        if (plugin.getUpdateChecker() == null) {
            sender.sendMessage(plugin.getMessageManager().getMessage("update-error"));
            return true;
        }

        // Invia messaggio "controllo..."
        plugin.getMessageManager().sendMessage(sender, "update-checking");

        // Controlla aggiornamenti in modo asincrono
        plugin.getUpdateChecker().checkForUpdates(result -> {
            if (result.updateAvailable) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("{current}", plugin.getUpdateChecker().getCurrentVersion());
                placeholders.put("{latest}", result.latestVersion);
                placeholders.put("{url}", result.downloadUrl != null ? result.downloadUrl : "https://modrinth.com/plugin/simpleitemsperms");

                plugin.getMessageManager().sendMessage(sender, "update-available", placeholders);
                if (result.downloadUrl != null) {
                    plugin.getMessageManager().sendMessage(sender, "update-download", placeholders);
                }
            } else {
                plugin.getMessageManager().sendMessage(sender, "update-not-available");
            }
        });

        return true;
    }
}
