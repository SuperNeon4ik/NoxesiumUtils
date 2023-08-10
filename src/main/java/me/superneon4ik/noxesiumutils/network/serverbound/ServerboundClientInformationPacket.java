package me.superneon4ik.noxesiumutils.network.serverbound;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerJoinEvent;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ServerboundClientInformationPacket extends ServerboundNoxesiumPacket {
    public ServerboundClientInformationPacket() {
        super(NoxesiumUtils.NOXESIUM_V1_CLIENT_INFORMATION_CHANNEL, 1);
    }

    @Override
    public void receive(Player player, int version, FriendlyByteBuf buffer) {
        int protocolVersion = buffer.readVarInt();
        NoxesiumUtils.getManager().registerClient(player.getUniqueId(), protocolVersion);
        NoxesiumUtils.getPlugin().getLogger().info(String.format(
                "%s has Noxesium installed. (ProtocolVersion: %d/v1)", player.getName(), protocolVersion));

        NoxesiumPlayerJoinEvent event = new NoxesiumPlayerJoinEvent(player, protocolVersion);
        Bukkit.getPluginManager().callEvent(event);
    }
}
