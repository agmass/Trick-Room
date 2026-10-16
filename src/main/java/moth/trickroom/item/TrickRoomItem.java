package moth.trickroom.item;

import moth.trickroom.effect.ModStatusEffects;
import moth.trickroom.entity.TrickRoomCollision;
import moth.trickroom.entity.TrickRoomEntity;
import moth.trickroom.network.TrickRoomFlashPacket;
import moth.trickroom.sound.ModSounds;
import moth.trickroom.util.TrickRoomNbt;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class TrickRoomItem extends Item {
    public static final String ABILITY_MODE_KEY = "TrickRoomAbilityMode";

    public static final String TEMPORARY_KEY = "temporary";
    private static final String ROOM_UUID_KEY = "TrickRoomUuid";
    public static final String MULTIPLIER_KEY = "Multiplier";
    private static final int SWAP_MODE = 0;
    private static final int PUSH_MODE = 1;
    private static final int FREEZE_MODE = 2;
    private static final int COLLAPSE_MODE = 3;

    public TrickRoomItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = new ItemStack(this);
        NbtCompound nbt = TrickRoomNbt.getOrCreate(stack);
        nbt.putInt(ABILITY_MODE_KEY, SWAP_MODE);
        nbt.putFloat(MULTIPLIER_KEY, 1);
        TrickRoomNbt.set(stack, nbt);
        return stack;
    }

    @Override
    public void onCraftByPlayer(ItemStack stack, PlayerEntity player) {
        super.onCraftByPlayer(stack, player);
        NbtCompound nbt = TrickRoomNbt.getOrCreate(stack);
        nbt.putBoolean(TEMPORARY_KEY, true);
        TrickRoomNbt.set(stack, nbt);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return getDefaultStack();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent,
                              Consumer<Text> tooltip, TooltipType type) {
        Text mode = switch (getAbilityMode(stack)) {
            case PUSH_MODE -> Text.literal("[Push]").formatted(Formatting.AQUA);
            case FREEZE_MODE -> Text.literal("[Freeze]").formatted(Formatting.DARK_PURPLE);
            default -> Text.literal("[Swap]").formatted(Formatting.GOLD);
        };
        tooltip.accept(Text.translatable("tooltip.trickroom.trick_room").formatted(Formatting.GRAY).append(mode));
        if (isTemporary(stack)) {
            tooltip.accept(Text.literal("Fragile").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld serverWorld)) return ActionResult.SUCCESS;
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            toggleRoom(world, user, stack);
            return ActionResult.SUCCESS;
        }


        TrickRoomEntity room = findBoundRoom(serverWorld, stack);
        if (room == null) {
            clearBoundRoom(stack);
            return ActionResult.FAIL;
        }
        boolean activated = activateMode(serverWorld, user, room, getAbilityMode(stack));
        if (activated && !user.getAbilities().creativeMode) {
            user.getItemCooldownManager().set(stack, Math.round(300 * getSizeMultiplier(stack)));
        }
        return activated ? ActionResult.SUCCESS_SERVER : ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResult.PASS;
        }
        if (player.isSneaking()) {
            toggleRoom(context.getWorld(), player, context.getStack());
            return ActionResult.SUCCESS;
        }
        return use(context.getWorld(), player, context.getHand());
    }

    private boolean activateMode(ServerWorld world, PlayerEntity user, TrickRoomEntity room, int mode) {
        return switch (mode) {
            case PUSH_MODE -> pushEntities(world, user, room);
            case FREEZE_MODE -> freezePlayers(world, user, room);
            default -> swapWithRandomLivingEntity(world, user, room);
        };
    }
    private boolean swapWithRandomLivingEntity(ServerWorld world, PlayerEntity user, TrickRoomEntity room) {
        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                room.getRoomBounds().contract(1.0D),
                entity -> entity.isAlive() && entity != user
        );
        if (targets.isEmpty()) {
            return false;
        }

        LivingEntity target = targets.get(world.random.nextInt(targets.size()));
        double userX = user.getX();
        double userY = user.getY();
        double userZ = user.getZ();
        user.requestTeleport(target.getX(), target.getY(), target.getZ());
        target.requestTeleport(userX, userY, userZ);
        return true;
    }

    private boolean pushEntities(ServerWorld world, PlayerEntity user, TrickRoomEntity room) {
        List<Entity> entities = world.getOtherEntities(user, room.getRoomBounds(), Entity::isAlive);
        if (entities.isEmpty()) {
            return false;
        }
        for (Entity entity : entities) {
            entity.setVelocity(user.getRotationVector().multiply(5.0D));
            entity.velocityDirty = true;
        }
        return true;
    }

    private boolean freezePlayers(ServerWorld world, PlayerEntity user, TrickRoomEntity room) {
        List<PlayerEntity> targets = world.getEntitiesByClass(
                PlayerEntity.class,
                room.getRoomBounds(),
                LivingEntity::isAlive
        );
        if (targets.isEmpty()) {
            return false;
        }
        for (PlayerEntity target : targets) {
            target.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IMMOBILIZED, 140));
            target.getEntityWorld().playSound(null, target.getBlockPos(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.PLAYERS);
        }
        return true;
    }

    private void toggleRoom(World world, PlayerEntity player, ItemStack stack) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (discardBoundRoom(serverWorld, stack)) {
            if (!player.getAbilities().creativeMode) {
                player.getItemCooldownManager().set(stack, Math.round(6000 * getSizeMultiplier(stack)));
                if (isTemporary(stack)) {
                    serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                }

            }
            return;
        }
        TrickRoomEntity room = TrickRoomEntity.create(serverWorld, player.getBlockPos());
        if (serverWorld.spawnEntity(room)) {
            NbtCompound nbt = TrickRoomNbt.getOrCreate(stack);
            TrickRoomNbt.putUuid(nbt, ROOM_UUID_KEY, room.getUuid());
            TrickRoomNbt.set(stack, nbt);
            serverWorld.playSound(null, room.getBlockPos(), ModSounds.TRICK_ROOM_SPAWN, SoundCategory.PLAYERS, 7.0F, 1.0F);
            TrickRoomFlashPacket.sendToNearby(serverWorld, player);
            room.unaffected = stack.get(DataComponentTypeInit.UNAFFECTED_PLAYERS);
        }

        if (!player.getAbilities().creativeMode) {
            player.getItemCooldownManager().set(stack, 20);
        }
    }

    public static boolean discardBoundRoom(World world, ItemStack stack) {
        NbtCompound nbt = TrickRoomNbt.get(stack);
        if (nbt == null || !TrickRoomNbt.containsUuid(nbt, ROOM_UUID_KEY)) {
            return false;
        }

        UUID roomUuid = TrickRoomNbt.getUuid(nbt, ROOM_UUID_KEY);
        if (world instanceof ServerWorld serverWorld) {
            MinecraftServer server = serverWorld.getServer();
            for (ServerWorld candidateWorld : server.getWorlds()) {
                Entity entity = candidateWorld.getEntity(roomUuid);
                if (entity instanceof TrickRoomEntity room) {
                    room.beginFadeOut();
                    if (isTemporary(stack)) {
                        stack.decrement(1);
                    }
                    serverWorld.playSound(null, room.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(), SoundCategory.PLAYERS, 7.0F, 1.0F);
                    clearBoundRoom(stack);
                    return true;
                }
            }
        }

        clearBoundRoom(stack);
        return false;
    }

    private static TrickRoomEntity findBoundRoom(ServerWorld world, ItemStack stack) {
        NbtCompound nbt = TrickRoomNbt.get(stack);
        if (nbt == null || !TrickRoomNbt.containsUuid(nbt, ROOM_UUID_KEY)) {
            return null;
        }
        UUID roomUuid = TrickRoomNbt.getUuid(nbt, ROOM_UUID_KEY);
        MinecraftServer server = world.getServer();
        for (ServerWorld candidateWorld : server.getWorlds()) {
            Entity entity = candidateWorld.getEntity(roomUuid);
            if (entity instanceof TrickRoomEntity room && !room.isFadingOut()) {
                return room;
            }
        }
        return null;
    }

    private static void clearBoundRoom(ItemStack stack) {
        NbtCompound nbt = TrickRoomNbt.get(stack);
        if (nbt == null) {
            return;
        }
        nbt.remove(ROOM_UUID_KEY);
        TrickRoomNbt.set(stack, nbt);
    }

    private static int getAbilityMode(ItemStack stack) {
        NbtCompound nbt = TrickRoomNbt.get(stack);
        return nbt == null ? SWAP_MODE : nbt.getInt(ABILITY_MODE_KEY, SWAP_MODE);
    }
    private static float getSizeMultiplier(ItemStack stack) {
        NbtCompound nbt = TrickRoomNbt.get(stack);
        return nbt == null ? 1 : nbt.getFloat(MULTIPLIER_KEY, 1);
    }

    private static boolean isTemporary(ItemStack stack) {
        NbtCompound nbt = TrickRoomNbt.get(stack);
        return nbt != null && nbt.getBoolean(TEMPORARY_KEY, false);
    }
}
