package kiv.janecekz.ma.tuner;

import android.os.AsyncTask;

import kiv.janecekz.ma.Informable;
import kiv.janecekz.ma.common.Recorder;
import kiv.janecekz.ma.common.SharedData;

public class AnalyzerWave extends AsyncTask<Void, Double, Void> {
    private Informable src;
//	private Recorder r;
//	private SharedData sd;

//	private int sampleFreq;
//	private short[] input;

    public AnalyzerWave(SharedData sd, Recorder r, Informable src) {
        this.src = src;
//		this.r = r;
//		this.sd = sd;
//		this.sampleFreq = r.getSampleFreq();
    }

    @Override
    protected void onProgressUpdate(Double... result) {
        super.onProgressUpdate(result);

        src.postInformation(result[0]);
    }

    @Override
    protected Void doInBackground(Void... params) {
        /*
        Short[] input = params[0];
    	Double[] resACF = new Double[input.length];
        double sum;
        
        double max = Double.MIN_VALUE;
        
        // ACF of the input signal
        for (int m = 0; m < input.length; m++) {
            sum = 0;
            for (int n = 0; n < input.length - m; n++) {
                sum += w(n, input.length) * input[n] * w(n + m, input.length) * input[n + m];
            }
            
            resACF[m] = Double.valueOf((sum * input.length) / (input.length - m));
            
            if (resACF[m] > max)
            	max = resACF[m].doubleValue();
        }
        
        // normalize
        for (int i = 0; i < resACF.length; i++) {
        	resACF[i] /= resACF[0];
		}
        
        // clipping
        double cl = max * 0.3;
        
        for (int i = 0; i < resACF.length; i++) {
			if (Math.abs(resACF[i]) < cl) {
				resACF[i] = Double.valueOf(0);
			} else if (resACF[i] > cl) {
				resACF[i] -= cl;
			} else {
				resACF[i] += cl;
			}
		}
        */
        return null;
    }

//	private double w(int n, int len) {
//		return 0.53836 - 0.46164 * Math.cos((2 * Math.PI * n) / (len - 1));
//	}
}