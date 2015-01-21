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

package ycdmdj.muassist.metronome;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import ycdmdj.muassist.R;

/**
 * Do animation and peep in the measure.
 *
 * @author Zdeněk Janeček
 */
public class Peeper {
    private final float MAX_TIME = 60000f / 30f; // Min is 30 BPM

    private final int PEEP = 0;
    private final int POP = 1;
    private final int POP2 = 2;

    private final int[][] paternTable = {
            {PEEP},
            {PEEP, POP},
            {PEEP, POP, POP},
            {PEEP, POP, POP, POP},
            {PEEP, POP2},
            {PEEP, POP2, POP, POP2},
            {PEEP, POP2, POP, POP2, POP, POP2},
            {PEEP, POP2, POP, POP2, POP, POP2, POP, POP2},
            {PEEP, POP2, POP2},
            {PEEP, POP2, POP2, POP, POP2, POP2},
            {PEEP, POP2, POP2, POP, POP2, POP2, POP, POP2, POP2},
            {PEEP, POP2, POP2, POP, POP2, POP2, POP, POP2, POP2, POP, POP2,
                    POP2},
            {PEEP, POP2, POP2, POP2},
            {PEEP, POP2, POP2, POP2, POP, POP2, POP2, POP2},
            {PEEP, POP2, POP2, POP2, POP, POP2, POP2, POP2, POP, POP2, POP2,
                    POP2},
            {PEEP, POP2, POP2, POP2, POP, POP2, POP2, POP2, POP, POP2, POP2,
                    POP2, POP, POP2, POP2, POP2}};

    private int[] peepIds = {R.raw.peep1, R.raw.peep2, R.raw.peep3};
    private int[] popIds = {R.raw.pop1, R.raw.pop2, R.raw.pop3};

    private RotateAnimation[] tickAnim = new RotateAnimation[2];
    private View pend;
    private AudioTrack sndTrack;
    short[] peep;
    short[] pop;

    private int time; // beat count in the measure
    private int phase; // FA state in patternTable
    private int state; // sound type to use
    private int split; // count of sub-beats
    private int ssplit; // actual sub-beat
    private int move; // actual pendulum's target

    public Peeper(byte choosen, View pend) {
        this.pend = pend;

        sndTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                4330, AudioTrack.MODE_STREAM);

        setSound(choosen);
    }

    /**
     * Do animation and peep.
     *
     * @param beatTime time between peeps in milliseconds
     * @param newbpm   indicates new BPM value
     */
    public void run(int beatTime, boolean newbpm) {
        // actual sound state
        state = paternTable[split * 4 + time][phase];

        switch (state)
        {
            case PEEP:
                sndTrack.write(peep, 0, peep.length);
                break;
            case POP:
            case POP2:
                sndTrack.write(pop, 0, pop.length);
                break;
        }

        if (ssplit-- == 0) {
            if (newbpm) {
                float angle = 45 * (beatTime / MAX_TIME);
                tickAnim[0] = new RotateAnimation(-angle, angle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
                tickAnim[1] = new RotateAnimation(angle, -angle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
                tickAnim[0].setFillAfter(true);
                tickAnim[1].setFillAfter(true);
            }
            move = (move + 1) % 2;
            tickAnim[move].setDuration(beatTime);

            pend.post(new Runnable() {
                @Override
                public void run() {
                    pend.startAnimation(tickAnim[move]);
                }
            });

            ssplit = split;
        }

        phase = (phase + 1) % paternTable[split * 4 + time].length;
    }

    /**
     * Sets beats in measure.
     *
     * @param time Count of beats (eg. 3/4 -> 3).
     */
    public void setBeats(int time) {
        this.time = time - 1;
        reset();
    }

    /**
     * Time measure getter
     *
     * @return Count of beats in measure.
     */
    public int getBeats() {
        return time + 1;
    }

    /**
     * Inserts sub-beats
     *
     * @param split count of sub-beats
     */
    public void setSplit(int split) {
        this.split = split;
        reset();
    }

    /**
     * Resets phase to the beginning.
     */
    public void reset() {
        phase = 0;
        ssplit = 0;
    }

    /**
     * Clears sound buffer
     */
    public void cleanup() {
        sndTrack.stop();
        sndTrack.release();
    }

    /**
     * Change sound from the library.
     *
     * @param intValue sound index (0..2)
     */
    public void setSound(byte intValue) {
        peep = WavReader.readFile(pend.getContext(), peepIds[intValue]);
        pop = WavReader.readFile(pend.getContext(), popIds[intValue]);

        sndTrack.flush();
        sndTrack.play();
    }
}
