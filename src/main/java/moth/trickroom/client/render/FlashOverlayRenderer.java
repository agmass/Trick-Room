package moth.trickroom.client.render;

import moth.trickroom.TrickRoomMod;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public final class FlashOverlayRenderer {
    private static final long FLASH_DURATION_MS = 2500L;
    private static long flashStartTime = 0L;

    private FlashOverlayRenderer() {
    }

    public static void init() {
        HudElementRegistry.addLast(TrickRoomMod.id("flash_overlay"), (DrawContext drawContext, RenderTickCounter tickCounter) -> {
            if (!isFlashing()) {
                return;
            }
            float progress = (System.currentTimeMillis() - flashStartTime) / (float) FLASH_DURATION_MS;
            float alpha = Math.max(1.0F - progress, 0.0F);
            drawContext.fill(
                    0,
                    0,
                    drawContext.getScaledWindowWidth(),
                    drawContext.getScaledWindowHeight(),
                    ((int) (alpha * 255) << 24) | 0x03fcfc
            );
        });
    }

    public static void triggerFlash() {
        flashStartTime = System.currentTimeMillis();
    }

    private static boolean isFlashing() {
        return System.currentTimeMillis() - flashStartTime < FLASH_DURATION_MS;
    }
}
