package com.vikingkittens.mc.customers.client.compatability;

import java.util.List;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class RenderCUtils {
    private static final RenderState TRANSLUCENT_NO_DEPTH_STATE =
            new RenderState(true, false, false);

    private RenderCUtils() {}

    public static RenderType getTranslucentNoDepthRenderType() {
        return TranslucentNoDepthHolder.RENDER_TYPE;
    }

    static RenderState getTranslucentNoDepthRenderState() {
        return TRANSLUCENT_NO_DEPTH_STATE;
    }

    private static final class TranslucentNoDepthHolder {
        private static final RenderType RENDER_TYPE =
                TranslucentNoDepthRenderType.create();

        private TranslucentNoDepthHolder() {}
    }

    private static final class TranslucentNoDepthRenderType
            extends RenderType {
        private TranslucentNoDepthRenderType(
                List<RenderStateShard> states
        ) {
            super(
                    "customers_translucent_no_depth",
                    DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.QUADS,
                    BIG_BUFFER_SIZE,
                    true,
                    true,
                    () -> states.forEach(
                            RenderStateShard::setupRenderState
                    ),
                    () -> states.forEach(
                            RenderStateShard::clearRenderState
                    )
            );
        }

        private static RenderType create() {
            RenderState state = getTranslucentNoDepthRenderState();
            return new TranslucentNoDepthRenderType(List.of(
                    BLOCK_SHEET_MIPPED,
                    RENDERTYPE_TRANSLUCENT_SHADER,
                    TRANSLUCENT_TRANSPARENCY,
                    state.depthTest()
                            ? LEQUAL_DEPTH_TEST
                            : NO_DEPTH_TEST,
                    state.cull() ? CULL : NO_CULL,
                    LIGHTMAP,
                    NO_OVERLAY,
                    NO_LAYERING,
                    TRANSLUCENT_TARGET,
                    DEFAULT_TEXTURING,
                    state.depthWrite()
                            ? COLOR_DEPTH_WRITE
                            : COLOR_WRITE,
                    DEFAULT_LINE,
                    NO_COLOR_LOGIC
            ));
        }
    }

    record RenderState(
            boolean depthTest,
            boolean depthWrite,
            boolean cull
    ) {}
}
