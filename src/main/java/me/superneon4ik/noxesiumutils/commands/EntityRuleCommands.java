package me.superneon4ik.noxesiumutils.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noxcrew.noxesium.api.protocol.rule.EntityRuleIndices;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.commands.arguments.QibDefinitionArgument;
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

public class EntityRuleCommands {
    private final NoxesiumUtils noxesiumUtils;

    public EntityRuleCommands(NoxesiumUtils noxesiumUtils) {
        this.noxesiumUtils = noxesiumUtils;
    }

    /**
     * Generate all needed entity rule commands
     */
    public List<LiteralArgumentBuilder<CommandSourceStack>> generate() {
        List<LiteralArgumentBuilder<CommandSourceStack>> commands = new LinkedList<>();

        commands.add(booleanRule("disableBubbles", EntityRuleIndices.DISABLE_BUBBLES));
        commands.add(colorRule("beamColor", EntityRuleIndices.BEAM_COLOR));
        commands.add(colorRule("beamColorFade", EntityRuleIndices.BEAM_COLOR_FADE));
        commands.add(colorRule("customGlowColor", EntityRuleIndices.CUSTOM_GLOW_COLOR));
        commands.add(integerRule("interactionWidthZ", EntityRuleIndices.QIB_WIDTH_Z));
        commands.add(qibBehaviorRule("qibBehavior", EntityRuleIndices.QIB_BEHAVIOR));

        commands.add(generateResetCommand());

        return commands;
    }

    private LiteralArgumentBuilder<CommandSourceStack> generateResetCommand() {
        return Commands.literal("reset")
                .then(
                        Commands.argument("entities", ArgumentTypes.entities())
                                .executes(ctx -> {
                                    final var entitySelectorArgumentResolver = ctx.getArgument("entities", EntitySelectorArgumentResolver.class);
                                    final var entities = entitySelectorArgumentResolver.resolve(ctx.getSource());

                                    AtomicInteger updates = new AtomicInteger();
                                    entities.forEach(entity -> {
                                        boolean hasChanged = false;
                                        for (Integer idx : noxesiumUtils.getManager().getEntityRules().getContents().keySet()) {
                                            var rule = noxesiumUtils.getEntityRuleManager().getEntityRule(entity, idx);
                                            if (rule == null) continue;
                                            rule.reset();
                                            hasChanged = true;
                                        }

                                        if (hasChanged)
                                            updates.incrementAndGet();
                                    });

                                    ctx.getSource().getSender().sendMessage(Component.text(updates.get() + " player(s) affected.", NamedTextColor.GREEN));
                                    return Command.SINGLE_SUCCESS;
                                })
                );
    }

    public LiteralArgumentBuilder<CommandSourceStack> resetCommand(int index) {
        return Commands.literal("reset")
                .executes(ctx -> {
                    final var entitySelectorArgumentResolver = ctx.getArgument("entities", EntitySelectorArgumentResolver.class);
                    final var entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
                    resetEntityRule(ctx.getSource().getSender(), entities, index);
                    return Command.SINGLE_SUCCESS;
                });
    }

    /**
     * Command that sets the rule's value to the value
     * returned by the provided CommandAPI Argument.
     * Also adds a {@link EntityRuleCommands#resetCommand(int)}
     */
    public LiteralArgumentBuilder<CommandSourceStack> argumentRule(String name, int index, ArgumentType<?> argument) {
        return Commands.literal(name)
                .then(Commands.argument("entities", ArgumentTypes.entities())
                        .then(Commands.argument("value", argument)
                                .executes(ctx -> {
                                    final var entitySelectorArgumentResolver = ctx.getArgument("entities", EntitySelectorArgumentResolver.class);
                                    final var entities = entitySelectorArgumentResolver.resolve(ctx.getSource());

                                    Object value;
                                    var valueTypeOrResolver = ctx.getArgument("value", Object.class);
                                    if (valueTypeOrResolver instanceof ArgumentResolver<?> resolver) {
                                        value = resolver.resolve(ctx.getSource());
                                    }
                                    else {
                                        value = valueTypeOrResolver;
                                    }

                                    updateEntityRule(ctx.getSource().getSender(), entities, index, value);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(resetCommand(index)));
    }

    public LiteralArgumentBuilder<CommandSourceStack> booleanRule(String name, int index) {
        return argumentRule(name, index, BoolArgumentType.bool());
    }

    public LiteralArgumentBuilder<CommandSourceStack> integerRule(String name, int index) {
        return argumentRule(name, index, IntegerArgumentType.integer());
    }

    public LiteralArgumentBuilder<CommandSourceStack> colorRule(String name, int index) {
        return Commands.literal(name)
                .then(Commands.argument("entities", ArgumentTypes.entities())
                        .then(Commands.argument("hex", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    final var entitySelectorArgumentResolver = ctx.getArgument("entities", EntitySelectorArgumentResolver.class);
                                    final var entities = entitySelectorArgumentResolver.resolve(ctx.getSource());

                                    if ((name.equals("beamColor") || name.equals("beamColorFade")) &&
                                            entities.stream().map(Entity::getType)
                                                .noneMatch(x -> List.of(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.END_CRYSTAL).contains(x))) {
                                        ctx.getSource().getSender().sendRichMessage(
                                                "<yellow><bold>WARNING:</bold> 'beamColor' EntityRule is applicable only to " +
                                                        "minecraft:guardian, minecraft:elder_guardian and minecraft:end_crystal entities."
                                        );
                                    }

                                    var hexColor = ctx.getArgument("hex", String.class);

                                    try {
                                        var color = Color.decode(hexColor);
                                        updateEntityRule(ctx.getSource().getSender(), entities, index, Optional.of(color));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    catch (NumberFormatException e) {
                                        ctx.getSource().getSender().sendMessage(Component.text("Invalid hex color '" + hexColor + "'.", NamedTextColor.RED));
                                        return -1;
                                    }
                                }))
                        .then(resetCommand(index)));
    }

    public LiteralArgumentBuilder<CommandSourceStack> qibBehaviorRule(String name, int index) {
        return Commands.literal(name)
                .then(Commands.argument("entities", ArgumentTypes.entities())
                        .then(Commands.argument("definition", new QibDefinitionArgument(noxesiumUtils))
                                .executes(ctx -> {
                                    final var entitySelectorArgumentResolver = ctx.getArgument("entities", EntitySelectorArgumentResolver.class);
                                    final var entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
                                    final var qibName = ctx.getArgument("definition", String.class);

                                    if (entities.stream().map(Entity::getType).noneMatch(x -> x == EntityType.INTERACTION)) {
                                        ctx.getSource().getSender().sendRichMessage(
                                                "<yellow><bold>WARNING:</bold> 'qibBehaviour' EntityRule is applicable " +
                                                        "only to minecraft:interaction entities."
                                        );
                                    }

                                    updateEntityRule(ctx.getSource().getSender(), entities, index, qibName);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(resetCommand(index)));
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
