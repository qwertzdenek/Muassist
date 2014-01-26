package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.TunerFragment;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class Recorder extends Thread {
    private static final int AUDIO_SAMPLE_FREQ = 8000;
    
    private byte[] audioBuffer;
    private Short[] recordedSamples;
    private AudioRecord recorder;
    private TunerFragment t;
    private boolean recording = true;

    public Recorder(TunerFragment t) {
        this.t = t;
        
        int framePeriod = AUDIO_SAMPLE_FREQ / 8;
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

        audioBuffer = new byte[bufferSize / 2];
        recordedSamples = new Short[audioBuffer.length / 2];
    }
    
    public int getFrameSize() {
        return recordedSamples.length;
    }
    
    public int getSampleFreq() {
        return AUDIO_SAMPLE_FREQ;
    }
    
    public synchronized void end() {
        recording = false;
    }
    
    @Override
    public void run() {
        recorder.startRecording();
        
        int readed = 0;
        
        while (recording) {
            readed += recorder.read(audioBuffer, readed, audioBuffer.length - readed);
            
            if (readed < audioBuffer.length)
                continue;
            
            readed = 0;
            
            prepareResults(audioBuffer, recordedSamples);
            t.postRec(recordedSamples);
        }
        
        recorder.stop();
        recorder.release();
    }
    
    public static void prepareResults(byte[] b, Short[] recs) {
        for (int i = 0, j = 0; i < b.length; i += 2, j++) {
            recs[j] = (short) ((b[i] << 8) | b[i+1]);
        }
    }
    
    /*
    public static Double[] prepareResults(boolean[] b) {
        Double[] f = new Double[b.length];
        
        for (int i = 0; i < b.length; i++) {
            f[i] = (double) (b[i] ? 1f : 0f);
        }

        return f;
    }
    
    public static Double[] prepareResults(double[] b) {
        Double[] f = new Double[b.length];
        
        for (int i = 0; i < b.length; i++) {
            f[i] = Double.valueOf(b[i]);
        }

        return f;
    }
    */
}
