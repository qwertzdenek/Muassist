package kiv.janecekz.ma.tuner;

import java.util.Arrays;

import kiv.janecekz.ma.TunerFragment;

public class AnalyzerACF extends Analyzer {
	private TunerFragment t;

	public AnalyzerACF(TunerFragment t) {
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
        
        return resACF;
    }

	private double w(int n, int len) {
		return 0.53836 - 0.46164 * Math.cos((2 * Math.PI * n) / (len - 1));
	}
}