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
            FriendlyByteBuf incBuf = new FriendlyByteBuf(Unpooled.copiedBuffer(message));
            int protocolVersion = incBuf.readInt();
            NoxesiumUtils.getPlugin().getLogger().info(player.getName() + " has Noxesium installed. (ProtocolVersion: " + protocolVersion + ")");
            NoxesiumUtils.getNoxesiumPlayers().put(player.getUniqueId(), protocolVersion);

            if (NoxesiumUtils.getPlugin().getConfig().getBoolean("sendDefaultsOnJoin", false)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        FriendlyByteBuf valuesBuffer = new FriendlyByteBuf(Unpooled.buffer());

                        List<Integer> modifiedRules = new ArrayList<>();
                        if (protocolVersion >= 1) {
                            // Tridents
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.disableAutoSpinAttack")) {
                                modifiedRules.add(0);
                                valuesBuffer.writeInt(0);
                                valuesBuffer.writeBoolean(NoxesiumUtils.getPlugin().getConfig().getBoolean("defaults.disableAutoSpinAttack", false));
                            }
                        }

                        FriendlyByteBuf finalBuffer = new FriendlyByteBuf(Unpooled.buffer());
                        finalBuffer.writeVarIntArray(modifiedRules);
                        finalBuffer.writeInt(modifiedRules.size());
                        finalBuffer.writeBytes(valuesBuffer.array());
//                        NoxesiumUtils.getPlugin().getLogger().info(NoxesiumUtils.toHexadecimal(finalBuffer.array()));
                        player.sendPluginMessage(NoxesiumUtils.getPlugin(), NoxesiumUtils.NOXESIUM_SERVER_RULE_CHANNEL, finalBuffer.array());
                    }
                }.runTaskLater(NoxesiumUtils.getPlugin(), 5);
            }
        }
    }
}
