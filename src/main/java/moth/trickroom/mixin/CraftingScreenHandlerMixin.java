package moth.trickroom.mixin;

import moth.trickroom.item.TrickRoomItem;
import moth.trickroom.util.TrickRoomNbt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {
    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;setStack(ILnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    private static void menagerie$updateResult(ScreenHandler handler, ServerWorld world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, @Nullable RecipeEntry<CraftingRecipe> recipe, CallbackInfo ci) {
        for (ItemStack stack : craftingInventory.getHeldStacks()) {
            if (stack.getItem() instanceof TrickRoomItem) {
                boolean bl = TrickRoomNbt.get(stack).getBoolean(TrickRoomItem.TEMPORARY_KEY).isPresent() ? TrickRoomNbt.get(stack).getBoolean(TrickRoomItem.TEMPORARY_KEY).get() : false;
                if (bl) {
                    resultInventory.setStack(0, new ItemStack(Items.AIR));
                }
            }
        }
    }
}
