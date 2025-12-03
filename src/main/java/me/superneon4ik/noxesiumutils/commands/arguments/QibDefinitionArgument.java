package me.superneon4ik.noxesiumutils.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class QibDefinitionArgument implements CustomArgumentType<@NotNull String, @NotNull String> {

    private static final DynamicCommandExceptionType ERROR_NO_QIB_DEFINITION = new DynamicCommandExceptionType(name ->
            MessageComponentSerializer.message().serialize(Component.text("Qib definition with name '" + name + "' does not exist!"))
    );

    private final NoxesiumUtils noxesiumUtils;

    public QibDefinitionArgument(NoxesiumUtils noxesiumUtils) {
        this.noxesiumUtils = noxesiumUtils;
    }

    @Override
    public String parse(@NotNull StringReader reader) throws CommandSyntaxException {
        var qibName = getNativeType().parse(reader);

        if (!noxesiumUtils.getConfig().getQibDefinitions().containsKey(qibName)) {
            throw ERROR_NO_QIB_DEFINITION.create(qibName);
        }

        return qibName;
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        noxesiumUtils.getConfig().getQibDefinitions().keySet().stream()
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }
}
