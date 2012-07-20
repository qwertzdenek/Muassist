package kiv.janecekz.ma.tone;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioDevice {
    private AudioTrack track;
    private short[] buffer = new short[1024];

    public AudioDevice(int sampleFreq) {
        int minSize = AudioTrack.getMinBufferSize(sampleFreq,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleFreq,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minSize, AudioTrack.MODE_STREAM);
        track.play();
    }

    public void writeSamples(double[] samples) {
        fillBuffer(samples);
        track.write(buffer, 0, samples.length);
    }

    private void fillBuffer(double[] samples) {
        if (buffer.length < samples.length)
            buffer = new short[samples.length];

        for (int i = 0; i < samples.length; i++)
            buffer[i] = (short) (samples[i] * Short.MAX_VALUE);
    }
    
    public void cleanUp() {
        track.stop();
        track.flush();
    }
}