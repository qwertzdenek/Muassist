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

package kiv.janecekz.ma.metronome;

import java.util.Observable;
import java.util.Observer;

/**
 * This class controls {@code Peeper} and WakeLock.
 * 
 * @author Zdeněk Janeček
 */
public class Operator extends Thread implements Observer {
    private int bpm = TempoControl.MIN_BPM;
    private Peeper peeper;
    private boolean loop;
    private boolean play = false;

    public Operator(Peeper peeper) {
        super();

        this.peeper = peeper;
    }

    @Override
    public void run() {
        while (loop) {
            try {
                pauseLoop();
                Thread.sleep(60000 / bpm);
            } catch (InterruptedException e) {
                loop = false;
                break;
            }
            peeper.run();
        }
    }

    private synchronized void pauseLoop() throws InterruptedException {
        if (!play) {
            wait();
        }
    }

    @Override
    public void start() {
        loop = true;
        peeper.reset();
        super.start();
    }

    public synchronized void finish() {
        loop = false;
    }

    /**
     * Toggles play. User should use this method.
     */
    public synchronized void togglePlay() {
        this.play = !play;
        
        if (play) {
            notify();
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        TempoControl t = (TempoControl) observable;
        bpm = t.getBPM();
    }

    public int getBpm() {
        return bpm;
    }
}
