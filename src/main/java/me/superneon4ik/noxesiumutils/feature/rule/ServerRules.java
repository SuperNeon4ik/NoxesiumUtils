package me.superneon4ik.noxesiumutils.feature.rule;

import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import me.superneon4ik.noxesiumutils.feature.rule.impl.AdventureModeCheckServerRule;
import me.superneon4ik.noxesiumutils.feature.rule.impl.BooleanServerRule;
import me.superneon4ik.noxesiumutils.feature.rule.impl.IntegerServerRule;

import java.util.Arrays;
import java.util.List;

public class ServerRules {
    public static ClientboundServerRule<Boolean> DISABLE_SPIN_ATTACK_COLLISIONS = new BooleanServerRule(ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS, false);
    public static ClientboundServerRule<List<String>> GLOBAL_CAN_PLACE_ON = new AdventureModeCheckServerRule(ServerRuleIndices.GLOBAL_CAN_PLACE_ON);
    public static ClientboundServerRule<List<String>> GLOBAL_CAN_DESTROY = new AdventureModeCheckServerRule(ServerRuleIndices.GLOBAL_CAN_DESTROY);
    public static ClientboundServerRule<Integer> HELD_ITEM_NAME_OFFSET = new IntegerServerRule(ServerRuleIndices.HELD_ITEM_NAME_OFFSET, 0);
    public static ClientboundServerRule<Boolean> CAMERA_LOCKED = new BooleanServerRule(ServerRuleIndices.CAMERA_LOCKED, false);
    public static ClientboundServerRule<Boolean> ENABLE_CUSTOM_MUSIC = new BooleanServerRule(ServerRuleIndices.ENABLE_CUSTOM_MUSIC, false);

    private static final ClientboundServerRule<?>[] SERVER_RULES = new ClientboundServerRule[] {
        DISABLE_SPIN_ATTACK_COLLISIONS,
        GLOBAL_CAN_PLACE_ON,
        GLOBAL_CAN_DESTROY,
        HELD_ITEM_NAME_OFFSET,
        CAMERA_LOCKED,
        ENABLE_CUSTOM_MUSIC
    };

    public static ClientboundServerRule<?> get(int i) {
        return Arrays.stream(SERVER_RULES).filter(x -> x.getIndex() == i).findFirst().orElse(null);
    }
}
