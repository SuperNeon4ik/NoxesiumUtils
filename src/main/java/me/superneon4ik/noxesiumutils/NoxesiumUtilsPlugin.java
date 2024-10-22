package me.superneon4ik.noxesiumutils;

import lombok.Getter;
import me.superneon4ik.noxesiumutils.commands.CommandRegistrar;
import me.superneon4ik.noxesiumutils.config.NoxesiumUtilsConfigBuilder;
import me.superneon4ik.noxesiumutils.listeners.PlayerJoinEventListener;
import me.superneon4ik.noxesiumutils.modules.ModrinthUpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class NoxesiumUtilsPlugin extends JavaPlugin {
    @Getter private static NoxesiumUtilsPlugin instance;
    @Getter private static ModrinthUpdateChecker updateChecker;
    @Getter private static NoxesiumUtils noxesiumUtils;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        var config = new NoxesiumUtilsConfigBuilder()
                .withConfig(getConfig())
                .withQibFolder(Path.of(getDataFolder().getPath(), "qibs").toFile())
                .withLogger(getLogger())
                .build();
        noxesiumUtils = new NoxesiumUtils(this, config, getLogger());
        noxesiumUtils.register();

        // Register update checker
        updateChecker = new ModrinthUpdateChecker(this, "noxesiumutils");
        if (config.isCheckForUpdates())
            updateChecker.beginChecking(5 * 60 * 20); // every 5 minutes
        
        // Register commands
        new CommandRegistrar(this, noxesiumUtils, updateChecker).registerCommands();
        
        // Register events
        getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(updateChecker), this);
    }

    @Override
    public void onDisable() {
        noxesiumUtils.unregister();
        instance = null;
    }
}
