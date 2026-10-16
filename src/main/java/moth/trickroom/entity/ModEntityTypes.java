package moth.trickroom.entity;

import moth.trickroom.TrickRoomMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class ModEntityTypes {
    private static final RegistryKey<EntityType<?>> TRICK_ROOM_KEY =
            RegistryKey.of(RegistryKeys.ENTITY_TYPE, TrickRoomMod.id("trick_room"));

    public static final EntityType<TrickRoomEntity> TRICK_ROOM = Registry.register(
            Registries.ENTITY_TYPE,
            TRICK_ROOM_KEY,
            EntityType.Builder.create(TrickRoomEntity::new, SpawnGroup.MISC)
                    .dimensions(1.0F, 1.0F)
                    .makeFireImmune()
                    .trackingTickInterval(1)
                    .maxTrackingRange(192)
                    .build(TRICK_ROOM_KEY)
    );

    private ModEntityTypes() {
    }

    public static void register() {
    }
}
