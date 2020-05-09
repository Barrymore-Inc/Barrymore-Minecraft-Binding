package me.uquark.barrymoreminecraftbinding;

import io.netty.buffer.Unpooled;
import me.uquark.barrymoreminecraftbinding.audio.FLAC;
import me.uquark.barrymoreminecraftbinding.audio.Recorder;
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

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BarrymoreMinecraftBindingClient implements ClientModInitializer, ClientTickCallback {
    public static List<SpeechRecognitionHud> huds = new ArrayList<>();

    private final Logger LOGGER = LogManager.getLogger();
    private final SpeechRecognitionHud speechRecognitionHud = new SpeechRecognitionHud();

    private FabricKeyBinding keyBinding;
    private Recorder recorder;
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

        recorder = new Recorder();
        huds.add(speechRecognitionHud);

        ClientSidePacketRegistry.INSTANCE.register(BarrymoreMinecraftBinding.RECOGNITION_CONTEXT_PACKET_ID, (packetContext, packetByteBuf) -> {
            while (packetByteBuf.isReadable())
                phrases.add(packetByteBuf.readString());
        });
    }


    private SpeechClient.RecognitionRequest.RecognitionConfig.SpeechContext getContext() {
        SpeechClient.RecognitionRequest.RecognitionConfig.SpeechContext context = new SpeechClient.RecognitionRequest.RecognitionConfig.SpeechContext();
        context.phrases = phrases.toArray(new String[0]);
        return context;
    }

    private void recognize(byte[] audio) {
        new Thread(() -> {
            speechRecognitionHud.recognizing();
            try {
                SpeechClient.RecognitionRequest request = new SpeechClient.RecognitionRequest();
                request.config.sampleRateHertz = (int) recorder.format.getSampleRate();
                request.config.audioChannelCount = recorder.format.getChannels();
                request.config.encoding = SpeechClient.RecognitionRequest.RecognitionConfig.AudioEncoding.FLAC;
                request.config.model = "command_and_search";
                request.config.useEnhanced = true;
                request.config.languageCode = "ru-RU";
                request.config.speechContexts = new SpeechClient.RecognitionRequest.RecognitionConfig.SpeechContext[] {getContext()};

                request.audio.content = Base64.getEncoder().encodeToString(FLAC.encode(recorder.format, audio));

                SpeechClient.RecognitionResponse response = SpeechClient.recognizeRaw(request);
                if (response == null || response.results == null || response.results.length == 0) {
                    speechRecognitionHud.resetAfter(0);
                    return;
                }
                String message = response.results[0].alternatives[0].transcript;
                sendMessageToServer(message);
                speechRecognitionHud.result(message);
                speechRecognitionHud.resetAfter(3000);
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
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
        if (keyBinding.isPressed() && !recorder.isRecording())
            startRecording();
        if (!keyBinding.isPressed() && recorder.isRecording())
            recorder.stopRecording();
    }

    private void startRecording() {
        speechRecognitionHud.listening();
        recorder.startRecording(this::recognize);
    }
}
