package me.superneon4ik.noxesiumutils.listeners;

import io.netty.buffer.Unpooled;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;
import me.superneon4ik.noxesiumutils.modules.NoxesiumServerRuleBuilder;
import me.superneon4ik.noxesiumutils.objects.PlayerClientSettings;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class NoxesiumMessageListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte @NotNull [] message) {
        if (channel.equals(NoxesiumUtils.NOXESIUM_LEGACY_CLIENT_INFORMATION_CHANNEL) || channel.equals(NoxesiumUtils.NOXESIUM_V1_CLIENT_INFORMATION_CHANNEL)) {
            // Get player's noxesium protocol version.
            FriendlyByteBuf incBuf = new FriendlyByteBuf(Unpooled.copiedBuffer(message));

            int protocolVersion;
            if (channel.equals(NoxesiumUtils.NOXESIUM_LEGACY_CLIENT_INFORMATION_CHANNEL)) protocolVersion = incBuf.readInt();
            else protocolVersion = incBuf.readVarInt();

            NoxesiumUtils.getPlugin().getLogger().info(player.getName() + " has Noxesium installed. (ProtocolVersion: " + protocolVersion + ")");
            NoxesiumUtils.getNoxesiumPlayers().put(player.getUniqueId(), protocolVersion);

            if (NoxesiumUtils.getPlugin().getConfig().getBoolean("sendDefaultsOnJoin", false)) {
                // Send defaults after a little time, so the client actually registers the packet.
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Create builder
                        var builder = new NoxesiumServerRuleBuilder(protocolVersion);

                        if (protocolVersion >= 1) {
                            // Tridents
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.disableAutoSpinAttack")) {
                                builder.add(0, NoxesiumUtils.getPlugin().getConfig().getBoolean("defaults.disableAutoSpinAttack", false));
                            }
                            // Global Can Place On
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.globalCanPlaceOn")) {
                                var blocks = NoxesiumUtils.getPlugin().getConfig().getStringList("defaults.globalCanPlaceOn");
                                builder.add(1, blocks);
                            }
                            // Global Can Destroy
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.globalCanDestroy")) {
                                var blocks = NoxesiumUtils.getPlugin().getConfig().getStringList("defaults.globalCanDestroy");
                                builder.add(2, blocks);
                            }
                        }
                        if (protocolVersion >= 2) {
                            // Held Item Name Offset
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.heldItemNameOffset")) {
                                builder.add(3, NoxesiumUtils.getPlugin().getConfig().getInt("defaults.heldItemNameOffset", 0));
                            }
                            // Camera Locked
                            if (NoxesiumUtils.getPlugin().getConfig().contains("defaults.cameraLocked")) {
                                builder.add(4, NoxesiumUtils.getPlugin().getConfig().getBoolean("defaults.cameraLocked", false));
                            }
                        }

                        // Send packet
                        NoxesiumUtils.sendServerRulesPacket(player, builder.build());
                    }
                }.runTaskLater(NoxesiumUtils.getPlugin(), 5);
            }
        }
        else if (channel.equals(NoxesiumUtils.NOXESIUM_LEGACY_CLIENT_SETTINGS_CHANNEL) || channel.equals(NoxesiumUtils.NOXESIUM_V1_CLIENT_SETTINGS_CHANNEL)) {
            FriendlyByteBuf incBuf = new FriendlyByteBuf(Unpooled.copiedBuffer(message));
            int protocolVersion = NoxesiumUtils.getPlayerProtocolVersion(player.getUniqueId());

            if (protocolVersion >= 3) {
                int guiScale = incBuf.readVarInt();
                double internalGuiScale = incBuf.readDouble();
                int scaledWidth = incBuf.readVarInt();
                int scaledHeight = incBuf.readVarInt();
                boolean enforceUnicode = incBuf.readBoolean();
                boolean touchscreenMode = incBuf.readBoolean();
                double notificationDisplayTime = incBuf.readDouble();

                var playerClientSettings = new PlayerClientSettings(
                        guiScale, internalGuiScale, scaledWidth, scaledHeight, enforceUnicode, touchscreenMode, notificationDisplayTime
                );
                NoxesiumUtils.getNoxesiumClientSettings().put(player.getUniqueId(), playerClientSettings);
            }
            else if (protocolVersion >= 1) {
                int guiScale = incBuf.readVarInt();
                boolean enforceUnicode = incBuf.readBoolean();
                var playerClientSettings = new PlayerClientSettings(
                        guiScale, null, null, null, enforceUnicode, null, null
                );
                NoxesiumUtils.getNoxesiumClientSettings().put(player.getUniqueId(), playerClientSettings);
            }
        }
    }
}
