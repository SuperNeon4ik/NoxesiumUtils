package me.superneon4ik.noxesiumutils;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.commands.CommandBuilder;
import me.superneon4ik.noxesiumutils.config.NoxesiumUtilsConfig;
import me.superneon4ik.noxesiumutils.config.NoxesiumUtilsConfigBuilder;
import me.superneon4ik.noxesiumutils.listeners.PlayerJoinEventListener;
import me.superneon4ik.noxesiumutils.listeners.ReloadCommandListener;
import me.superneon4ik.noxesiumutils.modules.ModrinthUpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.List;

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
            updateChecker.beginChecking(60 * 60 * 20); // every 1 hour
        
        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
        noxesiumUtils.unregister();
        updateChecker = null;
        noxesiumUtils = null;
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
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            var commandRegistrar = new CommandBuilder(noxesiumUtils, updateChecker);

            commandRegistrar.addAdditionalCommand(
                    Commands.literal("reload")
                            .requires(ctx -> ctx.getSender().hasPermission("noxesiumutils.reload"))
                            .executes(ctx -> {
                                // Reload the config
                                reloadConfig();
                                var oldConfig = noxesiumUtils.getConfig();
                                var config = buildConfig();
                                noxesiumUtils.setConfig(config);

                                ctx.getSource().getSender().sendRichMessage("<green>Reloaded configuration file!</green>");

                                // Send updated qibs
                                var oldQibs = oldConfig.getQibDefinitions();
                                var newQibs = config.getQibDefinitions();

                                List<String> changedQibs = newQibs.keySet().stream()
                                        .filter(oldQibs::containsKey)
                                        .filter(x -> !oldQibs.get(x).equals(newQibs.get(x)))
                                        .toList();

                                if (config.isExtraDebugOutput() && !changedQibs.isEmpty())
                                    getLogger().info("Qibs to be updated for online players: " + String.join(", ", changedQibs));

                                getServer().getOnlinePlayers().forEach(player -> {
                                    var qibRule = noxesiumUtils.getManager().getServerRule(player, noxesiumUtils.getServerRules().getQibBehaviors());
                                    if (qibRule == null) return;
                                    var qibs = qibRule.getValue();
                                    boolean hasChanged = false;
                                    for (String qibId : changedQibs) {
                                        if (qibs.containsKey(qibId)) {
                                            qibs.put(qibId, newQibs.get(qibId));
                                            hasChanged = true;
                                        }
                                    }

                                    if (!hasChanged) return;

                                    // We reset the rule first because Noxesium checks if the
                                    // map is the same before applying, which is always false,
                                    // because of some HashMap memery ig.
                                    qibRule.reset();
                                    qibRule.setValue(qibs);
                                });

                                if (config.isSendDefaultsOnReload()) {
                                    // Send defaults to online players
                                    getServer().getOnlinePlayers().forEach(noxesiumUtils::sendDefaultServerRules);
                                    ctx.getSource().getSender().sendRichMessage("<green>Sent default server rules!</green>");
                                }

                                return Command.SINGLE_SUCCESS;
                            })
            );

            var command = commandRegistrar.build();
            commands.registrar().register(command);

            var alias = Commands.literal("noxutils")
                    .redirect(command)
                    .build();

            commands.registrar().register(alias);
        });
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(updateChecker), this);
        getServer().getPluginManager().registerEvents(new ReloadCommandListener(), this);
    }
}
