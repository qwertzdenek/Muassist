/*
Musicians Assistant
    Copyright (C) 2012-2014  Zdeněk Janeček <jan.zdenek@gmail.com>

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

package kiv.janecekz.ma.metronome;

import android.os.SystemClock;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

/**
 * This class starts {@code Peeper} in time.
 *
 * @author Zdeněk Janeček
 */
public class Operator extends Thread implements Observer {
    private Semaphore s;
    private int bpm = TempoControl.MIN_BPM;
    private int split = 1;
    private Peeper peeper;
    private boolean loop = true;
    private boolean play = false;

    private boolean newbpm = false;
    private int beatTime;

    public Operator(Peeper peeper) {
        super();

        this.peeper = peeper;
        this.s = new Semaphore(0);
    }

    @Override
    public void run() {
        long waitStart;
        while (loop) {
            try {
                if (!play) {
                    s.acquire();
                } else {
                    peeper.run(beatTime * split, newbpm);
                    newbpm = false;
                }

                waitStart = SystemClock.elapsedRealtime();
                if (beatTime < 200) {
                    while (SystemClock.elapsedRealtime() - waitStart < beatTime)
                        ;
                } else {
                    Thread.sleep(beatTime);
                }
            } catch (InterruptedException e) {
                loop = false;
                break;
            }
        }
    }

    /**
     * Stops the thread clearly.
     */
    public void finish() {
        loop = false;
        if (!play)
            s.release();
    }

    /**
     * Toggles play. User should use this method.
     */
    public void togglePlay() {
        this.play = !play;

        if (play) {
            peeper.reset();
            s.release();
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        bpm = ((TempoControl) observable).getBPM();
        newbpm = true;
        beatTime = 60000 / (bpm * split);
    }

    /**
     * Inserts sub-beats
     *
     * @param split count of sub-beats
     */
    public void setSplit(int split) {
        this.split = split + 1;
        beatTime = 60000 / (bpm * this.split);
    }
}
