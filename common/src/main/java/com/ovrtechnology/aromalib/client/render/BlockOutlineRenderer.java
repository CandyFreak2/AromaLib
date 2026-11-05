package com.ovrtechnology.aromalib.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ovrtechnology.aromalib.AromaLib;
import com.ovrtechnology.aromalib.registry.BlockRegistry;
import com.ovrtechnology.aromalib.registry.ScentRegistry;
import com.ovrtechnology.aromalib.tracking.ActiveTrackingState;
import com.ovrtechnology.aromalib.tracking.TrackingCategory;
import com.ovrtechnology.aromalib.tracking.TrailDomain;
import com.ovrtechnology.aromalib.definition.trigger.ScentTriggerConfigLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


/**
 * Renders a visual wireframe outline at the destination block position
 * when tracking blocks or flowers within 32 blocks.
 */
public final class BlockOutlineRenderer {

    /**
     * Visual pipeline: lines that draw through walls (no depth test) with
     * translucent blending so distant outlines fade with alpha. Built from
     * vanilla {@code RenderPipelines.LINES_SNIPPET} (shaders, uniforms,
     * vertex format) plus our two overrides.
     */
    private static final RenderPipeline LINES_NO_DEPTH_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(AromaLib.MOD_ID, "pipeline/lines_no_depth"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withCull(false)
            .build();

    public static final RenderType LINES_NO_DEPTH = RenderType.create (
            AromaLib.MOD_ID + ":lines_no_depth",
            RenderSetup.builder(LINES_NO_DEPTH_PIPELINE).createRenderSetup()
    );

    private static final float OUTLINE_LINE_WIDTH = 2.5f;

    private BlockOutlineRenderer() {
    }

    public static void renderOutline(PoseStack poseStack, Vec3 cameraPos, OrderedSubmitNodeCollector collector) {
        if (!ActiveTrackingState.shouldShowOutline() || Minecraft.getInstance().isPaused()) {
            return;
        }

        BlockPos dest = ActiveTrackingState.getDestination();
        if (dest == null) return;

        // Resolve color: blocks from BlockRegistry, flowers from scent trigger config
        float r = 1.0f, g = 1.0f, b = 1.0f;
        float[] resolved = resolveColor();
        if (resolved != null) {
            r = resolved[0];
            g = resolved[1];
            b = resolved[2];
        }

        // Alpha fade-in: 32 blocks = 0 alpha, 24 blocks = full alpha
        int distance = ActiveTrackingState.getDistance();
        float baseAlpha;
        if (distance <= 24) {
            baseAlpha = 1.0f;
        } else {
            baseAlpha = 1.0f - (distance - 24) / 8.0f;
        }

        // Pulse animation
        float time = (System.currentTimeMillis() % 2000) / 2000.0f;
        float pulse = 0.7f + 0.3f * (float) Math.sin(time * Math.PI * 2.0);
        float alpha = baseAlpha * pulse;
        if (alpha <= 0.0f) return;

        // Camera-relative coordinates
        double dx = dest.getX() - cameraPos.x;
        double dy = dest.getY() - cameraPos.y;
        double dz = dest.getZ() - cameraPos.z;

        int red = (int) (r * 255);
        int green = (int) (g * 255);
        int blue = (int) (b * 255);
        int alpha8 = (int) (alpha * 255);
        collector.submitCustomGeometry(poseStack, LINES_NO_DEPTH, (pose, consumer) ->
                renderShapeWithLineWidth(pose, consumer, Shapes.block(),
                        dx, dy, dz, red, green, blue, alpha8, OUTLINE_LINE_WIDTH));
    }

    private static void renderShapeWithLineWidth(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            VoxelShape shape,
            double offsetX, double offsetY, double offsetZ,
            int red, int green, int blue, int alpha,
            float lineWidth) {
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float nx = (float) (x2 - x1);
            float ny = (float) (y2 - y1);
            float nz = (float) (z2 - z1);
            float invLen = (float) (1.0 / Math.sqrt(nx * nx + ny * ny + nz * nz));
            nx *= invLen;
            ny *= invLen;
            nz *= invLen;
            consumer.addVertex(pose,
                            (float) (x1 + offsetX),
                            (float) (y1 + offsetY),
                            (float) (z1 + offsetZ))
                    .setColor(red, green, blue, alpha)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(lineWidth);
            consumer.addVertex(pose,
                            (float) (x2 + offsetX),
                            (float) (y2 + offsetY),
                            (float) (z2 + offsetZ))
                    .setColor(red, green, blue, alpha)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(lineWidth);
        });
    }

    private static float[] resolveColor() {
        String targetId = ActiveTrackingState.getTargetId() != null
                ? ActiveTrackingState.getTargetId().toString() : null;
        if (targetId == null) return null;

        // Try BlockRegistry first (for BLOCKS category)
        var blockDef = BlockRegistry.INSTANCE.getDefinition(targetId);
        if (blockDef.isPresent()) {
            return blockDef.get().getColorAsFloats();
        }

        // For flowers (and any block not in BlockRegistry), resolve via scent trigger
        var trigger = ScentTriggerConfigLoader.getBlockTrigger(targetId);
        if (trigger.isPresent()) {
            String scentName = trigger.get().getScentName();
            var scent = ScentRegistry.INSTANCE.getScentByName(scentName);
            if (scent.isPresent()) {
                int[] rgb = scent.get().getColorRGB();
                return new float[]{rgb[0] / 255.0f, rgb[1] / 255.0f, rgb[2] / 255.0f};
            }
        }

        // Fallback for structures: gold/amber color
        TrackingCategory cat = ActiveTrackingState.getCategory();
        if (cat != null && cat.getTrailDomain() == TrailDomain.STRUCTURE) {
            return new float[]{1.0f, 0.8f, 0.2f};
        }

        return null;
    }
}
