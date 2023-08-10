package me.superneon4ik.noxesiumutils;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.api.protocol.NoxesiumServerManager;
import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;
import me.superneon4ik.noxesiumutils.feature.rule.ServerRules;
import me.superneon4ik.noxesiumutils.objects.ClientData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NoxesiumManager implements NoxesiumServerManager<Player> {

    private final Map<UUID, ClientData> clients = new HashMap<>();

    @Override
    @SuppressWarnings({"unsafe", "unchecked"})
    public @Nullable <T> ClientboundServerRule<T> getServerRule(Player player, int i) {
        if (!clients.containsKey(player.getUniqueId())) return null;
        var client = clients.get(player.getUniqueId());
        return (ClientboundServerRule<T>) client.serverRuleMap.getOrDefault(i, ServerRules.get(i));
    }

    @Override
    public @Nullable ClientSettings getClientSettings(Player player) {
        return getClientSettings(player.getUniqueId());
    }

    @Override
    public @Nullable ClientSettings getClientSettings(UUID uuid) {
        if (clients.containsKey(uuid))
            return clients.get(uuid).clientSettings;
        return null;
    }

    @Override
    public Integer getProtocolVersion(Player player) {
        return getProtocolVersion(player.getUniqueId());
    }

    @Override
    public Integer getProtocolVersion(UUID uuid) {
        if (clients.containsKey(uuid))
            return clients.get(uuid).protocolVersion;
        return null;
    }

    public void registerClient(UUID uuid, int protocolVersion) {
        clients.put(uuid, new ClientData(protocolVersion));
    }

    public void unregisterClient(UUID uuid) {
        clients.remove(uuid);
    }

    public void updateClientSettings(UUID uuid, ClientSettings clientSettings) {
        if (clients.containsKey(uuid))
            clients.get(uuid).clientSettings = clientSettings;
    }

    public boolean isUsingNoxesium(Player player, int minVersion) {
        Integer version = this.getProtocolVersion(player);
        if (version == null) {
            return false;
        } else {
            return version >= minVersion;
        }
    }
}
