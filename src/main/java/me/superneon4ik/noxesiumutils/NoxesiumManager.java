package me.superneon4ik.noxesiumutils;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.api.protocol.NoxesiumServerManager;
import com.noxcrew.noxesium.api.protocol.rule.ServerRule;
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
    public @Nullable <T> ServerRule<T, ?> getServerRule(Player player, int i) {
        throw new RuntimeException("Not implemented.");
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
}
