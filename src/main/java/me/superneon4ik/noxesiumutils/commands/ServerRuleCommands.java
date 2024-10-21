package me.superneon4ik.noxesiumutils.commands;

import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import com.noxcrew.noxesium.paper.api.rule.GraphicsType;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class ServerRuleCommands {
    private final NoxesiumUtils noxesiumUtils;

    public ServerRuleCommands(NoxesiumUtils noxesiumUtils) {
        this.noxesiumUtils = noxesiumUtils;
    }
    
    public List<CommandAPICommand> generate() {
        List<CommandAPICommand> commands = new LinkedList<>();
        commands.addAll(booleanRule("disableSpinAttackCollisions", ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS));
        commands.addAll(booleanRule("cameraLocked", ServerRuleIndices.CAMERA_LOCKED));
        commands.addAll(booleanRule("disableVanillaMusic", ServerRuleIndices.DISABLE_VANILLA_MUSIC));
        commands.addAll(booleanRule("disableBoatCollisions", ServerRuleIndices.DISABLE_BOAT_COLLISIONS));
        commands.addAll(booleanRule("disableUiOptimizations", ServerRuleIndices.DISABLE_UI_OPTIMIZATIONS));
        commands.addAll(booleanRule("showMapInUi", ServerRuleIndices.SHOW_MAP_IN_UI));
        commands.addAll(booleanRule("disableDeferredChunkUpdates", ServerRuleIndices.DISABLE_DEFERRED_CHUNK_UPDATES));
        commands.addAll(booleanRule("disableMapUi", ServerRuleIndices.DISABLE_MAP_UI));
        commands.addAll(booleanRule("disableMapUi", ServerRuleIndices.DISABLE_MAP_UI));

        // TODO: Implement Noxcrew's recommendations
        // https://github.com/Noxcrew/noxesium/blob/4b3f93fe6886eac60dbfffa6cb125e1e5a31886a/api/src/main/java/com/noxcrew/noxesium/api/protocol/rule/ServerRuleIndices.java#L85
        commands.addAll(booleanRule("enableSmootherClientTrident", ServerRuleIndices.ENABLE_SMOOTHER_CLIENT_TRIDENT));

        commands.addAll(integerRule("heldItemNameOffset", ServerRuleIndices.HELD_ITEM_NAME_OFFSET));
        commands.addAll(integerRule("riptideCoyoteTime", ServerRuleIndices.RIPTIDE_COYOTE_TIME));
        
        commands.addAll(itemStackRule("handItemOverride", ServerRuleIndices.HAND_ITEM_OVERRIDE));
        
        commands.addAll(valueToggleCommand("customCreativeItems", ServerRuleIndices.CUSTOM_CREATIVE_ITEMS, 
                noxesiumUtils.getConfig().getCustomCreativeItems()));
        
        commands.addAll(graphicsTypeCommand("overrideGraphicsMode", ServerRuleIndices.OVERRIDE_GRAPHICS_MODE));
        commands.addAll(qibBehaviorCommand("qibBehaviors", ServerRuleIndices.QIB_BEHAVIORS));
        
        return commands;
    }
    
    private CommandAPICommand resetRuleCommand(String name, int index) {
        return new CommandAPICommand(name)
                .withArguments(
                        new EntitySelectorArgument.ManyPlayers("players"),
                        new LiteralArgument("reset")
                )
                .executes((sender, args) -> {
                    var players = (Collection<Player>) args.get("players");
                    resetServerRule(sender, players, index);
                });
    }
    
    private List<CommandAPICommand> argumentRule(String name, int index, Argument<?> argument) {
        return List.of(
                new CommandAPICommand(name)
                        .withArguments(
                                new EntitySelectorArgument.ManyPlayers("players"),
                                argument
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Player>) args.get("players");
                            var value = args.get("value");
                            updateServerRule(sender, players, index, value);
                        }),
                resetRuleCommand(name, index)
        );
    }
    
    private List<CommandAPICommand> booleanRule(String name, int index) {
        return argumentRule(name, index, new BooleanArgument("value"));
    }

    private List<CommandAPICommand> integerRule(String name, int index) {
        return argumentRule(name, index, new BooleanArgument("value"));
    }
    
    private List<CommandAPICommand> itemStackRule(String name, int index) {
        return argumentRule(name, index, new ItemStackArgument("value"));
    }
    
    private List<CommandAPICommand> valueToggleCommand(String name, int index, Object value) {
        return List.of(
                new CommandAPICommand(name)
                        .withArguments(
                                new EntitySelectorArgument.ManyPlayers("players"),
                                new BooleanArgument("toggle")
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Player>) args.get("players");
                            var toggle = (Boolean) args.get("toggle");
                            if (Boolean.TRUE.equals(toggle))
                                updateServerRule(sender, players, index, value);
                            else 
                                resetServerRule(sender, players, index);
                        }),
                resetRuleCommand(name, index)
        );
    }
    
    private List<CommandAPICommand> graphicsTypeCommand(String name, int index) {
        List<CommandAPICommand> commands = new LinkedList<>();
        for (GraphicsType type : GraphicsType.values()) {
            commands.add(
                    new CommandAPICommand(name)
                            .withArguments(
                                    new EntitySelectorArgument.ManyPlayers("players"),
                                    new LiteralArgument(type.name().toLowerCase(Locale.ROOT))
                            )
                            .executes((sender, args) -> {
                                var players = (Collection<Player>) args.get("players");
                                updateServerRule(sender, players, index, Optional.of(type));
                            })
            );
        }
        
        commands.add(resetRuleCommand(name, index));
        return commands;
    }
    
    private List<CommandAPICommand> qibBehaviorCommand(String name, int index) {
        return List.of(
                new CommandAPICommand("qibBehaviors")
                        .withArguments(
                                new EntitySelectorArgument.ManyPlayers("players"),
                                new ListArgumentBuilder<Map.Entry<String, QibDefinition>>("definitions", ",")
                                        .withList(noxesiumUtils.getConfig().getQibDefinitions().entrySet())
                                        .withMapper(Map.Entry::getKey)
                                        .buildGreedy()
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Player>) args.get("players");
                            var effects = (Collection<Map.Entry<String, QibDefinition>>) args.get("definitions");
                            if (effects == null) return;
                            Map<String, QibDefinition> mappedEffects = new HashMap<>();
                            effects.forEach(effect -> mappedEffects.put(effect.getKey(), effect.getValue()));
                            noxesiumUtils.getLogger().info("Definitions: " + effects);
                            updateServerRule(sender, players, ServerRuleIndices.QIB_BEHAVIORS, mappedEffects);
                        }),
                new CommandAPICommand("qibBehaviors")
                        .withArguments(
                                new EntitySelectorArgument.ManyPlayers("players"),
                                new LiteralArgument("*")
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Player>) args.get("players");
                            noxesiumUtils.getLogger().info("Definitions: " + noxesiumUtils.getConfig().getQibDefinitions());
                            updateServerRule(sender, players, ServerRuleIndices.QIB_BEHAVIORS, noxesiumUtils.getConfig().getQibDefinitions());
                        }),
                resetRuleCommand(name, index)
        );
    }
    
    // ---

    private void updateServerRule(@Nullable CommandSender sender, Collection<Player> players, Integer index, Object value) {
        if (players == null) return;
        AtomicInteger updates = new AtomicInteger();
        players.forEach(player -> {
            if (noxesiumUtils.getManager().setServerRule(player, index, value))
                updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
    }

    private void resetServerRule(@Nullable CommandSender sender, Collection<Player> players, Integer index) {
        if (players == null) return;
        AtomicInteger updates = new AtomicInteger();
        players.forEach(player -> {
            if (noxesiumUtils.getManager().resetServerRule(player, index))
                updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
    }
    
}
