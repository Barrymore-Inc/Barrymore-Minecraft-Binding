package me.uquark.barrymoreminecraftbinding.subject;

import me.uquark.barrymore.api.Action;
import me.uquark.barrymoreminecraftbinding.BarrymoreMinecraftBinding;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Queue;

public class Door {
    public static void buildAndPutInQueue(World world, Action action, String address, String[] parameters, Queue<Runnable> queue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String[] parts = address.split(":");
        BlockPos pos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        Method method = Door.class.getDeclaredMethod(action.name, World.class, BlockPos.class, String[].class, Queue.class);
        method.invoke(null, world, pos, parameters, queue);
    }

    private static void open(World world, BlockPos pos, String[] parameters, Queue<Runnable> queue) {
        queue.add(RunnableLib.removeBlock(world, pos));
        queue.add(RunnableLib.skipTick());
        queue.add(RunnableLib.setRedstoneBlock(world, pos));
    }

    private static void close(World world, BlockPos pos, String[] parameters, Queue<Runnable> queue) {
        queue.add(RunnableLib.removeBlock(world, pos));
        queue.add(RunnableLib.skipTick());
        queue.add(RunnableLib.setRedstoneBlock(world, pos));
    }
}
