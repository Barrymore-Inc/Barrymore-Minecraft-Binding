package me.uquark.barrymoreminecraftbinding.mixin;

import me.uquark.barrymoreminecraftbinding.BarrymoreMinecraftBinding;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.snooper.SnooperListener;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantThreadExecutor<ServerTask> implements SnooperListener, CommandOutput, AutoCloseable, Runnable {
    public MinecraftServerMixin(String name) {
        super(name);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tick(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        BarrymoreMinecraftBinding.instance.tick();
    }
}
