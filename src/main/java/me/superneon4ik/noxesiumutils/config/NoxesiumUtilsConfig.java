package me.superneon4ik.noxesiumutils.config;

import com.mojang.brigadier.StringReader;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.api.qib.QibEffect;
import com.noxcrew.noxesium.paper.api.rule.GraphicsType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Getter
@Setter
public class NoxesiumUtilsConfig {
    private boolean extraDebugOutput = false;
    private boolean checkForUpdates = true;
    private boolean sendDefaultsOnJoin = false;
    @NotNull private ServerRuleDefaults defaults = new ServerRuleDefaults();
    @NotNull private List<ItemStack> customCreativeItems = new ArrayList<>();
    
    @NotNull private Map<String, QibEffect> qibEffects = new WeakHashMap<>();
    @NotNull private Map<String, QibDefinition> qibDefinitions = new WeakHashMap<>();
}
