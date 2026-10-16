package moth.trickroom.network;

import moth.trickroom.TrickRoomMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public record TrickRoomFlashPacket() implements CustomPayload {
    private static final Identifier PACKET_ID = TrickRoomMod.id("flash_screen");
    public static final Id<TrickRoomFlashPacket> ID = new Id<>(PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, TrickRoomFlashPacket> CODEC = CustomPayload.codecOf(
            (payload, buf) -> {
            },
            buf -> new TrickRoomFlashPacket()
    );
    private static final double FLASH_DISTANCE = 250.0D;

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void sendToNearby(ServerWorld world, Entity source) {
        double maxDistanceSquared = FLASH_DISTANCE * FLASH_DISTANCE;
        for (ServerPlayerEntity targetPlayer : world.getPlayers()) {
            if (targetPlayer.squaredDistanceTo(source) <= maxDistanceSquared
                    && ServerPlayNetworking.canSend(targetPlayer, ID)) {
                ServerPlayNetworking.send(targetPlayer, new TrickRoomFlashPacket());
            }
        }
    }
}
