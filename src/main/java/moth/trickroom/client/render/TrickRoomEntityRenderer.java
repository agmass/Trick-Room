package moth.trickroom.client.render;

import moth.trickroom.entity.ModEntityTypes;
import moth.trickroom.entity.TrickRoomEntity;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TrickRoomEntityRenderer extends EntityRenderer<TrickRoomEntity, EntityRenderState> {
    private static final double VISUAL_INSET = 1.0D / 32.0D;
    private static final double RENDER_QUERY_RANGE = 192.0D;

    public TrickRoomEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void render(EntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue,
                       CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
    }

    public static void renderLast(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null
                || context.matrices() == null
                || context.consumers() == null
                || context.worldState() == null
                || context.worldState().cameraRenderState == null
                || context.worldState().cameraRenderState.pos == null) {
            return;
        }

        Vec3d cameraPos = context.worldState().cameraRenderState.pos;
        Box queryBox = new Box(cameraPos, cameraPos).expand(RENDER_QUERY_RANGE);
        MatrixStack matrices = context.matrices();
        VertexConsumer consumer = context.consumers().getBuffer(TrickRoomShaders.trickRoom());
        float tickDelta = client.getRenderTickCounter().getTickProgress(false);

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        for (TrickRoomEntity room : client.world.getEntitiesByType(
                ModEntityTypes.TRICK_ROOM,
                queryBox,
                room -> room.hasRoomBounds() && room.getRoomBounds().intersects(queryBox)
        )) {
            float fadeAlpha = room.isFadingOut() ? room.getFadeAlpha(tickDelta) : 0.75F;
            if (fadeAlpha <= 0.0F) {
                continue;
            }
            int alpha = MathHelper.clamp(Math.round(fadeAlpha * 255.0F), 0, 255);
            emitTexturedFaces(consumer, matrix, room.getRoomBounds().contract(VISUAL_INSET), alpha);
        }
        matrices.pop();
    }

    @Override
    public boolean shouldRender(TrickRoomEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    private static void emitTexturedFaces(VertexConsumer consumer, Matrix4f matrix, Box box, int alpha) {
        emitTexturedQuad(consumer, matrix, box.minX, box.minY, box.minZ, 3.0D, 0.0D, box.minX, box.maxY, box.minZ, 3.0D, 1.0D, box.minX, box.maxY, box.maxZ, 4.0D, 1.0D, box.minX, box.minY, box.maxZ, 4.0D, 0.0D, alpha);
        emitTexturedQuad(consumer, matrix, box.maxX, box.minY, box.maxZ, 1.0D, 0.0D, box.maxX, box.maxY, box.maxZ, 1.0D, 1.0D, box.maxX, box.maxY, box.minZ, 2.0D, 1.0D, box.maxX, box.minY, box.minZ, 2.0D, 0.0D, alpha);
        emitTexturedQuad(consumer, matrix, box.minX, box.minY, box.maxZ, 0.0D, 0.0D, box.minX, box.maxY, box.maxZ, 0.0D, 1.0D, box.maxX, box.maxY, box.maxZ, 1.0D, 1.0D, box.maxX, box.minY, box.maxZ, 1.0D, 0.0D, alpha);
        emitTexturedQuad(consumer, matrix, box.maxX, box.minY, box.minZ, 2.0D, 0.0D, box.maxX, box.maxY, box.minZ, 2.0D, 1.0D, box.minX, box.maxY, box.minZ, 3.0D, 1.0D, box.minX, box.minY, box.minZ, 3.0D, 0.0D, alpha);
        emitTexturedQuad(consumer, matrix, box.minX, box.maxY, box.maxZ, 0.0D, 1.0D, box.minX, box.maxY, box.minZ, 0.0D, 2.0D, box.maxX, box.maxY, box.minZ, 1.0D, 2.0D, box.maxX, box.maxY, box.maxZ, 1.0D, 1.0D, alpha);
        emitTexturedQuad(consumer, matrix, box.minX, box.minY, box.minZ, 0.0D, -1.0D, box.minX, box.minY, box.maxZ, 0.0D, 0.0D, box.maxX, box.minY, box.maxZ, 1.0D, 0.0D, box.maxX, box.minY, box.minZ, 1.0D, -1.0D, alpha);
    }

    private static void emitTexturedQuad(VertexConsumer consumer, Matrix4f matrix,
                                         double x1, double y1, double z1, double u1, double v1,
                                         double x2, double y2, double z2, double u2, double v2,
                                         double x3, double y3, double z3, double u3, double v3,
                                         double x4, double y4, double z4, double u4, double v4,
                                         int alpha) {
        emitTexturedVertex(consumer, matrix, x1, y1, z1, u1, v1, alpha);
        emitTexturedVertex(consumer, matrix, x2, y2, z2, u2, v2, alpha);
        emitTexturedVertex(consumer, matrix, x3, y3, z3, u3, v3, alpha);
        emitTexturedVertex(consumer, matrix, x4, y4, z4, u4, v4, alpha);
    }

    private static void emitTexturedVertex(VertexConsumer consumer, Matrix4f matrix,
                                           double x, double y, double z, double u, double v, int alpha) {
        consumer.vertex(matrix, (float) x, (float) y, (float) z)
                .texture((float) u, (float) v)
                .color(255, 255, 255, alpha);
    }
}
