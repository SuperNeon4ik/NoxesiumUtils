package me.superneon4ik.noxesiumutils.listeners;

import io.netty.buffer.Unpooled;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NoxesiumMessageListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte @NotNull [] message) {
        if (channel.equals(NoxesiumUtils.NOXESIUM_CLIENT_INFORMATION_CHANNEL)) {
            // Get player's noxesium protocol version.
            FriendlyByteBuf incBuf = new FriendlyByteBuf(Unpooled.copiedBuffer(message));
            int protocolVersion = incBuf.readInt();
            NoxesiumUtils.getPlugin().getLogger().info(player.getName() + " has Noxesium installed. (ProtocolVersion: " + protocolVersion + ")");
            NoxesiumUtils.getNoxesiumPlayers().put(player.getUniqueId(), protocolVersion);

            if (NoxesiumUtils.getPlugin().getConfig().getBoolean("sendDefaultsOnJoin", false)) {
                // Send defaults after a little time, so the client actually registers the packet.
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Create buffers
                        FriendlyByteBuf valuesBuffer = new FriendlyByteBuf(Unpooled.buffer());
                        List<Integer> modifiedRules = new ArrayList<>();

                        if (protocolVersion >= 1) {
                            // Tridents
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.disableAutoSpinAttack")) {
                                modifiedRules.add(0);
                                valuesBuffer.writeInt(0);
                                valuesBuffer.writeBoolean(NoxesiumUtils.getPlugin().getConfig().getBoolean("defaults.disableAutoSpinAttack", false));
                            }
                            // Global Can Place On
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.globalCanPlaceOn")) {
                                var blocks = NoxesiumUtils.getPlugin().getConfig().getStringList("defaults.globalCanPlaceOn");
                                modifiedRules.add(1);
                                valuesBuffer.writeInt(1);
                                valuesBuffer.writeVarInt(blocks.size());
                                for (String string : blocks) {
                                    valuesBuffer.writeUtf(string);
                                }
                            }
                            // Global Can Destroy
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.globalCanDestroy")) {
                                var blocks = NoxesiumUtils.getPlugin().getConfig().getStringList("defaults.globalCanDestroy");
                                modifiedRules.add(2);
                                valuesBuffer.writeInt(2);
                                valuesBuffer.writeVarInt(blocks.size());
                                for (String string : blocks) {
                                    valuesBuffer.writeUtf(string);
                                }
                            }
                        }
                        if (protocolVersion >= 2) {
                            // Held Item Name Offset
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.heldItemNameOffset")) {
                                modifiedRules.add(3);
                                valuesBuffer.writeInt(3);
                                valuesBuffer.writeVarInt(NoxesiumUtils.getPlugin().getConfig().getInt("defaults.heldItemNameOffset", 0));
                            }
                            // Camera Locked
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.cameraLocked")) {
                                modifiedRules.add(4);
                                valuesBuffer.writeInt(4);
                                valuesBuffer.writeBoolean(NoxesiumUtils.getPlugin().getConfig().getBoolean("defaults.cameraLocked", false));
                            }
                        }

                        // Build the final byte buffer
                        FriendlyByteBuf finalBuffer = new FriendlyByteBuf(Unpooled.buffer());
                        finalBuffer.writeVarIntArray(modifiedRules);
                        finalBuffer.writeInt(modifiedRules.size());
                        finalBuffer.writeBytes(valuesBuffer.array());
                        // Send packet
                        player.sendPluginMessage(NoxesiumUtils.getPlugin(), NoxesiumUtils.NOXESIUM_SERVER_RULE_CHANNEL, finalBuffer.array());
                    }
                }.runTaskLater(NoxesiumUtils.getPlugin(), 5);
            }
        }
    }
}
