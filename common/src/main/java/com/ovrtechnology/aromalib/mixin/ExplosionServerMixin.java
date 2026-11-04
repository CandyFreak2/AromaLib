package com.ovrtechnology.aromalib.mixin;

import com.ovrtechnology.aromalib.trigger.event.ServerEventBusHandler;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Detects every explosion in the world: TNT, creepers, beds, end crystals, fireballs and the rest. */
@Mixin(ServerExplosion.class)
public abstract class ExplosionServerMixin {
    @Inject(method = "explode", at = @At("HEAD"), require = 0)
    private void aromalib$onExplode(CallbackInfoReturnable<Integer> cir) {
        ServerEventBusHandler.onExplosion((ServerExplosion) (Object) this);
    }
}
