package me.superneon4ik.noxesiumutils.modules;

import com.google.gson.Gson;
import kong.unirest.Unirest;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.enums.VersionStatus;
import me.superneon4ik.noxesiumutils.objects.ModrinthVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModrinthUpdateChecker {
    @Getter private final JavaPlugin plugin;
    @Getter private final String slug;
    @Getter private VersionStatus latestStatus;
    private String latestKnownVersion = null;

    public ModrinthUpdateChecker(JavaPlugin plugin, String slug) {
        this.plugin = plugin;
        this.slug = slug;
        this.latestStatus = VersionStatus.NOT_CHECKED;
    }

    /**
     * Run this once in {@code onEnable} to start checking for updates.
     * @param refreshTicks The delay between refreshes in Minecraft ticks.
     */
    public void beginChecking(int refreshTicks) {
        new BukkitRunnable() {
            boolean firstCheck = true;
            boolean reportedOutdatedStatus = false;

            @Override
            public void run() {
                var future = checkForUpdates();
                future.thenAccept(versionStatus -> {
                    if (versionStatus == VersionStatus.OUTDATED && !reportedOutdatedStatus) {
                        Bukkit.getConsoleSender().sendMessage(generateVersionMessage(versionStatus));
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.isOp()) player.sendMessage(generateVersionMessage(versionStatus));
                        }
                        reportedOutdatedStatus = true;
                    }
                    else if (firstCheck) {
                        Bukkit.getConsoleSender().sendMessage(generateVersionMessage(versionStatus));
                    }
                    firstCheck = false;
                });
            }
        }.runTaskTimerAsynchronously(plugin, 1, refreshTicks);
    }

    /**
     * Asynchronously fetches the latest available version of the plugin
     * on Modrinth for the current server's version and compares it against
     * the installed plugin's version.
     */
    public CompletableFuture<VersionStatus> checkForUpdates() {
        CompletableFuture<VersionStatus> future = new CompletableFuture<>();
        if (getCurrentPluginVersion().endsWith("-dev")) {
            future.complete(VersionStatus.DEVELOPMENT);
            return future;
        }
        
        String uri = "https://api.modrinth.com/v2/project/%s/version?game_versions=%%5B%%22%s%%22%%5D"
                .formatted(slug, Bukkit.getServer().getMinecraftVersion());
        var futureResponse = Unirest.get(uri).asJsonAsync();
        futureResponse.thenAccept(response -> {
            Gson gson = new Gson();
            var data = response.getBody().getArray().toString();
            var availableVersionsArray = gson.fromJson(data, ModrinthVersion[].class);

            var availableVersions = new ArrayList<>(Arrays.stream(availableVersionsArray)
                    .filter(v -> v.version_type.equalsIgnoreCase("release")).toList());
            availableVersions.sort(ModrinthVersion::compareDatePublishedTo);
            
            if (availableVersions.isEmpty()) {
                latestStatus = VersionStatus.NOT_FOUND;
                future.complete(VersionStatus.NOT_FOUND);
                return;
            }

            if (availableVersions.getFirst().version_number.toLowerCase().endsWith(getCurrentPluginVersion())) {
                latestStatus = VersionStatus.LATEST;
                future.complete(VersionStatus.LATEST);
                return;
            }

            int currentVersionIndex = indexOfVersion(availableVersions, getCurrentPluginVersion());
            if (currentVersionIndex == -1) {
                latestStatus = VersionStatus.DEVELOPMENT;
                future.complete(VersionStatus.DEVELOPMENT);
                return;
            }

            future.complete(VersionStatus.OUTDATED);
            latestStatus = VersionStatus.OUTDATED;
            latestKnownVersion = availableVersions.getFirst().version_number;
        }).exceptionally(throwable -> {
            latestStatus = VersionStatus.ERROR;
            future.complete(VersionStatus.ERROR);
            plugin.getLogger().warning("Failed to fetch version from Modrinth: " + throwable.getMessage());
            return null;
        });
        return future;
    }

    /**
     * Generate a version message based on the latest VersionStatus.<br>
     * Used in the {@code /noxesiumutils} command for output.
     * You most definitely don't need this.
     */
    public Component generateVersionMessage() {
        return generateVersionMessage(latestStatus);
    }

    /**
     * Generate a version message.<br>
     * Used in the {@code /noxesiumutils} command for output.
     * You most definitely don't need this.
     * @param versionStatus The version status to use.
     */
    public Component generateVersionMessage(VersionStatus versionStatus) {
        if (latestKnownVersion == null) latestKnownVersion = getCurrentPluginVersion();
        var currentVersion = Component.text("Running NoxesiumUtils v" + getCurrentPluginVersion(), NamedTextColor.AQUA);

        var statusMessage = Component.empty(); 
        switch (versionStatus) {
            case NOT_CHECKED -> statusMessage = Component.text("Still checking...", NamedTextColor.GRAY);
            case ERROR -> statusMessage = Component.text("Failed to check for updates!", NamedTextColor.RED);
            case NOT_FOUND -> statusMessage = Component.text("No versions found.", NamedTextColor.YELLOW);
            case LATEST -> {
                var support = Component.text("Support me on Patreon!", NamedTextColor.YELLOW)
                                .clickEvent(ClickEvent.openUrl("https://www.patreon.com/superneon4ik"))
                                .hoverEvent(Component.text("https://www.patreon.com/superneon4ik", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
                statusMessage = Component.text("You are running the latest version! ", NamedTextColor.GREEN).append(support);
            }
            case OUTDATED -> {
                var link = Component.text("Download on Modrinth!", NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/" + slug))
                        .hoverEvent(Component.text("https://modrinth.com/plugin/" + slug, NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
                statusMessage = Component.text("You are running an outdated version! The latest release is v" 
                        + latestKnownVersion, NamedTextColor.RED).append(link);
            }
            case DEVELOPMENT -> statusMessage = Component.text("You are running an unknown (development) version! Woah!", NamedTextColor.LIGHT_PURPLE);
        }
        
        return Component.empty()
                .append(currentVersion)
                .append(Component.newline())
                .append(statusMessage);
    }
    
    @SuppressWarnings("deprecation")
    private String getCurrentPluginVersion() {
        return plugin.getDescription().getVersion().toLowerCase();
    }

    private static int indexOfVersion(List<ModrinthVersion> versionList, String pluginVersion) {
        int i = 0;
        for (ModrinthVersion modrinthVersion : versionList) {
            if (modrinthVersion.version_number.toLowerCase().endsWith(pluginVersion.toLowerCase())) return i;
            i++;
        }
        return -1;
    }
}
