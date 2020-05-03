package me.uquark.barrymoreminecraftbinding;

import me.uquark.barrymore.api.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class BarrymoreMinecraftBinding implements ModInitializer, BarrymoreBinding {
    public static BarrymoreMinecraftBinding instance;

    private final Queue<Runnable> runs = new LinkedList<>();
    private BarrymoreBrain brain;
    private BarrymoreBinding stub;
    private final HashMap<Integer, ServerPlayerEntity> players = new HashMap<>();

    @Override
    public void onInitialize() {
        instance = this;
        try {
            Registry registry = LocateRegistry.getRegistry(BarrymoreConfig.BARRYMORE_RMI_REGISTRY_PORT);
            brain = (BarrymoreBrain) registry.lookup("BarrymoreBrain");
            stub = (BarrymoreBinding) UnicastRemoteObject.exportObject(this, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processOrder(Order order) throws RemoteException {
        for (Subject subject : order.subjects) {
            try {
                Class<?> klass = Class.forName("me.uquark.barrymoreminecraftbinding.subject." + subject.klass);
                Method method = klass.getDeclaredMethod("buildAndPutInQueue", World.class, Action.class, String.class, String[].class, Queue.class);
                method.invoke(null, players.get(order.userHashCode).world, order.action, subject.address, order.parameters, runs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() throws RemoteException {
        return "UQuark's Minecraft";
    }

    @Override
    public Coords getUserLocation(int userHashCode) throws RemoteException {
        BlockPos pos = players.get(userHashCode).getBlockPos();
        return new Coords(pos.getX(), pos.getY(), pos.getZ());
    }

    public void tick() {
        if (runs.size() == 0)
            return;
        runs.poll().run();
    }

    public void onChatMessage(ServerPlayerEntity player, String message) {
        players.putIfAbsent(player.hashCode(), player);
        final String KEYWORD = "бэрримор";
        message = message.toLowerCase();
        if (message.contains(KEYWORD))
            try {
                brain.processUserMessage(stub, player.hashCode(), message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

    }
}
