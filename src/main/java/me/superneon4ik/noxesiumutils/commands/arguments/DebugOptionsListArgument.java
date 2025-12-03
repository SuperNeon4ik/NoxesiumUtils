package me.superneon4ik.noxesiumutils.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.noxcrew.noxesium.api.util.DebugOption;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DebugOptionsListArgument implements CustomArgumentType<@NotNull Set<DebugOption>, @NotNull String> {

    private static final DynamicCommandExceptionType ERROR_NO_DEBUG_OPTION = new DynamicCommandExceptionType(name ->
            MessageComponentSerializer.message().serialize(Component.text("Debug option with name '" + name + "' does not exist!"))
    );

    @Override
    public Set<DebugOption> parse(@NotNull StringReader reader) throws CommandSyntaxException {
        var optionsString = getNativeType().parse(reader);
        var optionNames = optionsString.split(",");

        var result = new HashSet<DebugOption>(optionNames.length);

        for (String optionName : optionNames) {
            var debugOption = Arrays.stream(DebugOption.values())
                    .filter(v -> v.name().equalsIgnoreCase(optionName))
                    .findFirst();

            if (debugOption.isEmpty()) {
                throw ERROR_NO_DEBUG_OPTION.create(optionName);
            }

            result.add(debugOption.get());
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

        Arrays.stream(DebugOption.values())
                .map(Enum::name)
                .map(v -> v.toLowerCase(Locale.ROOT))
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(lastDefinition) && !usedSet.contains(name))
                .map(v -> previousDefinitions + v)
                .forEach(builder::suggest);

        return builder.buildFuture();
    }
}
