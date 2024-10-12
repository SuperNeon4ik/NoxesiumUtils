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
    private Boolean cameraLocked = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_DEFERRED_CHUNK_UPDATES) @Nullable 
    private Boolean disableDeferredChunkUpdates = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_MAP_UI) @Nullable 
    private Boolean disableMapUi = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_BOAT_COLLISIONS) @Nullable 
    private Boolean disableBoatCollisions = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_SPIN_ATTACK_COLLISIONS) @Nullable 
    private Boolean disableSpinAttackCollisions = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_UI_OPTIMIZATIONS) @Nullable 
    private Boolean disableUiOptimizations = null;
    
    @RuleIndex(index = ServerRuleIndices.DISABLE_VANILLA_MUSIC) @Nullable 
    private Boolean disableVanillaMusic = null;
    
    @RuleIndex(index = ServerRuleIndices.ENABLE_SMOOTHER_CLIENT_TRIDENT) @Nullable 
    private Boolean enableSmootherClientTrident = null;
    
    @RuleIndex(index = ServerRuleIndices.HELD_ITEM_NAME_OFFSET) @Nullable 
    private Integer heldItemNameOffset = null;
    
    @RuleIndex(index = ServerRuleIndices.HAND_ITEM_OVERRIDE) @Nullable 
    private ItemStack handItemOverride = null;
    
    @RuleIndex(index = ServerRuleIndices.OVERRIDE_GRAPHICS_MODE) @Nullable 
    private GraphicsType overrideGraphicsMode = null; // should be Optional<GraphicsType> when sending
    
    @RuleIndex(index = ServerRuleIndices.RIPTIDE_COYOTE_TIME) @Nullable 
    private Integer riptideCoyoteTime = null;
    
    @RuleIndex(index = ServerRuleIndices.SHOW_MAP_IN_UI) @Nullable 
    private Boolean showMapInUi = null;
    
    @RuleIndex(index = ServerRuleIndices.CUSTOM_CREATIVE_ITEMS) @Nullable 
    private Boolean customCreativeItems = null; // should be List<ItemStack> when sending
    
    @RuleIndex(index = ServerRuleIndices.QIB_BEHAVIORS) @Nullable 
    private List<String> qibBehaviors = null; // should be Map<String, QibBehavior> when sending
}
