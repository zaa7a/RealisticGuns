package z4na.minecraft.realistic_guns.network;

import z4na.minecraft.realistic_guns.RealisticGuns;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ShootPacket(int hand) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ShootPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(RealisticGuns.MODID, "shoot")
            );

    public static final StreamCodec<FriendlyByteBuf, ShootPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeInt(pkt.hand),
                    buf -> new ShootPacket(buf.readInt())
            );

    @Override
    public CustomPacketPayload.Type<ShootPacket> type() {
        return TYPE;
    }

    public static void handle(ShootPacket packet, IPayloadContext ctx) {
        // サーバー側での射撃確認はGunItem.useで処理済み
    }
}