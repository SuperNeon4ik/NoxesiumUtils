package me.superneon4ik.noxesiumutils;

import dev.jorel.commandapi.CommandAPICommand;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.commands.CommandRegistrar;
import me.superneon4ik.noxesiumutils.config.NoxesiumUtilsConfig;
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
        
        // Load config
        var config = buildConfig();
        
        // Register NoxesiumUtils
        noxesiumUtils = new NoxesiumUtils(this, config, getLogger());
        noxesiumUtils.register();

        // Register update checker
        updateChecker = new ModrinthUpdateChecker(this, "noxesiumutils");
        if (config.isCheckForUpdates())
            updateChecker.beginChecking(5 * 60 * 20); // every 5 minutes
        
        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
        noxesiumUtils.unregister();
        instance = null;
    }
    
    private NoxesiumUtilsConfig buildConfig() {
        var config = new NoxesiumUtilsConfigBuilder()
                .withConfig(getConfig())
                .withQibFolder(Path.of(getDataFolder().getPath(), "qibs").toFile())
                .withLogger(getLogger())
                .build();

        if (config.isExtraDebugOutput())
            getLogger().info("Loaded config: " + config);
        
        return config;
    }
    
    private void registerCommands() {
        var registrar = new CommandRegistrar(this, noxesiumUtils, updateChecker);
        
        // Reload command
        registrar.addAdditionalCommand(
                new CommandAPICommand("reload")
                        .withPermission("noxesiumutils.reload")
                        .executes(((sender, args) -> {
                            reloadConfig();
                            var config = buildConfig();
                            noxesiumUtils.setConfig(config);
                            registerCommands(); // Reload available commands
                            
                            // TODO: 
                            //  - Resend updated Qibs
                            //  - If a config value is set - send defaults to everyone on the server
                            
                            sender.sendRichMessage("<green>Reloaded configuration file!</green>");
                        }))
        );
        
        registrar.registerCommands();
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(updateChecker), this);
    }
}
