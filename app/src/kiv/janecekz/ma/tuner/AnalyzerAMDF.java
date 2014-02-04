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
import java.util.LinkedList;

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
    private final int ACF_START = 16;
    private final int ACF_END = 160;

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
        LinkedList<Integer> dists = new LinkedList<Integer>();
        LinkedList<LinkedList<Integer>> bucket = new LinkedList<LinkedList<Integer>>();
        LinkedList<Integer> most = new LinkedList<Integer>();
        double[] resAMDF = new double[input.length];
        boolean[] resClip = new boolean[ACF_END - ACF_START];
        Double[] resACF = new Double[ACF_END - ACF_START];

//        long time;
//        long amdf;
//        long rest;
//        long post;
        
        while (!isCancelled()) {
            // TODO: don't analyze silence
            
            bucket.clear();
            most.clear();
            dists.clear();

            int N = input.length >> 1;
            double den = (double) 1 / (N - 1);
            
            try {
                t.full.acquire();
                t.data.acquire();
            } catch (InterruptedException e) {
                t.full.release();
                t.data.release();
            }

//            time = SystemClock.elapsedRealtime();
            
            // AMDF of the input signal
            for (int m = 0; m < N; m++) {
                sum = 0;
                for (int n = 2 * N - 1; n >= N; n--) {
                    sum += Math.abs(input[n - m] - input[n]);
                }

                resAMDF[m] = sum * den;
            }

            t.data.release();
            t.free.release();

//            amdf = SystemClock.elapsedRealtime() - time;
            
            // find min max in ACF range
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            for (int i = ACF_START; i < ACF_END; i++) {
                min = Math.min(min, resAMDF[i]);
                max = Math.max(min, resAMDF[i]);
            }

            // clip values
            double level = 0.42 * (max + min);

            for (int i = ACF_START; i < ACF_END; i++) {
                resClip[i - ACF_START] = resAMDF[i] < level;
            }

            // And now the ACF
            for (int k = 0; k < resClip.length; k++) {
                sum = 0;
                for (int n = 0; n < resClip.length - k; n++) {
                    sum += resClip[n] && resClip[n + k] ? 1 : 0;
                }
                resACF[k] = (double) sum / (resClip.length - k);
            }
            
//            rest = SystemClock.elapsedRealtime() - time - amdf;
            
            // Find tops
            double epsilon = 1E-4;
            int index = 0;
            int oldIndex = 0;
            int i = 0;
            while (i < resACF.length - 1) {
                max = Double.MIN_VALUE;
                index = 0;
                while ((i < resACF.length) && (resACF[i] < epsilon))
                    i++;
                
                while ((i < resACF.length) && (resACF[i] >= epsilon)) {
                    if (max < resACF[i]) {
                        index = i;
                        max = resACF[i];
                    }
                    i++;
                }
                
                dists.add(index - oldIndex);
                oldIndex = index;
            }

            boolean newVal = false;
            for (Integer dist : dists) {
                newVal = true;
                for (LinkedList<Integer> b : bucket) {
                    if (Math.abs(b.getFirst() - dist) < 5) {
                        b.add(dist);
                        newVal = false;
                        break;
                    }
                }

                if (newVal) {
                    LinkedList<Integer> newList = new LinkedList<Integer>();
                    newList.add(dist);
                    bucket.add(newList);
                }
            }

            for (LinkedList<Integer> list : bucket) {
                if (list.size() > most.size())
                    most = list;
            }

            int bucketSum = 0;
            for (int d : most) {
                bucketSum += d;
            }

            if (bucketSum < 10)
                continue;
            else
                freq = (double) most.size() * sampleFreq / bucketSum;

//            Log.d(MainActivity.TAG, Double.toString(freq));
//            post = SystemClock.elapsedRealtime() - time - amdf - rest;
            
//            writeToFile(resAMDF, freq, bucket);

//            Log.d(MainActivity.TAG, String.format("amdf=%d ms, rest=%d ms, post=%d ms",
//                    amdf, rest, post));
            
            publishProgress(freq);
        }

        return null;
    }

    private void writeToFile(double[] array, double freq, LinkedList<LinkedList<Integer>> bucket) {
        PrintWriter file = null;
        try {
            file = new PrintWriter(new FileOutputStream(Environment.getExternalStorageDirectory()
                    .getPath() + "/result", true));
        } catch (FileNotFoundException e) {
            Log.d(MainActivity.TAG, "File result cannot be opened");
            return;
        }
        
        file.println(String.format("##\n%% frequency %f", freq));
        for (LinkedList<Integer> linkedList : bucket) {
            file.print("% ");
            for (Integer integer : linkedList) {
                file.print(String.format(" %d",integer.intValue()));
            }
            file.println();
        }
        for (int i = 0; i < array.length; i++) {
            file.println(String.format("%f",array[i]));
        }
        file.write('\n');
        file.close();
    }
}
