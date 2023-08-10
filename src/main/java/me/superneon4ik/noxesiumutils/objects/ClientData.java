package me.superneon4ik.noxesiumutils.objects;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import com.noxcrew.noxesium.api.protocol.rule.ServerRule;
import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;
import me.superneon4ik.noxesiumutils.feature.rule.ServerRules;
import me.superneon4ik.noxesiumutils.modules.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class ClientData {

    public final int protocolVersion;
    public final Map<Integer, ServerRule<?, FriendlyByteBuf>> serverRuleMap = new HashMap<>();
    public ClientSettings clientSettings = null;

    public ClientData(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}
