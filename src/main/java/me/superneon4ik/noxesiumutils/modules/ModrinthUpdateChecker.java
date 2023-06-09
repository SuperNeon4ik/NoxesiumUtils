package me.superneon4ik.noxesiumutils.modules;

import com.google.gson.Gson;
import kong.unirest.Unirest;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.enums.VersionStatus;
import me.superneon4ik.noxesiumutils.objects.ModrinthVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModrinthUpdateChecker {
    @Getter private final String slug;
    @Getter private VersionStatus latestStatus;
    private String latestKnownVersion = null;

    public ModrinthUpdateChecker(String slug) {
        this.slug = slug;
        this.latestStatus = VersionStatus.NOT_CHECKED;
    }

    public void beginChecking(int refreshTicks) {
        new BukkitRunnable() {
            boolean firstCheck = true;
            boolean reportedOutdatedStatus = false;

            @Override
            public void run() {
                var future = checkForUpdates();
                future.thenAccept(versionStatus -> {
                    if (versionStatus == VersionStatus.OUTDATED && !reportedOutdatedStatus) {
                        sendVersionMessage(Bukkit.getConsoleSender(), versionStatus);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.isOp()) sendVersionMessage(player, versionStatus);
                        }
                        reportedOutdatedStatus = true;
                    }
                    else if (firstCheck) {
                        sendVersionMessage(Bukkit.getConsoleSender(), versionStatus);
                    }
                    firstCheck = false;
                });
            }
        }.runTaskTimerAsynchronously(NoxesiumUtils.getPlugin(), 1, refreshTicks);
    }

    public CompletableFuture<VersionStatus> checkForUpdates() {
        CompletableFuture<VersionStatus> future = new CompletableFuture<>();
        String uri = "https://api.modrinth.com/v2/project/%s/version?game_versions=%%5B%%22%s%%22%%5D"
                .formatted(slug, Bukkit.getServer().getMinecraftVersion());
        var futureResponse = Unirest.get(uri).asJsonAsync();
        futureResponse.thenAccept(response -> {
            Gson gson = new Gson();
            var data = response.getBody().getArray().toString();
            var availableVersionsArray = gson.fromJson(data, ModrinthVersion[].class);
            if (availableVersionsArray.length == 0) {
                latestStatus = VersionStatus.NOT_FOUND;
                future.complete(VersionStatus.NOT_FOUND);
                return;
            }

            var availableVersions = new ArrayList<>(Arrays.stream(availableVersionsArray)
                    .filter(v -> v.version_type.equalsIgnoreCase("release")).toList());
            availableVersions.sort(ModrinthVersion::compareDatePublishedTo);

            if (availableVersions.get(0).version_number.toLowerCase().endsWith(NoxesiumUtils.getPlugin().getDescription().getVersion().toLowerCase())) {
                latestStatus = VersionStatus.LATEST;
                future.complete(VersionStatus.LATEST);
                return;
            }

            int currentVersionIndex = indexOfVersion(availableVersions, NoxesiumUtils.getPlugin().getDescription().getVersion());
            if (currentVersionIndex == -1) {
                latestStatus = VersionStatus.DEVELOPMENT;
                future.complete(VersionStatus.DEVELOPMENT);
                return;
            }

            future.complete(VersionStatus.OUTDATED);
            latestStatus = VersionStatus.OUTDATED;
            latestKnownVersion = availableVersions.get(0).version_number;
        }).exceptionally(throwable -> {
            latestStatus = VersionStatus.ERROR;
            future.complete(VersionStatus.ERROR);
            NoxesiumUtils.getPlugin().getLogger().warning("Failed to fetch version from Modrinth.");
            throwable.printStackTrace();
            return null;
        });
        return future;
    }

    public void sendVersionMessage(CommandSender sender) {
        sendVersionMessage(sender, latestStatus);
    }

    public void sendVersionMessage(CommandSender sender, VersionStatus versionStatus) {
        if (latestKnownVersion == null) latestKnownVersion = NoxesiumUtils.getPlugin().getDescription().getVersion();
        sender.sendMessage(ChatColor.AQUA + "Running NoxesiumUtils v" + NoxesiumUtils.getPlugin().getDescription().getVersion());
        switch (versionStatus) {
            case NOT_CHECKED -> sender.sendMessage(ChatColor.GRAY + "Still checking...");
            case ERROR -> sender.sendMessage(ChatColor.RED + "Failed to check for updates!");
            case NOT_FOUND -> sender.sendMessage(ChatColor.YELLOW + "No versions found.");
            case LATEST -> sender.sendMessage(ChatColor.GREEN + "You are running the latest version! " +
                    ChatColor.YELLOW + "Support me: " + ChatColor.WHITE + "https://www.patreon.com/superneon4ik");
            case OUTDATED -> sender.sendMessage(ChatColor.RED + "You are running an outdated version! The latest release is v" + latestKnownVersion +
                    ". " + ChatColor.YELLOW + "Download here: " + ChatColor.WHITE + "https://modrinth.com/plugin/noxesiumutils");
            case DEVELOPMENT -> sender.sendMessage(ChatColor.LIGHT_PURPLE + "You are running unknown (development) version! Woah!");
        }
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
