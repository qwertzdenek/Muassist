/*
Musicians Assistant
    Copyright (C) 2014  Zdeněk Janeček <jan.zdenek@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package kiv.janecekz.ma.tuner;

import java.util.ArrayList;

import kiv.janecekz.ma.TunerFragment;

public class AnalyzerAMDF extends Analyzer {
	private static final int POINT_BUFFER_SIZE = 50;
	
    private TunerFragment t;
    private int sampleFreq;

    // modified by giveData
    private Short[] input;

    // that's for voice and 8000 sampling frequency...
    private final double EPS = 1e-6;
    
    /**
     * Averange magnitude difference function analyzator.
     * @param t assigned Fragment
     * @param sampleFreq sampling frequency of the source
     * @param window analyzed data
     */
    public AnalyzerAMDF(TunerFragment t, int sampleFreq, Short[] window) {
        this.t = t;
        this.sampleFreq = sampleFreq;
        input = window;
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);

        t.postAnalyzed(values[0]);
    }

    @Override
    protected Void doInBackground(Void... params) {
    	int N = input.length >> 1;
        double den = (double) 1 / (N - 1);
        int sum = 0;
        Double freq = Double.valueOf(0);
        ArrayList<Double> tops = new ArrayList<Double>(20);
        double[] resAMDF = new double[N];
        boolean[] resClip = new boolean[N - 10];
        double[] resACF = new double[N - 10];
        double[] pointBuffer = new double[POINT_BUFFER_SIZE];

        while (!isCancelled()) {
            // TODO: don't analyze silence
            try {
                t.full.acquire();
            } catch (InterruptedException e) {
                t.free.release();
                continue;
            }
            
            // AMDF of the input signal
            for (int m = 0; m < N; m++) {
                sum = 0;
                for (int n = 2 * N - 1; n >= N; n--) {
                    sum += Math.abs(input[n - m] - input[n]);
                }

                resAMDF[m] = sum * den;
            }

            t.free.release();
            
            // find min max in ACF range
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            for (int i = 1; i < resClip.length; i++) {
                min = Math.min(min, resAMDF[i]);
                max = Math.max(max, resAMDF[i]);
            }

            // clip values
            double level = 0.4 * (max + min);

            for (int i = 0; i < resClip.length; i++) {
                resClip[i] = resAMDF[i] < level;
            }
            
            // And now the ACF
            for (int k = 0; k < resACF.length; k++) {
                sum = 0;
                for (int n = 0; n < resClip.length - k; n++) {
                    sum += resClip[n] && resClip[n + k] ? 1 : 0;
                }
                resACF[k] = (double) sum / (resClip.length - k);
            }
            
            // ---- Get frequency ----
            
            freq = Double.valueOf(0);
            
            // find starting value
            int i = 0;
            int start = 0;
            int end = 0;
            int count = 0;
            double[][] matrix;
            double[] res;
            double[] coef;
            
            tops.clear();
            
            while (i < resACF.length && resACF[i] > EPS)
                i++;
            
            while (i < resACF.length) {
                // find first non zero value -> start
                while (i < resACF.length && resACF[i] < EPS)
                    i++;
                start = i;
                
                // find first zero value -> end
                while (i < resACF.length && resACF[i] > EPS)
                    i++;
                end = i;
                
                count = end - start;
                
                switch (count) {
                case 0:
                    continue;
                case 1:
                    tops.add((double) start);
                    break;
                case 2:
                    tops.add((double) (start + end - 1) / 2);
                    break;
                case 3:
                    tops.add((double) (start + 1));
                    break;
                default:
                	if (count >= POINT_BUFFER_SIZE)
                    	continue;
                	
                    for (int j = 0; j < count; j++) {
                        pointBuffer[j] = resACF[j + start];
                    }
                    
                    matrix = LeastSquares.getMatrix(pointBuffer, count);
                    res = LeastSquares.getB(pointBuffer, count);
                    
                    coef = LeastSquares.solve(matrix, res);
                    
                    tops.add(-coef[1] / (2 * coef[2]) + start);
                    break;
                }
            }
            
            if (tops.size() == 0) {
            	//Log.d(MainActivity.TAG, "-- No tops");
                continue;
            }
            
            double s = 0; // součet
            double lastPeak = 0; // souřadnice posledního peeku
            
            for (double t : tops) {
                // TODO: check if peak it's near
                s += t - lastPeak;
                lastPeak = t;
            }
            
            freq = (double) tops.size() * sampleFreq / s;
//            freq = (double) sampleFreq / tops.get(0);
            
            //writeToFile(resACF, freq, tops);
            
            publishProgress(freq);
        }
        
        return null;
    }
}
