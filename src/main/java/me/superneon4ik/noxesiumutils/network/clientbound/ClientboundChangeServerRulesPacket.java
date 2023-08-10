package me.superneon4ik.noxesiumutils.network.clientbound;

import com.noxcrew.noxesium.api.protocol.rule.ServerRule;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

import java.util.List;

public class ClientboundChangeServerRulesPacket<T> extends ClientboundNoxesiumPacket {

    private final List<ServerRule<T, FriendlyByteBuf>> serverRules;
    public ClientboundChangeServerRulesPacket(List<ServerRule<T, FriendlyByteBuf>> serverRules) {
        super(NoxesiumUtils.NOXESIUM_V1_CHANGE_SERVER_RULES_CHANNEL, NoxesiumUtils.NOXESIUM_LEGACY_SERVER_RULE_CHANNEL, 1);
        this.serverRules = serverRules;
    }

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeVarInt(serverRules.size());
        for (var serverRule : serverRules) {
            buffer.writeVarInt(serverRule.getIndex());
            serverRule.write(serverRule.getValue(), buffer);
        }
    }

    @Override
    public void legacySerialize(FriendlyByteBuf buffer) {
        buffer.writeVarIntArray(serverRules.stream().map(ServerRule::getIndex).toList());
        buffer.writeInt(serverRules.size());
        for (var serverRule : serverRules) {
            buffer.writeInt(serverRule.getIndex());
            serverRule.write(serverRule.getValue(), buffer);
        }
    }
}
