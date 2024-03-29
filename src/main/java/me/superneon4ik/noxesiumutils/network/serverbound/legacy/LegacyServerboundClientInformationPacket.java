package me.superneon4ik.noxesiumutils.network.serverbound.legacy;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.events.NoxesiumPlayerJoinEvent;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LegacyServerboundClientInformationPacket extends LegacyServerboundNoxesiumPacket {

    public LegacyServerboundClientInformationPacket() {
        super(NoxesiumUtils.NOXESIUM_LEGACY_CLIENT_INFORMATION_CHANNEL);
    }

    @Override
    public void receive(Player player, FriendlyByteBuf buffer) {
        int protocolVersion = buffer.readInt();
        NoxesiumUtils.getManager().registerClient(player.getUniqueId(), protocolVersion);
        NoxesiumUtils.getPlugin().getLogger().info(String.format(
                "%s has Noxesium installed. (ProtocolVersion: %d/legacy)", player.getName(), protocolVersion));

        NoxesiumPlayerJoinEvent event = new NoxesiumPlayerJoinEvent(player, protocolVersion);
        Bukkit.getPluginManager().callEvent(event);

        NoxesiumUtils.getPlugin().sendLoginServerRules(player);
    }
}
