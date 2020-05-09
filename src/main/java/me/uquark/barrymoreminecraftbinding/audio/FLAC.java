package me.uquark.barrymoreminecraftbinding.audio;

import net.sourceforge.javaflacencoder.FLACEncoder;
import net.sourceforge.javaflacencoder.FLACOutputStream;
import net.sourceforge.javaflacencoder.FLACStreamOutputStream;
import net.sourceforge.javaflacencoder.StreamConfiguration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FLAC {

    public static int[] bytesToInts(AudioFormat format, byte[] bytes) {
        int[] result;

        if (bytes.length % format.getFrameSize() == 0)
            result = new int[bytes.length / format.getFrameSize()];
        else
            result = new int[bytes.length / format.getFrameSize() + 1];

        for (int i = 0; i < bytes.length; i++) {
            int index = i / format.getFrameSize();
            int shift = (i % format.getFrameSize()) * 8;

            if (shift == 0 && format.getFrameSize() > 1)
                result[index] |= bytes[i] & 0xFF;
            else
                result[index] |= bytes[i] << shift;
        }

        return result;
    }

    public static byte[] encode(AudioFormat format, byte[] audio) throws IOException, UnsupportedAudioFileException {
        StreamConfiguration streamConfiguration = new StreamConfiguration();
        streamConfiguration.setSampleRate((int) format.getSampleRate());
        streamConfiguration.setChannelCount(format.getChannels());
        streamConfiguration.setBitsPerSample(format.getSampleSizeInBits());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FLACOutputStream flacOutputStream = new FLACStreamOutputStream(outputStream);

        FLACEncoder flacEncoder = new FLACEncoder();
        flacEncoder.setStreamConfiguration(streamConfiguration);
        flacEncoder.setOutputStream(flacOutputStream);

        int[] samples = bytesToInts(format, audio);

        flacEncoder.addSamples(samples, samples.length);

        flacEncoder.openFLACStream();
        flacEncoder.encodeSamples(samples.length, false);
        flacEncoder.encodeSamples(flacEncoder.samplesAvailableToEncode(), true);

        return outputStream.toByteArray();
    }
}
