package me.superneon4ik.noxesiumutils.modules;

import lombok.Getter;
import me.superneon4ik.noxesiumutils.NoxesiumUtils;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.ScoreComponent;

import java.util.UUID;

@Getter
public class NoxesiumPlayerSkullBuilder {
    private UUID uuid;
    private boolean grayscale = false;
    private int advance = 0;
    private int ascent = 0;
    private float scale = 1.0f;

    public NoxesiumPlayerSkullBuilder(UUID uuid) {
        this.uuid = uuid;
    }

    public NoxesiumPlayerSkullBuilder setUniqueID(UUID value) {
        uuid = value;
        return this;
    }

    public NoxesiumPlayerSkullBuilder setGrayscale(boolean value) {
        grayscale = value;
        return this;
    }

    public NoxesiumPlayerSkullBuilder setAdvance(int value) {
        advance = value;
        return this;
    }

    public NoxesiumPlayerSkullBuilder setAscent(int value) {
        ascent = value;
        return this;
    }

    public NoxesiumPlayerSkullBuilder setScale(float value) {
        scale = value;
        return this;
    }

    public String buildString() {
        var args = new String[] {
                uuid.toString(),
                String.valueOf(grayscale),
                String.valueOf(advance),
                String.valueOf(ascent),
                String.valueOf(scale)
        };
        return "%NCPH%" + String.join(",", args);
    }

    public ScoreComponent buildSpigot() {
        return new ScoreComponent(buildString(), "");
    }

    public Component build() {
        return Component.score(buildString(), "");
    }
}
