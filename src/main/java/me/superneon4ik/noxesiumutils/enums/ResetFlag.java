package me.superneon4ik.noxesiumutils.enums;

import lombok.Getter;

@Getter
public enum ResetFlag {
    ALL_SERVER_RULES((byte) 0x01),
    CACHED_PLAYER_HEADS((byte) 0x02);

    private final byte flag;
    ResetFlag(byte flag) {
        this.flag = flag;
    }
}
