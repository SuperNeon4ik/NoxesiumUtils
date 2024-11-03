package me.superneon4ik.noxesiumutils;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.paper.api.NoxesiumManager;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerReadyEvent;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerRegisteredEvent;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerSettingsReceivedEvent;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerVersionReceivedEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * This is just {@link NoxesiumManager}, but it calls Bukkit
 * events on actions.
 */
public class HookedNoxesiumManager extends NoxesiumManager {
    public HookedNoxesiumManager(@NotNull Plugin plugin, @NotNull Logger logger) {
        super(plugin, logger);
    }

    @Override
    protected void onReady(@NotNull Player player) {
        new NoxesiumPlayerReadyEvent(player).callEvent();
    }

    @Override
    protected void onPlayerRegistered(@NotNull Player player) {
        new NoxesiumPlayerRegisteredEvent(player).callEvent();
    }

    @Override
    protected void onPlayerVersionReceived(@NotNull Player player, @NotNull String version, int protocolVersion) {
        new NoxesiumPlayerVersionReceivedEvent(player, version, protocolVersion).callEvent();
    }

    @Override
    protected void onPlayerSettingsReceived(@NotNull Player player, @NotNull ClientSettings clientSettings) {
        new NoxesiumPlayerSettingsReceivedEvent(player, clientSettings).callEvent();
    }

    /**
     * Tries to update a server rule for a player.
     * @param player Player to update the server rule for.
     * @param index Index of the server rule (from {@link com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices} for example)
     * @param value The value of the server rule
     * @return True if the rule for successfully set, false if not.
     */
    public boolean setServerRule(Player player, int index, Object value) {
        var rule = getServerRule(player, index);
        if (rule == null) return false;
        rule.setValue(value);
        return true;
    }

    /**
     * Tries to reset a server rule to the default value for a player.
     * @param player Player to reset the server rule for.
     * @param index Index of the server rule (from {@link com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices} for example)
     * @return True if the rule for successfully set, false if not.
     */
    public boolean resetServerRule(Player player, int index) {
        var rule = getServerRule(player, index);
        if (rule == null) return false;
        rule.reset();
        return true;
    }
}
