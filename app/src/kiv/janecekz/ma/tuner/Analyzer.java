package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.Informable;
import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.rec.ExtAudioRecorder;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class Analyzer {
    private Informable listener;
    private AudioRecord audioRecorder;

    private short[] input;
    private short[] output;

    public Analyzer() {
        int framePeriod = 22050 * 120 / 1000;
        int bufferSize = framePeriod * 2 * 16 / 8;
        
        // Check to make sure buffer size is not smaller than the smaller
        // than the mallest allowed one
        if (bufferSize < AudioRecord.getMinBufferSize(22050,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)) {
            bufferSize = AudioRecord.getMinBufferSize(22050,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            // Set frame period and timer interval accordingly
            framePeriod = bufferSize / (2 * 16 / 8);
            Log.w(ExtAudioRecorder.class.getName(),
                    "Increasing buffer size to "
                            + Integer.toString(bufferSize));
        }
        
//        int bufferSize = AudioRecord.getMinBufferSize(22050,
//                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        try {
            audioRecorder = new AudioRecord(AudioSource.MIC, 22050,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
                throw new Exception("AudioRecord initialization failed");

        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(ExtAudioRecorder.class.getName(), e.getMessage());
            } else {
                Log.e(ExtAudioRecorder.class.getName(),
                        "Unknown error occured while initializing recording");
            }
        }

        audioRecorder.setPositionNotificationPeriod(framePeriod);
        audioRecorder
                .setRecordPositionUpdateListener(new OnRecordPositionUpdateListener() {
                    @Override
                    public void onPeriodicNotification(AudioRecord recorder) {
                        Log.d(MainActivity.TAG, "onPeriodicNotification");
                        recorder.read(input, 0, input.length);

                        fct();

                        int dominant = 0;
                        short domVal = output[0];

                        for (int i = 1; i < output.length; i++) {
                            if (output[i] > domVal) {
                                dominant = i;
                                domVal = output[i];
                            }
                        }

                        listener.onMessage(String.format("freq: %d\nval: %d",
                                dominant, domVal));
                    }

                    @Override
                    public void onMarkerReached(AudioRecord recorder) {
                        Log.d(MainActivity.TAG, "onMarkerReached");
                    }
                });

        // int markerPos = 22050 * TIMER_INTERVAL / 1000;

        input = new short[bufferSize];
        output = new short[bufferSize];
        
        audioRecorder.startRecording();
    }

    public void cleanUp() {
        audioRecorder.stop();
        audioRecorder.release();
    }

    private void fct() {
        for (int k = 0; k < output.length; k++) {
            int sum = 0;
            for (int n = 0; n < input.length; n++) {
                short xk = input[n];
                double c = (Math.PI * (2 * n + 1) * k) / (2 * input.length);
                sum += xk * Math.cos(c);
            }
            if (2 * sum > Short.MAX_VALUE)
                Log.d(MainActivity.TAG, "součet pro k=" + k
                        + " je větší než short");
            output[k] = (short) (2 * sum);
        }
    }

    public void setOnMessageListener(Informable listener) {
        this.listener = listener;
    }
}
