package moth.trickroom.mixin;

import moth.trickroom.entity.TrickRoomCollision;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class TrickRoomEntityCollisionMixin {
    private static final ThreadLocal<Boolean> trickroom$collisionPass = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void trickroom$keepMovementInsideRoom(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        Entity entity = (Entity) (Object) this;
        cir.setReturnValue(TrickRoomCollision.clampExitMovement(entity, cir.getReturnValue()));
    }

    @Inject(
            method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void trickroom$addCollisions(Entity entity, Vec3d movement, Box entityBoundingBox,
                                                World world, List<VoxelShape> collisions,
                                                CallbackInfoReturnable<Vec3d> cir) {
        if (trickroom$collisionPass.get()) {
            return;
        }

        List<VoxelShape> roomShapes = TrickRoomCollision.collisionShapes(entity, entityBoundingBox, world, movement);
        if (roomShapes.isEmpty()) {
            return;
        }

        List<VoxelShape> combinedCollisions = new ArrayList<>(collisions.size() + roomShapes.size());
        combinedCollisions.addAll(collisions);
        combinedCollisions.addAll(roomShapes);
        trickroom$collisionPass.set(true);
        try {
            cir.setReturnValue(Entity.adjustMovementForCollisions(entity, movement, entityBoundingBox, world, combinedCollisions));
        } finally {
            trickroom$collisionPass.set(false);
        }
    }
}
