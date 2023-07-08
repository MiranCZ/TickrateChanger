package net.miran.tickratechanger;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.miran.tickratechanger.mixin.accessor.MinecraftAccessor;
import net.miran.tickratechanger.mixin.accessor.TimerAccessor;

import static net.miran.tickratechanger.Packets.TICKRATE_PACKET;

@Environment(EnvType.CLIENT)
public class MainClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(TICKRATE_PACKET, (player, handler, data, d) -> {
			try {
				onTickratePacket(data.readFloat());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((a, b) -> {
			try {
				onTickratePacket(20.0f);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static void onTickratePacket(float tickrate) {
		((TimerAccessor) ((MinecraftAccessor) Minecraft.getInstance()).getTimer()).setMsPerTick(1000F / tickrate);
	}
}
