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

package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.TunerFragment;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class Recorder {
    private static final int AUDIO_SAMPLE_FREQ = 8000;

    private byte[] audioBuffer;
    private Short[] recordedSamples;
    private AudioRecord recorder;
    private TunerFragment t;

    /**
     * Constructs simple recording class assigned to the one Fragment.
     * 
     * @param t Fragment to assign. It uses his Semaphore locks.
     */
    public Recorder(TunerFragment t) {
        this.t = t;

        int bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_FREQ,
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

        audioBuffer = new byte[800];
        recordedSamples = new Short[audioBuffer.length / 2];

        recorder.setRecordPositionUpdateListener(updateListener);
        recorder.setPositionNotificationPeriod(audioBuffer.length / 2);
    }

    /**
     * @return sampling frequency used in this Recorder.
     */
    public int getSampleFreq() {
        return AUDIO_SAMPLE_FREQ;
    }

    /**
     * @return Buffer where are saved recorded samples. For thread safety,
     *         acquire TunerFragment.data lock.
     */
    public Short[] getBuffer() {
        return recordedSamples;
    }

    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        @Override
        public void onPeriodicNotification(AudioRecord recorder) {
            try {
                t.free.acquire();
                recorder.read(audioBuffer, 0, audioBuffer.length);

                t.data.acquire();
                prepareResults(audioBuffer, recordedSamples);
                t.data.release();
                t.full.release();
            } catch (InterruptedException e) {
                t.data.release();
                t.full.release();
            }
        }

        @Override
        public void onMarkerReached(AudioRecord recorder) {
            // NOT USED
        }
    };

    /**
     * Starts recording to the buffer returned by getBuffer().
     */
    public void start() {
        recorder.startRecording();
        recorder.read(audioBuffer, 0, audioBuffer.length);
    }

    /**
     * Stops and release device.
     */
    public void stop() {
        recorder.stop();
        recorder.release();
    }

    private static void prepareResults(byte[] b, Short[] recs) {
        for (int i = 0, j = 0; i < b.length; i += 2, j++) {
            recs[j] = (short) (b[i] | (b[i + 1] << 8));
        }
    }
}
