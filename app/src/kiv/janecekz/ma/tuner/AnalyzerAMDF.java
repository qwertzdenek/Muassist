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

import java.util.LinkedList;

import kiv.janecekz.ma.TunerFragment;

public class AnalyzerAMDF extends Analyzer {
    private TunerFragment t;
    private int sampleFreq;
    
    // modified by giveData
    private Short[] input;

    // that's for voice and 8000 sampling frequency...
    private final int ACF_START = 16;
    private final int ACF_END = 400;

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
        LinkedList<Integer> tops = new LinkedList<Integer>();
        LinkedList<LinkedList<Integer>> bucket = new LinkedList<LinkedList<Integer>>();
        LinkedList<Integer> most = new LinkedList<Integer>();
        double[] resAMDF = new double[input.length];
        boolean[] resClip = new boolean[ACF_END - ACF_START];
        Double[] resACF = new Double[ACF_END - ACF_START];
        
        while (!isCancelled()) {
            try {
                t.full.acquire();
                t.data.acquire();
            } catch (InterruptedException e) {
                t.full.release();
                t.data.release();
            }
            
            tops.clear();
            bucket.clear();
            most.clear();
            
            // AMDF of the input signal
            for (int m = 0; m < input.length; m++) {
                sum = 0;
                for (int n = 0; n < input.length - m; n++) {
                    sum += Math.abs(input[n + m] - input[n]);
                }

                resAMDF[m] = (double) sum / (input.length - m);
            }
            
            t.data.release();
            t.free.release();

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

            for (int i = ACF_START; i < ACF_END; i++) {
                resClip[i - ACF_START] = resAMDF[i] < level;
            }

            // And now the ACF
            for (int k = 0; k < resClip.length; k++) {
                sum = 0;
                for (int n = 0; n < resClip.length - k; n++) {
                    if (resClip[n] && resClip[n + k])
                        sum++;
                }
                resACF[k] = (double) sum / (resClip.length - k);
            }

            // Find top
            int i = 0;
            while (i < resACF.length - 1) {
                // going top
                while ((i < resACF.length - 1) && (resACF[i] < resACF[i + 1])) {
                    i++;
                    if (resACF[i] > resACF[i + 1]) {
                        tops.add(i);
                        break;
                    }
                }

                // going down
                while ((i < resACF.length - 1) && (resACF[i] >= resACF[i + 1]))
                    i++;
            }

            int[] dists = new int[tops.size()];

            i = 0;
            int lastPeak = 0;
            for (int peak : tops) {
                dists[i++] = peak - lastPeak;
                lastPeak = peak;
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
                freq = 0.0;
            else
                freq = (double) most.size() * sampleFreq / bucketSum;
            
            // // find median
            // Arrays.sort(dists);
            // double freq;
            // if (dists.length == 0 || dists.length == 1)
            // freq = 0.0;
            // else if ((dists.length & 0x1) == 1) {
            // freq = (double) recorder.getSampleFreq() / dists[dists.length /
            // 2];
            // } else {
            // int between = (dists[dists.length / 2] + dists[dists.length / 2 -
            // 1]) / 2;
            // freq = (double) recorder.getSampleFreq() / between;
            // }

            publishProgress(freq);
        }

        return null;
    }
}


