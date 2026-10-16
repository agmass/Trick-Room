package moth.trickroom.mixin.client;

import moth.trickroom.effect.ModStatusEffects;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void trickroom$cancelKeysWhenFrozen(long window, int action, KeyInput input, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player.hasStatusEffect(ModStatusEffects.IMMOBILIZED)) {
            if (client.player.isAlive()) {
                if (input.key() == GLFW.GLFW_KEY_ESCAPE || action == GLFW.GLFW_RELEASE) {
                    return;
                }
                ci.cancel();
            }
        }
    }
}
