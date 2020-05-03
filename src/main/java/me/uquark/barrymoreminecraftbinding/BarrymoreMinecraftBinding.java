package me.uquark.barrymoreminecraftbinding;

import me.uquark.barrymore.api.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;

public class BarrymoreMinecraftBinding implements ModInitializer, BarrymoreBinding {
    public static BarrymoreMinecraftBinding instance;

    public World world;
    private final String bindingName = "UQuark's Minecraft";
    private final Queue<Runnable> runs = new LinkedList<>();
    private BarrymoreBrain brain;

    @Override
    public void onInitialize() {
        instance = this;
        try {
            Registry registry = LocateRegistry.getRegistry(BarrymoreConfig.BARRYMORE_RMI_REGISTRY_PORT);
            brain = (BarrymoreBrain) registry.lookup("BarrymoreBrain");
            BarrymoreBinding stub = (BarrymoreBinding) UnicastRemoteObject.exportObject(this, 0);
            brain.registerBinding(stub);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processOrder(Order order) throws RemoteException {
        for (Subject subject : order.subjects) {
            try {
                Class<?> klass = Class.forName("me.uquark.barrymoreminecraftbinding.subject." + subject.klass);
                Method method = klass.getDeclaredMethod("buildAndPutInQueue", Action.class, String.class, String[].class, Queue.class);
                method.invoke(null, order.action, subject.address, order.parameters, runs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() throws RemoteException {
        return bindingName;
    }

    public void tick() {
        if (runs.size() == 0)
            return;
        runs.poll().run();
    }

    public void onChatMessage(ServerPlayerEntity player, String message) {
        world = player.world;
        final String KEYWORD = "бэрримор";
        message = message.toLowerCase();
        if (message.contains(KEYWORD))
            try {
                brain.processUserMessage(bindingName, message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

    }
}
