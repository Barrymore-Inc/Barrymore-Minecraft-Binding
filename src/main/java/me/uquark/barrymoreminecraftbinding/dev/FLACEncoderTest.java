package me.uquark.barrymoreminecraftbinding.dev;

import me.uquark.barrymoreminecraftbinding.audio.FLAC;
import me.uquark.barrymoreminecraftbinding.audio.Recorder;
import org.apache.commons.io.FileUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FLACEncoderTest implements Recorder.RecordingOverCallback {
    public final Recorder recorder = new Recorder();

    public static void main(String[] args) throws InterruptedException {
        FLACEncoderTest flacEncoderTest = new FLACEncoderTest();
        flacEncoderTest.run();
    }

    public void run() throws InterruptedException {
        System.out.println("RECORDING");
        System.out.flush();
        recorder.startRecording(this);
        Thread.sleep(30000);
        recorder.stopRecording();
        System.out.println("DONE");
        System.out.flush();
    }

    @Override
    public void onRecordingOver(byte[] audio) {
        try {
            byte[] flac = FLAC.encode(recorder.format, audio);
            FileUtils.writeByteArrayToFile(new File("debug.raw"), audio);
            FileUtils.writeByteArrayToFile(new File("debug.flac"), flac);

            System.out.printf("%d\t%d\n", audio.length, flac.length);
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
