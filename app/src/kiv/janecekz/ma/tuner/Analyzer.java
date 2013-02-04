package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.Informable;
import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.rec.ExtAudioRecorder;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class Analyzer extends Thread {
    private Informable listener;
    private AudioRecord audioRecorder;
    private boolean loop = true;

    private short[] input;
    private short[] output;

    public Analyzer() {
        int bufferSize = AudioRecord.getMinBufferSize(22050,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

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

        audioRecorder.setPositionNotificationPeriod(bufferSize);
        audioRecorder
                .setRecordPositionUpdateListener(new OnRecordPositionUpdateListener() {
                    @Override
                    public void onPeriodicNotification(AudioRecord recorder) {
                        recorder.read(input, 0, input.length);

                        fct(input, output);

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
                    }
                });

        // int markerPos = 22050 * TIMER_INTERVAL / 1000;

        audioRecorder.startRecording();
    }

    public void cleanUp() {
        audioRecorder.stop();
        audioRecorder.release();
    }

    @Override
    public void interrupt() {
        loop = false;
        super.interrupt();
    }
    
    @Override
    public void run() {
        super.run();
    }

    private void fct(short[] input, short[] output) {
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
