package me.uquark.barrymoreminecraftbinding.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;

import java.util.Random;

public class SpeechRecognitionHud extends DrawableHelper {
    private final Random random = new Random();

    private int animationFrame;
    private int step1, step2;
    private boolean recognized;
    private boolean doRender;
    private String recognizedText;
    private final String[] animationStrings = new String[]{
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

    public void draw(MinecraftClient client) {
        if (!doRender) {
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
        if (recognized)
            return recognizedText;
        else
            return animationStrings[animationFrame % animationStrings.length] +
                    animationStrings[(animationFrame + step1) % animationStrings.length] +
                    animationStrings[(animationFrame + step2) % animationStrings.length];
    }

    public void startRender() {
        doRender = true;
        step1 = random.nextInt(animationStrings.length);
        step2 = random.nextInt(animationStrings.length);
    }

    public void recognized(String text) {
        recognized = true;
        recognizedText = text;
    }

    public void stopRender() {
        doRender = false;
    }
}
