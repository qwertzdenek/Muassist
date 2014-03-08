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

package kiv.janecekz.ma.common;

import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.metronome.WavReader;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class Recorder extends Thread {
	private static final int AUDIO_SAMPLE_FREQ = 44100;

	private AudioRecord recorder;
	private SharedData sd;
	private boolean recording = true;

	/**
	 * Constructs simple recording class assigned to the one Fragment.
	 * 
	 * @param windowSize
	 *            recordedSamples size in short
	 */
	public Recorder(SharedData sd) {
		super();
		this.sd = sd;
	}

	@Override
	public void run() {
		super.run();

		int bufferSize = 3 * AudioRecord.getMinBufferSize(AUDIO_SAMPLE_FREQ,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

		try {
			// init recorder
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bufferSize);

			if (recorder.getState() != AudioRecord.STATE_INITIALIZED)
				throw new Exception("AudioRecord initialization failed");
		} catch (IllegalArgumentException e) {
			Log.d(MainActivity.TAG, "Recorder: invalid argument");
		} catch (Exception e) {
			Log.d(MainActivity.TAG, e.getMessage());
		}

		recorder.startRecording();
		recorder.read(sd.byteBuffer, 0, sd.byteBuffer.length);
		
		while (isRecording()) {
			synchronized (sd) {
				while (sd.available)
					try {
						sd.wait();
					} catch (InterruptedException e) {
						break;
					}
				
				recorder.read(sd.byteBuffer, 0, sd.byteBuffer.length);
				
				WavReader.prepareResults(sd.byteBuffer, sd.shortBuffer);
				
				sd.available = true;
				sd.notify();
			}
		}

		recorder.release();
		recorder = null;
	}

	/**
	 * @return sampling frequency used in this Recorder.
	 */
	public int getSampleFreq() {
		return AUDIO_SAMPLE_FREQ;
	}

	/**
	 * Indicates recording state
	 * 
	 * @return true if is recording
	 */
	public boolean isRecording() {
		return recording;
	}
	
	public void stopRecording() {
		recording = false;
	}
}
