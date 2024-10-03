package me.superneon4ik.noxesiumutils.modules;

import com.google.gson.Gson;
import kong.unirest.Unirest;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.enums.VersionStatus;
import me.superneon4ik.noxesiumutils.objects.ModrinthVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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
        sender.sendMessage(Component.text("Running NoxesiumUtils v" + NoxesiumUtils.getPlugin().getDescription().getVersion(), NamedTextColor.AQUA));
        switch (versionStatus) {
            case NOT_CHECKED -> sender.sendMessage(Component.text("Still checking...", NamedTextColor.GRAY));
            case ERROR -> sender.sendMessage(Component.text("Failed to check for updates!", NamedTextColor.RED));
            case NOT_FOUND -> sender.sendMessage(Component.text("No versions found.", NamedTextColor.YELLOW));
            case LATEST -> {
                var support = Component.text("Support me on Patreon!", NamedTextColor.YELLOW)
                                .clickEvent(ClickEvent.openUrl("https://www.patreon.com/superneon4ik"))
                                .hoverEvent(Component.text("https://www.patreon.com/superneon4ik", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
                sender.sendMessage(Component.text("You are running the latest version! ", NamedTextColor.GREEN).append(support));
            }
            case OUTDATED -> {
                var link = Component.text("Download on Modrinth!", NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.openUrl("https://modrinth.com/plugin/noxesiumutils"))
                        .hoverEvent(Component.text("https://modrinth.com/plugin/noxesiumutils", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
                sender.sendMessage(Component.text("You are running an outdated version! The latest release is v" 
                        + latestKnownVersion, NamedTextColor.RED).append(link));
            }
            case DEVELOPMENT -> sender.sendMessage(Component.text("You are running an unknown (development) version! Woah!", NamedTextColor.LIGHT_PURPLE));
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
