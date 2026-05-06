package z4na.minecraft.realistic_guns.init;

import z4na.minecraft.realistic_guns.RealisticGuns;
import z4na.minecraft.realistic_guns.registry.GunRegistry;
import z4na.minecraft.realistic_guns.item.GunItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RealisticGuns.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GUNS_TAB =
            TABS.register("guns_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.realistic_guns.guns"))
                            .icon(ModItems.GUN::toStack)
                            .displayItems((params, output) -> {
                                GunRegistry.getAllGuns().forEach(def ->
                                        output.accept(GunItem.createStack(def.id))
                                );
                                if (GunRegistry.getAllGuns().isEmpty()) {
                                    output.accept(ModItems.GUN.get());
                                }
                            })
                            .build()
            );
}