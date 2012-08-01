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

import kiv.janecekz.ma.R;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class Player extends Thread implements OnClickListener {
    private boolean loop;
    private boolean play = false;

    final static double PI2 = 2 * Math.PI;
    final static int SAMPLE_FREQ = 11025;

    static double piDivSampleFreq;

    double[] audioData;
    double angleMain = 0;
    double angleHarmonic = 0;
    float freq;
    boolean updatedFreq;
    int sampleLength;
    AudioDevice ad;
    TextView act;

    /**
     * Constructor for the tone generator. To start use start() and the
     * togglePlay() to start/stop.
     * 
     * @param v
     *            LinearLayout for tone choose.
     */
    public Player(ViewGroup l) {
        ad = new AudioDevice(SAMPLE_FREQ);
        piDivSampleFreq = PI2 / SAMPLE_FREQ;

        for (int i = 0; i < l.getChildCount(); i++) {
            TextView v = (TextView) l.getChildAt(i);
            v.setOnClickListener(this);
        }

        // Defaulting to the 440 Hz.
        act = ((TextView) l.findViewById(R.id.toneA));
        act.setTextColor(act.getResources().getColor(
                android.R.color.holo_red_light));
        freq = 440f;
        updatedFreq = true;
    }

    public synchronized void togglePlay() {
        this.play = !play;
        if (play) {
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
                double incMain = piDivSampleFreq * freq;
                double incHarm = piDivSampleFreq * 2 * freq;

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
        loop = true;
        super.start();
    }

    public synchronized void onClick(View arg0) {
        TextView v = (TextView) arg0;
        act.setTextColor(act.getResources().getColor(
                android.R.color.holo_blue_light));

        float freq = 0;
        switch (v.getId()) {
        case R.id.toneC:
            freq = 261.63f;
            break;
        case R.id.toneD:
            freq = 293.66f;
            break;
        case R.id.toneE:
            freq = 329.23f;
            break;
        case R.id.toneF:
            freq = 349.23f;
            break;
        case R.id.toneG:
            freq = 392f;
            break;
        case R.id.toneA:
            freq = 440f;
            break;
        case R.id.toneB:
            freq = 493.88f;
            break;

        default:
            break;
        }

        v.setTextColor(v.getResources()
                .getColor(android.R.color.holo_red_light));
        AnimationSet push = (AnimationSet) AnimationUtils.loadAnimation(v.getContext(),
                R.anim.push);
        v.startAnimation(push);

        act = v;
        this.freq = freq;

        // starting when selected
        if (!play) {
            play = true;
            ad.resume();
            notify();
        }
        updatedFreq = true;
    }
}
