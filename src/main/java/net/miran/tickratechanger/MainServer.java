package net.miran.tickratechanger;

import com.mojang.brigadier.arguments.FloatArgumentType;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import static net.miran.tickratechanger.Packets.HANDSHAKE_PACKET;
import static net.miran.tickratechanger.Packets.TICKRATE_PACKET;

@Environment(EnvType.SERVER)
public class MainServer implements DedicatedServerModInitializer {

    private static MinecraftServer server;
    private static float TICKRATE = 20;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> MainServer.server = server);

        ServerPlayConnectionEvents.JOIN.register((packetListener,packetSender,minecraftServer)->{
            ServerPlayer player = packetListener.player;

            if (!ServerPlayNetworking.canSend(player,HANDSHAKE_PACKET)) {
                player.connection.disconnect(Component.literal("You need to have Tickrate Changer installed to join this server!").withStyle(ChatFormatting.RED));
                return;
            }
            sendTickratePacket(player,TICKRATE);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, dedicated) -> {
            dispatcher.register(Commands.literal("tickrate").requires(source -> source.hasPermission(4)).then(Commands.argument("ticks", FloatArgumentType.floatArg()).requires(source -> source.hasPermission(4)).executes(c -> {
                float tickrate = FloatArgumentType.getFloat(c, "ticks");
                if (tickrate < 0.1 || tickrate > 100) {
                    c.getSource().sendFailure(Component.literal("Tickrate needs to be between '0.1' and '100' (" + tickrate + " was given)").withStyle(ChatFormatting.RED));
                    return -1;
                }

                ((MinecraftServerMixin)server).setMsPerTick((int) (1000/tickrate));


                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    sendTickratePacket(player,tickrate);
                }
                TICKRATE = tickrate;


                return 1;
            })).executes(c -> {
                c.getSource().sendFailure(Component.literal("You need to provide a tickrate").withStyle(ChatFormatting.RED));

                return 1;
            }));
        });
    }

    private static void sendTickratePacket(ServerPlayer player, float tickrate) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeFloat(tickrate);

        ServerPlayNetworking.send(player, TICKRATE_PACKET, buf);
    }


}
