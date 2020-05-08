package me.uquark.barrymoreminecraftbinding.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;

public class SpeechRecognitionHud extends DrawableHelper {
    public enum Mode {
        Listening,
        Recognizing,
        Result,
        None
    }

    private Mode mode = Mode.None;
    private int frame;
    private String message;
    private Thread delayThread;

    private final String[] listeningColon = new String[]{
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
        frame++;
        switch (mode) {
            case Listening:
            case Recognizing:
                drawListeningColons(client);
                break;
            case Result:
                drawResult(client);
                break;
            case None:
                frame = 0;
                break;
        }
    }

    private void drawListeningColons(MinecraftClient client) {
        drawString(
            client.textRenderer,
            listeningColon[frame % listeningColon.length] + listeningColon[(frame + 4) % listeningColon.length] + listeningColon[(frame + 8) % listeningColon.length],
            5,
            5,
            0xFFFFFF
        );
    }

    private void drawResult(MinecraftClient client) {
        drawString(
            client.textRenderer,
                message,
            5,
            5,
            0xFFFFFF
        );
    }

    public void listening() {
        interruptDelayThread();
        mode = Mode.Listening;
    }

    public void recognizing() {
        interruptDelayThread();
        mode = Mode.Recognizing;
    }

    public void result(String message) {
        interruptDelayThread();
        this.message = message;
        mode = Mode.Result;
    }

    public void interruptDelayThread() {
        if (delayThread != null && delayThread.isAlive())
            delayThread.interrupt();
    }

    public void resetAfter(long delay) {
        delayThread = new Thread(() -> {
            try {
                Thread.sleep(delay);
                this.message = "";
                mode = Mode.None;
            } catch (InterruptedException ignored) {}
        });
        delayThread.start();
    }
}
