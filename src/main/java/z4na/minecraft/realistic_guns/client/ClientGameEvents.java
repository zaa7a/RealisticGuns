package z4na.minecraft.realistic_guns.client;

import z4na.minecraft.realistic_guns.RealisticGuns;
import z4na.minecraft.realistic_guns.item.GunItem;
import z4na.minecraft.realistic_guns.network.InspectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * GAMEバス用イベント (キー入力検知)
 * NeoForge 1.21系でGAMEバスを指定する場合は isBus=true の代わりに
 * NeoForge.EVENT_BUS に手動登録するか、以下のように別クラスに分離して
 * RealisticGuns コンストラクタで NeoForge.EVENT_BUS.register() する
 */
@EventBusSubscriber(modid = RealisticGuns.MODID, value = Dist.CLIENT)
public class ClientGameEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (ClientEvents.INSPECT_KEY.consumeClick()) {
            ItemStack mainHand = mc.player.getMainHandItem();
            if (mainHand.getItem() instanceof GunItem gunItem) {
                PacketDistributor.sendToServer(new InspectPacket(0));
                gunItem.triggerInspect(mc.player, mainHand);
            }
        }
    }
}