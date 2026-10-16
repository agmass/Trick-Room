package moth.trickroom.network;

import moth.trickroom.TrickRoomMod;
import moth.trickroom.item.ModItems;
import moth.trickroom.item.TrickRoomItem;
import moth.trickroom.util.TrickRoomNbt;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public record TrickRoomModePacket() implements CustomPayload {
    private static final Identifier PACKET_ID = TrickRoomMod.id("trick_room_mode");
    public static final Id<TrickRoomModePacket> ID = new Id<>(PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, TrickRoomModePacket> CODEC = CustomPayload.codecOf(
            (payload, buf) -> {
            },
            buf -> new TrickRoomModePacket()
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void receive(TrickRoomModePacket packet, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayerEntity player = context.player();
            ItemStack stack = player.getMainHandStack();
            if (!stack.isOf(ModItems.TRICK_ROOM) || player.getItemCooldownManager().isCoolingDown(stack)) {
                return;
            }

            NbtCompound nbt = TrickRoomNbt.getOrCreate(stack);
            int mode = Math.floorMod(nbt.getInt(TrickRoomItem.ABILITY_MODE_KEY, 0) + 1, 3);
            nbt.putInt(TrickRoomItem.ABILITY_MODE_KEY, mode);
            TrickRoomNbt.set(stack, nbt);
            player.getEntityWorld().playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );
            if (!player.getAbilities().creativeMode) {
                player.getItemCooldownManager().set(stack, 60);
            }
        });
    }
}
