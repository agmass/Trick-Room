package moth.trickroom.client;

import moth.trickroom.TrickRoomMod;
import moth.trickroom.client.render.FlashOverlayRenderer;
import moth.trickroom.client.render.TrickRoomShaders;
import moth.trickroom.client.render.TrickRoomEntityRenderer;
import moth.trickroom.entity.ModEntityTypes;
import moth.trickroom.effect.ModStatusEffects;
import moth.trickroom.item.ModItems;
import moth.trickroom.network.TrickRoomFlashPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TrickRoomClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TrickRoomShaders.register();
        EntityRendererFactories.register(ModEntityTypes.TRICK_ROOM, TrickRoomEntityRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(TrickRoomFlashPacket.ID, (payload, context) ->
                context.client().execute(FlashOverlayRenderer::triggerFlash)
        );
        FlashOverlayRenderer.init();
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(TrickRoomEntityRenderer::renderLast);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.hasStatusEffect(ModStatusEffects.IMMOBILIZED)) {
                KeyBinding.unpressAll();
            }
        });
        ItemTooltipCallback.EVENT.register((stack, context, type, text) -> {
            addCooldownText(ModItems.TRICK_ROOM, text, stack);
        });
    }
    private static void addCooldownText(Item item, List<Text> tooltipList, @NotNull ItemStack itemStack) {
        if (!itemStack.isOf(item)) return;
        ItemCooldownManager itemCooldownManager = MinecraftClient.getInstance().player.getItemCooldownManager();
        if (itemCooldownManager.isCoolingDown(itemStack)) {
            ItemCooldownManager.Entry knifeEntry = itemCooldownManager.entries.get(TrickRoomMod.id("trick_room"));
            int timeLeft = knifeEntry.endTick - itemCooldownManager.tick;
            if (timeLeft > 0) {
                int minutes = (int) Math.floor((double) timeLeft / 1200);
                int seconds = (timeLeft - (minutes * 1200)) / 20;
                String countdown = (minutes > 0 ? minutes + "m" : "") + " " + (seconds > 0 ? seconds + "s" : "");
                tooltipList.add(Text.translatable("tip.cooldown", countdown).withColor(0x03fcfc));
            }
        }
    }
}
