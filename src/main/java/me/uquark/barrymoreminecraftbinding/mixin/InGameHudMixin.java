package me.uquark.barrymoreminecraftbinding.mixin;

import me.uquark.barrymoreminecraftbinding.BarrymoreMinecraftBindingClient;
import me.uquark.barrymoreminecraftbinding.gui.SpeechRecognitionHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
    @Shadow
    public MinecraftClient client;

    @Inject(method = "render", at = @At("RETURN"))
    public void render(float tickDelta, CallbackInfo info) {
        for (SpeechRecognitionHud hud : BarrymoreMinecraftBindingClient.huds)
            hud.draw(client);
    }
}
