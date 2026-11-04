package com.ovrtechnology.aromalib.mixin;

import com.ovrtechnology.aromalib.trigger.event.ServerEventBusHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ChangeOverTimeBlock.class)
public interface CopperWeatherMixin {

    @Inject(method = "getNextState", at = @At("HEAD"))
    private void aromalib$onGetNextState(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfoReturnable<Optional<BlockState>> cir) {
        if (this instanceof WeatheringCopper) {
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                if (player.blockPosition().distSqr(pos) <= 15.0) {
                    ServerEventBusHandler.onCopperOxidize(player);
                }
            }
        }
    }
}
