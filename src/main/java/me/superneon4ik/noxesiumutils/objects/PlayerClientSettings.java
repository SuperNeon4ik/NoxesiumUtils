package me.superneon4ik.noxesiumutils.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public class PlayerClientSettings {
    @NotNull Integer guiScale;
    @Nullable Double internalGuiScale;
    @Nullable Integer scaledWidth;
    @Nullable Integer scaledHeight;
    @NotNull Boolean enforceUnicode;
    @Nullable Boolean touchscreenMode;
    @Nullable Double notificationDisplayTime;

    @Override
    public String toString() {
        return "PlayerClientSettings{" +
                "guiScale=" + guiScale +
                ", internalGuiScale=" + internalGuiScale +
                ", scaledWidth=" + scaledWidth +
                ", scaledHeight=" + scaledHeight +
                ", enforceUnicode=" + enforceUnicode +
                ", touchscreenMode=" + touchscreenMode +
                ", notificationDisplayTime=" + notificationDisplayTime +
                '}';
    }
}
