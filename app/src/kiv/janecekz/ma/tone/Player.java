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


public class Player extends Thread {
    private boolean loop;
    private boolean play = false;

    public static final double PI2 = 2 * Math.PI;
    public static final int SAMPLE_FREQ = 11025;
    public static final double MIN_FREQ = 10f;
    public static final double MAX_FREQ = 4000f;

    private float freq;
    private double deltaHarmStart = 0;
    private double deltaHarm5Start = 0;
    private double deltaMainStart = 0;
    private int per = 10;
    private AudioDevice ad;
//    private BufferedWriter buf;

    /**
     * Constructor for the tone generator. To start use start() and the
     * togglePlay() to start/stop.
     */
    public Player() {
        ad = new AudioDevice(SAMPLE_FREQ);

//        try {
//            buf = new BufferedWriter(new FileWriter("/sdcard/sine.csv"));
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
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

            double sampleLength = SAMPLE_FREQ / freq;
            double incMain = (PI2 * freq) / SAMPLE_FREQ;
            double incHarm = (PI2 * freq * 3) / SAMPLE_FREQ;
            double incHarm5 = (PI2 * freq * 5) / SAMPLE_FREQ;

            // Start at what was not finished at last sampling part.
            double angleMain = deltaMainStart;
            double angleHarmonic = deltaHarmStart;
            double angleHarmonic5 = deltaHarm5Start;

            double[] samples = new double[(int) (per * sampleLength)];
            for (int i = 0; i < samples.length; i++) {
                samples[i] = (Math.sin(angleMain) +
                        0.5 * Math.sin(angleHarmonic) +
                        0.3 * Math.sin(angleHarmonic5)
                              ) / 3;
                
                angleMain += incMain;
                angleHarmonic += incHarm;
                angleHarmonic5 += incHarm5;
            }

            deltaMainStart = Math.ceil(per * sampleLength) * incMain - per
                    * PI2;
            deltaHarmStart = Math.ceil(per * sampleLength) * incHarm - per
                    * PI2;
            deltaHarm5Start = Math.ceil(per * sampleLength) * incHarm5 - per
                    * PI2;
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
//                // TODO Auto-generated catch block
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
        if ((value < MAX_FREQ) && (value > MIN_FREQ)) {
            this.freq = value;
            return true;
        } else
            return false;
    }

    public float getFreq() {
        return freq;
    }
}
