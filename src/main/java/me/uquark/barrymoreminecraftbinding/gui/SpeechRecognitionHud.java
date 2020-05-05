package me.uquark.barrymoreminecraftbinding.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;

public class SpeechRecognitionHud extends DrawableHelper {
    private int animationFrame;
    private boolean recognized;
    private boolean doDrawing;
    private String recognizedText;

    public void draw(MinecraftClient client) {
        if (!doDrawing) {
            recognized = false;
            animationFrame = 0;
            return;
        }
        drawString(
            client.textRenderer,
            getString(),
            5,
            5,
            0xFFFFFF
        );
        animationFrame++;
    }

    private String getString() {
        final String[] animationStrings = new String[]{
            "▁",
            "▂",
            "▃",
            "▄",
            "▅",
            "▆",
            "▇",
            "█",
            "▇",
            "▆",
            "▅",
            "▄",
            "▃",
            "▁"
        };

        if (recognized)
            return recognizedText;
        else
            return animationStrings[animationFrame % animationStrings.length] +
                    animationStrings[(animationFrame + 4) % animationStrings.length] +
                    animationStrings[(animationFrame + 8) % animationStrings.length];
    }

    public void startAnimation() {
        doDrawing = true;
    }

    public void recognized(String text) {
        recognized = true;
        recognizedText = text;
    }

    public void stopAnimation() {
        doDrawing = false;
    }
}
