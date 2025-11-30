package it.alessiogta.simpleItemsPerms.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.alessiogta.simpleItemsPerms.SimpleItemsPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Controlla automaticamente se ci sono aggiornamenti disponibili
 * su Spigot e Modrinth
 */
public class UpdateChecker {

    private final SimpleItemsPerms plugin;
    private final String currentVersion;

    // IDs per le API
    private static final String SPIGOT_RESOURCE_ID = "119801"; // Sostituisci con il tuo ID Spigot quando pubblichi
    private static final String MODRINTH_PROJECT_ID = "simpleitemsperms"; // Sostituisci con il tuo project slug

    // URLs API
    private static final String SPIGOT_API = "https://api.spigotmc.org/legacy/update.php?resource=";
    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/";

    // Cache
    private String latestVersion = null;
    private String downloadUrl = null;
    private String changelog = null;
    private long lastCheck = 0;
    private static final long CACHE_TIME = 3600000; // 1 ora in millisecondi

    public UpdateChecker(SimpleItemsPerms plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    /**
     * Controlla aggiornamenti in modo asincrono
     */
    public void checkForUpdates(Consumer<UpdateResult> callback) {
        // Se abbiamo una cache valida, usa quella
        if (lastCheck > 0 && (System.currentTimeMillis() - lastCheck) < CACHE_TIME && latestVersion != null) {
            callback.accept(new UpdateResult(
                isUpdateAvailable(),
                latestVersion,
                downloadUrl,
                changelog
            ));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                // Prova prima Modrinth (più affidabile)
                if (checkModrinth()) {
                    lastCheck = System.currentTimeMillis();
                    Bukkit.getScheduler().runTask(plugin, () ->
                        callback.accept(new UpdateResult(
                            isUpdateAvailable(),
                            latestVersion,
                            downloadUrl,
                            changelog
                        ))
                    );
                    return;
                }

                // Fallback a Spigot
                if (checkSpigot()) {
                    lastCheck = System.currentTimeMillis();
                    Bukkit.getScheduler().runTask(plugin, () ->
                        callback.accept(new UpdateResult(
                            isUpdateAvailable(),
                            latestVersion,
                            downloadUrl,
                            changelog
                        ))
                    );
                    return;
                }

                // Nessuna API ha funzionato
                Bukkit.getScheduler().runTask(plugin, () ->
                    callback.accept(new UpdateResult(false, null, null, null))
                );

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                    callback.accept(new UpdateResult(false, null, null, null))
                );
            }
        });
    }

    /**
     * Controlla aggiornamenti su Spigot
     */
    private boolean checkSpigot() {
        try {
            URL url = new URL(SPIGOT_API + SPIGOT_RESOURCE_ID);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return false;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String version = reader.readLine();
            reader.close();

            if (version != null && !version.isEmpty()) {
                latestVersion = version;
                downloadUrl = "https://www.spigotmc.org/resources/simpleitemsperms." + SPIGOT_RESOURCE_ID + "/";
                changelog = null; // Spigot API non fornisce changelog
                return true;
            }

        } catch (Exception e) {
            plugin.getLogger().fine("Spigot update check failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Controlla aggiornamenti su Modrinth
     */
    private boolean checkModrinth() {
        try {
            URL url = new URL(MODRINTH_API + MODRINTH_PROJECT_ID + "/version");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "SimpleItemsPerms/" + currentVersion);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return false;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON response (Modrinth restituisce array di versioni)
            String jsonResponse = response.toString();
            if (jsonResponse.startsWith("[") && jsonResponse.length() > 2) {
                // Prendi la prima versione (la più recente)
                int firstObjectEnd = jsonResponse.indexOf("},{");
                if (firstObjectEnd == -1) {
                    firstObjectEnd = jsonResponse.length() - 1;
                }
                String firstVersion = jsonResponse.substring(1, firstObjectEnd);

                JsonObject versionObj = JsonParser.parseString(firstVersion).getAsJsonObject();

                latestVersion = versionObj.get("version_number").getAsString();

                // URL download
                if (versionObj.has("files") && versionObj.get("files").isJsonArray()) {
                    JsonObject firstFile = versionObj.get("files").getAsJsonArray().get(0).getAsJsonObject();
                    downloadUrl = firstFile.get("url").getAsString();
                }

                // Changelog
                if (versionObj.has("changelog")) {
                    changelog = versionObj.get("changelog").getAsString();
                    // Limita changelog a primi 200 caratteri
                    if (changelog.length() > 200) {
                        changelog = changelog.substring(0, 200) + "...";
                    }
                }

                return true;
            }

        } catch (Exception e) {
            plugin.getLogger().fine("Modrinth update check failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica se c'è un aggiornamento disponibile
     */
    public boolean isUpdateAvailable() {
        if (latestVersion == null) {
            return false;
        }

        return compareVersions(latestVersion, currentVersion) > 0;
    }

    /**
     * Confronta due versioni (es: 1.2.0 vs 1.1.9)
     * @return > 0 se v1 è più recente, < 0 se v2 è più recente, 0 se uguali
     */
    private int compareVersions(String v1, String v2) {
        try {
            String[] parts1 = v1.replaceAll("[^0-9.]", "").split("\\.");
            String[] parts2 = v2.replaceAll("[^0-9.]", "").split("\\.");

            int maxLength = Math.max(parts1.length, parts2.length);
            for (int i = 0; i < maxLength; i++) {
                int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
                int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

                if (num1 != num2) {
                    return num1 - num2;
                }
            }
            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to compare versions: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Invia notifica aggiornamento a un player
     */
    public void notifyPlayer(Player player) {
        if (latestVersion == null) {
            return;
        }

        if (!isUpdateAvailable()) {
            return;
        }

        MessageManager msgManager = plugin.getMessageManager();

        // Invia messaggio update-available
        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("{current}", currentVersion);
        placeholders.put("{latest}", latestVersion);
        placeholders.put("{url}", downloadUrl != null ? downloadUrl : "https://github.com/Ghibli/SimpleItemsPerm");

        msgManager.sendMessage(player, "update-available", placeholders);

        if (downloadUrl != null) {
            msgManager.sendMessage(player, "update-download", placeholders);
        }
    }

    /**
     * Classe risultato controllo aggiornamenti
     */
    public static class UpdateResult {
        public final boolean updateAvailable;
        public final String latestVersion;
        public final String downloadUrl;
        public final String changelog;

        public UpdateResult(boolean updateAvailable, String latestVersion, String downloadUrl, String changelog) {
            this.updateAvailable = updateAvailable;
            this.latestVersion = latestVersion;
            this.downloadUrl = downloadUrl;
            this.changelog = changelog;
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
