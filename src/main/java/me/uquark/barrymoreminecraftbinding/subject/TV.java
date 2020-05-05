package me.uquark.barrymoreminecraftbinding.subject;

import me.uquark.barrymore.api.Action;
import me.uquark.barrymoreminecraftbinding.util.RunnablesLib;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Queue;

public class TV {
    public static void buildAndPutInQueue(World world, Action action, String address, String[] parameters, Queue<Runnable> queue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String[] parts = address.split(":");
        BlockPos pos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        Method method = TV.class.getDeclaredMethod(action.name, World.class, BlockPos.class, String[].class, Queue.class);
        method.invoke(null, world, pos, parameters, queue);
    }

    private static void turnOn(World world, BlockPos pos, String[] parameters, Queue<Runnable> queue) {
        queue.add(RunnablesLib.setRedstoneBlock(world, pos));
    }

    private static void turnOff(World world, BlockPos pos, String[] parameters, Queue<Runnable> queue) {
        queue.add(RunnablesLib.removeBlock(world, pos));
    }
}
