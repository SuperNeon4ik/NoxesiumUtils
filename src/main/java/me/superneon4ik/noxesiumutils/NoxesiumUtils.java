package me.superneon4ik.noxesiumutils;

import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.paper.api.EntityRuleManager;
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets;
import com.noxcrew.noxesium.paper.api.rule.EntityRules;
import com.noxcrew.noxesium.paper.api.rule.ServerRules;
import lombok.Getter;
import lombok.Setter;
import me.superneon4ik.noxesiumutils.config.NoxesiumUtilsConfig;
import me.superneon4ik.noxesiumutils.config.RuleIndex;
import me.superneon4ik.noxesiumutils.config.ServerRuleDefaults;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerRiptideEvent;
import me.superneon4ik.noxesiumutils.events.NoxesiumQibTriggeredEvent;
import me.superneon4ik.noxesiumutils.listeners.NoxesiumPlayerRegisteredListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.logging.Logger;

/**
 * If your plugin depends on NoxesiumUtils use the instance from
 * {@link NoxesiumUtilsPlugin#getNoxesiumUtils()}.
 * <p>
 * If you're shading NoxesiumUtils you need to create your own instance
 * of this class in your plugin.<br>
 * Something like this:
 * <pre>{@code
 * var config = new NoxesiumUtilsConfigBuilder().build();
 * var noxesiumUtils = new NoxesiumUtils(plugin, config, plugin.getLogger());
 * noxesiumUtils.register();
 * }</pre>
 */
@Getter
public class NoxesiumUtils {
    private final JavaPlugin plugin;
    private final Logger logger;
    
    @Setter @NotNull
    private NoxesiumUtilsConfig config;    
    
    private HookedNoxesiumManager manager;
    private EntityRuleManager entityRuleManager;
    private ServerRules serverRules;
    private EntityRules entityRules;
    
    public NoxesiumUtils(@NotNull JavaPlugin plugin, @NotNull NoxesiumUtilsConfig config, @Nullable Logger logger) {
        this.plugin = plugin;
        this.config = config;
        this.logger = logger;
    }

    /**
     * Register all the needed managers and listeners.
     * Run this once in {@code onEnable()} before use.
     * <p>
     * This also registers all the {@link me.superneon4ik.noxesiumutils.events} events.
     */
    public void register() {
        manager = new HookedNoxesiumManager(getPlugin(), LoggerFactory.getLogger("NoxesiumPaperManager"));
        manager.register();
        entityRuleManager = new EntityRuleManager(manager);
        entityRuleManager.register();

        serverRules = new ServerRules(manager);
        entityRules = new EntityRules(manager);
        
        // Register NoxesiumPlayerRegisteredEvent Bukkit listener (for default server rules on join)
        plugin.getServer().getPluginManager().registerEvents(new NoxesiumPlayerRegisteredListener(this), plugin);
        
        // Hook into packets to introduce events for them
        NoxesiumPackets.INSTANCE.getSERVER_QIB_TRIGGERED().addListener(getManager(), (manager, event, player) -> {
            new NoxesiumQibTriggeredEvent(player, event.getBehavior(), event.getQibType(), event.getEntityId()).callEvent();
            return null;
        });

        NoxesiumPackets.INSTANCE.getSERVER_RIPTIDE().addListener(getManager(), (manager, event, player) -> {
            new NoxesiumPlayerRiptideEvent(player, event.getSlot()).callEvent();
            return null;
        });
    }

    /**
     * Please run this in {@code onDisable} plsplspls 🙏
     */
    public void unregister() {
        entityRuleManager.unregister();
        manager.unregister();
    }

    /**
     * Send the default server rules from the NoxesiumUtils
     * config to the player.
     * @param player Player to apply the rules for
     */
    public void sendDefaultServerRules(Player player) {
        var clazz = ServerRuleDefaults.class;
        var defaults = getConfig().getDefaults();
        var fields = clazz.getDeclaredFields();
        for (var field : fields) {
            var fieldName = field.getName();
            
            try {
                field.setAccessible(true);
                if (field.get(defaults) == null) continue;

                if (field.isAnnotationPresent(RuleIndex.class)) {
                    int index = field.getAnnotation(RuleIndex.class).index();
                    Object value = getServerRuleValue(fieldName, field.get(defaults));
                    manager.setServerRule(player, index, value);
                }
            } catch (Exception e) {
                if (logger != null) 
                    logger.warning("Couldn't read field '" + fieldName + "' while sending default server rules to " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Since the {@link ServerRuleDefaults} uses data types used for the config,
     * and not sending the data we need to convert data types for some
     * fields before sending them.
     * @param fieldName Name of the server rule field in {@link ServerRuleDefaults}
     * @param value The value of the field
     * @return The converted value, that we can send to the player
     */
    private Object getServerRuleValue(String fieldName, Object value) {
        switch (fieldName) {
            case "overrideGraphicsMode" -> value = Optional.of(value);
            case "customCreativeItems" -> value = getConfig().getCustomCreativeItems();
            case "qibBehaviors" -> {
                @SuppressWarnings("unchecked") 
                List<String> values = (List<String>) value;
                Map<String, QibDefinition> mappedQibDefinitions = new WeakHashMap<>();
                
                values.forEach(qibId -> {
                    var qibDefinition = getConfig().getQibDefinitions().get(qibId);
                    if (qibDefinition == null) return;
                    mappedQibDefinitions.put(qibId, qibDefinition);
                });
                
                value = mappedQibDefinitions;
            }
        }
        return value;
    }
}
