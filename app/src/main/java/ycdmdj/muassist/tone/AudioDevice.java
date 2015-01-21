package ycdmdj.muassist.tone;

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
    }

    /**
     * Use this to write sample data. They must be in range of <0;1>.
     *
     * @param samples Sample data.
     */
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

    /**
     * Pause data pushing.
     */
    public void pause() {
        track.pause();
        track.flush();
    }

    /**
     * Starts pushing data to the PCM device.
     */
    public void resume() {
        track.play();
    }

    /**
     * Call when changing frequency to avoid collision or
     * at end.
     */
    public void cleanUp() {
        track.pause();
        track.flush();
        track.release();
    }
}