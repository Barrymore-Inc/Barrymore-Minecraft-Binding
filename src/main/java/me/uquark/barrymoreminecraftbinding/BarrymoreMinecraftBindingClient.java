package me.uquark.barrymoreminecraftbinding;

import io.netty.buffer.Unpooled;
import me.uquark.barrymoreminecraftbinding.googlecloud.SpeechClient;
import me.uquark.barrymoreminecraftbinding.gui.SpeechRecognitionHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BarrymoreMinecraftBindingClient implements ClientModInitializer, ClientTickCallback {
    public static List<SpeechRecognitionHud> huds = new ArrayList<>();

    private final Logger LOGGER = LogManager.getLogger();
    private final AudioFormat format = new AudioFormat(8000, 16, 1, true, false);
    private final SpeechRecognitionHud speechRecognitionHud = new SpeechRecognitionHud();

    private FabricKeyBinding keyBinding;
    private TargetDataLine microphone;
    private ByteArrayOutputStream out;
    private volatile boolean recording = false;
    private final List<String> phrases = new ArrayList<>();

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
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        huds.add(speechRecognitionHud);

        ClientSidePacketRegistry.INSTANCE.register(BarrymoreMinecraftBinding.RECOGNITION_CONTEXT_PACKET_ID, (packetContext, packetByteBuf) -> {
            while (packetByteBuf.isReadable())
                phrases.add(packetByteBuf.readString());
        });
    }

    private void startRecording() {
        recording = true;
        new Thread(() -> {
            try {
                speechRecognitionHud.startRender();
                byte[] buffer = new byte[microphone.getBufferSize() / 5];
                int bytesRead;
                out = new ByteArrayOutputStream();
                microphone.open(format);
                microphone.start();
                while (recording) {
                    bytesRead = microphone.read(buffer, 0, buffer.length);
                    out.write(buffer, 0, bytesRead);
                }
                microphone.stop();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } finally {
                microphone.close();
            }
        }).start();
    }

    private void stopRecording() {
        recording = false;
        recognize();
    }

    private SpeechClient.RecognitionRequest.RecognitionConfig.SpeechContext getContext() {
        SpeechClient.RecognitionRequest.RecognitionConfig.SpeechContext context = new SpeechClient.RecognitionRequest.RecognitionConfig.SpeechContext();
        context.phrases = phrases.toArray(new String[0]);
        return context;
    }

    private void recognize() {
        new Thread(() -> {
            try {
                SpeechClient.RecognitionRequest request = new SpeechClient.RecognitionRequest();
                request.config.sampleRateHertz = (int) format.getSampleRate();
                request.config.audioChannelCount = format.getChannels();
                request.config.encoding = SpeechClient.RecognitionRequest.RecognitionConfig.AudioEncoding.LINEAR16;
                request.config.model = "command_and_search";
                request.config.useEnhanced = true;
                request.config.languageCode = "ru-RU";
                request.config.speechContexts = new SpeechClient.RecognitionRequest.RecognitionConfig.SpeechContext[] {getContext()};

                request.audio.content = Base64.getEncoder().encodeToString(out.toByteArray());

                SpeechClient.RecognitionResponse response = SpeechClient.recognizeRaw(request);
                if (response == null || response.results == null || response.results.length == 0)
                    return;
                String message = response.results[0].alternatives[0].transcript;
                speechRecognitionHud.recognized(message);
                sendMessageToServer(message);
                Thread.sleep(3000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                speechRecognitionHud.stopRender();
            }
        }).start();
    }

    private void sendMessageToServer(String message) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        packetByteBuf.writeString(message);
        ClientSidePacketRegistry.INSTANCE.sendToServer(BarrymoreMinecraftBinding.SPEECH_RECOGNIZED_PACKET_ID, packetByteBuf);
    }

    @Override
    public void tick(MinecraftClient minecraftClient) {
        if (keyBinding.isPressed() && !recording)
            startRecording();
        if (!keyBinding.isPressed() && recording)
            stopRecording();
    }
}
