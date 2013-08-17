package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.TunerFragment;
import android.os.AsyncTask;

public class Analyzer extends AsyncTask<Short[], Void, Double[]> {
    private TunerFragment t;
    
    // that's for voice...
    private final int ACF_START = 16;
    private final int ACF_END = 161;

    public Analyzer(TunerFragment t) {
        this.t = t;
    }
    
    @Override
    protected void onPostExecute(Double[] result) {
        super.onPostExecute(result);
        
        // FIXME: may be from the static output buffer
        t.postAnalyzed(result);
    }

    @Override
    protected Double[] doInBackground(Short[]... params) {
        Short[] input = params[0];
        double[] resAMDF = new double[input.length];
        int sum;
        
        // AMDF of the input signal
        for (int m = 0; m < input.length; m++) {
            sum = 0;
            for (int n = 0; n < input.length - m; n++) {
                sum += Math.abs(input[n + m] - input[n]);
            }
            
            resAMDF[m] = (double) sum / (input.length - m);
        }

        // find min max in ACF range
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        
        for (int i = ACF_START; i < ACF_END; i++) {
            if (resAMDF[i] < min)
                min = resAMDF[i];
            if (resAMDF[i] > max)
                max = resAMDF[i];
        }
        
        // clip values
        double level = 0.42 * (max + min);
        boolean[] resClip = new boolean[ACF_END - ACF_START];

        for (int i = ACF_START; i < ACF_END; i++) {
            resClip[i - ACF_START] = resAMDF[i] < level;
        }
        
        // And now the ACF
        Double[] resACF = new Double[resClip.length];
        
        for (int k = 0; k < resClip.length; k++) {
            sum = 0;
            for (int n = 0; n < resClip.length - k; n++) {
                 if (resClip[n] && resClip[n + k])
                     sum++;
            }
            resACF[k] = (double) sum / (resClip.length - k);
        }

        // TODO: find the peaks in the resACF
        return resACF;
    }
}
