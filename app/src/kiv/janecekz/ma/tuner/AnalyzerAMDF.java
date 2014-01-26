package kiv.janecekz.ma.tuner;

import java.util.LinkedList;

import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.TunerFragment;
import android.util.Log;

public class AnalyzerAMDF extends Analyzer {
    private TunerFragment t;
    private int sampleFreq;
    
    // that's for voice...
    private final int ACF_START = 16;
    private final int ACF_END = 161;

    public AnalyzerAMDF(TunerFragment t, int sampleFreq) {
        this.t = t;
        this.sampleFreq = sampleFreq;
    }
    
    @Override
    protected void onPostExecute(Double result) {
        super.onPostExecute(result);
        
        // FIXME: may be from the static output buffer
        t.postAnalyzed(result);
    }

    @Override
    protected Double doInBackground(Short[]... params) {
        Short[] input = params[0];
        double[] resAMDF = new double[input.length];
        int sum = 0;
        Double freq = Double.valueOf(0);
        
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
        
        // Find top
        LinkedList<Integer> tops = new LinkedList<Integer>();
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
        
        LinkedList<LinkedList<Integer>> bucket = new LinkedList<LinkedList<Integer>>();
        boolean newVal = false;
        for (Integer dist : dists) {
            newVal = true;
            for (LinkedList<Integer> b : bucket) {
                if (Math.abs(b.getFirst() - dist) < 6) {
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
        
        LinkedList<Integer> most = new LinkedList<Integer>();
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
            freq = (double) most.size() * sampleFreq / sum;
        
        Log.d(MainActivity.TAG, "sum="+sum+" freq="+freq);
        
//        // find median
//        Arrays.sort(dists);
//        double freq;
//        if (dists.length == 0 || dists.length == 1)
//            freq = 0.0;
//        else if ((dists.length & 0x1) == 1) {
//            freq = (double) recorder.getSampleFreq() / dists[dists.length / 2];
//        } else {
//            int between = (dists[dists.length / 2] + dists[dists.length / 2 - 1]) / 2;
//            freq = (double) recorder.getSampleFreq() / between;
//        }
        
        return freq;
    }
}
