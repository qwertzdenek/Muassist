package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.TunerFragment;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class Recorder extends Thread {
    private static final int AUDIO_SAMPLE_FREQ = 8000;
    
    private short[] audioBuffer;
    private AudioRecord recorder;
    private TunerFragment t;
    private boolean recording = true;

    public Recorder(TunerFragment t) {
        this.t = t;
        
        int framePeriod = AUDIO_SAMPLE_FREQ / 16;
        int bufferSize = framePeriod * 2;

        if (bufferSize < AudioRecord.getMinBufferSize(AUDIO_SAMPLE_FREQ,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)) {
            bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_FREQ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            // Set frame period and timer interval accordingly
            framePeriod = bufferSize / 2;
            Log.w(MainActivity.TAG,
                    "Increasing buffer size to "
                            + Integer.toString(bufferSize));
        }
        
        try {
            // init recorder
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        audioBuffer = new short[bufferSize/2];
    }
    
    public synchronized void end() {
        recording = false;
    }
    
    @Override
    public void run() {
        recorder.startRecording();
        
        while (recording) {
            recorder.read(audioBuffer, 0, audioBuffer.length);
            t.postRec(prepareResults(audioBuffer));
        }
        
        recorder.stop();
        recorder.release();
    }

    public static Short[] prepareResults(short[] b) {
        Short[] f = new Short[b.length];
        
        for (int i = 0; i < b.length; i++) {
            f[i] = new Short(b[i]);
        }

        return f;
    }
    
    public static Short[] prepareResults(boolean[] b) {
        Short[] f = new Short[b.length];
        
        for (int i = 0; i < b.length; i++) {
            f[i] = new Short((short) (b[i] ? 2 : 0));
        }

        return f;
    }
}
