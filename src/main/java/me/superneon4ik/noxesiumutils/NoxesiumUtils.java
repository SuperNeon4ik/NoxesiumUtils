package me.superneon4ik.noxesiumutils;

import com.noxcrew.noxesium.api.protocol.rule.EntityRuleIndices;
import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.api.qib.QibEffect;
import com.noxcrew.noxesium.paper.api.EntityRuleManager;
import com.noxcrew.noxesium.paper.api.rule.EntityRules;
import com.noxcrew.noxesium.paper.api.rule.GraphicsType;
import com.noxcrew.noxesium.paper.api.rule.ServerRules;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.listeners.NoxesiumBukkitListener;
import me.superneon4ik.noxesiumutils.modules.ModrinthUpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class NoxesiumUtils extends JavaPlugin {
    @Getter private static NoxesiumUtils plugin;
    @Getter private static final ModrinthUpdateChecker updateChecker = new ModrinthUpdateChecker("noxesiumutils");
    @Getter private static HookedNoxesiumManager manager;
    @Getter private static EntityRuleManager entityRuleManager;
    @Getter private static ServerRules serverRules;
    @Getter private static EntityRules entityRules;
    @Getter private static final Map<String, QibEffect> qibEffects = new HashMap<>();
    @Getter private static final Map<String, QibDefinition> qibDefinitions = new HashMap<>();

    private static final Map<String, Integer> booleanServerRules = new HashMap<>() {{
        put("disableSpinAttackCollisions", ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS);
        put("cameraLocked", ServerRuleIndices.CAMERA_LOCKED);
        put("disableVanillaMusic", ServerRuleIndices.DISABLE_VANILLA_MUSIC);
        put("disableBoatCollisions", ServerRuleIndices.DISABLE_BOAT_COLLISIONS);
        put("disableUiOptimizations", ServerRuleIndices.DISABLE_UI_OPTIMIZATIONS);
        put("showMapInUi", ServerRuleIndices.SHOW_MAP_IN_UI);
        put("disableDeferredChunkUpdates", ServerRuleIndices.DISABLE_DEFERRED_CHUNK_UPDATES);
        put("disableMapUi", ServerRuleIndices.DISABLE_MAP_UI);

        // TODO: Implement Noxcrew's recommendations before enabling
        // https://github.com/Noxcrew/noxesium/blob/4b3f93fe6886eac60dbfffa6cb125e1e5a31886a/api/src/main/java/com/noxcrew/noxesium/api/protocol/rule/ServerRuleIndices.java#L85
        put("enableSmootherClientTrident", ServerRuleIndices.ENABLE_SMOOTHER_CLIENT_TRIDENT);
    }};

    private static final Map<String, Integer> integerServerRules = new HashMap<>() {{
        put("heldItemNameOffset", ServerRuleIndices.HELD_ITEM_NAME_OFFSET);
        put("riptideCoyoteTime", ServerRuleIndices.RIPTIDE_COYOTE_TIME);
    }};
    
    private static final Map<String, Integer> allServerRules = new HashMap<>() {{
        putAll(booleanServerRules); 
        putAll(integerServerRules);
        put("handItemOverride", ServerRuleIndices.HAND_ITEM_OVERRIDE);
        put("overrideGraphicsMode", ServerRuleIndices.OVERRIDE_GRAPHICS_MODE);
        put("customCreativeItems", ServerRuleIndices.CUSTOM_CREATIVE_ITEMS);
        put("qibBehaviors", ServerRuleIndices.QIB_BEHAVIORS);
    }};
    
    private static final Map<String, Integer> allEntityRules = new HashMap<>() {{
        put("disableBubbles", EntityRuleIndices.DISABLE_BUBBLES);
        put("beamColor", EntityRuleIndices.BEAM_COLOR);
        put("qibBehavior", EntityRuleIndices.QIB_BEHAVIOR);
        put("interactionWidthZ", EntityRuleIndices.QIB_WIDTH_Z);
    }};

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        
        manager = new HookedNoxesiumManager(this, LoggerFactory.getLogger("NoxesiumPaperManager"));
        manager.register();
        entityRuleManager = new EntityRuleManager(manager);
        entityRuleManager.register();
        
        serverRules = new ServerRules(manager);
        entityRules = new EntityRules(manager);

        loadQibEffectsAndDefinitions();
        registerCommands();

        // Register Bukkit listener
        getServer().getPluginManager().registerEvents(new NoxesiumBukkitListener(), this);

        // Check for updates
        if (getConfig().getBoolean("checkForUpdates")) updateChecker.beginChecking(5 * 60 * 20);

    }

    @Override
    public void onDisable() {
        entityRuleManager.unregister();
        manager.unregister();
    }

    private void loadQibEffectsAndDefinitions() {
        qibEffects.clear();
        qibDefinitions.clear();
        
        var path = Path.of(getDataFolder().getPath(), "qibs");
        if (!path.toFile().exists() && !path.toFile().mkdirs()) {
            getLogger().warning("Couldn't find '/qibs' folder.");
            return;
        }
        
        try (Stream<Path> files = Files.list(path)) {
            var qibGson = QibDefinition.QIB_GSON;
            files.forEach(qibFilePath -> {
                if (Files.isDirectory(qibFilePath) || !qibFilePath.toFile().getName().endsWith(".json")) return;
                try {
                    var qibEffect = qibGson.fromJson(Files.readString(qibFilePath), QibEffect.class);
                    var name = qibFilePath.toFile().getName().replace(".json", "");
                    qibEffects.put(name, qibEffect);
                    getLogger().info("Loaded qibEffect '%s': %s!".formatted(name, qibEffect.toString()));
                } catch (IOException e) {
                    getLogger().warning("Failed to read the '/qibs/%s' file.".formatted(qibFilePath.toFile().getName()));
                }
            });
        }
        catch (IOException e) {
            getLogger().warning("Failed to read the '/qibs' folder.");
        }
        
        var qibDefinitionsConfigSection = getConfig().getConfigurationSection("qibDefinitions");
        if (qibDefinitionsConfigSection == null || qibDefinitionsConfigSection.getKeys(false).isEmpty()) {
            getLogger().warning("No QIB definitions found.");
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
            
            if (section.contains("onEnter")) onEnter = qibEffects.get(section.getString("onEnter"));
            if (section.contains("onLeave")) onLeave = qibEffects.get(section.getString("onLeave"));
            if (section.contains("whileInside")) whileInside = qibEffects.get(section.getString("whileInside"));
            if (section.contains("onJump")) onJump = qibEffects.get(section.getString("onJump"));
            
            QibDefinition qibDefinition = new QibDefinition(onEnter, onLeave, whileInside, onJump, triggerEnterLeaveOnSwitch);
            qibDefinitions.put(key, qibDefinition);
            
            getLogger().info("Loaded qibDefinition '%s': %s!".formatted(key, qibDefinition.toString()));
        });
    }

    @SuppressWarnings({"unsafe", "unchecked"})
    private void registerCommands() {
        CommandAPI.registerCommand(NoxesiumUtilsCommand.class);

        List<CommandAPICommand> serverRulesSubcommands = new LinkedList<>();
        List<CommandAPICommand> entityRulesSubcommands = new LinkedList<>();
        
        // Reset option for all ServerRules
        allServerRules.forEach((String name, Integer index) -> {
            serverRulesSubcommands.add(
                    new CommandAPICommand(name)
                            .withArguments(
                                    new EntitySelectorArgument.ManyPlayers("players"),
                                    new LiteralArgument("reset")
                            )
                            .executes((sender, args) -> {
                                var players = (Collection<Player>) args.get("players");
                                resetServerRule(sender, players, index);
                            })
            );
        });

        allEntityRules.forEach((String name, Integer index) -> {
            entityRulesSubcommands.add(
                    new CommandAPICommand(name)
                            .withArguments(
                                    new EntitySelectorArgument.ManyEntities("entities"),
                                    new LiteralArgument("reset")
                            )
                            .executes((sender, args) -> {
                                var entities = (Collection<Entity>) args.get("entities");
                                resetEntityRule(sender, entities, index);
                            })
            );
        });
        
        // Reset all ServerRules
        serverRulesSubcommands.add(
                new CommandAPICommand("reset")
                        .withArguments(
                                new EntitySelectorArgument.ManyPlayers("players")
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Player>) args.get("players");
                            if (players == null) return;
                            AtomicInteger updates = new AtomicInteger();
                            players.forEach(player -> {
                                allServerRules.forEach((String name, Integer index) -> {
                                    var rule = NoxesiumUtils.getManager().getServerRule(player, index);
                                    if (rule == null) return;
                                    rule.reset();
                                });
                                updates.getAndIncrement();
                            });

                            if (sender != null)
                                sender.sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
                        })
        );

        entityRulesSubcommands.add(
                new CommandAPICommand("reset")
                        .withArguments(
                                new EntitySelectorArgument.ManyEntities("entities")
                        )
                        .executes((sender, args) -> {
                            var entities = (Collection<Entity>) args.get("entities");
                            if (entities == null) return;
                            AtomicInteger updates = new AtomicInteger();
                            entities.forEach(entity -> {
                                allEntityRules.forEach((String name, Integer index) -> {
                                    var rule = NoxesiumUtils.getEntityRuleManager().getEntityRule(entity, index);
                                    if (rule == null) return;
                                    rule.reset();
                                });
                                updates.getAndIncrement();
                            });

                            if (sender != null)
                                sender.sendMessage(Component.text(updates.get() + " entities affected.", NamedTextColor.GREEN));
                        })
        );
        
        // Anything that stores a Boolean
        booleanServerRules.forEach((String name, Integer index) -> {
            serverRulesSubcommands.add(
                    new CommandAPICommand(name)
                        .withArguments(
                                new EntitySelectorArgument.ManyPlayers("players"),
                                new BooleanArgument("value")
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Player>) args.get("players");
                            var value = args.get("value");
                            updateServerRule(sender, players, index, value);
                        })
            );
        });

        // Anything that stores an Int
        integerServerRules.forEach((String name, Integer index) -> {
            serverRulesSubcommands.add(
                    new CommandAPICommand(name)
                        .withArguments(
                                new EntitySelectorArgument.ManyPlayers("players"),
                                new IntegerArgument("value")
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Player>) args.get("players");
                            var value = args.get("value");
                            updateServerRule(sender, players, index, value);                        
                        })
            );
        });

        // handItemOverride
        serverRulesSubcommands.add(
                new CommandAPICommand("handItemOverride")
                        .withArguments(
                                new EntitySelectorArgument.ManyPlayers("players"),
                                new ItemStackArgument("value")
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Player>) args.get("players");
                            var value = args.get("value");
                            updateServerRule(sender, players, ServerRuleIndices.HAND_ITEM_OVERRIDE, value);
                        })
        );
        
        // overrideGraphicsMode
        for (var type : GraphicsType.getEntries()) {
            serverRulesSubcommands.add(
                    new CommandAPICommand("overrideGraphicsMode")
                            .withArguments(
                                    new EntitySelectorArgument.ManyPlayers("players"),
                                    new LiteralArgument(type.name().toLowerCase(Locale.ROOT))
                            )
                            .executes((sender, args) -> {
                                var players = (Collection<Player>) args.get("players");
                                updateServerRule(sender, players, ServerRuleIndices.OVERRIDE_GRAPHICS_MODE, Optional.of(type));
                            })
            );
        }
        
        // TODO: Implement ServerRule: customCreativeItems
        //       Currently no idea what would be the best way to do them.
        //       I could do add/remove commands, but then it would be too hard to 
        //       handle everyone. Probably will do it in the config and just make this a Boolean.
        
        // TODO: Implement ServerRule: qibBehaviors
        //       Might either do JSON files for each behaviour, since I see
        //       some deserialization in the Noxesium API
        serverRulesSubcommands.add(
                new CommandAPICommand("qibBehaviors")
                        .withArguments(
                                new EntitySelectorArgument.ManyPlayers("players"),
                                new ListArgumentBuilder<Map.Entry<String, QibDefinition>>("definitions", ",")
                                        .withList(qibDefinitions.entrySet())
                                        .withMapper(Map.Entry::getKey)
                                        .buildGreedy()
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Player>) args.get("players");
                            var effects = (Collection<Map.Entry<String, QibDefinition>>) args.get("definitions");
                            if (effects == null) return;
                            Map<String, QibDefinition> mappedEffects = new HashMap<>();
                            effects.forEach(effect -> mappedEffects.put(effect.getKey(), effect.getValue()));
                            getLogger().info("Definitions: " + effects);
                            updateServerRule(sender, players, ServerRuleIndices.QIB_BEHAVIORS, mappedEffects);
                        })
        );
        
        qibDefinitions.keySet().forEach(name -> {
            entityRulesSubcommands.add(
                    new CommandAPICommand("qibBehavior")
                            .withArguments(
                                    new EntitySelectorArgument.ManyEntities("entities"),
                                    new LiteralArgument(name)
                            )
                            .executes((sender, args) -> {
                                var entities = (Collection<Entity>) args.get("entities");
                                if (entities == null) return;
                                if (entities.stream().map(Entity::getType).noneMatch(x -> x == EntityType.INTERACTION)) {
                                    sender.sendMessage(Component.text("WARNING: 'qibBehaviour' EntityRule is applicable only to minecraft:interaction entities.", NamedTextColor.YELLOW));
                                }
                                updateEntityRule(sender, entities, EntityRuleIndices.QIB_BEHAVIOR, name);
                            })
            ); 
        });
        
        entityRulesSubcommands.add(
                new CommandAPICommand("disableBubbles")
                        .withArguments(
                                new EntitySelectorArgument.ManyEntities("entities"),
                                new BooleanArgument("value")
                        )
                        .executes((sender, args) -> {
                            var entities = (Collection<Entity>) args.get("entities");
                            var value = args.get("value");
                            if (entities == null) return;
                            if (entities.stream().map(Entity::getType).noneMatch(x -> x == EntityType.GUARDIAN || x == EntityType.ELDER_GUARDIAN)) {
                                sender.sendMessage(Component.text("WARNING: 'disableBubbles' EntityRule is applicable only to minecraft:guardian & minecraft:elder_guardian entities.", NamedTextColor.YELLOW));
                            }
                            updateEntityRule(sender, entities, EntityRuleIndices.DISABLE_BUBBLES, value);
                        })
        );

        entityRulesSubcommands.add(
                new CommandAPICommand("beamColor")
                        .withArguments(
                                new EntitySelectorArgument.ManyEntities("entities"),
                                new TextArgument("hex")
                        )
                        .executes((sender, args) -> {
                            var entities = (Collection<Entity>) args.get("entities");
                            if (entities == null) return;
                            if (entities.stream().map(Entity::getType).noneMatch(x -> x == EntityType.GUARDIAN || x == EntityType.ELDER_GUARDIAN)) {
                                sender.sendMessage(Component.text("WARNING: 'beamColor' EntityRule is applicable only to minecraft:guardian & minecraft:elder_guardian entities.", NamedTextColor.YELLOW));
                            }
                            
                            var hexColor = (String) args.get("hex");
                            if (hexColor == null) return;
                            var color = Color.decode(hexColor);
                            updateEntityRule(sender, entities, EntityRuleIndices.BEAM_COLOR, Optional.of(color));
                        })
        );

        entityRulesSubcommands.add(
                new CommandAPICommand("interactionWidthZ")
                        .withArguments(
                                new EntitySelectorArgument.ManyEntities("entities"),
                                new DoubleArgument("value")
                        )
                        .executes((sender, args) -> {
                            var entities = (Collection<Entity>) args.get("entities");
                            var value = args.get("value");
                            if (entities == null) return;
                            if (entities.stream().map(Entity::getType).noneMatch(x -> x == EntityType.INTERACTION)) {
                                sender.sendMessage(Component.text("WARNING: 'qibBehaviour' EntityRule is applicable only to minecraft:interaction entities.", NamedTextColor.YELLOW));
                            }
                            updateEntityRule(sender, entities, EntityRuleIndices.QIB_WIDTH_Z, value);
                        })
        );
        
        var serverRules = new CommandAPICommand("serverRules")
                .withPermission("noxesiumutils.serverrules")
                .withSubcommands(serverRulesSubcommands.toArray(new CommandAPICommand[0]));

        var entityRules = new CommandAPICommand("entityRules")
                .withPermission("noxesiumutils.entityrules")
                .withSubcommands(entityRulesSubcommands.toArray(new CommandAPICommand[0]));

        new CommandAPICommand("noxesiumutils")
                .withPermission("noxesiumutils.about")
                .withSubcommands(serverRules, entityRules)
                .register(this);
    }
    
    private static void updateServerRule(@Nullable CommandSender sender, Collection<Player> players, Integer index, Object value) {
        if (players == null) return;
        AtomicInteger updates = new AtomicInteger();
        players.forEach(player -> {
            var rule = NoxesiumUtils.getManager().getServerRule(player, index);
            if (rule == null) return;
            rule.setValue(value);
            updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
    }

    private static void resetServerRule(@Nullable CommandSender sender, Collection<Player> players, Integer index) {
        if (players == null) return;
        AtomicInteger updates = new AtomicInteger();
        players.forEach(player -> {
            var rule = NoxesiumUtils.getManager().getServerRule(player, index);
            if (rule == null) return;
            rule.reset();
            updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
    }

    private static void updateEntityRule(@Nullable CommandSender sender, Collection<Entity> entities, Integer index, Object value) {
        if (entities == null) return;
        AtomicInteger updates = new AtomicInteger();
        entities.forEach(entity -> {
            var rule = NoxesiumUtils.getEntityRuleManager().getEntityRule(entity, index);
            if (rule == null) return;
            rule.setValue(value);
            updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " entities affected.", NamedTextColor.GREEN));
    }

    private static void resetEntityRule(@Nullable CommandSender sender, Collection<Entity> entities, Integer index) {
        if (entities == null) return;
        AtomicInteger updates = new AtomicInteger();
        entities.forEach(entity -> {
            var rule = NoxesiumUtils.getEntityRuleManager().getEntityRule(entity, index);
            if (rule == null) return;
            rule.reset();
            updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " entities affected.", NamedTextColor.GREEN));
    }

    public static void sendLoginServerRules(Player player) {
        // Send defaults
        if (NoxesiumUtils.getPlugin().getConfig().getBoolean("sendDefaultsOnJoin", false)) {
            // Send defaults after a little time, so the client actually registers the packet.
            var defaults = NoxesiumUtils.getPlugin().getConfig().getConfigurationSection("defaults");
            if (defaults == null) return;
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Send present Boolean ServerRules  
                    booleanServerRules.forEach((String name, Integer index) -> {
                        if (!defaults.contains(name)) return;
                        var value = defaults.getBoolean(name, false);
                        updateServerRule(null, List.of(player), index, value);
                    });

                    // Send present Int ServerRules
                    integerServerRules.forEach((String name, Integer index) -> {
                        if (!defaults.contains(name)) return;
                        var value = defaults.getInt(name, 0);
                        updateServerRule(null, List.of(player), index, value);
                    });

                    // overrideGraphicsMode
                    if (defaults.contains("overrideGraphicsMode")) {
                        var overrideGraphicsModeStr = defaults.getString("overrideGraphicsMode");
                        var overrideGraphicsModeValue = Optional.of(GraphicsType.valueOf(overrideGraphicsModeStr));
                        updateServerRule(null, List.of(player), ServerRuleIndices.OVERRIDE_GRAPHICS_MODE, overrideGraphicsModeValue);
                    }
                    
                    // TODO:
                    //  handItemOverride
                    //  customCreativeItems
                    //  qibBehaviors
                }
            }.runTaskLater(NoxesiumUtils.getPlugin(), 5);
        }
    }
}
