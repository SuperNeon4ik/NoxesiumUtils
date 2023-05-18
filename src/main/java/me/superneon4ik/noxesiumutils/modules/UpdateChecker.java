package me.superneon4ik.noxesiumutils.modules;

import lombok.Getter;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {
    @Getter private final String githubUser;
    @Getter private final String githubRepo;

    public UpdateChecker(String githubUser, String githubRepo) {
        this.githubUser = githubUser;
        this.githubRepo = githubRepo;
    }

    public void sendVersionInfoMessage(CommandSender sender) {
        var plugin = NoxesiumUtils.getPlugin();
        try {
            String json = IOUtils.toString(URI.create("https://api.github.com/repos/" + githubUser + "/" + githubRepo + "/releases/latest"), StandardCharsets.UTF_8);
            var jsonObj = new JSONObject(json);
            String latestReleaseTag = jsonObj.getString("tag_name");
            if (latestReleaseTag.equalsIgnoreCase(plugin.getDescription().getVersion())) {
                sender.sendMessage(ChatColor.GREEN + "NoxesiumUtils is up-to-date!");
            }
            else {
                sender.sendMessage(ChatColor.YELLOW + "Your NoxesiumUtils may be outdated! Latest release version is " + latestReleaseTag + ". Download: " + ChatColor.WHITE + jsonObj.getString("html_url"));
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to check for updates.");
            e.printStackTrace();
        }
        sender.sendMessage(ChatColor.AQUA + "Running " + plugin.getName() + " v" + plugin.getDescription().getVersion() + ".");
    }

    public void checkVersion(CommandSender sender) {
        var plugin = NoxesiumUtils.getPlugin();
        try {
            String json = IOUtils.toString(URI.create("https://api.github.com/repos/" + githubUser + "/" + githubRepo + "/releases/latest"), StandardCharsets.UTF_8);
            var jsonObj = new JSONObject(json);
            String latestReleaseTag = jsonObj.getString("tag_name");
            if (!latestReleaseTag.equalsIgnoreCase(plugin.getDescription().getVersion())) {
                sender.sendMessage(ChatColor.YELLOW + "Your NoxesiumUtils may be outdated! Latest release version is " + latestReleaseTag + ". Download: " + ChatColor.WHITE + jsonObj.getString("html_url"));
                sender.sendMessage(ChatColor.AQUA + "Running " + plugin.getName() + " v" + plugin.getDescription().getVersion() + ".");
            }
        } catch (IOException ignore) {
            sender.sendMessage(ChatColor.RED + "Failed to check for updates.");
        }
    }
}
