package me.uquark.barrymoreminecraftbinding.mixin;

import me.uquark.barrymoreminecraftbinding.BarrymoreMinecraftBinding;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onChatMessage", at = @At("HEAD"))
    public void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo info) {
        if (!Thread.currentThread().getName().equals("Server thread"))
            return;
        BarrymoreMinecraftBinding.instance.onPlayerMessage(player, packet.getChatMessage());
    }
}
