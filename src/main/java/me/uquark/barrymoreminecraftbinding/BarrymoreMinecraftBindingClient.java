package me.uquark.barrymoreminecraftbinding;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class BarrymoreMinecraftBindingClient implements ClientModInitializer, ClientTickCallback {
    private final Logger LOGGER = LogManager.getLogger();
    private final AudioFormat format = new AudioFormat(16000, 16, 1, true, false);

    private FabricKeyBinding keyBinding;
    private TargetDataLine microphone;
    private ByteArrayOutputStream out;
    private SpeechClient speechClient;
    private volatile boolean recording = false;

    @Override
    public void onInitializeClient() {
        final String category = "Barrymore Minecraft Binding";
        keyBinding = FabricKeyBinding.Builder.create(
            new Identifier("barrymore_minecraft_binding", "listen_microphone"),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            category
        ).build();
        KeyBindingRegistry.INSTANCE.addCategory(category);
        KeyBindingRegistry.INSTANCE.register(keyBinding);
        ClientTickCallback.EVENT.register(this);

        try {
            microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            speechClient = SpeechClient.create();
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {
        recording = true;
        new Thread(() -> {
            byte[] buffer = new byte[microphone.getBufferSize() / 5];
            int bytesRead;
            out = new ByteArrayOutputStream();
            microphone.start();
            while (recording) {
                bytesRead = microphone.read(buffer, 0, buffer.length);
                out.write(buffer, 0, buffer.length);
            }
            microphone.stop();
        }).start();
    }

    private void stopRecording() {
        recording = false;
        recognize();
    }

    private void recognize() {
        new Thread(() -> {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode("ru-RU")
                .addSpeechContexts(SpeechContext.newBuilder().build())
                .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(out.toByteArray())).build();
            RecognizeResponse response = speechClient.recognize(config, audio);

            if (response.isInitialized()) {
                List<SpeechRecognitionResult> results = response.getResultsList();
                if (!results.isEmpty())
                    System.out.println(results.get(0).getAlternatives(0).getTranscript());
            }
        }).start();
    }

    @Override
    public void tick(MinecraftClient minecraftClient) {
        if (keyBinding.isPressed() && !recording)
            startRecording();
        if (!keyBinding.isPressed() && recording)
            stopRecording();
    }
}
