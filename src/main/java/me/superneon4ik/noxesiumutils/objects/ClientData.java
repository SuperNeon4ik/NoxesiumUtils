package me.superneon4ik.noxesiumutils.objects;

import com.noxcrew.noxesium.api.protocol.ClientSettings;
import me.superneon4ik.noxesiumutils.feature.rule.ClientboundServerRule;

import java.util.HashMap;
import java.util.Map;

public class ClientData {

    public final int protocolVersion;
    public final Map<Integer, ClientboundServerRule<?>> serverRuleMap = new HashMap<>();
    public ClientSettings clientSettings = null;

    public ClientData(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}
