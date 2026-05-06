package z4na.minecraft.realistic_guns;

import com.mojang.logging.LogUtils;
import z4na.minecraft.realistic_guns.init.ModItems;
import z4na.minecraft.realistic_guns.init.ModCreativeTabs;
import z4na.minecraft.realistic_guns.network.ModNetworking;
import z4na.minecraft.realistic_guns.pack.GunPackLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.slf4j.Logger;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import java.util.Optional;

import java.io.File;
import java.util.Optional;

@Mod(RealisticGuns.MODID)
public class RealisticGuns {

    public static final String MODID = "realistic_guns";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static File GUNPACKS_DIR = new File("rlguns");

    public RealisticGuns(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addPackFinders);
        ModNetworking.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        if (!GUNPACKS_DIR.exists()) {
            GUNPACKS_DIR.mkdirs();
            LOGGER.info("[RealisticGuns] Created gunpacks directory: {}", GUNPACKS_DIR.getAbsolutePath());
        }
    }

    private void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        File[] packs = GUNPACKS_DIR.listFiles(File::isDirectory);
        if (packs == null) return;

        for (File packDir : packs) {
            String packId = "rlguns/" + packDir.getName();
            LOGGER.info("[RealisticGuns] Registering resource pack: {}", packId);

            // フォルダ用の ResourcesSupplier を実装
            Pack.ResourcesSupplier supplier = location ->
                    new net.minecraft.server.packs.PathPackResources(
                            packId, packDir.toPath(), false
                    );

            Pack pack = Pack.readMetaAndCreate(
                    new PackLocationInfo(
                            packId,
                            Component.literal(packDir.getName()),
                            PackSource.DEFAULT,
                            Optional.empty()
                    ),
                    supplier,
                    PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(true, Pack.Position.TOP, false)
            );

            if (pack != null) {
                event.addRepositorySource(consumer -> consumer.accept(pack));
            } else {
                LOGGER.warn("[RealisticGuns] Failed to read pack metadata for: {}", packDir.getName());
            }
        }
    }

    @EventBusSubscriber(modid = MODID)
    public static class GameEvents {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            GunPackLoader.loadAll();
        }
    }
}