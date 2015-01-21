/*
Musicians Assistant
    Copyright (C) 2012,2013  Zdeněk Janeček <jan.zdenek@gmail.com>

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

package ycdmdj.muassist.tone;

import android.content.Context;

import java.util.Arrays;

import ycdmdj.muassist.prefs.SharedPref;

public class Player extends Thread {
    private boolean loop;
    private boolean play = false;

    public static final double PI2 = 2 * Math.PI;
    public static final int SAMPLE_FREQ = 22050;
    public static final int MIN_FREQ = 50;
    public static final int MAX_FREQ = 11000;

    /*
     * Count of harmonic frequencies.
     */
    private int harmony;

    private float freq;
    private double freqenc[];
    private double freqinc[];
    private double angle = 0;
    private double strength;
    private double strengthDelta;
    private double[] samples;
    private AudioDevice ad;
//    private BufferedWriter buf;

    /**
     * Constructor for the tone generator. To start use start() and the
     * togglePlay() to start/stop.
     */
    public Player(Context context) {
        harmony = SharedPref.getHarmDensity(context);

        freqenc = new double[harmony];
        freqinc = new double[harmony];
        strengthDelta = 1f / harmony;

        ad = new AudioDevice(SAMPLE_FREQ);

        samples = new double[1024];

//        try {
//            buf = new BufferedWriter(new FileWriter("/sdcard/sine.csv"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Toggle actual play.
     */
    public synchronized void togglePlay() {
        this.play = !play;
        if (play) {
            ad.resume();
            notify();
        }
    }

    public boolean isPlay() {
        return play;
    }

    @Override
    public void run() {
        while (loop) {
            try {
                pauseLoop();
            } catch (InterruptedException e) {
                loop = false;
                break;
            }

            Arrays.fill(samples, 0);

            for (int i = 0; i < samples.length; i++) {
                strength = 1;

                for (int j = 0; j < freqenc.length; j++) {
                    samples[i] += strength * Math.sin(freqenc[j]);

                    strength -= strengthDelta;
                    angle = freqenc[j] + freqinc[j];
                    if (angle > PI2) {
                        angle -= PI2;
                    }

                    freqenc[j] = angle;
                }

                samples[i] /= freqenc.length;
            }

            ad.writeSamples(samples);

//            StringBuilder sb = new StringBuilder();
//            for (int i = 0; i < samples.length; i++) {
//                sb.append(String.format("%1$.5f;\n", samples[i]));
//            }
//            Log.d(MainActivity.TAG, samples.length+"");
//
//            try {
//                buf.write(sb.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    private synchronized void pauseLoop() throws InterruptedException {
        if (!play) {
            ad.pause();
            wait();
        }
    }

    @Override
    public void interrupt() {
        loop = false;
        ad.cleanUp();
        super.interrupt();
    }

    @Override
    public synchronized void start() {
        super.start();
        loop = true;
    }

    public synchronized boolean setFreq(float value) {
        if ((value < (MAX_FREQ / (2 * harmony - 1))) && (value > MIN_FREQ)) {
            this.freq = value;

//            Log.d(MainActivity.TAG, "freq="+this.freq+" harm="+harmony+" len="+freqinc.length);
            double speed = 1 / Math.pow(2, (harmony >> 1));
            for (int i = 0; i < freqinc.length; i++) {
                freqinc[i] = (PI2 * this.freq * speed) / SAMPLE_FREQ;
                speed *= 2;
            }

            Arrays.fill(freqenc, 0);

            return true;
        } else
            return false;
    }

    public float getFreq() {
        return freq;
    }
}
