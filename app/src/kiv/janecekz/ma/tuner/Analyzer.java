package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.TunerFragment;
import android.os.AsyncTask;

public class Analyzer extends AsyncTask<Short[], Void, Short[]> {
    private TunerFragment t;
    
    // that's for voice...
    private final int ACF_START = 16;
    private final int ACF_END = 400;

    public Analyzer(TunerFragment t) {
        this.t = t;
    }
    
    @Override
    protected void onPostExecute(Short[] result) {
        super.onPostExecute(result);
        
        // FIXME: may be from the static output buffer
        t.postAnalyzed(result);
    }

    @Override
    protected Short[] doInBackground(Short[]... params) {
        Short[] input = params[0];
        short[] resAMDF = new short[input.length];
        
        // AMDF of the input signal
        for (int m = 0; m < input.length; m++) {
            for (int n = 0; n < input.length - m; n++) {
                resAMDF[m] += Math.abs(input[n + m] - input[n]);
            }
            
            resAMDF[m] /= input.length - m;
        }

        // find min max in ACF range
        short max = Short.MIN_VALUE;
        short min = Short.MAX_VALUE;
        
        for (int i = ACF_START; i < ACF_END; i++) {
            if (resAMDF[i] < min)
                min = resAMDF[i];
            if (resAMDF[i] > max)
                max = resAMDF[i];
        }
        
        // clip values
        short level = (short) (0.42 * (max + min));
        boolean[] resClip = new boolean[ACF_END - ACF_START];

        for (int i = ACF_START; i < ACF_END; i++) {
            resClip[i - ACF_START] = resAMDF[i] < level ? true : false;
        }
        
        // And now the ACF
        Short[] resACF = new Short[ACF_END - ACF_START];
        
        for (int k = 0; k < resClip.length; k++) {
            for (int n = 0; n < resClip.length - k; n++) {
                 short add = (short) (resClip[n] & resClip[n + k] ? 1 : 0);
                 if (resACF[k] == null)
                     resACF[k] = new Short(add);
                 else
                     resACF[k] = (short) (resACF[k] + add);
            }
            resACF[k] = (short) (resACF[k] / (resClip.length - k));
        }

        // TODO: find the peaks in the resACF
        return resACF;
    }
}
