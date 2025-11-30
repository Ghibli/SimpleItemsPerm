package it.alessiogta.simpleItemsPerms;

import it.alessiogta.simpleItemsPerms.commands.SipCommand;
import it.alessiogta.simpleItemsPerms.commands.SipTabCompleter;
import it.alessiogta.simpleItemsPerms.listeners.*;
import it.alessiogta.simpleItemsPerms.metrics.Metrics;
import it.alessiogta.simpleItemsPerms.utils.ConfigManager;
import it.alessiogta.simpleItemsPerms.utils.MessageManager;
import it.alessiogta.simpleItemsPerms.utils.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * SimpleItemsPerms - Sistema di permessi per items specifici
 *
 * @author AlessioGTAII
 * @version 1.0.0
 *
 * Developed with ❤ by AlessioGTAII for Simple Survival
 */
public class SimpleItemsPerms extends JavaPlugin {

    private static SimpleItemsPerms instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private ItemRegistry itemRegistry;

    @Override
    public void onEnable() {
        instance = this;

        // ASCII Art
        getLogger().info("╔═══════════════════════════════════════════════════════════════╗");
        getLogger().info("║              SimpleItemsPerms v1.1.0                          ║");
        getLogger().info("║          Developed with ❤ by AlessioGTAII                    ║");
        getLogger().info("╚═══════════════════════════════════════════════════════════════╝");

        // Salva config e file di default
        saveDefaultConfig();

        // Assicurati che i file lingua esistano
        ensureLanguageFiles();

        // Inizializza managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        itemRegistry = new ItemRegistry(this);

        // Inizializza bStats (versione embedded)
        initMetrics();

        // Registra comandi
        getCommand("sip").setExecutor(new SipCommand(this));
        getCommand("sip").setTabCompleter(new SipTabCompleter());

        // Registra listeners
        registerListeners();

        // Task per pulire token scaduti ogni 5 minuti
        getServer().getScheduler().runTaskTimer(this, () -> {
            it.alessiogta.simpleItemsPerms.gui.PermissionGUI.cleanupExpiredTokens();
        }, 6000L, 6000L); // 6000 ticks = 5 minuti

        getLogger().info("Plugin abilitato con successo!");
    }

    @Override
    public void onDisable() {
        getLogger().info("╔═══════════════════════════════════════════════════════════════╗");
        getLogger().info("║              SimpleItemsPerms disabilitato                    ║");
        getLogger().info("║              Grazie per aver usato il plugin!                 ║");
        getLogger().info("╚═══════════════════════════════════════════════════════════════╝");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ItemUseListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorEquipListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemConsumeListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemDropListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemPickupListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileShootListener(this), this);
        getServer().getPluginManager().registerEvents(new LoreUpdateListener(this), this);
    }

    public void reload() {
        configManager.reload();
        messageManager.reload();
        itemRegistry.reload();
    }

    public static SimpleItemsPerms getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    /**
     * Assicura che i file lingua siano estratti dal JAR
     */
    private void ensureLanguageFiles() {
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            getLogger().info("Created lang folder");
        }

        // Salva i file lingua se non esistono
        try {
            saveResource("lang/messages_en_US.yml", false);
            getLogger().info("✓ messages_en_US.yml extracted");
        } catch (Exception e) {
            getLogger().warning("Could not extract messages_en_US.yml: " + e.getMessage());
        }

        try {
            saveResource("lang/messages_it_IT.yml", false);
            getLogger().info("✓ messages_it_IT.yml extracted");
        } catch (Exception e) {
            getLogger().warning("Could not extract messages_it_IT.yml: " + e.getMessage());
        }

        getLogger().info("✓ Language files ready");
    }

    /**
     * Inizializza bStats metrics (versione embedded)
     */
    private void initMetrics() {
        try {
            int pluginId = 28099;
            Metrics metrics = new Metrics(this, pluginId);

            // Chart 1: Total Items (Single Line Chart)
            metrics.addCustomChart(new Metrics.SingleLineChart("total_items", () -> {
                return itemRegistry.getAllItemUUIDs().size();
            }));

            // Chart 2: Items Distribution (Simple Pie)
            metrics.addCustomChart(new Metrics.SimplePie("items_distribution", () -> {
                int count = itemRegistry.getAllItemUUIDs().size();
                if (count == 0) return "No items";
                if (count <= 5) return "1-5 items";
                if (count <= 10) return "6-10 items";
                if (count <= 25) return "11-25 items";
                if (count <= 50) return "26-50 items";
                if (count <= 100) return "51-100 items";
                return "100+ items";
            }));

            // Chart 3: Server Language (Simple Pie)
            metrics.addCustomChart(new Metrics.SimplePie("server_language", () -> {
                String lang = getConfig().getString("language", "en_US");
                if (lang.equals("en_US")) return "English";
                if (lang.equals("it_IT")) return "Italian";
                return "Other (" + lang + ")";
            }));

            // Chart 4: Permission Types (Advanced Pie) - TUTTI i permessi usati!
            metrics.addCustomChart(new Metrics.AdvancedPie("permission_types", () -> {
                Map<String, Integer> permCounts = new HashMap<>();
                
                // Conta items per ogni permesso
                for (String uuid : itemRegistry.getAllItemUUIDs()) {
                    ItemRegistry.ItemInfo info = itemRegistry.getItemInfo(uuid);
                    if (info != null) {
                        // Rimuovi "simpleitemsperms." dal permesso per rendere più leggibile
                        String perm = info.permission.replace("simpleitemsperms.", "");
                        permCounts.put(perm, permCounts.getOrDefault(perm, 0) + 1);
                    }
                }
                
                return permCounts.isEmpty() ? null : permCounts;
            }));

            // Chart 5: Hide Lore Setting (Simple Pie)
            metrics.addCustomChart(new Metrics.SimplePie("hide_lore_setting", () -> {
                boolean hide = configManager.shouldHideLoreForPermitted();
                return hide ? "Enabled" : "Disabled";
            }));

            // Chart 6: Server Size (Simple Pie)
            metrics.addCustomChart(new Metrics.SimplePie("server_size", () -> {
                int players = Bukkit.getOnlinePlayers().size();
                if (players == 0) return "Empty";
                if (players <= 10) return "Small (1-10)";
                if (players <= 50) return "Medium (11-50)";
                if (players <= 100) return "Large (51-100)";
                return "Very Large (100+)";
            }));

            getLogger().info("✓ bStats metrics abilitato con 6 custom charts!");
            
        } catch (Exception e) {
            getLogger().warning("Impossibile inizializzare bStats: " + e.getMessage());
            e.printStackTrace();
        }
    }
}