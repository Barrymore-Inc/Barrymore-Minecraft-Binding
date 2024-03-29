package me.uquark.barrymoreminecraftbinding;

import io.netty.buffer.Unpooled;
import me.uquark.barrymore.api.*;
import me.uquark.barrymoreminecraftbinding.util.RunnablesLib;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BarrymoreMinecraftBinding implements ModInitializer, BarrymoreBinding, ServerStartCallback, ServerTickCallback {
    public static final Identifier SPEECH_RECOGNIZED_PACKET_ID = new Identifier("barrymore", "speech_recognized");
    public static final Identifier RECOGNITION_CONTEXT_PACKET_ID = new Identifier("barrymore", "recognition_context");

    public static BarrymoreMinecraftBinding instance;

    private final Queue<Runnable> runs = new LinkedList<>();
    private List<String> phrases;
    private BarrymoreBrain brain;
    private MinecraftServer server;

    @Override
    public void onInitialize() {
        instance = this;
        ServerStartCallback.EVENT.register(this);
        ServerTickCallback.EVENT.register(this);

        ServerSidePacketRegistry.INSTANCE.register(SPEECH_RECOGNIZED_PACKET_ID, (packetContext, packetByteBuf) -> {
            onPlayerMessage(packetContext.getPlayer(), packetByteBuf.readString(), false);
        });
    }

    public void processOrder(Order order, PlayerEntity player) throws RemoteException {
        if (order.subjects != null)
            for (Subject subject : order.subjects) {
                try {
                    Class<?> klass = Class.forName("me.uquark.barrymoreminecraftbinding.subject." + subject.klass);
                    Method method = klass.getDeclaredMethod("buildAndPutInQueue", World.class, Action.class, String.class, String[].class, Queue.class);
                    method.invoke(null, player.world, order.action, subject.address, order.parameters, runs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        if (order.response != null)
            runs.add(RunnablesLib.chatMessage(server.getPlayerManager(), order.response));
    }

    public void onPlayerMessage(PlayerEntity player, String message, boolean isChatMessage) {
        if (brain == null)
            return;
        final String KEYWORD = "бэрримор";
        BlockPos pos = player.getBlockPos();
        Coords coords = new Coords(pos.getX(), pos.getY(), pos.getZ());
        message = message.toLowerCase();
        if (!isChatMessage || message.contains(KEYWORD))
            try {
                message = message.replace(KEYWORD, "");
                processOrder(brain.processUserMessage(coords, message), player);
            } catch (RemoteException | NullPointerException e) {
                e.printStackTrace();
            }
    }

    public void onPlayerConnect(ServerPlayerEntity playerEntity) {
        if (phrases == null)
            return;
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        for (String phrase : phrases)
            packetByteBuf.writeString(phrase);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntity, RECOGNITION_CONTEXT_PACKET_ID, packetByteBuf);
    }

    @Override
    public void onStartServer(MinecraftServer minecraftServer) {
        try {
            Registry registry = LocateRegistry.getRegistry(BarrymoreConfig.BARRYMORE_RMI_REGISTRY_PORT);
            brain = (BarrymoreBrain) registry.lookup("BarrymoreBrain");
            server = minecraftServer;

            phrases = brain.getPhrases();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tick(MinecraftServer minecraftServer) {
        if (runs.size() == 0)
            return;
        runs.poll().run();
    }
}
