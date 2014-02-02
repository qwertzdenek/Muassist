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

import kiv.janecekz.ma.TunerFragment;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class Recorder extends Thread {
    private static final int AUDIO_SAMPLE_FREQ = 8000;
    
    private byte[] audioBuffer;
    private Short[] recordedSamples;
    private AudioRecord recorder;
    private TunerFragment t;
    private boolean recording = true;

    public Recorder(TunerFragment t) {
        this.t = t;
        
        int bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_FREQ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        
        try {
            // init recorder
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        audioBuffer = new byte[800];
        recordedSamples = new Short[audioBuffer.length / 2];
    }
    
    public int getFrameSize() {
        return recordedSamples.length;
    }
    
    public int getSampleFreq() {
        return AUDIO_SAMPLE_FREQ;
    }
    
    public Short[] getBuffer() {
        return recordedSamples;
    }
    
    public synchronized void end() {
        recording = false;
    }
    
    @Override
    public void run() {
        recorder.startRecording();
        
        int readed = 0;
        
        try {
            while (recording) {
                t.free.acquire();
                readed += recorder.read(audioBuffer, readed, audioBuffer.length - readed);
                
                if (readed < audioBuffer.length) {
                    t.free.release();
                    continue;
                }
                
                readed = 0;
                
                t.data.acquire();
                prepareResults(audioBuffer, recordedSamples);
                t.data.release();
                t.full.release();
            }
        } catch (InterruptedException e) {
            t.data.release();
            t.full.release();
        }
        
        recorder.stop();
        recorder.release();
    }
    
    public static void prepareResults(byte[] b, Short[] recs) {
        for (int i = 0, j = 0; i < b.length; i += 2, j++) {
            recs[j] = (short) ((b[i] << 8) | b[i+1]);
        }
    }
}
