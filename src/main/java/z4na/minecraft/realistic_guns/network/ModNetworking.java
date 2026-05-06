package z4na.minecraft.realistic_guns.network;

import z4na.minecraft.realistic_guns.RealisticGuns;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(RealisticGuns.MODID);

        registrar.playToServer(
                InspectPacket.TYPE,
                InspectPacket.STREAM_CODEC,
                InspectPacket::handle
        );

        registrar.playToServer(
                ShootPacket.TYPE,
                ShootPacket.STREAM_CODEC,
                ShootPacket::handle
        );
    }
}