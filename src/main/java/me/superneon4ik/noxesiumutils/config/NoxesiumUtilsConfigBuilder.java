package me.superneon4ik.noxesiumutils.config;

import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.api.qib.QibEffect;
import com.noxcrew.noxesium.paper.api.rule.GraphicsType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class NoxesiumUtilsConfigBuilder {
    @Nullable private FileConfiguration config = null;
    @Nullable private File qibEffectsFolder = null;
    @Nullable private Logger logger = null;
    
    public NoxesiumUtilsConfigBuilder() {}
    
    public NoxesiumUtilsConfigBuilder withConfig(FileConfiguration config) {
        this.config = config;
        return this;
    }
    
    public NoxesiumUtilsConfigBuilder withQibFolder(File qibEffectsFolder) {
        this.qibEffectsFolder = qibEffectsFolder;
        return this;
    }
    
    public NoxesiumUtilsConfigBuilder withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }
    
    public NoxesiumUtilsConfig build() {
        var noxesiumUtilsConfig = new NoxesiumUtilsConfig();

        loadRootSettings(noxesiumUtilsConfig);
        loadDefaults(noxesiumUtilsConfig);
        loadCustomCreativeItems(noxesiumUtilsConfig);
        loadQibEffects(noxesiumUtilsConfig);
        loadQibDefinitions(noxesiumUtilsConfig);

        return noxesiumUtilsConfig;
    }
    
    private void loadRootSettings(NoxesiumUtilsConfig noxesiumUtilsConfig) {
        if (config == null) return;

        // Load root settings
        noxesiumUtilsConfig.setExtraDebugOutput(config.getBoolean("extraDebugOutput", false));
        noxesiumUtilsConfig.setCheckForUpdates(config.getBoolean("checkForUpdates", true));
        noxesiumUtilsConfig.setSendDefaultsOnJoin(config.getBoolean("sendDefaultsOnJoin", false));
    }

    @SuppressWarnings("UnstableApiUsage")
    private void loadDefaults(NoxesiumUtilsConfig noxesiumUtilsConfig) {
        if (config == null) return;
        
        // Load root settings
        noxesiumUtilsConfig.setExtraDebugOutput(config.getBoolean("extraDebugOutput", false));
        noxesiumUtilsConfig.setCheckForUpdates(config.getBoolean("checkForUpdates", true));
        noxesiumUtilsConfig.setSendDefaultsOnJoin(config.getBoolean("sendDefaultsOnJoin", false));

        // Load `defaults`
        var defaultsConfigSection = config.getConfigurationSection("defaults");
        if (defaultsConfigSection != null) {
            var defaults = new ServerRuleDefaults();

            // Most safe and obvious way to fix boilerplate code, mhm mhm
            var clazz = defaults.getClass();
            var fields = clazz.getDeclaredFields();
            for (var field : fields) {
                var fieldName = field.getName();
                if (defaultsConfigSection.contains(fieldName)) {
                    try {
                        field.setAccessible(true);
                        if (field.getType().equals(Boolean.class)) {
                            var value = (Boolean) config.getBoolean(fieldName);
                            field.set(defaults, value);
                        } else if (field.getType().equals(Integer.class)) {
                            var value = (Integer) config.getInt(fieldName);
                            field.set(defaults, value);
                        } else if (field.getType().equals(ItemStack.class)) {
                            var value = config.getString(fieldName);
                            var item = ArgumentTypes.itemStack().parse(new StringReader(value));
                            field.set(defaults, item);
                        } else if (field.getType().equals(GraphicsType.class)) {
                            var value = config.getString(fieldName);
                            field.set(defaults, GraphicsType.valueOf(value));
                        } else if (field.getType().equals(List.class)) {
                            if (field.getGenericType() instanceof ParameterizedType parameterizedType) {
                                var actualType = parameterizedType.getActualTypeArguments()[0];
                                if (actualType.equals(String.class)) {
                                    // List<String>
                                    var value = config.getStringList(fieldName);
                                    field.set(defaults, value);
                                } else {
                                    if (logger != null)
                                        logger.warning("Found an incompatible generic type of " + actualType.getTypeName() + " for " + fieldName);
                                }
                            }
                        } else {
                            if (logger != null)
                                logger.warning("Found an incompatible type of " + field.getType().getName() + " for " + fieldName);
                        }
                    } catch (Exception e) {
                        if (logger != null)
                            logger.warning("Error setting value for defaults." + fieldName + ": " + e.getMessage());
                    }
                }
            }

            noxesiumUtilsConfig.setDefaults(defaults);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void loadCustomCreativeItems(NoxesiumUtilsConfig noxesiumUtilsConfig) {
        if (config == null) return;
        
        // Load customCreativeItems
        var customCreativeItemStrings = config.getStringList("customCreativeItems");
        customCreativeItemStrings.forEach(itemString -> {
            try {
                var item = ArgumentTypes.itemStack().parse(new StringReader(itemString));
                noxesiumUtilsConfig.getCustomCreativeItems().add(item);

                if (noxesiumUtilsConfig.isExtraDebugOutput() && logger != null)
                    logger.info("Loaded customCreativeItem: " + item);
            } catch (CommandSyntaxException e) {
                if (logger != null) {
                    logger.warning("Failed to parse customCreativeItem: " + itemString);
                    logger.warning(e.getMessage());
                }
            }
        });
    }
    
    private void loadQibEffects(NoxesiumUtilsConfig noxesiumUtilsConfig) {
        if (qibEffectsFolder == null) return;

        if (!qibEffectsFolder.exists() && !qibEffectsFolder.mkdirs()) {
            if (logger != null) 
                logger.warning("Couldn't find '/qibs' folder.");
            return;
        }

        // Loading QibEffects
        try (Stream<Path> files = Files.list(qibEffectsFolder.toPath())) {
            var qibGson = QibDefinition.QIB_GSON;
            files.forEach(qibFilePath -> {
                if (Files.isDirectory(qibFilePath) || !qibFilePath.toFile().getName().endsWith(".json")) return;
                try {
                    var qibEffect = qibGson.fromJson(Files.readString(qibFilePath), QibEffect.class);
                    var name = qibFilePath.toFile().getName().replace(".json", "");
                    noxesiumUtilsConfig.getQibEffects().put(name, qibEffect);

                    if (noxesiumUtilsConfig.isExtraDebugOutput() && logger != null)
                        logger.info("Loaded qibEffect '%s': %s!".formatted(name, qibEffect.toString()));
                } catch (IOException e) {
                    if (logger != null) 
                        logger.warning("Failed to read the '/qibs/%s' file.".formatted(qibFilePath.toFile().getName()));
                } catch (JsonSyntaxException e) {
                    if (logger != null) 
                        logger.warning("JSON Syntax Error in '/qibs/%s' file.".formatted(qibFilePath.toFile().getName()));
                }
            });
        } catch (IOException e) {
            if (logger != null) logger.warning("Failed to read the '/qibs' folder.");
        }

        if (logger != null) logger.info("Loaded %d qibEffects!".formatted(noxesiumUtilsConfig.getQibEffects().size()));
    }
    
    private void loadQibDefinitions(NoxesiumUtilsConfig noxesiumUtilsConfig) {
        if (config == null || noxesiumUtilsConfig.getQibEffects().isEmpty()) return;
        
        var qibDefinitionsConfigSection = config.getConfigurationSection("qibDefinitions");
        if (qibDefinitionsConfigSection == null || qibDefinitionsConfigSection.getKeys(false).isEmpty()) {
            if (noxesiumUtilsConfig.isExtraDebugOutput() && logger != null)
                logger.warning("No QIB definitions found.");
            return;
        }

        qibDefinitionsConfigSection.getKeys(false).forEach(key -> {
            var section = qibDefinitionsConfigSection.getConfigurationSection(key);
            if (section == null) return;

            QibEffect onEnter = null;
            QibEffect onLeave = null;
            QibEffect whileInside = null;
            QibEffect onJump = null;
            boolean triggerEnterLeaveOnSwitch = section.getBoolean("triggerEnterLeaveOnSwitch", false);

            if (section.contains("onEnter")) onEnter = noxesiumUtilsConfig.getQibEffects().get(section.getString("onEnter"));
            if (section.contains("onLeave")) onLeave = noxesiumUtilsConfig.getQibEffects().get(section.getString("onLeave"));
            if (section.contains("whileInside")) whileInside = noxesiumUtilsConfig.getQibEffects().get(section.getString("whileInside"));
            if (section.contains("onJump")) onJump = noxesiumUtilsConfig.getQibEffects().get(section.getString("onJump"));

            QibDefinition qibDefinition = new QibDefinition(onEnter, onLeave, whileInside, onJump, triggerEnterLeaveOnSwitch);
            noxesiumUtilsConfig.getQibDefinitions().put(key, qibDefinition);

            if (noxesiumUtilsConfig.isExtraDebugOutput() && logger != null)
                logger.info("Loaded qibDefinition '%s': %s!".formatted(key, qibDefinition.toString()));
        });

        if (logger != null)
            logger.info("Loaded %d qibDefinitions!".formatted(noxesiumUtilsConfig.getQibDefinitions().size()));
    }
}
