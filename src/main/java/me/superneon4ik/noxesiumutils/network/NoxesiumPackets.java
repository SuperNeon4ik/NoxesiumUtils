package me.superneon4ik.noxesiumutils.network;

import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import me.superneon4ik.noxesiumutils.network.serverbound.legacy.LegacyServerboundClientInformationPacket;
import me.superneon4ik.noxesiumutils.network.serverbound.legacy.LegacyServerboundClientSettingsPacket;
import me.superneon4ik.noxesiumutils.network.serverbound.legacy.LegacyServerboundNoxesiumPacket;
import me.superneon4ik.noxesiumutils.network.serverbound.v1.ServerboundClientInformationPacket;
import me.superneon4ik.noxesiumutils.network.serverbound.v1.ServerboundClientSettingsPacket;
import me.superneon4ik.noxesiumutils.network.serverbound.v1.ServerboundNoxesiumPacket;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class NoxesiumPackets {

    private static final Map<String, ServerboundNoxesiumPacket> serverboundPackets = new HashMap<>();
    private static final Map<String, LegacyServerboundNoxesiumPacket> legacyServerboundPackets = new HashMap<>();

    static {
        server(new ServerboundClientInformationPacket());
        server(new ServerboundClientSettingsPacket());
        server(new LegacyServerboundClientInformationPacket());
        server(new LegacyServerboundClientSettingsPacket());
    }

    private static void server(ServerboundNoxesiumPacket packet) {
        serverboundPackets.put(packet.channel, packet);
    }

    private static void server(LegacyServerboundNoxesiumPacket packet) {
        legacyServerboundPackets.put(packet.channel, packet);
    }

    public static void handle(Player player, String channel, FriendlyByteBuf buffer) {
        if (!serverboundPackets.containsKey(channel))
            throw new UnsupportedOperationException(String.format("No serverbound packet handler registered for channel '%s'", channel));
        serverboundPackets.get(channel).receive(player, buffer.readVarInt(), buffer);
    }

    public static void handleLegacy(Player player, String channel, FriendlyByteBuf buffer) {
        if (!legacyServerboundPackets.containsKey(channel))
            throw new UnsupportedOperationException(String.format("No serverbound packet handler registered for channel '%s'", channel));
        legacyServerboundPackets.get(channel).receive(player, buffer);
    }
}
