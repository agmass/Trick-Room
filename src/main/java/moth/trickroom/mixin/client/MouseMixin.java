package moth.trickroom.mixin.client;

import moth.trickroom.effect.ModStatusEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void trickroom$cancelMouseButtonsWhenFrozen(long window, MouseInput input, int button, CallbackInfo ci) {
        if (isFrozen(MinecraftClient.getInstance().player) && !MinecraftClient.getInstance().isPaused()) {
            KeyBinding.unpressAll();
            ci.cancel();
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void trickroom$cancelMouseScrollWhenFrozen(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (isFrozen(MinecraftClient.getInstance().player) && !MinecraftClient.getInstance().isPaused()) {
            KeyBinding.unpressAll();
            ci.cancel();
        }
    }

    private static boolean isFrozen(@Nullable LivingEntity entity) {
        return entity != null && entity.hasStatusEffect(ModStatusEffects.IMMOBILIZED) && !entity.isSpectator() && entity.isAlive();
    }
}
