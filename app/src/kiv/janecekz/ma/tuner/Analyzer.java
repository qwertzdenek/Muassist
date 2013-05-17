package kiv.janecekz.ma.tuner;

import java.util.Arrays;

import kiv.janecekz.ma.Informable;
import kiv.janecekz.ma.MainActivity;
import android.util.Log;

public class Analyzer {
    private Informable listener;
    private Recorder recorder;
    
    private Byte[] input;
    private Short[] output;
    
    public Analyzer() {
        recorder = new Recorder(this);
        recorder.execute();
    }
    
    public void sendData(Byte[] data) {
        input = data;
        
        // Now we can do some computation
        
        listener.onMessage("I got "+input.length+" bytes");
    }

    /**
     * Not tested!!!
     * @deprecated
     */
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
    
    public void setOnMessageListener(Informable tunerFragment) {
        listener = tunerFragment;
    }

    public void cleanUp() {
        recorder.end();
    }
}
