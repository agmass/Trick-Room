package moth.trickroom.item;

import com.mojang.serialization.Codec;
import moth.trickroom.TrickRoomMod;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.UnaryOperator;

public interface DataComponentTypeInit {
    static void init() {}
    private static <T> ComponentType<T> register(String name, @NotNull UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, TrickRoomMod.id(name), builderOperator.apply(ComponentType.builder()).build());
    }
    ComponentType<List<String>> UNAFFECTED_PLAYERS = register("unaffecteds", builder -> builder.codec(Codec.list(Codec.STRING)));
}
