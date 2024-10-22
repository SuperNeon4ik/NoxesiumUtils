package me.superneon4ik.noxesiumutils.config;

import com.noxcrew.noxesium.api.protocol.rule.ServerRuleIndices;
import com.noxcrew.noxesium.paper.api.rule.GraphicsType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
public class ServerRuleDefaults {
    @RuleIndex(index = ServerRuleIndices.CAMERA_LOCKED) @Nullable 
    public Boolean cameraLocked = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_DEFERRED_CHUNK_UPDATES) @Nullable
    public Boolean disableDeferredChunkUpdates = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_MAP_UI) @Nullable
    public Boolean disableMapUi = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_BOAT_COLLISIONS) @Nullable
    public Boolean disableBoatCollisions = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS) @Nullable
    public Boolean disableSpinAttackCollisions = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_UI_OPTIMIZATIONS) @Nullable
    public Boolean disableUiOptimizations = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_VANILLA_MUSIC) @Nullable
    public Boolean disableVanillaMusic = null;
    
    @RuleIndex(index = ServerRuleIndices.ENABLE_SMOOTHER_CLIENT_TRIDENT) @Nullable
    public Boolean enableSmootherClientTrident = null;
    
    @RuleIndex(index = ServerRuleIndices.HELD_ITEM_NAME_OFFSET) @Nullable
    public Integer heldItemNameOffset = null;
    
    @RuleIndex(index = ServerRuleIndices.HAND_ITEM_OVERRIDE) @Nullable
    public ItemStack handItemOverride = null;
    
    @RuleIndex(index = ServerRuleIndices.OVERRIDE_GRAPHICS_MODE) @Nullable
    public GraphicsType overrideGraphicsMode = null; // should be Optional<GraphicsType> when sending
    
    @RuleIndex(index = ServerRuleIndices.RIPTIDE_COYOTE_TIME) @Nullable
    public Integer riptideCoyoteTime = null;
    
    @RuleIndex(index = ServerRuleIndices.SHOW_MAP_IN_UI) @Nullable
    public Boolean showMapInUi = null;
    
    @RuleIndex(index = ServerRuleIndices.CUSTOM_CREATIVE_ITEMS) @Nullable
    public Boolean customCreativeItems = null; // should be List<ItemStack> when sending
    
    @RuleIndex(index = ServerRuleIndices.QIB_BEHAVIORS) @Nullable
    public List<String> qibBehaviors = null; // should be Map<String, QibBehavior> when sending

    @Override
    public String toString() {
        return "ServerRuleDefaults{" +
                "cameraLocked=" + cameraLocked +
                ", disableDeferredChunkUpdates=" + disableDeferredChunkUpdates +
                ", disableMapUi=" + disableMapUi +
                ", disableBoatCollisions=" + disableBoatCollisions +
                ", disableSpinAttackCollisions=" + disableSpinAttackCollisions +
                ", disableUiOptimizations=" + disableUiOptimizations +
                ", disableVanillaMusic=" + disableVanillaMusic +
                ", enableSmootherClientTrident=" + enableSmootherClientTrident +
                ", heldItemNameOffset=" + heldItemNameOffset +
                ", handItemOverride=" + handItemOverride +
                ", overrideGraphicsMode=" + overrideGraphicsMode +
                ", riptideCoyoteTime=" + riptideCoyoteTime +
                ", showMapInUi=" + showMapInUi +
                ", customCreativeItems=" + customCreativeItems +
                ", qibBehaviors=" + qibBehaviors +
                '}';
    }
}
