package me.superneon4ik.noxesiumutils.modules;

import io.netty.buffer.Unpooled;
import lombok.Getter;
import me.superneon4ik.noxesiumutils.enums.ServerRule;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class NoxesiumServerRuleBuilder {
    private final FriendlyByteBuf valuesBuffer = new FriendlyByteBuf(Unpooled.buffer());
    private final List<Integer> modifiedRules = new ArrayList<>();
    @Getter private final int protocolVersion;

    public NoxesiumServerRuleBuilder(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public NoxesiumServerRuleBuilder add(int index, boolean value) {
        modifiedRules.add(index);
        if (protocolVersion >= 3) valuesBuffer.writeVarInt(index);
        else if (protocolVersion >= 1) valuesBuffer.writeInt(index);
        valuesBuffer.writeBoolean(value);
        return this;
    }

    public NoxesiumServerRuleBuilder add(ServerRule serverRule, boolean value) {
        int index = serverRule.getIndex();
        modifiedRules.add(index);
        if (protocolVersion >= 3) valuesBuffer.writeVarInt(index);
        else if (protocolVersion >= 1) valuesBuffer.writeInt(index);
        valuesBuffer.writeBoolean(value);
        return this;
    }

    public NoxesiumServerRuleBuilder add(int index, int value) {
        modifiedRules.add(index);
        if (protocolVersion >= 3) valuesBuffer.writeVarInt(index);
        else if (protocolVersion >= 1) valuesBuffer.writeInt(index);
        valuesBuffer.writeVarInt(value);
        return this;
    }

    public NoxesiumServerRuleBuilder add(ServerRule serverRule, int value) {
        int index = serverRule.getIndex();
        modifiedRules.add(index);
        if (protocolVersion >= 3) valuesBuffer.writeVarInt(index);
        else if (protocolVersion >= 1) valuesBuffer.writeInt(index);
        valuesBuffer.writeVarInt(value);
        return this;
    }

    /**
     * Add UTF List packet.
     * @param index Packet index.
     * @param values UTF Strings.
     * @return Builder.
     */
    public NoxesiumServerRuleBuilder add(int index, List<String> values) {
        return add(index, values.toArray(new String[0]));
    }

    /**
     * Add UTF List packet.
     * @param serverRule Server rule.
     * @param values UTF Strings.
     * @return Builder.
     */
    public NoxesiumServerRuleBuilder add(ServerRule serverRule, List<String> values) {
        return add(serverRule.getIndex(), values.toArray(new String[0]));
    }

    /**
     * Add UTF List packet.
     * @param index Packet index.
     * @param values UTF Strings.
     * @return Builder.
     */
    public NoxesiumServerRuleBuilder add(int index, String... values) {
        modifiedRules.add(index);
        if (protocolVersion >= 3) valuesBuffer.writeVarInt(index);
        else if (protocolVersion >= 1) valuesBuffer.writeInt(index);
        valuesBuffer.writeVarInt(values.length);
        for (String string : values) {
            valuesBuffer.writeUtf(string);
        }
        return this;
    }

    /**
     * Add UTF List packet.
     * @param serverRule Server rule.
     * @param values UTF Strings.
     * @return Builder.
     */
    public NoxesiumServerRuleBuilder add(ServerRule serverRule, String... values) {
        int index = serverRule.getIndex();
        modifiedRules.add(index);
        if (protocolVersion >= 3) valuesBuffer.writeVarInt(index);
        else if (protocolVersion >= 1) valuesBuffer.writeInt(index);
        valuesBuffer.writeVarInt(values.length);
        for (String string : values) {
            valuesBuffer.writeUtf(string);
        }
        return this;
    }

    /**
     * Builds the final byte array.
     * @return Packet bytes.
     */
    public byte[] build() {
        FriendlyByteBuf finalBuffer = new FriendlyByteBuf(Unpooled.buffer());
        finalBuffer.writeVarIntArray(modifiedRules);
        if (protocolVersion >= 3) finalBuffer.writeVarInt(modifiedRules.size());
        else if (protocolVersion >= 1) finalBuffer.writeInt(modifiedRules.size());
        finalBuffer.writeBytes(valuesBuffer.array());
        return finalBuffer.array();
    }
}
