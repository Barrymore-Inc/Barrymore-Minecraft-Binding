package me.uquark.barrymoreminecraftbinding.subject;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RunnableLib {
    public static Runnable removeBlock(World world, BlockPos pos) {
        return () -> world.removeBlock(pos, false);
    }

    public static Runnable setRedstoneBlock(World world, BlockPos pos) {
        return () -> world.setBlockState(pos, Blocks.REDSTONE_BLOCK.getDefaultState());
    }

    public static Runnable skipTick() {
        return () -> {};
    }
}
