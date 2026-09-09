package joni.status4discordmc.libs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

public class UpdateChecker {

    private static final String API_BASE = "https://api.modrinth.com/v2/project/";
    private static final long CHECK_INTERVAL_MS = 12 * 60 * 60 * 1000L;

    private final Logger logger;
    private final String userAgent;
    private final String projectId;
    private final String currentVersion;
    private final List<String> loaders;
    private final List<String> gameVersions;
    private final Executor executor;

    private volatile VersionInfo latestKnown;
    private volatile long lastCheck = 0L;

    /**
     * Simple constructor for the common case (synchronous fetch, all loaders/game versions)
     * VERSION SCHEME: MAJOR.MINOR.PATCH[-PRERELEASE]
     * Examples: "1.4.0", "1.4.1-beta.2", "2.0.0-rc.1"
     *
     * @param plugin         - plugin instance for logging and user-agent
     * @param projectId      - modrinth id
     * @param currentVersion - current plugin version
     * @param loaders        - loaders can be NULL
     * @param gameVersions   - game versions can be NULL
     */
    public UpdateChecker(JavaPlugin plugin, String projectId, String currentVersion, List<String> loaders, List<String> gameVersions) {
        this.logger = plugin.getLogger();
        this.userAgent = plugin.getName() + "/" + currentVersion;
        this.projectId = projectId;
        this.currentVersion = currentVersion;
        this.loaders = loaders;
        this.gameVersions = gameVersions;
        this.executor = ForkJoinPool.commonPool();

        // check for new update
        isUpdateAvailable();
    }

    private static String toJsonArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append('"').append(items.get(i)).append('"');
            if (i < items.size() - 1) sb.append(',');
        }
        return sb.append(']').toString();
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /**
     * Cached check (re-fetches only every 12h), resolves true if a newer version than currentVersion exists.
     */
    public CompletableFuture<Boolean> isUpdateAvailableWithCache() {
        if ((System.currentTimeMillis() - lastCheck) >= CHECK_INTERVAL_MS) {
            return checkForUpdate().thenApply(this::isNewerThanCurrent);
        }
        return CompletableFuture.completedFuture(isNewerThanCurrent(latestKnown));
    }

    /**
     * Internet check (always fetches), resolves true if a newer version than currentVersion exists.
     */
    public CompletableFuture<Boolean> isUpdateAvailable() {
        return checkForUpdate().thenApply(this::isNewerThanCurrent);
    }

    /**
     * Returns the latest known version number, or "unknown" if not yet fetched.
     */
    public String getLatestVersion() {
        return latestKnown != null ? latestKnown.versionNumber() : "unknown";
    }

    private CompletableFuture<VersionInfo> checkForUpdate() {
        logger.info("Checking for updates...");
        return CompletableFuture.supplyAsync(this::fetchLatest, executor)
                .whenComplete((info, ex) -> {
                    if (ex != null) {
                        logger.warning("Failed to check for update via Modrinth: " + ex.getMessage());
                        return;
                    }
                    lastCheck = System.currentTimeMillis();
                    if (info != null) {
                        latestKnown = info;
                        if (isNewerThanCurrent(info)) {
                            logger.info("A new version is available! Current: " + currentVersion + " Latest: " + info.versionNumber());
                            logger.info("https://modrinth.com/plugin/" + projectId + "/version/" + info.versionNumber());
                        } else {
                            logger.info("You are on the latest version! (" + currentVersion + ")");
                        }
                    }
                });
    }

    private boolean isNewerThanCurrent(VersionInfo info) {
        return info != null && isNewerVersion(currentVersion, info.versionNumber());
    }

    private VersionInfo fetchLatest() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(buildUrl()).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", userAgent);

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();
                if (versions.isEmpty()) {
                    logger.warning("No versions found for Modrinth project " + projectId + " matching the given loaders/game versions");
                    return null;
                }

                List<VersionInfo> parsed = getVersionInfos(versions);

                if (parsed.isEmpty()) return null;

                // Sort by actual publish time, newest first - not by parsing version_number.
                parsed.sort(Comparator.comparing((VersionInfo v) -> Instant.parse(v.datePublished())).reversed());
                return parsed.get(0);
            }
        } catch (Exception ex) {
            logger.warning("Modrinth update check failed: " + ex.getMessage());
            return null;
        }
    }

    private List<VersionInfo> getVersionInfos(JsonArray versions) {
        List<VersionInfo> parsed = new ArrayList<>();
        for (var el : versions) {
            JsonObject obj = el.getAsJsonObject();
            String type = obj.get("version_type").getAsString();
            String number = obj.get("version_number").getAsString();
            String date = obj.get("date_published").getAsString();
            parsed.add(new VersionInfo(number, type, date,
                    "https://modrinth.com/plugin/" + projectId + "/version/" + number));
        }
        return parsed;
    }

    private String buildUrl() {
        StringBuilder sb = new StringBuilder(API_BASE).append(projectId).append("/version");
        List<String> params = new ArrayList<>();
        if (loaders != null && !loaders.isEmpty()) params.add("loaders=" + encode(toJsonArray(loaders)));
        if (gameVersions != null && !gameVersions.isEmpty())
            params.add("game_versions=" + encode(toJsonArray(gameVersions)));
        if (!params.isEmpty()) sb.append("?").append(String.join("&", params));
        return sb.toString();
    }

    private boolean isNewerVersion(String current, String latest) {
        String[] currentParts = current.split("-", 2);
        String[] latestParts = latest.split("-", 2);

        String[] currentNumeric = currentParts[0].split("\\.");
        String[] latestNumeric = latestParts[0].split("\\.");

        int maxLength = Math.max(currentNumeric.length, latestNumeric.length);
        for (int i = 0; i < maxLength; i++) {
            int currentVal = i < currentNumeric.length ? Integer.parseInt(currentNumeric[i]) : 0;
            int latestVal = i < latestNumeric.length ? Integer.parseInt(latestNumeric[i]) : 0;

            if (latestVal > currentVal) return true;
            if (latestVal < currentVal) return false;
        }

        String currentPre = currentParts.length > 1 ? currentParts[1] : null;
        String latestPre = latestParts.length > 1 ? latestParts[1] : null;

        if (currentPre != null && latestPre == null) return true;
        if (currentPre == null && latestPre != null) return false;
        if (currentPre == null) return false;

        String[] currentPreParts = currentPre.split("\\.");
        String[] latestPreParts = latestPre.split("\\.");

        int preMaxLength = Math.max(currentPreParts.length, latestPreParts.length);
        for (int i = 0; i < preMaxLength; i++) {
            String cPart = i < currentPreParts.length ? currentPreParts[i] : "";
            String lPart = i < latestPreParts.length ? latestPreParts[i] : "";

            if (cPart.isEmpty() && !lPart.isEmpty()) return true;
            if (!cPart.isEmpty() && lPart.isEmpty()) return false;

            try {
                int cNum = Integer.parseInt(cPart);
                int lNum = Integer.parseInt(lPart);
                if (lNum > cNum) return true;
                if (lNum < cNum) return false;
            } catch (NumberFormatException e) {
                int comparison = lPart.compareTo(cPart);
                if (comparison > 0) return true;
                if (comparison < 0) return false;
            }
        }
        return false;
    }

    public record VersionInfo(String versionNumber, String versionType, String datePublished, String downloadPage) {
    }
}