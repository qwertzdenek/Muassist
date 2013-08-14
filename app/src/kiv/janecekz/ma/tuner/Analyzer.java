package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.Tones;
import kiv.janecekz.ma.TunerFragment;

public class Analyzer {
    private TunerFragment listener;
    private Recorder recorder;
    
    public Analyzer(TunerFragment f) {
        listener = f;
        recorder = new Recorder(this);
        recorder.execute();
    }
    
    public void sendData(Byte[] data) {
        // Now we can do some computation
        
        listener.onMessage("I got "+findTone(computeACF(data)));
        
        // TODO: when we have dominant frequency, it is time to get tone
    }
    
    /**
     * Returns played tone.
     * @param corInput Output from computeACF().
     * @return One of Tones enum numbers.
     */
    public Tones findTone(int[] corInput) {
        return Tones.A;
    } 
    
    /**
     * Do the Autocorrelation on the input.
     * @param input Input data.
     * @return correlated input
     */
    private int[] computeACF(Byte[] input) {
        int N = input.length;
        int[] res = new int[N];
        
        for (int tau = 0; tau < N; tau++) {
            for (int n = 0; n < N - tau - 1; n++) {
                res[tau] += input[n] * input[n + tau];
            }
            
            res[tau] /= N;
        }
        
        return res;
    }
    
    public void cleanUp() {
        recorder.end();
    }
}
