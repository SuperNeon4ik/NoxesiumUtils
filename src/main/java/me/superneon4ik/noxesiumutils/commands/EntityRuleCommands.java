package me.superneon4ik.noxesiumutils.commands;

import com.noxcrew.noxesium.api.protocol.rule.EntityRuleIndices;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class EntityRuleCommands {
    private final NoxesiumUtils noxesiumUtils;

    public EntityRuleCommands(NoxesiumUtils noxesiumUtils) {
        this.noxesiumUtils = noxesiumUtils;
    }

    public List<CommandAPICommand> generate() {
        List<CommandAPICommand> commands = new LinkedList<>();
        
        commands.addAll(booleanRule("disableBubbles", EntityRuleIndices.DISABLE_BUBBLES));
        commands.addAll(colorRule("beamColor", EntityRuleIndices.BEAM_COLOR));
        commands.addAll(integerRule("interactionWidthZ", EntityRuleIndices.QIB_WIDTH_Z));
        commands.addAll(qibBehaviorRule("qibBehavior", EntityRuleIndices.QIB_BEHAVIOR));
        
        return commands;
    }

    public CommandAPICommand resetRuleCommand(String name, int index) {
        return new CommandAPICommand(name)
                .withArguments(
                        new EntitySelectorArgument.ManyEntities("entities"),
                        new LiteralArgument("reset")
                )
                .executes((sender, args) -> {
                    var players = (Collection<Entity>) args.get("entities");
                    resetEntityRule(sender, players, index);
                });
    }

    public List<CommandAPICommand> argumentRule(String name, int index, Argument<?> argument) {
        return List.of(
                new CommandAPICommand(name)
                        .withArguments(
                                new EntitySelectorArgument.ManyEntities("entities"),
                                argument
                        )
                        .executes((sender, args) -> {
                            var players = (Collection<Entity>) args.get("entities");
                            var value = args.get("value");
                            updateEntityRule(sender, players, index, value);
                        }),
                resetRuleCommand(name, index)
        );
    }

    public List<CommandAPICommand> booleanRule(String name, int index) {
        return argumentRule(name, index, new BooleanArgument("value"));
    }

    public List<CommandAPICommand> integerRule(String name, int index) {
        return argumentRule(name, index, new BooleanArgument("value"));
    }

    public List<CommandAPICommand> colorRule(String name, int index) {
        return List.of(
                new CommandAPICommand(name)
                        .withArguments(
                                new EntitySelectorArgument.ManyEntities("entities"),
                                new GreedyStringArgument("hex")
                        )
                        .executes((sender, args) -> {
                            var entities = (Collection<Entity>) args.get("entities");
                            if (entities == null) return;
                            if (entities.stream().map(Entity::getType).noneMatch(x -> x == EntityType.GUARDIAN || x == EntityType.ELDER_GUARDIAN)) {
                                sender.sendMessage(Component.text("WARNING: 'beamColor' EntityRule is applicable only to minecraft:guardian & minecraft:elder_guardian entities.", NamedTextColor.YELLOW));
                            }

                            var hexColor = (String) args.get("hex");
                            if (hexColor == null) return;
                            try {
                                var color = Color.decode(hexColor);
                                updateEntityRule(sender, entities, index, Optional.of(color));
                            }
                            catch (NumberFormatException e) {
                                sender.sendMessage(Component.text("Invalid hex color '" + hexColor + "'.", NamedTextColor.RED));
                            }
                        })      
        );
    }

    public List<CommandAPICommand> qibBehaviorRule(String name, int index) {
        return List.of(
                new CommandAPICommand(name)
                    .withArguments(
                            new EntitySelectorArgument.ManyEntities("entities"),
                            new MultiLiteralArgument("value", noxesiumUtils.getConfig().getQibDefinitions().keySet().toArray(new String[0]))
                    )
                    .executes((sender, args) -> {
                        var entities = (Collection<Entity>) args.get("entities");
                        var qibName = (String) args.get("value");
                        if (entities == null) return;
                        if (entities.stream().map(Entity::getType).noneMatch(x -> x == EntityType.INTERACTION)) {
                            sender.sendMessage(Component.text("WARNING: 'qibBehaviour' EntityRule is applicable only to minecraft:interaction entities.", NamedTextColor.YELLOW));
                        }
                        updateEntityRule(sender, entities, index, qibName);
                    }),
                resetRuleCommand(name, index)
        );
    } 
    
    // ---

    public void updateEntityRule(@Nullable CommandSender sender, Collection<Entity> entities, Integer index, Object value) {
        if (entities == null) return;
        AtomicInteger updates = new AtomicInteger();
        entities.forEach(entity -> {
            var rule = noxesiumUtils.getEntityRuleManager().getEntityRule(entity, index);
            if (rule == null) return;
            rule.setValue(value);
            updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " entities affected.", NamedTextColor.GREEN));
    }

    public void resetEntityRule(@Nullable CommandSender sender, Collection<Entity> entities, Integer index) {
        if (entities == null) return;
        AtomicInteger updates = new AtomicInteger();
        entities.forEach(entity -> {
            var rule = noxesiumUtils.getEntityRuleManager().getEntityRule(entity, index);
            if (rule == null) return;
            rule.reset();
            updates.getAndIncrement();
        });

        if (sender != null)
            sender.sendMessage(Component.text(updates.get() + " entities affected.", NamedTextColor.GREEN));
    }
}
