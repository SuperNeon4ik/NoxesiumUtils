package me.superneon4ik.noxesiumutils.config;

import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.api.qib.QibEffect;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class NoxesiumUtilsConfig {
    private boolean extraDebugOutput = false;

    private boolean checkForUpdates = true;

    private boolean sendDefaultsOnJoin = true;

    private boolean sendDefaultsOnReload = false;

    @NotNull
    private ServerRuleDefaults defaults = new ServerRuleDefaults();

    @NotNull
    private List<ItemStack> customCreativeItems = new LinkedList<>();
    
    @NotNull
    private Map<String, QibEffect> qibEffects = new HashMap<>();

    @NotNull
    private Map<String, QibDefinition> qibDefinitions = new HashMap<>();

    @Override
    public String toString() {
        return "NoxesiumUtilsConfig{" +
                "extraDebugOutput=" + extraDebugOutput +
                ", checkForUpdates=" + checkForUpdates +
                ", sendDefaultsOnJoin=" + sendDefaultsOnJoin +
                ", sendDefaultsOnReload=" + sendDefaultsOnReload +
                ", defaults=" + defaults +
                ", customCreativeItems=" + customCreativeItems +
                ", qibEffects=" + qibEffects +
                ", qibDefinitions=" + qibDefinitions +
                '}';
    }
}
