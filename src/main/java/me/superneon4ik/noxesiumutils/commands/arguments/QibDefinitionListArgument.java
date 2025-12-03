package me.superneon4ik.noxesiumutils.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.noxcrew.noxesium.api.qib.QibDefinition;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class QibDefinitionListArgument implements CustomArgumentType<@NotNull Set<Map.Entry<String, QibDefinition>>, @NotNull String> {

    private static final DynamicCommandExceptionType ERROR_NO_QIB_DEFINITION = new DynamicCommandExceptionType(name ->
            MessageComponentSerializer.message().serialize(Component.text("Qib definition with name '" + name + "' does not exist!"))
    );

    private final NoxesiumUtils noxesiumUtils;

    public QibDefinitionListArgument(NoxesiumUtils noxesiumUtils) {
        this.noxesiumUtils = noxesiumUtils;
    }

    @Override
    public Set<Map.Entry<String, QibDefinition>> parse(@NotNull StringReader reader) throws CommandSyntaxException {
        var definitionsString = getNativeType().parse(reader);
        var definitionNames = definitionsString.split(",");

        var result = new HashSet<Map.Entry<String, QibDefinition>>(definitionNames.length);

        for (String definitionName : definitionNames) {
            if (!noxesiumUtils.getConfig().getQibDefinitions().containsKey(definitionName)) {
                throw ERROR_NO_QIB_DEFINITION.create(definitionName);
            }

            result.add(Map.entry(definitionName, noxesiumUtils.getConfig().getQibDefinitions().get(definitionName)));
        }

        return result;
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        var previousDefinitions = builder.getRemainingLowerCase()
                .substring(0, builder.getRemainingLowerCase().lastIndexOf(',') + 1);
        var lastDefinition = builder.getRemainingLowerCase()
                .substring(builder.getRemainingLowerCase().lastIndexOf(',') + 1);

        var usedSet = Arrays.stream(previousDefinitions
                        .substring(0, Math.max(previousDefinitions.length() - 1, 0))
                        .split(","))
                .map(v -> v.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());

        noxesiumUtils.getConfig().getQibDefinitions().keySet().stream()
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(lastDefinition) && !usedSet.contains(name))
                .map(v -> previousDefinitions + v)
                .forEach(builder::suggest);

        return builder.buildFuture();
    }
}
