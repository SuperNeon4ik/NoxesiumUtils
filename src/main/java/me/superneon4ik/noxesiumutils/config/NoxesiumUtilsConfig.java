package me.superneon4ik.noxesiumutils.config;

import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.api.qib.QibEffect;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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

    @Override
    public String toString() {
        return "NoxesiumUtilsConfig{" +
                "extraDebugOutput=" + extraDebugOutput +
                ", checkForUpdates=" + checkForUpdates +
                ", sendDefaultsOnJoin=" + sendDefaultsOnJoin +
                ", defaults=" + defaults +
                ", customCreativeItems=" + customCreativeItems +
                ", qibEffects=" + qibEffects +
                ", qibDefinitions=" + qibDefinitions +
                '}';
    }
}
