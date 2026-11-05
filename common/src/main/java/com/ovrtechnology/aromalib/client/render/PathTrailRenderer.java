package com.ovrtechnology.aromalib.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.client.config.AromaConfig;
import com.ovrtechnology.aromalib.registry.BlockRegistry;
import com.ovrtechnology.aromalib.registry.ScentRegistry;
import com.ovrtechnology.aromalib.item.tracker.TrackingItemHelper;
import com.ovrtechnology.aromalib.item.tracker.ITrackableItem;
import com.ovrtechnology.aromalib.tracking.ActiveTrackingState;
import com.ovrtechnology.aromalib.tracking.TrackingCategory;
import com.ovrtechnology.aromalib.tracking.TrailDomain;
import com.ovrtechnology.aromalib.trigger.ScentPriority;
import com.ovrtechnology.aromalib.trigger.ScentTrigger;
import com.ovrtechnology.aromalib.trigger.ScentTriggerManager;
import com.ovrtechnology.aromalib.trigger.ScentTriggerSource;
import com.ovrtechnology.aromalib.menu.screen.PathTrackingMaskOverlay;
import com.ovrtechnology.aromalib.definition.trigger.ScentTriggerConfigLoader;
import com.ovrtechnology.aromalib.definition.trigger.TriggerSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class PathTrailRenderer {
    private static final double SAMPLE_SPACING = 1.5;
    private static final double HEIGHT_OFFSET = 0.6;
    private static final double MAX_RENDER_DISTANCE = 120.0;
    private static final double MAX_Y_STEP = 1.5;
    private static final long INTRO_GUIDE_MS = 6000;
    private static final long INTRO_FADE_MS = 2000;
    private static final double INTRO_CONTROL_DISTANCE = 5.0;
    private static final double INTRO_JOIN_DISTANCE = 12.0;
    private static final float INTRO_STRENGTH_TAU = 0.35f;
    private static final float INTRO_AIM_TAU = 0.30f;
    private static final double INTRO_AIM_PITCH_DAMP = 0.35;
    private static final double INTRO_FACING_DOT = 0.65;
    private static final long INTRO_FACING_RELEASE_MS = 1200;
    private static final float INTRO_GLOW_ALPHA = 0.22f;
    private static final float INTRO_CORE_ALPHA = 0.9f;
    private static final double INTRO_LEAD_FADE_IN = 0.18;
    private static final double INTRO_LEAD_FADE_OUT = 0.4;

    private static final double WAVE1_AMPLITUDE = 0.5;
    private static final double WAVE1_FREQUENCY = 0.12;
    private static final double WAVE2_AMPLITUDE = 0.15;
    private static final double WAVE2_FREQUENCY = 0.324;
    private static final double WAVE2_PHASE_OFFSET = 1.3;

    private static final long PULSE_DURATION_NEAR_MS = 700;
    private static final long PULSE_DURATION_FAR_MS = 2500;
    private static final long PULSE_CYCLE_NEAR_MS = 1700;
    private static final long PULSE_CYCLE_FAR_MS = 4000;
    private static final long BREATHE_PERIOD_MS = 3000;

    private static final long WAKE_VISIBLE_MS = 2500;
    private static final long WAKE_FADE_MS = 1000;

    private static final double LEADING_EDGE_WIDTH = 6.0;
    private static final double WAKE_GLOW_WIDTH = 20.0;

    private static final int POWER_PULSE_INTERVAL = 5;

    private static final double POWER_LEADING_EDGE_WIDTH = 10.0;
    private static final double POWER_WAKE_GLOW_WIDTH = 35.0;
    private static final long PARTICLE_SPAWN_INTERVAL_MS = 250;
    private static final RenderPipeline TRAIL_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(AromaLib.MOD_ID, "pipeline/trail_no_depth"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withCull(false)
            .build();
    private static final float TRAIL_GLOW_WIDTH = 5.0f;
    private static final float TRAIL_CORE_WIDTH = 1.5f;
    private static final float TRAIL_GLOW_POWER_WIDTH = 8.0f;
    private static final float TRAIL_CORE_POWER_WIDTH = 2.5f;
    private static final RenderType TRAIL_GLOW = TrailRenderType.create("trail_glow");
    private static final RenderType TRAIL_CORE = TrailRenderType.create("trail_core");
    private static final RenderType TRAIL_GLOW_POWER = TrailRenderType.create("trail_glow_power");
    private static final RenderType TRAIL_CORE_POWER = TrailRenderType.create("trail_core_power");

    private static final int PLAYER_BLEND_POINTS = 10;
    private static List<Vec3> stablePath = new ArrayList<>();
    private static List<Vec3> incomingPath = null;
    private static List<Vec3> prevStablePath = null;
    private static BlockPos cachedDest = null;
    private static long lastPulseStart = 0;
    private static long prevPulseStart = 0;
    private static long currentPulseDurationMs = PULSE_DURATION_FAR_MS;
    private static long prevPulseDurationMs = PULSE_DURATION_FAR_MS;
    private static long introGuideStart = 0;
    private static Vec3 introAimDir = null;
    private static float introStrength = 0f;
    private static long introFacingSince = 0;
    private static long introLastUpdate = 0;
    private static Vec3 stablePathOrigin = null;
    private static Vec3 incomingPathOrigin = null;
    private static Vec3 prevPathOrigin = null;
    private static int pulseCount = 0;
    private static boolean currentPulseIsPower = false;
    private static boolean prevPulseIsPower = false;
    private static long lastParticleSpawnTime = 0;

    private PathTrailRenderer() {
    }

    private static Vec3 getPlayerTrailAnchor(LocalPlayer player) {
        var stack = TrackingItemHelper.getEquippedItem(player);
        float offset = stack.map(ITrackableItem::getOffset).orElse(0.0f);
        return player.getEyePosition().add(0.0, offset, 0.0);
    }

    public static void renderTrail(PoseStack poseStack, Vec3 cameraPos, OrderedSubmitNodeCollector collector) {
        if (!ActiveTrackingState.isActivelyTracking()) {
            if (cachedDest != null) clearCache();
            return;
        }

        BlockPos dest = ActiveTrackingState.getDestination();
        if (dest == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;

        Vec3 playerPos = getPlayerTrailAnchor(mc.player);
        long now = System.currentTimeMillis();

        if (!dest.equals(cachedDest)) {
            beginIntroGuide(now);
            stablePath = computeTrailPoints(playerPos, dest, mc.level);
            stablePathOrigin = playerPos;
            cachedDest = dest;
            incomingPath = new ArrayList<>(stablePath);
            incomingPathOrigin = playerPos;
            prevStablePath = null;
            prevPathOrigin = null;
            prevPulseStart = 0;
            prevPulseDurationMs = PULSE_DURATION_FAR_MS;
            lastPulseStart = now;
            currentPulseDurationMs = resolvePulseDurationMs(totalLength(stablePath));
            pulseCount = POWER_PULSE_INTERVAL;
            currentPulseIsPower = true;
            prevPulseIsPower = false;
        }

        long elapsed = now - lastPulseStart;
        boolean pulseActive = incomingPath != null && elapsed < currentPulseDurationMs;

        if (!pulseActive && incomingPath != null) {
            if (currentPulseIsPower) {
                firePowerPulsePuff();
            }
            stablePath = incomingPath;
            stablePathOrigin = incomingPathOrigin;
            incomingPath = null;
        }

        long cycleMs = resolvePulseCycleMs(currentPulseDurationMs);
        if (incomingPath == null && elapsed >= cycleMs) {
            prevStablePath = stablePath;
            prevPathOrigin = stablePathOrigin;
            incomingPath = computeTrailPoints(playerPos, dest, mc.level);
            incomingPathOrigin = playerPos;
            prevPulseStart = lastPulseStart;
            prevPulseDurationMs = currentPulseDurationMs;
            prevPulseIsPower = currentPulseIsPower;
            lastPulseStart = now;
            currentPulseDurationMs = resolvePulseDurationMs(totalLength(incomingPath));
            pulseCount++;
            currentPulseIsPower = (pulseCount % POWER_PULSE_INTERVAL == 0);
            elapsed = 0;
            pulseActive = true;
        }

        boolean useIncoming = pulseActive && incomingPath != null && !incomingPath.isEmpty();
        List<Vec3> basePath = useIncoming ? incomingPath : stablePath;
        Vec3 baseOrigin = useIncoming ? incomingPathOrigin : stablePathOrigin;
        if (basePath.size() < 2) return;

        updateIntroGuide(mc.player, playerPos, dest, now);
        List<Vec3> renderPoints = withIntroCurve(
                adjustedForPlayer(basePath, baseOrigin, playerPos), playerPos);

        float[] color = resolveColor();
        float r = color[0], g = color[1], b = color[2];

        double totalLen = totalLength(renderPoints);
        float breathe = 0.85f + 0.15f * (float) Math.sin(
                (now % BREATHE_PERIOD_MS) / (double) BREATHE_PERIOD_MS * Math.PI * 2.0);

        if (prevStablePath != null && prevStablePath.size() >= 2 && prevPulseStart > 0) {
            List<Vec3> adjustedPrev = withIntroCurve(
                    adjustedForPlayer(prevStablePath, prevPathOrigin, playerPos), playerPos);
            double prevTotalLen = totalLength(adjustedPrev);
            long lastPointReach = prevPulseStart + prevPulseDurationMs;
            float prevFade = 1.0f - Math.clamp((float) elapsed / currentPulseDurationMs, 0.0f, 1.0f);
            if (prevFade > 0.0f && now - lastPointReach < WAKE_VISIBLE_MS + WAKE_FADE_MS) {
                RenderType prevGlow = prevPulseIsPower ? TRAIL_GLOW_POWER : TRAIL_GLOW;
                RenderType prevCore = prevPulseIsPower ? TRAIL_CORE_POWER : TRAIL_CORE;
                float prevGlowWidth = prevPulseIsPower ? TRAIL_GLOW_POWER_WIDTH : TRAIL_GLOW_WIDTH;
                float prevCoreWidth = prevPulseIsPower ? TRAIL_CORE_POWER_WIDTH : TRAIL_CORE_WIDTH;
                float prevGlowAlpha = (prevPulseIsPower ? 0.20f : 0.12f) * prevFade;
                float prevCoreAlpha = (prevPulseIsPower ? 0.75f : 0.55f) * prevFade;
                collector.submitCustomGeometry(poseStack, prevGlow, (pose, consumer) ->
                        renderLayer(pose, cameraPos, consumer, prevGlowWidth,
                                adjustedPrev, r, g, b, prevGlowAlpha * breathe,
                                now, prevPulseStart, 0, prevPulseDurationMs, prevTotalLen, true, prevPulseIsPower));
                collector.submitCustomGeometry(poseStack, prevCore, (pose, consumer) ->
                        renderLayer(pose, cameraPos, consumer, prevCoreWidth,
                                adjustedPrev, r, g, b, prevCoreAlpha * breathe,
                                now, prevPulseStart, 0, prevPulseDurationMs, prevTotalLen, false, prevPulseIsPower));
            } else {
                prevStablePath = null;
                prevPathOrigin = null;
            }
        }

        RenderType curGlow = currentPulseIsPower ? TRAIL_GLOW_POWER : TRAIL_GLOW;
        RenderType curCore = currentPulseIsPower ? TRAIL_CORE_POWER : TRAIL_CORE;
        float curGlowWidth = currentPulseIsPower ? TRAIL_GLOW_POWER_WIDTH : TRAIL_GLOW_WIDTH;
        float curCoreWidth = currentPulseIsPower ? TRAIL_CORE_POWER_WIDTH : TRAIL_CORE_WIDTH;
        float curGlowAlpha = currentPulseIsPower ? 0.20f : 0.12f;
        float curCoreAlpha = currentPulseIsPower ? 0.75f : 0.55f;
        collector.submitCustomGeometry(poseStack, curGlow, (pose, consumer) ->
                renderLayer(pose, cameraPos, consumer, curGlowWidth,
                        renderPoints, r, g, b, curGlowAlpha * breathe,
                        now, lastPulseStart, 0, currentPulseDurationMs, totalLen, true, currentPulseIsPower));
        collector.submitCustomGeometry(poseStack, curCore, (pose, consumer) ->
                renderLayer(pose, cameraPos, consumer, curCoreWidth,
                        renderPoints, r, g, b, curCoreAlpha * breathe,
                        now, lastPulseStart, 0, currentPulseDurationMs, totalLen, false, currentPulseIsPower));


        if (introStrength > 0.004f && renderPoints.size() >= 4) {
            int leadEnd = introJoinIndex(renderPoints.size());
            float leadFade = introStrength * breathe;
            collector.submitCustomGeometry(poseStack, TRAIL_GLOW, (pose, consumer) ->
                    renderIntroLead(pose, cameraPos, consumer, TRAIL_GLOW_WIDTH,
                            renderPoints, leadEnd, r, g, b, INTRO_GLOW_ALPHA * leadFade));
            collector.submitCustomGeometry(poseStack, TRAIL_CORE, (pose, consumer) ->
                    renderIntroLead(pose, cameraPos, consumer, TRAIL_CORE_WIDTH,
                            renderPoints, leadEnd, r, g, b, INTRO_CORE_ALPHA * leadFade));
        }

        if (now - lastParticleSpawnTime >= PARTICLE_SPAWN_INTERVAL_MS) {
            lastParticleSpawnTime = now;
            spawnPulseParticles(mc.level, renderPoints, totalLen, now, lastPulseStart, currentPulseDurationMs, color, currentPulseIsPower);
            if (prevStablePath != null && prevStablePath.size() >= 2) {
                List<Vec3> adjustedPrevParticles = withIntroCurve(
                        adjustedForPlayer(prevStablePath, prevPathOrigin, playerPos), playerPos);
                double prevTotalLen = totalLength(adjustedPrevParticles);
                spawnPulseParticles(mc.level, adjustedPrevParticles, prevTotalLen, now, prevPulseStart, prevPulseDurationMs, color, prevPulseIsPower);
            }
        }
    }

    /**
     * Clears all trail state. Call when tracking stops.
     */
    public static void clearCache() {
        stablePath = new ArrayList<>();
        incomingPath = null;
        prevStablePath = null;
        cachedDest = null;
        lastPulseStart = 0;
        prevPulseStart = 0;
        currentPulseDurationMs = PULSE_DURATION_FAR_MS;
        prevPulseDurationMs = PULSE_DURATION_FAR_MS;
        lastParticleSpawnTime = 0;
        pulseCount = 0;
        currentPulseIsPower = false;
        prevPulseIsPower = false;
        introGuideStart = 0;
        introAimDir = null;
        introStrength = 0f;
        introFacingSince = 0;
        introLastUpdate = 0;
        stablePathOrigin = null;
        incomingPathOrigin = null;
        prevPathOrigin = null;
    }

    private static List<Vec3> computeTrailPoints(Vec3 playerPos, BlockPos dest, ClientLevel level) {
        double destX = dest.getX() + 0.5;
        double destZ = dest.getZ() + 0.5;
        double destY = dest.getY() + 0.5;

        double dx = destX - playerPos.x;
        double dz = destZ - playerPos.z;
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        if (horizDist < 1.0) return List.of();

        double renderDist = Math.min(horizDist, MAX_RENDER_DISTANCE);
        double dirX = dx / horizDist;
        double dirZ = dz / horizDist;
        double perpX = -dirZ;
        double perpZ = dirX;

        int numPoints = Math.max(10, (int) (renderDist / SAMPLE_SPACING));
        List<Vec3> points = new ArrayList<>(numPoints + 1);

        double smoothedY = playerPos.y;

        for (int i = 0; i <= numPoints; i++) {
            double t = (double) i / numPoints;
            double dist = t * renderDist;

            double distFromDest = horizDist - dist;

            double wave1 = Math.sin(distFromDest * WAVE1_FREQUENCY) * WAVE1_AMPLITUDE;
            double wave2 = Math.sin(distFromDest * WAVE2_FREQUENCY + WAVE2_PHASE_OFFSET) * WAVE2_AMPLITUDE;
            double wave = wave1 + wave2;

            double fadeIn = Math.min(1.0, dist / 6.0);
            double fadeOut = Math.min(1.0, (renderDist - dist) / 6.0);
            wave *= fadeIn * fadeOut;

            double baseX = playerPos.x + dirX * dist;
            double baseZ = playerPos.z + dirZ * dist;
            double x = baseX + perpX * wave;
            double z = baseZ + perpZ * wave;

            double interpY = playerPos.y + (destY - playerPos.y) * (dist / horizDist);
            double surfaceY = getGroundHeight(level, (int) x, (int) z) + HEIGHT_OFFSET;
            double targetY = Math.min(interpY, surfaceY);

            double yDiff = targetY - smoothedY;
            if (yDiff > MAX_Y_STEP) smoothedY += MAX_Y_STEP;
            else if (yDiff < -MAX_Y_STEP) smoothedY -= MAX_Y_STEP;
            else smoothedY = targetY;

            points.add(new Vec3(x, smoothedY, z));
        }

        return points;
    }

    private static void beginIntroGuide(long now) {
        introGuideStart = now;
        introAimDir = null;
        introStrength = 0f;
        introFacingSince = 0;
        introLastUpdate = now;
    }

    private static void updateIntroGuide(net.minecraft.client.player.LocalPlayer player,
                                         Vec3 playerPos, BlockPos dest, long now) {
        float dt = introLastUpdate == 0
                ? 0.05f
                : (float) Math.clamp((now - introLastUpdate) / 1000.0, 0.0, 0.25);
        introLastUpdate = now;

        float target = 0f;
        if (introGuideStart > 0) {
            long age = now - introGuideStart;
            long fadeAt = INTRO_GUIDE_MS - INTRO_FADE_MS;
            if (age < fadeAt) {
                target = 1f;
            } else if (age < INTRO_GUIDE_MS) {
                target = smoothstep(1f - (age - fadeAt) / (float) INTRO_FADE_MS);
            }

            Vec3 toDest = new Vec3(dest.getX() + 0.5 - playerPos.x,
                    dest.getY() + 0.5 - playerPos.y,
                    dest.getZ() + 0.5 - playerPos.z);
            if (toDest.lengthSqr() > 1.0e-4
                    && player.getViewVector(1.0f).dot(toDest.normalize()) >= INTRO_FACING_DOT) {
                if (introFacingSince == 0) introFacingSince = now;
                long held = now - introFacingSince;
                target = Math.min(target, smoothstep(1f - held / (float) INTRO_FACING_RELEASE_MS));
                if (held >= INTRO_FACING_RELEASE_MS) introGuideStart = 0;
            } else {
                introFacingSince = 0;
            }
        }

        Vec3 look = player.getViewVector(1.0f);
        Vec3 desired = new Vec3(look.x, look.y * INTRO_AIM_PITCH_DAMP, look.z);
        if (desired.lengthSqr() < 1.0e-4) desired = look;
        if (desired.lengthSqr() > 1.0e-6) {
            desired = desired.normalize();
            if (introAimDir == null) {
                introAimDir = desired;
            } else {
                double blend = 1.0 - Math.exp(-dt / INTRO_AIM_TAU);
                Vec3 mixed = introAimDir.add(desired.subtract(introAimDir).scale(blend));
                if (mixed.lengthSqr() > 1.0e-6) introAimDir = mixed.normalize();
            }
        }

        double ease = 1.0 - Math.exp(-dt / INTRO_STRENGTH_TAU);
        introStrength += (float) ((target - introStrength) * ease);
        if (target == 0f && introStrength < 0.004f) {
            introStrength = 0f;
            introAimDir = null;
        }
    }

    private static int introJoinIndex(int size) {
        return Math.clamp((int) Math.round(INTRO_JOIN_DISTANCE / SAMPLE_SPACING), 3, size - 1);
    }

    private static void renderIntroLead(PoseStack.Pose pose, Vec3 cam, VertexConsumer consumer,
                                        float lineWidth, List<Vec3> points, int joinIndex,
                                        float r, float g, float b, float baseAlpha) {
        if (baseAlpha <= 0.002f || joinIndex < 2) return;

        for (int i = 0; i < joinIndex; i++) {
            Vec3 a = points.get(i);
            Vec3 next = points.get(i + 1);

            float alphaA = baseAlpha * introLeadTaper((double) i / joinIndex);
            float alphaB = baseAlpha * introLeadTaper((double) (i + 1) / joinIndex);
            if (alphaA < 0.004f && alphaB < 0.004f) continue;

            float nx = (float) (next.x - a.x);
            float ny = (float) (next.y - a.y);
            float nz = (float) (next.z - a.z);
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len > 0) {
                nx /= len;
                ny /= len;
                nz /= len;
            }

            consumer.addVertex(pose, (float) (a.x - cam.x), (float) (a.y - cam.y), (float) (a.z - cam.z))
                    .setColor(r, g, b, alphaA)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(lineWidth);
            consumer.addVertex(pose, (float) (next.x - cam.x), (float) (next.y - cam.y), (float) (next.z - cam.z))
                    .setColor(r, g, b, alphaB)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(lineWidth);
        }
    }

    private static float introLeadTaper(double t) {
        float in = smoothstep((float) (t / INTRO_LEAD_FADE_IN));
        float out = smoothstep((float) ((1.0 - t) / INTRO_LEAD_FADE_OUT));
        return in * out;
    }

    private static List<Vec3> withIntroCurve(List<Vec3> points, Vec3 playerPos) {
        if (introStrength <= 0.004f || introAimDir == null || points == null || points.size() < 4) {
            return points;
        }

        int joinIndex = introJoinIndex(points.size());
        Vec3 join = points.get(joinIndex);
        Vec3 control = playerPos.add(introAimDir.scale(INTRO_CONTROL_DISTANCE));

        List<Vec3> curved = new ArrayList<>(points);
        for (int i = 1; i < joinIndex; i++) {
            double t = (double) i / joinIndex;
            double oneMinusT = 1.0 - t;
            Vec3 bezier = playerPos.scale(oneMinusT * oneMinusT)
                    .add(control.scale(2.0 * oneMinusT * t))
                    .add(join.scale(t * t));
            Vec3 original = points.get(i);
            curved.set(i, original.add(bezier.subtract(original).scale(introStrength)));
        }
        return curved;
    }

    private static float smoothstep(float x) {
        if (x <= 0f) return 0f;
        if (x >= 1f) return 1f;
        return x * x * (3f - 2f * x);
    }

    private static int getGroundHeight(ClientLevel level, int x, int z) {
        try {
            if (!level.getChunkSource().hasChunk(x >> 4, z >> 4)) {
                return level.getSeaLevel();
            }

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
            int minY = Math.max(level.getMinY(), y - 20);

            for (int scanY = y; scanY > minY; scanY--) {
                pos.setY(scanY);
                BlockState below = level.getBlockState(pos);
                pos.setY(scanY + 1);
                BlockState above = level.getBlockState(pos);

                if (isGroundBlock(below) && isPassable(above)) {
                    return scanY + 1;
                }
            }

            return y;
        } catch (Exception e) {
            return level.getSeaLevel();
        }
    }

    private static boolean isGroundBlock(BlockState state) {
        if (!state.isSolid()) return false;
        if (state.is(BlockTags.LOGS)) return false;
        if (state.is(BlockTags.FENCES)) return false;
        return !state.is(BlockTags.WALLS);
    }

    private static boolean isPassable(BlockState state) {
        return state.isAir() || !state.isSolid() || state.is(BlockTags.REPLACEABLE);
    }

    private static void renderLayer(PoseStack.Pose pose, Vec3 cam, VertexConsumer consumer,
                                    float lineWidth,
                                    List<Vec3> points, float r, float g, float b,
                                    float baseAlpha, long now, long pulseStart,
                                    long prevPulse, long pulseDurationMs, double totalLen, boolean isGlow,
                                    boolean isPower) {
        double accumulated = 0.0;

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 a = points.get(i);
            Vec3 next = points.get(i + 1);
            double segLen = a.distanceTo(next);

            float alphaA = computeAlpha(accumulated, totalLen, now, pulseStart, prevPulse, pulseDurationMs, baseAlpha, isGlow, isPower);
            float alphaB = computeAlpha(accumulated + segLen, totalLen, now, pulseStart, prevPulse, pulseDurationMs, baseAlpha, isGlow, isPower);

            if (alphaA < 0.005f && alphaB < 0.005f) {
                accumulated += segLen;
                continue;
            }

            float gradA = (float) (accumulated / totalLen);
            float gradB = (float) ((accumulated + segLen) / totalLen);
            float rA = r + (1.0f - r) * gradA * 0.25f;
            float gA = g + (1.0f - g) * gradA * 0.25f;
            float bA = b + (1.0f - b) * gradA * 0.25f;
            float rB = r + (1.0f - r) * gradB * 0.25f;
            float gB = g + (1.0f - g) * gradB * 0.25f;
            float bB = b + (1.0f - b) * gradB * 0.25f;

            float whitenA = computePulseWhiten(accumulated, now, pulseStart, pulseDurationMs, totalLen, isPower);
            float whitenB = computePulseWhiten(accumulated + segLen, now, pulseStart, pulseDurationMs, totalLen, isPower);
            rA += (1.0f - rA) * whitenA;
            gA += (1.0f - gA) * whitenA;
            bA += (1.0f - bA) * whitenA;
            rB += (1.0f - rB) * whitenB;
            gB += (1.0f - gB) * whitenB;
            bB += (1.0f - bB) * whitenB;

            float nx = (float) (next.x - a.x);
            float ny = (float) (next.y - a.y);
            float nz = (float) (next.z - a.z);
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len > 0) {
                nx /= len;
                ny /= len;
                nz /= len;
            }

            float ax = (float) (a.x - cam.x);
            float ay = (float) (a.y - cam.y);
            float az = (float) (a.z - cam.z);
            float bx = (float) (next.x - cam.x);
            float by = (float) (next.y - cam.y);
            float bz = (float) (next.z - cam.z);

            consumer.addVertex(pose, ax, ay, az)
                    .setColor(rA, gA, bA, alphaA)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(lineWidth);
            consumer.addVertex(pose, bx, by, bz)
                    .setColor(rB, gB, bB, alphaB)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(lineWidth);

            accumulated += segLen;
        }
    }

    /**
     * Computes alpha for a single pulse given its start time.
     * The pulse sweeps from destination (dist=totalLen) toward the player (dist=0).
     */
    private static float computeAlphaForPulse(double dist, double totalLen,
                                              long now, long pulseStart,
                                              long pulseDurationMs,
                                              float baseAlpha, boolean isGlow,
                                              boolean isPower) {
        if (pulseStart <= 0) return 0;

        double reachTime = pulseStart + ((totalLen - dist) / totalLen) * pulseDurationMs;
        double timeSincePassed = now - reachTime;

        if (timeSincePassed < 0) {
            return 0;
        }

        float visibility = 1.0f;
        if (timeSincePassed > WAKE_VISIBLE_MS) {
            double fadeTime = timeSincePassed - WAKE_VISIBLE_MS;
            if (fadeTime >= WAKE_FADE_MS) {
                return 0; // Fully faded
            }
            visibility = 1.0f - (float) (fadeTime / WAKE_FADE_MS);
        }

        double leadingEdgeW = isPower ? POWER_LEADING_EDGE_WIDTH : LEADING_EDGE_WIDTH;
        double wakeGlowW = isPower ? POWER_WAKE_GLOW_WIDTH : WAKE_GLOW_WIDTH;

        float pulseMod = 1.0f;
        long elapsed = now - pulseStart;
        if (elapsed >= 0 && elapsed < pulseDurationMs) {
            double frontierDist = totalLen * (1.0 - (double) elapsed / pulseDurationMs);
            double distToFrontier = Math.abs(dist - frontierDist);

            if (distToFrontier < leadingEdgeW) {
                float intensity = 1.0f - (float) (distToFrontier / leadingEdgeW);
                intensity *= intensity;
                float glowMult = isPower ? 16.0f : 10.0f;
                float coreMult = isPower ? 10.0f : 6.0f;
                pulseMod = 1.0f + intensity * (isGlow ? glowMult : coreMult);
            } else if (dist > frontierDist) {
                double wakeDist = dist - frontierDist - leadingEdgeW;
                if (wakeDist > 0 && wakeDist < wakeGlowW) {
                    float wakeIntensity = 1.0f - (float) (wakeDist / wakeGlowW);
                    float wakeGlowMult = isPower ? 5.0f : 3.0f;
                    float wakeCoreMult = isPower ? 3.0f : 1.5f;
                    pulseMod = 1.0f + wakeIntensity * (isGlow ? wakeGlowMult : wakeCoreMult);
                }
            }
        }

        return Math.min(1.0f, baseAlpha * visibility * pulseMod);
    }

    /**
     * Computes the final alpha as the MAX of the current pulse and the
     * previous pulse. This prevents points from briefly blinking out when
     * a new pulse fires while the old trail is still fading.
     */
    private static float computeAlpha(double dist, double totalLen,
                                      long now, long pulseStart, long prevPulse, long pulseDurationMs,
                                      float baseAlpha, boolean isGlow,
                                      boolean isPower) {
        float fade = 1.0f;
        if (dist < 4.0) fade = (float) (dist / 4.0);
        if (dist > totalLen - 4.0) fade = (float) ((totalLen - dist) / 4.0);
        fade = Math.clamp(fade, 0.0f, 1.0f);

        float alphaCurrent = computeAlphaForPulse(dist, totalLen, now, pulseStart, pulseDurationMs, baseAlpha * fade, isGlow, isPower);
        float alphaPrev = computeAlphaForPulse(dist, totalLen, now, prevPulse, pulseDurationMs, baseAlpha * fade, isGlow, isPower);

        return Math.max(alphaCurrent, alphaPrev);
    }

    /**
     * Computes color whitening at the pulse leading edge.
     * Returns 0..0.7 (normal) or 0..0.9 (power), the fraction to shift color toward white.
     */
    private static float computePulseWhiten(double dist, long now, long pulseStart,
                                            long pulseDurationMs, double totalLen, boolean isPower) {
        long elapsed = now - pulseStart;
        if (elapsed < 0 || elapsed >= pulseDurationMs) return 0;

        double leadingEdgeW = isPower ? POWER_LEADING_EDGE_WIDTH : LEADING_EDGE_WIDTH;

        double frontierDist = totalLen * (1.0 - (double) elapsed / pulseDurationMs);
        double absDist = Math.abs(dist - frontierDist);
        if (absDist < leadingEdgeW) {
            float intensity = 1.0f - (float) (absDist / leadingEdgeW);
            float maxWhiten = isPower ? 0.9f : 0.7f;
            return intensity * intensity * maxWhiten;
        }
        return 0;
    }

    /**
     * Fires the scent trigger and overlay exactly when a power pulse
     * completes its sweep toward the player, zero network latency.
     */
    private static void firePowerPulsePuff() {
        String scentName = resolveScentName();
        if (scentName == null) return;

        double intensity = resolveScentIntensity();

        ScentTrigger trigger = ScentTrigger.create(
                scentName,
                null,
                null,
                ScentTriggerSource.PATH_TRACKING,
                ScentPriority.MEDIUM,
                100,
                intensity
        );

        boolean triggered = ScentTriggerManager.getInstance().trigger(trigger);
        if (triggered) {
            PathTrackingMaskOverlay.onPathScentPuff(scentName, intensity);

            if (AromaConfig.getGeneral().debugScentMessages) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    int intensityPercent = (int) Math.round(intensity * 100);
                    String message = String.format(
                            "§d[Aroma Lib] §7Scent: §e%s §7(§dpath tracking§7) §8[%d%%]",
                            scentName, intensityPercent);
                    mc.player.sendSystemMessage(Component.literal(message));
                }
            }
        }
    }

    /**
     * Resolves the scent name for the current tracking target.
     * Uses the same lookup chain as {@link #resolveColor()}.
     */
    private static String resolveScentName() {
        String targetId = ActiveTrackingState.getTargetId() != null
                ? ActiveTrackingState.getTargetId().toString() : null;
        TrackingCategory cat = ActiveTrackingState.getCategory();
        if (targetId == null || cat == null) return null;

        TrailDomain domain = cat.getTrailDomain();
        if (domain == TrailDomain.BLOCK) {
            var blockTrigger = ScentTriggerConfigLoader.getBlockTrigger(targetId);
            if (blockTrigger.isPresent()) return blockTrigger.get().getScentName();
        } else if (domain == TrailDomain.BIOME) {
            var biomeTrigger = ScentTriggerConfigLoader.getBiomeTrigger(targetId);
            if (biomeTrigger.isPresent()) return biomeTrigger.get().getScentName();
        } else if (domain == TrailDomain.STRUCTURE) {
            var structureTrigger = ScentTriggerConfigLoader.getStructureTrigger(targetId);
            if (structureTrigger.isPresent()) return structureTrigger.get().getScentName();
        }

        return null;
    }

    /**
     * Resolves the scent intensity for the current tracking target.
     * Falls back to category-specific defaults from {@link TriggerSettings}.
     */
    private static double resolveScentIntensity() {
        String targetId = ActiveTrackingState.getTargetId() != null
                ? ActiveTrackingState.getTargetId().toString() : null;
        TrackingCategory cat = ActiveTrackingState.getCategory();
        TriggerSettings settings = ScentTriggerConfigLoader.getSettings();
        TrailDomain domain = cat != null ? cat.getTrailDomain() : TrailDomain.BLOCK;

        if (targetId != null) {
            if (domain == TrailDomain.BLOCK) {
                var blockTrigger = ScentTriggerConfigLoader.getBlockTrigger(targetId);
                if (blockTrigger.isPresent())
                    return blockTrigger.get().getIntensityOrDefault(settings.getBlockIntensity());
            } else if (domain == TrailDomain.BIOME) {
                var biomeTrigger = ScentTriggerConfigLoader.getBiomeTrigger(targetId);
                if (biomeTrigger.isPresent())
                    return biomeTrigger.get().getIntensityOrDefault(settings.getBiomeIntensity());
            } else if (domain == TrailDomain.STRUCTURE) {
                var structureTrigger = ScentTriggerConfigLoader.getStructureTrigger(targetId);
                if (structureTrigger.isPresent())
                    return structureTrigger.get().getIntensityOrDefault(settings.getStructureIntensity());
            }
        }

        if (domain == TrailDomain.BIOME) return settings.getBiomeIntensity();
        if (domain == TrailDomain.STRUCTURE) return settings.getStructureIntensity();
        return settings.getBlockIntensity();
    }

    private static float[] resolveColor() {
        String targetId = ActiveTrackingState.getTargetId() != null
                ? ActiveTrackingState.getTargetId().toString() : null;
        TrackingCategory cat = ActiveTrackingState.getCategory();

        if (targetId != null) {
            var blockDef = BlockRegistry.INSTANCE.getDefinition(targetId);
            if (blockDef.isPresent()) {
                return blockDef.get().getColorAsFloats();
            }

            var blockTrigger = ScentTriggerConfigLoader.getBlockTrigger(targetId);
            if (blockTrigger.isPresent()) {
                var scent = ScentRegistry.INSTANCE.getScentByName(blockTrigger.get().getScentName());
                if (scent.isPresent()) {
                    int[] rgb = scent.get().getColorRGB();
                    return new float[]{rgb[0] / 255.0f, rgb[1] / 255.0f, rgb[2] / 255.0f};
                }
            }

            var biomeTrigger = ScentTriggerConfigLoader.getBiomeTrigger(targetId);
            if (biomeTrigger.isPresent()) {
                var scent = ScentRegistry.INSTANCE.getScentByName(biomeTrigger.get().getScentName());
                if (scent.isPresent()) {
                    int[] rgb = scent.get().getColorRGB();
                    return new float[]{rgb[0] / 255.0f, rgb[1] / 255.0f, rgb[2] / 255.0f};
                }
            }

            var structureTrigger = ScentTriggerConfigLoader.getStructureTrigger(targetId);
            if (structureTrigger.isPresent()) {
                var scent = ScentRegistry.INSTANCE.getScentByName(structureTrigger.get().getScentName());
                if (scent.isPresent()) {
                    int[] rgb = scent.get().getColorRGB();
                    return new float[]{rgb[0] / 255.0f, rgb[1] / 255.0f, rgb[2] / 255.0f};
                }
            }
        }

        if (cat != null) {
            return cat.getTrailDomain().fallbackTrailColor();
        }

        return new float[]{0.8f, 0.85f, 1.0f};
    }

    private static void spawnPulseParticles(ClientLevel level, List<Vec3> points,
                                            double totalLen, long now, long pulseStart, long pulseDurationMs,
                                            float[] color, boolean isPower) {
        if (pulseStart <= 0 || points.size() < 2 || totalLen < 1.0) return;

        int argb = 0xFF000000
                | (Math.min(255, (int) (color[0] * 255)) << 16)
                | (Math.min(255, (int) (color[1] * 255)) << 8)
                | Math.min(255, (int) (color[2] * 255));
        var particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, argb);

        double accum = 0.0;
        for (int i = 0; i < points.size(); i++) {
            Vec3 pt = points.get(i);
            if (i > 0) accum += points.get(i - 1).distanceTo(pt);

            int count = getParticleCount(accum, totalLen, now, pulseStart, pulseDurationMs, isPower);
            if (count <= 0) continue;
            if (count == 1 && i % 2 != 0) continue;

            for (int c = 0; c < count; c++) {
                level.addParticle(particle, pt.x, pt.y, pt.z, 0, 0, 0);
            }
        }
    }

    private static int getParticleCount(double dist, double totalLen, long now, long pulseStart, long pulseDurationMs,
                                        boolean isPower) {
        if (pulseStart <= 0) return 0;

        // Reversed: pulse starts at dest (dist=totalLen)
        double reachTime = pulseStart + ((totalLen - dist) / totalLen) * pulseDurationMs;
        double timeSincePassed = now - reachTime;

        if (timeSincePassed < 0) return 0;
        if (timeSincePassed > WAKE_VISIBLE_MS) return 0;

        double leadingEdgeW = isPower ? POWER_LEADING_EDGE_WIDTH : LEADING_EDGE_WIDTH;
        double wakeGlowW = isPower ? POWER_WAKE_GLOW_WIDTH : WAKE_GLOW_WIDTH;

        long elapsed = now - pulseStart;
        if (elapsed >= 0 && elapsed < pulseDurationMs) {
            double frontierDist = totalLen * (1.0 - (double) elapsed / pulseDurationMs);
            double distToFrontier = Math.abs(dist - frontierDist);

            if (distToFrontier < leadingEdgeW) return isPower ? 5 : 3;
            if (dist > frontierDist && (dist - frontierDist) < wakeGlowW) return isPower ? 3 : 2;
        }

        return 1;
    }

    /**
     * Returns a copy of the path with the first few points shifted so that
     * point[0] matches the current player position. Uses quadratic falloff
     * so the adjustment blends smoothly back to the original computed path.
     * The original list is NEVER mutated.
     */
    private static List<Vec3> adjustedForPlayer(List<Vec3> path, Vec3 originalPlayerPos, Vec3 currentPlayerPos) {
        if (path == null || path.size() < 2 || originalPlayerPos == null) return path;
        Vec3 delta = currentPlayerPos.subtract(originalPlayerPos);
        if (delta.lengthSqr() < 0.001) return path; // No significant movement

        List<Vec3> adjusted = new ArrayList<>(path);
        int blendCount = Math.min(PLAYER_BLEND_POINTS, adjusted.size());
        for (int i = 0; i < blendCount; i++) {
            double weight = 1.0 - (double) i / blendCount;
            weight *= weight;
            adjusted.set(i, path.get(i).add(delta.scale(weight)));
        }
        return adjusted;
    }

    private static long resolvePulseDurationMs(double pathLen) {
        int trackedDistance = ActiveTrackingState.getDistance();
        double basis = trackedDistance >= 0 ? trackedDistance : pathLen;
        double normalized = Math.clamp(basis / MAX_RENDER_DISTANCE, 0.0, 1.0);
        double eased = normalized * normalized;
        long duration = (long) (PULSE_DURATION_NEAR_MS + (PULSE_DURATION_FAR_MS - PULSE_DURATION_NEAR_MS) * eased);
        return Math.clamp(duration, PULSE_DURATION_NEAR_MS, PULSE_DURATION_FAR_MS);
    }

    private static long resolvePulseCycleMs(long pulseDurationMs) {
        long base = pulseDurationMs + 900L;
        return Math.clamp(base, PULSE_CYCLE_NEAR_MS, PULSE_CYCLE_FAR_MS);
    }


    private static double totalLength(List<Vec3> points) {
        double len = 0;
        for (int i = 1; i < points.size(); i++) {
            len += points.get(i - 1).distanceTo(points.get(i));
        }
        return len;
    }

    private static final class TrailRenderType {
        private TrailRenderType() {
        }

        static RenderType create(String name) {
            return RenderType.create(AromaLib.MOD_ID + ":" + name,
                    RenderSetup.builder(TRAIL_PIPELINE).createRenderSetup()
            );
        }
    }
}
