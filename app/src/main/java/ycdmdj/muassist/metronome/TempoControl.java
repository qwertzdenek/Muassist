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

package ycdmdj.muassist.metronome;

import java.util.Observable;

/**
 * This class sends to all observers actual BPM.
 *
 * @author Zdeněk Janeček
 */
public class TempoControl extends Observable {
    public static final int MAX_BPM = 220;
    public static final int MIN_BPM = 30;

    private int bpm;

    public void setBPM(int bpm) {
        if ((bpm >= MIN_BPM) && (bpm <= MAX_BPM)) {
            this.bpm = bpm;
            setChanged();
            notifyObservers();
        }
    }

    public int getBPM() {
        return bpm;
    }

    public void refreshObservers() {
        setChanged();
        notifyObservers();
    }
}
