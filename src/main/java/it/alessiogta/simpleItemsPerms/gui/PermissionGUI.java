package it.alessiogta.simpleItemsPerms.gui;

import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import it.alessiogta.simpleItemsPerms.utils.ColorUtil;
import it.alessiogta.simpleItemsPerms.utils.ItemPermissionUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PermissionGUI implements Listener {
    
    private final SimpleItemsPerms plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<UUID, List<ItemStack>> pendingItems;
    private final Map<UUID, String> pendingPermissions;
    private static final Set<UUID> awaitingInput = new HashSet<>();
    private static final Set<UUID> awaitingConfirmation = new HashSet<>();
    
    // Sistema di token
    private static final Map<String, TokenData> activeTokens = new HashMap<>();
    private static final long TOKEN_EXPIRY_MS = 60000; // 60 secondi
    
    private final int confirmSlot;
    private final int cancelSlot;
    
    public PermissionGUI(SimpleItemsPerms plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.pendingItems = new HashMap<>();
        this.pendingPermissions = new HashMap<>();
        
        int size = plugin.getConfigManager().getGuiSize();
        String title = plugin.getMessageManager().getMessage("gui-title");
        
        this.inventory = Bukkit.createInventory(null, size, title);
        
        // Carica i pulsanti dalla config
        this.confirmSlot = plugin.getConfig().getInt("gui.confirm-button.slot", 49);
        this.cancelSlot = plugin.getConfig().getInt("gui.cancel-button.slot", 53);
        
        setupButtons();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    // Classe interna per gestire i token
    private static class TokenData {
        final UUID playerUUID;
        final long createdAt;
        final String type; // "input" o "confirm"
        
        TokenData(UUID playerUUID, String type) {
            this.playerUUID = playerUUID;
            this.createdAt = System.currentTimeMillis();
            this.type = type;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > TOKEN_EXPIRY_MS;
        }
    }
    
    // Genera un token casuale
    private static String generateToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    // Valida un token
    public static boolean validateToken(String token, UUID playerUUID, String expectedType) {
        TokenData data = activeTokens.get(token);
        if (data == null) return false;
        if (data.isExpired()) {
            activeTokens.remove(token);
            return false;
        }
        if (!data.playerUUID.equals(playerUUID)) return false;
        if (!data.type.equals(expectedType)) return false;
        
        return true;
    }
    
    // Consuma un token (rimuovilo dopo l'uso)
    public static void consumeToken(String token) {
        activeTokens.remove(token);
    }
    
    // Pulisci token scaduti
    public static void cleanupExpiredTokens() {
        activeTokens.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    private void setupButtons() {
        // Pulsante conferma
        ItemStack confirmButton = new ItemStack(Material.valueOf(
                plugin.getConfig().getString("gui.confirm-button.material", "LIME_CONCRETE")
        ));
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.setDisplayName(ColorUtil.color(
                plugin.getMessageManager().getMessage("gui-confirm-button-name")
        ));
        List<String> confirmLore = new ArrayList<>();
        for (String line : plugin.getMessageManager().getMessageList("gui-confirm-button-lore")) {
            confirmLore.add(line);
        }
        confirmMeta.setLore(confirmLore);
        confirmButton.setItemMeta(confirmMeta);
        inventory.setItem(confirmSlot, confirmButton);
        
        // Pulsante annulla
        ItemStack cancelButton = new ItemStack(Material.valueOf(
                plugin.getConfig().getString("gui.cancel-button.material", "RED_CONCRETE")
        ));
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(ColorUtil.color(
                plugin.getMessageManager().getMessage("gui-cancel-button-name")
        ));
        List<String> cancelLore = new ArrayList<>();
        for (String line : plugin.getMessageManager().getMessageList("gui-cancel-button-lore")) {
            cancelLore.add(line);
        }
        cancelMeta.setLore(cancelLore);
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(cancelSlot, cancelButton);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        
        int slot = event.getRawSlot();
        
        // Blocca il click sui pulsanti
        if (slot == confirmSlot) {
            event.setCancelled(true);
            handleConfirm();
            return;
        }
        
        if (slot == cancelSlot) {
            event.setCancelled(true);
            handleCancel();
            return;
        }
        
        // Permetti di mettere/togliere items negli altri slot
        if (slot < inventory.getSize()) {
            // Slot nella GUI
            return;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!event.getPlayer().equals(player)) return;
        
        // Se il giocatore sta aspettando l'input, non fare nulla
        if (awaitingInput.contains(player.getUniqueId())) return;
        
        // Restituisci gli items se la GUI viene chiusa senza confermare
        if (!pendingItems.containsKey(player.getUniqueId())) {
            returnItems();
        }
    }
    
    private void handleConfirm() {
        List<ItemStack> items = collectItems();
        
        if (items.isEmpty()) {
            plugin.getMessageManager().sendMessage(player, "gui-no-items");
            player.closeInventory();
            return;
        }
        
        pendingItems.put(player.getUniqueId(), items);
        awaitingInput.add(player.getUniqueId());
        player.closeInventory();
        
        // Invia il messaggio formattato con cornice
        sendPermissionPrompt(player, items.size());
    }
    
    private void sendPermissionPrompt(Player player, int itemCount) {
        // Genera token per questa sessione
        String cancelToken = generateToken();
        activeTokens.put(cancelToken, new TokenData(player.getUniqueId(), "input"));
        
        // Riga vuota
        player.sendMessage("");
        
        // Messaggio principale con cornice gialla
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{amount}", String.valueOf(itemCount));
        placeholders.put("{plural}", itemCount > 1 ? "s" : "");
        String prompt = plugin.getMessageManager().getMessage("gui-chat-prompt", placeholders);
        player.sendMessage(ColorUtil.color(prompt));
        player.sendMessage("");
        
        // "Scrivi ANNULLA per annullare"
        String cancelMsg = plugin.getMessageManager().getMessage("gui-chat-cancel");
        player.sendMessage(ColorUtil.color(cancelMsg));
        
        // Componente ANNULLA (rosso, cliccabile)
        TextComponent cancelComponent = new TextComponent("✘ ");
        cancelComponent.setColor(net.md_5.bungee.api.ChatColor.RED);
        cancelComponent.setBold(true);
        
        String clickCancelText = plugin.getMessageManager().getMessage("gui-chat-click-cancel");
        String clickCancelHover = plugin.getMessageManager().getMessage("gui-chat-click-cancel-hover");
        
        TextComponent cancelText = new TextComponent(clickCancelText);
        cancelText.setColor(net.md_5.bungee.api.ChatColor.RED);
        cancelText.setBold(true);
        cancelText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sip cancelinput-" + cancelToken));
        cancelText.setHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder(clickCancelHover).create()
        ));
        
        cancelComponent.addExtra(cancelText);
        player.spigot().sendMessage(cancelComponent);
        
        // Riga vuota
        player.sendMessage("");
    }
    
    private void handleCancel() {
        plugin.getMessageManager().sendMessage(player, "gui-cancelled");
        returnItems();
        player.closeInventory();
    }
    
    private List<ItemStack> collectItems() {
        List<ItemStack> items = new ArrayList<>();
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == confirmSlot || i == cancelSlot) continue;
            
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) {
                items.add(item.clone());
            }
        }
        
        return items;
    }
    
    private void returnItems() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == confirmSlot || i == cancelSlot) continue;
            
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) {
                player.getInventory().addItem(item);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player chatPlayer = event.getPlayer();
        
        // Step 1: Player scrive il nome del permesso
        if (awaitingInput.contains(chatPlayer.getUniqueId()) && 
            !awaitingConfirmation.contains(chatPlayer.getUniqueId())) {
            
            if (!pendingItems.containsKey(chatPlayer.getUniqueId())) return;
            
            event.setCancelled(true);
            String permissionInput = event.getMessage().trim();
            
            // Valida il suffisso del permesso (solo lettere, numeri, underscore, punti)
            if (!permissionInput.matches("^[a-zA-Z0-9_.]+$")) {
                plugin.getMessageManager().sendMessage(chatPlayer, "gui-invalid-permission");
                awaitingInput.remove(chatPlayer.getUniqueId());
                
                // Restituisci gli items
                Bukkit.getScheduler().runTask(plugin, () -> {
                    List<ItemStack> items = pendingItems.get(chatPlayer.getUniqueId());
                    if (items != null) {
                        for (ItemStack item : items) {
                            chatPlayer.getInventory().addItem(item);
                        }
                    }
                    pendingItems.remove(chatPlayer.getUniqueId());
                });
                return;
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
            
            pendingPermissions.put(chatPlayer.getUniqueId(), fullPermission);
            
            // Passa alla fase di conferma
            awaitingInput.remove(chatPlayer.getUniqueId());
            awaitingConfirmation.add(chatPlayer.getUniqueId());
            
            // Invia messaggio di conferma con pulsanti cliccabili
            Bukkit.getScheduler().runTask(plugin, () -> {
                sendConfirmationMessage(chatPlayer, fullPermission);
            });
            return;
        }
    }
    
    private void sendConfirmationMessage(Player player, String permission) {
        // Genera token per questa sessione
        String confirmToken = generateToken();
        String cancelToken = generateToken();
        activeTokens.put(confirmToken, new TokenData(player.getUniqueId(), "confirm"));
        activeTokens.put(cancelToken, new TokenData(player.getUniqueId(), "confirm"));
        
        // Riga vuota
        player.sendMessage("");
        
        // Messaggio principale con il permesso
        player.sendMessage(ColorUtil.color("&6⚠ &eStai per assegnare il permesso: &6" + permission));
        player.sendMessage("");
        
        // Componente ANNULLA (rosso, cliccabile)
        TextComponent cancelComponent = new TextComponent("✘ ");
        cancelComponent.setColor(net.md_5.bungee.api.ChatColor.RED);
        cancelComponent.setBold(true);
        
        String clickCancelText2 = plugin.getMessageManager().getMessage("gui-chat-click-cancel");
        String clickCancelHover2 = plugin.getMessageManager().getMessage("gui-chat-click-cancel-operation");
        
        TextComponent cancelText = new TextComponent(clickCancelText2);
        cancelText.setColor(net.md_5.bungee.api.ChatColor.RED);
        cancelText.setBold(true);
        cancelText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sip cancel-" + cancelToken));
        cancelText.setHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder(clickCancelHover2).create()
        ));
        
        cancelComponent.addExtra(cancelText);
        player.spigot().sendMessage(cancelComponent);
        
        // Riga vuota
        player.sendMessage("");
        
        // Componente CONFERMA (verde/giallo, cliccabile)
        TextComponent confirmComponent = new TextComponent("✔ ");
        confirmComponent.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        confirmComponent.setBold(true);
        
        String clickConfirmText = plugin.getMessageManager().getMessage("gui-chat-click-confirm");
        String clickConfirmHover = plugin.getMessageManager().getMessage("gui-chat-click-confirm-hover");
        
        TextComponent confirmText = new TextComponent(clickConfirmText);
        confirmText.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        confirmText.setBold(true);
        confirmText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sip confirm-" + confirmToken));
        confirmText.setHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder(clickConfirmHover).create()
        ));
        
        confirmComponent.addExtra(confirmText);
        player.spigot().sendMessage(confirmComponent);
        
        // Riga vuota
        player.sendMessage("");
    }
    
    public void handleConfirmCommand(Player player, String token) {
        if (!validateToken(token, player.getUniqueId(), "confirm")) {
            plugin.getMessageManager().sendMessage(player, "token-invalid");
            return;
        }
        
        if (!awaitingConfirmation.contains(player.getUniqueId())) return;
        if (!pendingItems.containsKey(player.getUniqueId())) return;
        if (!pendingPermissions.containsKey(player.getUniqueId())) return;
        
        String permission = pendingPermissions.get(player.getUniqueId());
        List<ItemStack> items = pendingItems.get(player.getUniqueId());
        
        // Assegna il permesso agli items
        int count = 0;
        for (ItemStack item : items) {
            ItemStack modifiedItem = ItemPermissionUtil.setPermission(item, permission);
            
            // Registra l'item nel registro globale
            plugin.getItemRegistry().registerItem(modifiedItem, permission, player.getName());
            
            player.getInventory().addItem(modifiedItem);
            count++;
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{permission}", permission);
        placeholders.put("{amount}", String.valueOf(count));
        plugin.getMessageManager().sendMessage(player, "gui-permission-assigned", placeholders);
        
        // Cleanup
        consumeToken(token);
        awaitingConfirmation.remove(player.getUniqueId());
        pendingItems.remove(player.getUniqueId());
        pendingPermissions.remove(player.getUniqueId());
    }
    
    public void handleCancelCommand(Player player, String token) {
        if (!validateToken(token, player.getUniqueId(), "confirm")) {
            plugin.getMessageManager().sendMessage(player, "token-invalid");
            return;
        }
        
        if (!awaitingConfirmation.contains(player.getUniqueId())) return;
        
        // Restituisci gli items
        if (pendingItems.containsKey(player.getUniqueId())) {
            for (ItemStack item : pendingItems.get(player.getUniqueId())) {
                player.getInventory().addItem(item);
            }
        }
        
        plugin.getMessageManager().sendMessage(player, "gui-cancelled");
        
        // Cleanup
        consumeToken(token);
        awaitingConfirmation.remove(player.getUniqueId());
        pendingItems.remove(player.getUniqueId());
        pendingPermissions.remove(player.getUniqueId());
    }
    
    public void handleCancelInputCommand(Player player, String token) {
        if (!validateToken(token, player.getUniqueId(), "input")) {
            plugin.getMessageManager().sendMessage(player, "token-invalid");
            return;
        }
        
        if (!awaitingInput.contains(player.getUniqueId())) return;
        
        // Restituisci gli items
        if (pendingItems.containsKey(player.getUniqueId())) {
            for (ItemStack item : pendingItems.get(player.getUniqueId())) {
                player.getInventory().addItem(item);
            }
        }
        
        plugin.getMessageManager().sendMessage(player, "gui-cancelled");
        
        // Cleanup
        consumeToken(token);
        awaitingInput.remove(player.getUniqueId());
        pendingItems.remove(player.getUniqueId());
    }
    
    public static boolean isAwaitingInput(UUID uuid) {
        return awaitingInput.contains(uuid) || awaitingConfirmation.contains(uuid);
    }
}
