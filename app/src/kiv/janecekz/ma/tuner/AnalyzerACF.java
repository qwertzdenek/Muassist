package kiv.janecekz.ma.tuner;

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
    	double[] resACF = new double[input.length];
        double sum;
        
        double max = Double.MIN_VALUE;
        
        // ACF of the input signal
        for (int m = 0; m < input.length; m++) {
            sum = 0;
            for (int n = 0; n < input.length - m; n++) {
                sum += w(n, input.length) * input[n] * w(n + m, input.length) * input[n + m];
            }
            
            resACF[m] = (sum * input.length) / (input.length - m);
            
            if (resACF[m] > max)
            	max = resACF[m];
        }
        
        max /= resACF[0];
        
        Double[] resACFcl = new Double[resACF.length];
        for (int i = 0; i < resACF.length; i++) {
        	resACF[i] /= resACF[0];
			resACFcl[i] = Double.valueOf(resACF[i]);
		}
        
        // clipping
        double cl = max * 0.3;
        
        for (int i = 0; i < resACFcl.length; i++) {
			if (Math.abs(resACFcl[i]) < cl) {
				resACFcl[i] = Double.valueOf(0);
			} else if (resACFcl[i] > cl) {
				resACFcl[i] -= cl;
			} else {
				resACFcl[i] += cl;
			}
		}
        
        Double[] res = new Double[resACF.length];
        System.arraycopy(resACF, 0, res, 0, resACF.length);
        
        return res; 
    }

	private double w(int n, int len) {
		return 0.53836 - 0.46164 * Math.cos((2 * Math.PI * n) / (len - 1));
	}
}