package com.ovrtechnology.aromalib.mixin;

import com.ovrtechnology.aromalib.trigger.event.FurnaceLitAccess;
import com.ovrtechnology.aromalib.trigger.event.ServerEventBusHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Detects a furnace / blast furnace / smoker igniting (transition to lit) so the event-trigger
 * system can fire COOKING_STARTED (→ Smoky) at the start of the cooking process — only when the
 * furnace actually starts burning, which sidesteps the "item inserted but no fuel" false
 * positive.
 *
 * <p>The per-tick cost is a single block-state read plus a boolean compare; the nearest-player
 * lookup and dispatch only run on the rare ignition tick. Firing on the burning edge (rather than
 * on take) means one puff per session, gated further by the event cooldown. Cross-platform
 * (vanilla target); {@code require = 0} so a mapping mismatch degrades to a no-op instead of a
 * crash.</p>
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceIgniteServerMixin implements FurnaceLitAccess {

    @Unique private boolean aromalib$wasLit = false;

    @Override
    public boolean aromalib$wasLit() {
        return aromalib$wasLit;
    }

    @Override
    public void aromalib$setWasLit(boolean wasLit) {
        this.aromalib$wasLit = wasLit;
    }

    @Inject(method = "serverTick", at = @At("TAIL"), require = 0)
    private static void aromalib$onServerTick(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractFurnaceBlockEntity blockEntity,
            CallbackInfo ci) {
        if (level == null) return;

        boolean lit =
                state.hasProperty(BlockStateProperties.LIT)
                        && state.getValue(BlockStateProperties.LIT);
        FurnaceLitAccess tracker = (FurnaceLitAccess) blockEntity;
        if (lit && !tracker.aromalib$wasLit()) {
            ServerEventBusHandler.onFurnaceIgnited(level, pos);
        }
        tracker.aromalib$setWasLit(lit);
    }
}
