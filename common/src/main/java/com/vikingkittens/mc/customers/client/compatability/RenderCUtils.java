package com.vikingkittens.mc.customers.client.compatability;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import com.vikingkittens.mc.customers.Customers;

public final class RenderCUtils {
    private static final RenderState TRANSLUCENT_NO_DEPTH_STATE =
            new RenderState(true, false, false);
    private static final RenderPipeline TRANSLUCENT_NO_DEPTH_PIPELINE =
            RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath(
                            Customers.MODID,
                            "pipeline/translucent_no_depth"
                    ))
                    .withVertexShader(
                            "core/rendertype_translucent_moving_block"
                    )
                    .withFragmentShader(
                            "core/rendertype_translucent_moving_block"
                    )
                    .withSampler("Sampler0")
                    .withSampler("Sampler2")
                    .withUniform(
                            "DynamicTransforms",
                            UniformType.UNIFORM_BUFFER
                    )
                    .withUniform(
                            "Projection",
                            UniformType.UNIFORM_BUFFER
                    )
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withVertexFormat(
                            DefaultVertexFormat.BLOCK,
                            VertexFormat.Mode.QUADS
                    )
                    .build();
    private static final RenderSetup TRANSLUCENT_NO_DEPTH_SETUP =
            RenderSetup.builder(TRANSLUCENT_NO_DEPTH_PIPELINE)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    .useLightmap()
                    .sortOnUpload()
                    .createRenderSetup();

    private static RenderType translucentNoDepthRenderType;

    private RenderCUtils() {}

    public static RenderPipeline getTranslucentNoDepthPipeline() {
        return TRANSLUCENT_NO_DEPTH_PIPELINE;
    }

    public static RenderSetup getTranslucentNoDepthRenderSetup() {
        return TRANSLUCENT_NO_DEPTH_SETUP;
    }

    public static void setTranslucentNoDepthRenderType(
            RenderType renderType
    ) {
        translucentNoDepthRenderType = renderType;
    }

    public static RenderType getTranslucentNoDepthRenderType() {
        if (translucentNoDepthRenderType == null) {
            throw new IllegalStateException(
                    "Translucent display-case render type was not registered"
            );
        }
        return translucentNoDepthRenderType;
    }

    static RenderState getTranslucentNoDepthRenderState() {
        return TRANSLUCENT_NO_DEPTH_STATE;
    }

    record RenderState(
            boolean depthTest,
            boolean depthWrite,
            boolean cull
    ) {}
}
