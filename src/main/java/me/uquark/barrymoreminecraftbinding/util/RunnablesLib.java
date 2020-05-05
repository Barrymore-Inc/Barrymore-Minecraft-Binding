package me.uquark.barrymoreminecraftbinding.util;

import net.minecraft.block.Blocks;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RunnablesLib {
    public static Runnable removeBlock(World world, BlockPos pos) {
        return () -> world.removeBlock(pos, false);
    }

    public static Runnable setRedstoneBlock(World world, BlockPos pos) {
        return () -> world.setBlockState(pos, Blocks.REDSTONE_BLOCK.getDefaultState());
    }

    public static Runnable skipTick() {
        return () -> {};
    }

    public static Runnable chatMessage(PlayerManager playerManager, String message) {
        return () -> playerManager.sendToAll(new LiteralText(message));
    }
}
