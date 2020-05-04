package me.uquark.barrymoreminecraftbinding;

import me.uquark.barrymore.api.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.Queue;

public class BarrymoreMinecraftBinding implements ModInitializer, BarrymoreBinding, ServerStartCallback, ServerTickCallback {
    public static BarrymoreMinecraftBinding instance;

    private final Queue<Runnable> runs = new LinkedList<>();
    private BarrymoreBrain brain;
    private MinecraftServer server;

    @Override
    public void onInitialize() {
        instance = this;
        ServerStartCallback.EVENT.register(this);
        ServerTickCallback.EVENT.register(this);
    }

    public void processOrder(Order order, ServerPlayerEntity player) throws RemoteException {
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

    public void onChatMessage(ServerPlayerEntity player, String message) {
        final String KEYWORD = "бэрримор";
        BlockPos pos = player.getBlockPos();
        Coords coords = new Coords(pos.getX(), pos.getY(), pos.getZ());
        message = message.toLowerCase();
        if (message.contains(KEYWORD))
            try {
                processOrder(brain.processUserMessage(coords, message), player);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void onStartServer(MinecraftServer minecraftServer) {
        try {
            Registry registry = LocateRegistry.getRegistry(BarrymoreConfig.BARRYMORE_RMI_REGISTRY_PORT);
            brain = (BarrymoreBrain) registry.lookup("BarrymoreBrain");
            server = minecraftServer;
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
