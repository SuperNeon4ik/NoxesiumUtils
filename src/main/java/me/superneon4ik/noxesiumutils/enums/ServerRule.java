package me.superneon4ik.noxesiumutils.enums;

import lombok.Getter;

public enum ServerRule {
    DISABLE_AUTO_SPIN_ATTACK(0),
    GLOBAL_CAN_PLACE_ON(1),
    GLOBAL_CAN_DESTROY(2),
    HELD_ITEM_NAME_OFFSET(3),
    CAMERA_LOCK(4);

    @Getter private final int index;
    ServerRule(int index) {
        this.index = index;
    }
}
