package com.ovrtechnology.aromalib.network;

import com.ovrtechnology.aromalib.command.path.ActivePathManager;
import com.ovrtechnology.aromalib.command.sub.PathSubCommand;
import com.ovrtechnology.aromalib.compat.Accessory;
import com.ovrtechnology.aromalib.tracker.TrackingItemHelper;
import com.ovrtechnology.aromalib.tracking.*;
import com.ovrtechnology.aromalib.util.Ids;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.event.callback.ServerPlayerCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;

/** Requests contain no destination: the server always resolves the player's current respawn point. */
public final class RespawnTrackingNetworking {
    private static final TrackingRequestLimiter lastTrack = new TrackingRequestLimiter(1_000_000_000L);
    private static final TrackingRequestLimiter lastRefresh = new TrackingRequestLimiter(1_000_000_000L);

    private RespawnTrackingNetworking() {}

    public record RequestC2S(boolean track) implements CustomPacketPayload {
        public static final Type<RequestC2S> TYPE = new Type<>(Ids.mod("respawn_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestC2S> CODEC = StreamCodec.of(
                (buf, p) -> buf.writeBoolean(p.track()), buf -> new RequestC2S(buf.readBoolean()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record StartedS2C() implements CustomPacketPayload {
        public static final Type<StartedS2C> TYPE = new Type<>(Ids.mod( "respawn_tracking_started"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StartedS2C> CODEC = StreamCodec.unit(new StartedS2C());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record StopC2S() implements CustomPacketPayload {
        public static final Type<StopC2S> TYPE = new Type<>(Ids.mod( "stop_tracking"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StopC2S> CODEC = StreamCodec.unit(new StopC2S());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void init() {
        Balm.networking().registerServerboundPacket(RequestC2S.TYPE, RequestC2S.class, RequestC2S.CODEC,
                (player, request) -> {
                    if (request.track()) track(player);
                    else if (allow(lastRefresh, player)) RespawnSyncHandler.sync(player);
                });
        Balm.networking().registerServerboundPacket(StopC2S.TYPE, StopC2S.class, StopC2S.CODEC,
                (player, request) -> {
                    PathSubCommand.cancelSearch(player);
                    ActivePathManager.getInstance().removePath(player.getUUID());
                });
        ServerPlayerCallback.Leave.EVENT.register(player -> {
            lastTrack.remove(player.getUUID());
            lastRefresh.remove(player.getUUID());
        });
    }

    public static void request(boolean track) { Balm.networking().sendToServer(new RequestC2S(track)); }
    public static void stop() { Balm.networking().sendToServer(new StopC2S()); }

    private static boolean allow(TrackingRequestLimiter requests, ServerPlayer player) {
        return requests.allow(player.getUUID(), System.nanoTime());
    }

    public static void track(ServerPlayer player) {
        if (!allow(lastTrack, player)) return;
        PathSubCommand.cancelSearch(player);
        if (!player.isAlive() || player.isSpectator() || !TrackingItemHelper.canTrackRespawn(player)) {
            reject(player, "tier_required");
            return;
        }
        var config = player.getRespawnConfig();
        if (config == null || config.respawnData() == null) {
            PathScentNetworking.sendRespawnCleared(player);
            reject(player, "missing");
            return;
        }
        var data = config.respawnData();
        if (!player.level().dimension().equals(data.dimension())) {
            reject(player, "other_dimension");
            return;
        }
        var level = player.level();
        var state = level.getBlockState(data.pos());
        if (!(state.getBlock() instanceof BedBlock)
                && !(state.getBlock() instanceof RespawnAnchorBlock
                     && state.getValue(RespawnAnchorBlock.CHARGE) > 0)) {
            PathScentNetworking.sendRespawnCleared(player);
            reject(player, "missing");
            return;
        }
        var nose = Accessory.getEquipped(player);
        int cost = TrackingConfig.getInstance().getHistoryRetrackCost();
        if (nose.isDamageableItem() && nose.getMaxDamage() - nose.getDamageValue() < cost) {
            reject(player, "durability");
            return;
        }
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        nose.hurtAndBreak(cost, player, EquipmentSlot.HEAD);
        ActivePathManager.getInstance().createPath(player, level, data.pos(), ActivePathManager.TargetType.BLOCK, blockId);
        PathScentNetworking.sendRespawnSync(player, data.dimension().identifier().toString(), data.pos(), blockId);
        Balm.networking().sendTo(player, new StartedS2C());
        int distance = (int) Math.hypot(player.getX() - data.pos().getX(), player.getZ() - data.pos().getZ());
        PathScentNetworking.sendPathFound(player, distance, data.pos());
    }

    private static void reject(ServerPlayer player, String reason) {
        Component message = Component.translatable("message.aromaaffect.respawn." + reason);
        player.sendSystemMessage(message, true);
    }
}
