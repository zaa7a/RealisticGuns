package z4na.minecraft.realistic_guns.init;

import z4na.minecraft.realistic_guns.RealisticGuns;
import z4na.minecraft.realistic_guns.item.GunItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * アイテム登録
 * GunItem は1種類だけ登録し、NBTで銃IDを切り替える設計
 */
public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(RealisticGuns.MODID);

    public static final DeferredItem<GunItem> GUN =
            ITEMS.register("gun", () -> new GunItem(
                    new Item.Properties()
                            .stacksTo(1)
            ));
}