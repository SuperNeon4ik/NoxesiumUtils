package me.superneon4ik.noxesiumutils.network.clientbound;

import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

import java.util.List;

public class ClientboundChangeServerRulesPacket extends ClientboundNoxesiumPacket {

    private final List<ClientboundServerRule<?>> serverRules;
    public ClientboundChangeServerRulesPacket(List<ClientboundServerRule<?>> serverRules) {
        super(NoxesiumUtils.NOXESIUM_V1_CHANGE_SERVER_RULES_CHANNEL, 1);
        this.serverRules = serverRules;
    }

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeVarInt(serverRules.size());
        for (ClientboundServerRule<?> serverRule : serverRules) {
            buffer.writeVarInt(serverRule.getIndex());
            serverRule.write(buffer);
        }
    }
}
