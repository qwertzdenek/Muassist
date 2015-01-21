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

import android.os.AsyncTask;

import kiv.janecekz.ma.Informable;
import kiv.janecekz.ma.common.Recorder;
import kiv.janecekz.ma.common.SharedData;

public class AnalyzerAMDF extends AsyncTask<Void, Double, Void> {
    private static final int POINT_BUFFER_SIZE = 50;

    private SharedData sd;
    private Informable src;

    private int sampleFreq;

    private final double EPS = 1e-5;

    /**
     * Averange magnitude difference function analyzator.
     */
    public AnalyzerAMDF(SharedData sd, Recorder r, Informable src) {
        this.sd = sd;
        this.src = src;
        this.sampleFreq = r.getSampleFreq();
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);

        src.postInformation(values[0]);
    }

    @Override
    protected Void doInBackground(Void... params) {
        int N = sd.shortBuffer.length >> 1;
        int sum = 0;
        Double freq = Double.valueOf(0);
        short[] input = sd.shortBuffer;
        double[] res = new double[N];
        double[] pointBuffer = new double[POINT_BUFFER_SIZE];

        while (!isCancelled()) {
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            synchronized (sd) {
                while (!sd.available)
                    try {
                        sd.wait();
                    } catch (InterruptedException e) {
                    }

                // AMDF of the input signal
                for (int m = 0; m < N; m++) {
                    sum = 0;
                    for (int n = 2 * N - 1; n >= N; n--) {
                        sum += Math.abs(input[n - m] - input[n]);
                    }

                    res[m] = (double) sum / (N - 1);

                    min = Math.min(min, res[m]);
                    max = Math.max(max, res[m]);
                }

                sd.available = false;
                sd.notify();
            }

            // clip value
            double level = 0.4 * (max + min);

            for (int i = 0; i < res.length; i++) {
                res[i] = res[i] > level ? 0 : level - res[i];
            }

            // find starting value
            int i = 0;
            int start = 0;
            int end = 0;
            int count = 0;
            int xi = 0;
            double xv = 0;

            double[] t = new double[2];

            // skip first values
            while (i < res.length && res[i] > EPS)
                i++;

            int it = 0;
            while (it < 2) {
                // find first non zero value -> start
                while (i < res.length && res[i] < EPS)
                    i++;

                if (i >= res.length)
                    break;

                start = i;

                xv = Double.MIN_VALUE;
                xi = start;
                // find first zero value -> end
                while (i < res.length && res[i] > EPS) {
                    if (res[i] > xv) {
                        xi = i;
                        xv = res[i];
                    }
                    i++;
                }
                end = i;

                count = end - start;

                switch (count) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        i++;
                        continue;
                    default:
                        // first line
                        count = xi - start + 1;
                        if (count >= POINT_BUFFER_SIZE)
                            continue;

                        for (int j = 0; j < count; j++) {
                            pointBuffer[j] = res[j + start];
                        }

                        int[][] A;
                        double[] b;

                        A = LeastSquares.getMatrix(start, count);
                        b = LeastSquares.getB(start, pointBuffer, count);
                        double[] coefa = LeastSquares.solve(A, b);

                        // second line
                        xv = Double.MIN_VALUE;
                        xi = end - 1; // find right max
                        for (int fn = end - 1; fn >= start; fn--) {
                            if (res[fn] > xv) {
                                xi = fn;
                                xv = res[fn];
                            }
                        }

                        count = end - xi;
                        if (count >= POINT_BUFFER_SIZE)
                            continue;

                        for (int j = 0; j < count; j++) {
                            pointBuffer[j] = res[j + xi];
                        }

                        A = LeastSquares.getMatrix(xi, count);
                        b = LeastSquares.getB(xi, pointBuffer, count);
                        double[] coefb = LeastSquares.solve(A, b);

                        t[it++] = (coefb[0] - coefa[0]) / (coefa[1] - coefb[1]);
                        break;
                }
            }

            double topt = t[1] - t[0];

            if (topt < EPS || it < 2)
                continue;

            freq = sampleFreq / topt;

            publishProgress(freq);
        }

        return null;
    }
}
