package moth.trickroom.mixin.client;

import moth.trickroom.item.ModItems;
import moth.trickroom.network.TrickRoomModePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "doAttack", at = @At("HEAD"))
    private void trickroom$cycleModeOnAirAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.player == null || !client.player.getMainHandStack().isOf(ModItems.TRICK_ROOM)) {
            return;
        }
        if (client.crosshairTarget == null || client.crosshairTarget.getType() == HitResult.Type.MISS) {
            if (ClientPlayNetworking.canSend(TrickRoomModePacket.ID)) {
                ClientPlayNetworking.send(new TrickRoomModePacket());
            }
        }
    }
}
