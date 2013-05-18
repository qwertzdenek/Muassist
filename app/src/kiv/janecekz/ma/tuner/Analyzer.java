package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.Tones;
import kiv.janecekz.ma.TunerFragment;
import android.util.Log;

public class Analyzer {
    private TunerFragment listener;
    private Recorder recorder;
    
    private Byte[] input;
    private Short[] output;
    
    public Analyzer(TunerFragment f) {
        listener = f;
        recorder = new Recorder(this);
        recorder.execute();
    }
    
    public void sendData(Byte[] data) {
        input = data;
        
        // Now we can do some computation
        
        listener.onMessage("I got "+input.length+" bytes");
        
        // TODO: when we have dominant frequency, it is time to get tone
    }

    private Tones closeTone(float freq) {
//        int baseFreq = SharedPref.getBaseFreq(getActivity());
        
        // get octave
        
        for (Tones tone : Tones.values()) {
            // compare difference Math.abs(freq - tone)
        }
        return null;
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
    
    public void cleanUp() {
        recorder.end();
    }
}
