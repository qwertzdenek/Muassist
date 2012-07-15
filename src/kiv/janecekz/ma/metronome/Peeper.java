/*
Metronom for Android
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

package kiv.janecekz.ma.control;

import kiv.janecekz.metronome.R;
import kiv.janecekz.metronome.prefs.SharedPref;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

/**
 * This class plays peeps. Lenght of measure is hidden in the {@code time}
 * variable.
 * 
 * @author nox
 * 
 */
public class Peeper {
    private final int SOUNDS_COUNT = 3;
    private final int PEEP = 1;
    private final int POP = 0;

    private final int[][] paternTable = { { PEEP }, { PEEP, POP },
            { PEEP, POP, POP }, { PEEP, POP, POP, POP } };
    private final MediaPlayer[] peepPlayers = new MediaPlayer[SOUNDS_COUNT];
    private final MediaPlayer[] popPlayers = new MediaPlayer[SOUNDS_COUNT];;
    private Context context;

    private final ObjectAnimator[] fadeAnim;
    private final ImageView sun;

    private MediaPlayer[] sounds;
    private byte choosenSound = 0;

    private int time;
    private int phase;
    private int state;

    public Peeper(Context context, ImageView sun) {
        peepPlayers[0] = MediaPlayer.create(context, R.raw.peep1);
        peepPlayers[1] = MediaPlayer.create(context, R.raw.peep2);
        peepPlayers[2] = MediaPlayer.create(context, R.raw.peep3);
        popPlayers[0] = MediaPlayer.create(context, R.raw.pop1);
        popPlayers[1] = MediaPlayer.create(context, R.raw.pop2);
        popPlayers[2] = MediaPlayer.create(context, R.raw.pop3);

        this.sun = sun;

        fadeAnim = new ObjectAnimator[2];
        fadeAnim[PEEP] = ObjectAnimator.ofFloat(this.sun, "alpha", 0f, 1f,
                0.3f, 0f);
        fadeAnim[PEEP].setDuration(100);
        fadeAnim[PEEP].setInterpolator(new LinearInterpolator());

        fadeAnim[POP] = ObjectAnimator.ofFloat(this.sun, "alpha", 0f, 0.5f,
                0.2f, 0f);
        fadeAnim[POP].setDuration(100);
        fadeAnim[POP].setInterpolator(new LinearInterpolator());

        sounds = new MediaPlayer[2];
        setSound(choosenSound);

        phase = 0;

        this.context = context;
        setTime(SharedPref.getTime(this.context));
    }

    public void run() {
        state = paternTable[time][phase];
        sun.post(new Runnable() {

            public void run() {
                fadeAnim[state].start();
            }
        });
        sounds[state].start();
        phase = (phase + 1) % paternTable[time].length;
    }

    /**
     * Sets time Measure.
     * 
     * @param time
     *            It means duration of one measure.
     */
    public void setTime(int time) {
        if ((time >= 1) && (time <= paternTable.length)) {
            SharedPref.setTime(context, time);
            this.time = time - 1;
            phase = 0;
        }
    }

    public void reset() {
        phase = 0;
    }

    public void cleanup() {
        for (int i = 0; i < sounds.length; i++) {
            sounds[i].release();
        }
    }

    public void setSound(byte intValue) {
        if ((intValue >= 0) && (intValue < SOUNDS_COUNT)) {
            choosenSound = intValue;
            sounds[PEEP] = peepPlayers[choosenSound];
            sounds[POP] = popPlayers[choosenSound];
        }
    }
}
