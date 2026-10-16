package moth.trickroom.sound;

import moth.trickroom.TrickRoomMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

public final class ModSounds {
    public static final SoundEvent TRICK_ROOM_SPAWN = register("trick_room");

    private ModSounds() {
    }

    public static void register() {
    }

    private static SoundEvent register(String path) {
        SoundEvent event = SoundEvent.of(TrickRoomMod.id(path));
        return Registry.register(Registries.SOUND_EVENT, TrickRoomMod.id(path), event);
    }
}
