package kiv.janecekz.ma.rec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import kiv.janecekz.ma.MainActivity;
import kiv.janecekz.ma.common.Recorder;
import kiv.janecekz.ma.common.SharedData;
import android.util.Log;

public class WavWriter extends Thread {
	private RandomAccessFile randomAccessFile;
	private SharedData sd;
	private Recorder r;

	/** sampling rate */
	private int sRate;

	/** readed size */
	private int payloadSize;

	/** maximal amplitude in the last window */
	private short maxAmplitude;

	public WavWriter(SharedData sd, Recorder r, File file) {
		super();
		this.r = r;
		this.sd = sd;
		this.sRate = r.getSampleFreq();
		try {
			setUp(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		super.run();

		short[] buffer;
		while (r.isRecording()) {
			synchronized (sd) {
				while (!sd.available)
					try {
						Log.d(MainActivity.TAG, "WavWriter: going sleep");
						sd.wait();
						Log.d(MainActivity.TAG, "Recorder: waking up");
					} catch (InterruptedException e) {
					}

				payloadSize += sd.byteBuffer.length; // Fill buffer
				try {
					randomAccessFile.write(sd.byteBuffer); // Write buffer to
				} catch (IOException e) {
					break;
				}

				maxAmplitude = Short.MIN_VALUE;
				buffer = sd.shortBuffer;
				for (int i = 0; i < buffer.length; i++) {
					maxAmplitude = (short) Math.max(maxAmplitude, buffer[i]);
				}

				sd.available = false;
				sd.notify();
			}
		}

		Log.d(MainActivity.TAG, "WavReader exiting");
		try {
			randomAccessFile.seek(4); // Write size to RIFF header
			randomAccessFile.writeInt(Integer.reverseBytes(36 + payloadSize));

			randomAccessFile.seek(40); // Write size to Subchunk2Size
										// field
			randomAccessFile.writeInt(Integer.reverseBytes(payloadSize));

			randomAccessFile.close(); // Remove prepared file
		} catch (IOException e) {
			// some log??
		}

	}

	public int getMaxAmplitude() {
		return maxAmplitude;
	}

	private void setUp(File file) throws FileNotFoundException, IOException {
		// write file header
		randomAccessFile = new RandomAccessFile(file, "rw");

		randomAccessFile.setLength(0);
		randomAccessFile.writeBytes("RIFF");

		// Final file size not known yet, write 0
		randomAccessFile.writeInt(0);
		randomAccessFile.writeBytes("WAVE");
		randomAccessFile.writeBytes("fmt ");

		// Sub-chunk size, 16 for PCM
		randomAccessFile.writeInt(Integer.reverseBytes(16));

		// AudioFormat, 1 for PCM
		randomAccessFile.writeShort(Short.reverseBytes((short) 1));

		// mono
		randomAccessFile.writeShort(Short.reverseBytes((short) 1));

		randomAccessFile.writeInt(Integer.reverseBytes(sRate));
		randomAccessFile.writeInt(Integer.reverseBytes(sRate * 2));

		// bytes per sample?
		randomAccessFile.writeShort(Short.reverseBytes((short) 2));

		randomAccessFile.writeShort(Short.reverseBytes((short) 16));
		randomAccessFile.writeBytes("data");
		randomAccessFile.writeInt(0);
	}
}
