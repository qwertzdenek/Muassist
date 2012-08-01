/*
Musicians Assistant
    Copyright (C) 2012  Zdeněk Janeček <jan.zdenek@gmail.com>

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

package kiv.janecekz.ma.tone;

import kiv.janecekz.ma.MainActivity;
import android.util.Log;

public class Player extends Thread {
    private boolean loop;
    private boolean play = false;

    public static final double PI2 = 2 * Math.PI;
    public static final int SAMPLE_FREQ = 11025;
    public static final double MIN_FREQ = 10f;
    public static final double MAX_FREQ = 4000f;

    private float freq;
    private double[] audioData;
    private double angleMain = 0;
    private double angleHarmonic = 0;
    private boolean updatedFreq;
    private int sampleLength;
    private AudioDevice ad;

    /**
     * Constructor for the tone generator. To start use start() and the
     * togglePlay() to start/stop.
     */
    public Player() {
        ad = new AudioDevice(SAMPLE_FREQ);
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

    /**
     * Starts the play if isn't play already.
     */
    public synchronized void play() {
        if (!play) {
            play = true;
            ad.resume();
            notify();
        }
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

            if (updatedFreq) {
                sampleLength = (int) (SAMPLE_FREQ / freq);
                double incMain = (PI2 * freq) / SAMPLE_FREQ;
                double incHarm = (PI2 * freq * 2) / SAMPLE_FREQ;

                double[] samples = new double[sampleLength];
                for (int i = 0; i < samples.length; i++) {
                    samples[i] = (Math.sin(angleMain) - 0.5 * Math
                            .sin(angleHarmonic)) / 2;
                    angleMain += incMain;
                    angleHarmonic += incHarm;
                }

                audioData = new double[10 * samples.length];

                int index = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < samples.length; j++) {
                        audioData[index++] = samples[j];
                    }
                }

                updatedFreq = false;
            }

            ad.writeSamples(audioData);
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
        if ((value < MAX_FREQ) && (value > MIN_FREQ)) {
            Log.d(MainActivity.TAG, "setFreq: " + value);
            this.freq = value;
            updatedFreq = true;
            return true;
        } else
            return false;
    }

    public float getFreq() {
        return freq;
    }
}
