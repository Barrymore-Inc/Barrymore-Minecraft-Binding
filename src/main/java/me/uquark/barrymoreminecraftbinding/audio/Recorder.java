package me.uquark.barrymoreminecraftbinding.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;

public class Recorder {
    public final AudioFormat format = new AudioFormat(8000, 8, 1, true, false);
    private TargetDataLine microphone;
    private volatile boolean recording = false;
    private ByteArrayOutputStream audio;

    public interface RecordingOverCallback {
        void onRecordingOver(byte[] audio);
    }

    public Recorder() {
        try {
            microphone = AudioSystem.getTargetDataLine(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void startRecording(RecordingOverCallback callback) {
        recording = true;
        new Thread(() -> {
            try {
                byte[] buffer = new byte[microphone.getBufferSize() / 5];
                int bytesRead;
                audio = new ByteArrayOutputStream();
                microphone.open(format);
                microphone.start();

                while (recording) {
                    bytesRead = microphone.read(buffer, 0, buffer.length);
                    audio.write(buffer, 0, bytesRead);
                }

                microphone.stop();

                while ((bytesRead = microphone.read(buffer, 0, buffer.length)) > 0)
                    audio.write(buffer, 0, bytesRead);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } finally {
                microphone.close();
                callback.onRecordingOver(audio.toByteArray());
            }
        }).start();
    }

    public void stopRecording() {
        recording = false;
    }

    public boolean isRecording() {
        return recording;
    }
}
