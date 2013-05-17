package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.MainActivity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

public class Recorder extends AsyncTask<Void, Byte[], String> {
    private static final int AUDIO_SAMPLE_FREQ = 8000;
    
    private byte[] audioBuffer;
    private AudioRecord recorder;
    private Analyzer a;
    private boolean recording = true;

    public Recorder(Analyzer a) {
        this.a = a;
        
        int framePeriod = AUDIO_SAMPLE_FREQ / 4;
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

        audioBuffer = new byte[bufferSize];
    }
    
    public synchronized void end() {
        recording = false;
    }

    @Override
    protected String doInBackground(Void... params) {
        recorder.startRecording();
        
        while (recording) {
            recorder.read(audioBuffer, 0, audioBuffer.length);
            publishProgress(prepareResults(audioBuffer));
        }
        
        recorder.stop();
        recorder.release();
        return "AsyncTask finished";
    }
    
    @Override
    protected void onProgressUpdate(Byte[]... values) {
        super.onProgressUpdate(values);
        a.sendData(values[0]);
    }

    private Byte[] prepareResults(byte[] b) {
        Byte[] res = new Byte[b.length];
        
        for (int i = 0; i < b.length; i++) {
            res[i] = new Byte(b[i]);
        }
        
        return res;
    }
}

