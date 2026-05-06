package z4na.minecraft.realistic_guns.network;

import z4na.minecraft.realistic_guns.RealisticGuns;
import z4na.minecraft.realistic_guns.item.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * クライアントが「残弾確認」キーを押したときにサーバーへ送るパケット
 */
public record InspectPacket(int hand) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<InspectPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(RealisticGuns.MODID, "inspect")
            );

    public static final StreamCodec<FriendlyByteBuf, InspectPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeInt(pkt.hand),
                    buf -> new InspectPacket(buf.readInt())
            );

    @Override
    public CustomPacketPayload.Type<InspectPacket> type() {
        return TYPE;
    }

    public static void handle(InspectPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            ItemStack stack = packet.hand() == 0
                    ? player.getMainHandItem()
                    : player.getOffhandItem();

            if (stack.getItem() instanceof GunItem gunItem) {
                gunItem.triggerInspect(player, stack);
            }
        });
    }
}