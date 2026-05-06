package z4na.minecraft.realistic_guns.client;

import com.mojang.blaze3d.platform.InputConstants;
import z4na.minecraft.realistic_guns.RealisticGuns;
import z4na.minecraft.realistic_guns.item.GunItem;
import z4na.minecraft.realistic_guns.network.InspectPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * MODバス用イベント (キー登録)
 * NeoForge 1.21系: @EventBusSubscriber のデフォルトは MOD バス
 * → bus= 指定不要
 */
@EventBusSubscriber(modid = RealisticGuns.MODID, value = Dist.CLIENT)
public class ClientEvents {

    public static final KeyMapping INSPECT_KEY = new KeyMapping(
            "key.realistic_guns.inspect",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.realistic_guns"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(INSPECT_KEY);
    }
}