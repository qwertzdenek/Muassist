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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.TunerFragment;
import android.os.Environment;
import android.util.Log;

public class AnalyzerAMDF extends Analyzer {
    private TunerFragment t;
    private int sampleFreq;

    // modified by giveData
    private Short[] input;

    // that's for voice and 8000 sampling frequency...
    private final double EPS = 1e-6;

    public class Point {
        public double x;
        public double y;
        
        public Point(double x, double y) {
            super();
            this.x = x;
            this.y = y;
        }
    }
    
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
        int sum = 0;
        Double freq = Double.valueOf(0);
        ArrayList<Double> tops = new ArrayList<Double>(20);
        double[] resAMDF = new double[input.length];
        boolean[] resClip = new boolean[input.length - 10];
        double[] resACF = new double[input.length - 10];
        Point[] pointBuffer = new Point[20];
        
        int it = 0;
        
        for (int i = 0; i < pointBuffer.length; i++) {
            pointBuffer[i] = new Point(0.0, 0.0);
        }
        
        while (!isCancelled()) {
            // TODO: don't analyze silence

            int N = input.length >> 1;
            double den = (double) 1 / (N - 1);
            
            try {
                t.full.acquire();
            } catch (InterruptedException e) {
                t.free.release();
                continue;
            }
            
            it++;
            
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

            for (int i = 0; i < resClip.length; i++) {
                min = Math.min(min, resAMDF[i]);
                max = Math.max(max, resAMDF[i]);
            }

            // clip values
            double level = 0.4 * (max + min);

            for (int i = 0; i < resClip.length; i++) {
                resClip[i] = resAMDF[i] < level;
            }
            
            // And now the ACF
            for (int k = 0; k < resClip.length; k++) {
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
//                    for (int j = start; j < end; j++) {
//                        pointBuffer[j - start].x = (double) j;
//                        pointBuffer[j - start].y = resACF[j];
//                    }
//                    
//                    matrix = LeastSquares.getMatrix(pointBuffer, count);
//                    res = LeastSquares.getB(pointBuffer, count);
//                    
//                    coef = LeastSquares.solve(matrix, res);
//                    
//                    tops.add(-coef[1] / (2 * coef[2]));
                    break;
                }
            }
            
            if (tops.size() == 0)
                continue;
            
            double s = 0; // součet
            double lastPeak = 0; // souřadnice posledního peeku
            
            for (double t : tops) {
                // TODO: check if it's peak near
                s += t - lastPeak;
                lastPeak = t;
            }
            
            freq = (double) tops.size() * sampleFreq / s;
            
            //writeToFile(resACF, freq, tops);
            
            publishProgress(freq);
        }
        
        return null;
    }
    
    /*
     * Only Debug method
     */
    private void writeInput(Short[] array, int it) {
        PrintWriter file = null;
        try {
            file = new PrintWriter(new FileOutputStream(Environment.getExternalStorageDirectory()
                    .getPath() + "/input", true));
        } catch (FileNotFoundException e) {
            Log.d(MainActivity.TAG, "File result cannot be opened");
            return;
        }
        file.print(String.format("i%d=[",it));
        for (int i = 0; i < array.length; i++) {
            file.print(String.format("%d ",array[i]));
        }
        file.println("]");
        file.close();
    }
    
    /*
     * Only Debug method
     */
    private void writeToFile(double[] array, double freq, ArrayList<Double> tops) {
        PrintWriter file = null;
        try {
            file = new PrintWriter(new FileOutputStream(Environment.getExternalStorageDirectory()
                    .getPath() + "/result", true));
        } catch (FileNotFoundException e) {
            Log.d(MainActivity.TAG, "File result cannot be opened");
            return;
        }
        
        file.println(String.format("##\n%% frequency %f", freq));
        for (Double d : tops) {
            file.print(String.format(" %f",d));
        }
        file.println();
        for (int i = 0; i < array.length; i++) {
            file.println(String.format("%f",array[i]));
        }
        file.write('\n');
        file.close();
    }
}
