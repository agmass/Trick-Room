package moth.trickroom.mixin;

import moth.trickroom.entity.TrickRoomCollision;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void trickroom$use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Item item = (Item)(Object)this;
        if (TrickRoomCollision.entityInsideAnyRoom(user)) {
            if (item.getComponents().contains(DataComponentTypes.FOOD)) {
                if (item.getComponents().get(DataComponentTypes.FOOD).equals(FoodComponents.CHORUS_FRUIT)) {
                    user.sendMessage(Text.of("The chorus fruit feels like acid on your tongue...").copy().formatted(Formatting.ITALIC, Formatting.GRAY), true);
                    user.getItemCooldownManager().set(user.getStackInHand(hand), 100);
                    cir.setReturnValue(ActionResult.FAIL);
                }
            }
            if (item.getRegistryEntry().isIn(ItemTags.BOATS)) {
                user.sendMessage(Text.of("The boat falls apart as you try to place it...").copy().formatted(Formatting.ITALIC, Formatting.GRAY), true);
                user.getItemCooldownManager().set(user.getStackInHand(hand), 100);
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}
