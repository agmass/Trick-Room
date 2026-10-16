package moth.trickroom.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;
import java.util.UUID;

public final class TrickRoomNbt {
    private TrickRoomNbt() {
    }

    public static NbtCompound get(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component == null || component.isEmpty() ? null : component.copyNbt();
    }

    public static NbtCompound getOrCreate(ItemStack stack) {
        NbtCompound nbt = get(stack);
        return nbt == null ? new NbtCompound() : nbt;
    }

    public static void set(ItemStack stack, NbtCompound nbt) {
        if (nbt == null || nbt.isEmpty()) {
            stack.remove(DataComponentTypes.CUSTOM_DATA);
            return;
        }
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static void putUuid(NbtCompound nbt, String key, UUID uuid) {
        nbt.putString(key, uuid.toString());
    }

    public static boolean containsUuid(NbtCompound nbt, String key) {
        return getUuidOptional(nbt, key).isPresent();
    }

    public static UUID getUuid(NbtCompound nbt, String key) {
        return getUuidOptional(nbt, key).orElse(new UUID(0L, 0L));
    }

    public static Optional<UUID> getUuidOptional(NbtCompound nbt, String key) {
        try {
            return Optional.of(UUID.fromString(nbt.getString(key, "")));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
