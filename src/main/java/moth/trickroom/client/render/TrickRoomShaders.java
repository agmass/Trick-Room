package moth.trickroom.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import moth.trickroom.TrickRoomMod;
import moth.trickroom.mixin.client.RenderLayerAccessor;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexFormats;

public final class TrickRoomShaders {
    private static final RenderLayer TRICK_ROOM = RenderLayerAccessor.trickroom$of(
            "trickroom_trick_room",
            RenderSetup.builder(RenderPipeline.builder()
                            .withLocation(TrickRoomMod.id("pipeline/trick_room"))
                            .withVertexShader(TrickRoomMod.id("core/trick_room"))
                            .withFragmentShader(TrickRoomMod.id("core/trick_room"))
                            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                            .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                            .withBlend(BlendFunction.TRANSLUCENT)
                            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                            .withDepthWrite(false)
                            .withCull(false)
                            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                            .build())
                    .translucent()
                    .expectedBufferSize(4096)
                    .build()
    );

    private TrickRoomShaders() {
    }

    public static RenderLayer trickRoom() {
        return TRICK_ROOM;
    }

    public static void register() {
    }
}
