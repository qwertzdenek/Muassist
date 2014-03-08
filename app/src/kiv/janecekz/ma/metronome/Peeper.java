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

package kiv.janecekz.ma.metronome;

import kiv.janecekz.ma.R;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

/**
 * This class plays peeps. Lenght of measure is hidden in the {@code time}
 * variable.
 * 
 * @author Zdeněk Janeček
 * 
 */
public class Peeper {
	private final int PEEP = 1;
	private final int POP = 0;

	private final int[][] paternTable = { { PEEP }, { PEEP, POP },
			{ PEEP, POP, POP }, { PEEP, POP, POP, POP } };

	private int[] peepIds;
	private int[] popIds;

	private AlphaAnimation[] fadeAnim = new AlphaAnimation[4];
	private ImageView sun;
	private AudioTrack[] snd = new AudioTrack[2];

	private int time;
	private int phase;
	private int state;

	public Peeper(byte choosen, ImageView sun) {
		this.sun = sun;

		peepIds = new int[] { R.raw.peep1, R.raw.peep2, R.raw.peep3 };
		popIds = new int[] { R.raw.pop1, R.raw.pop2, R.raw.pop3 };

		snd[PEEP] = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				4330, AudioTrack.MODE_STATIC);
		snd[POP] = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
				4330, AudioTrack.MODE_STATIC);

		setSound(choosen);
		reset();

		fadeAnim[2*PEEP] = new AlphaAnimation(0f, 1f);
		fadeAnim[2*PEEP + 1] = new AlphaAnimation(1f, 0f);
		fadeAnim[2*POP] = new AlphaAnimation(0f, 0.5f);
		fadeAnim[2*POP + 1] = new AlphaAnimation(0.5f, 0f);
		
		for (int i = 0; i < 4; i++) {
			fadeAnim[i].setDuration(100);
		}
		
		fadeAnim[1].setFillAfter(true);
		fadeAnim[1].setStartOffset(100);
		
		fadeAnim[3].setFillAfter(true);
		fadeAnim[3].setStartOffset(100);
		
	}

	public void run() {
		state = paternTable[time][phase];

		sun.post(new Runnable() {
			@Override
			public void run() {
				snd[state].play();
				snd[state].stop();
				snd[state].reloadStaticData();
				
				sun.startAnimation(fadeAnim[state*2]);
				sun.startAnimation(fadeAnim[state*2 + 1]);
			}
		});
		phase = (phase + 1) % paternTable[time].length;
	}

	/**
	 * Sets time Measure.
	 * 
	 * @param time
	 *            It means duration of one measure.
	 */
	public void setTime(int time) {
		this.time = time - 1;
		phase = 0;
	}

	/**
	 * Time measure getter
	 * 
	 * @return Count of peeps per measure.
	 */
	public int getTime() {
		return time + 1;
	}

	public void reset() {
		phase = 0;
	}

	public void cleanup() {
		snd[PEEP].release();
		snd[POP].release();
	}

	public void setSound(byte intValue) {
		short[] peep = WavReader.readFile(sun.getContext(), peepIds[intValue]);
		short[] pop = WavReader.readFile(sun.getContext(), popIds[intValue]);

		snd[PEEP].flush();
		snd[POP].flush();
		snd[PEEP].write(peep, 0, peep.length);
		snd[POP].write(pop, 0, pop.length);
	}
}
