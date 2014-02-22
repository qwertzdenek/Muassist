package kiv.janecekz.ma.rec;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import kiv.janecekz.ma.common.Recorder;

public class WavWriter extends Thread {
	private RandomAccessFile randomAccessFile;
	private Recorder r;

	/** sampling rate */
	private int sRate;

	/** readed size */
	private int payloadSize;

	/** maximal amplitude in the last window */
	private Short maxAmplitude;

	/** indicates running state */
	private boolean writting;

	public WavWriter(Recorder r, String file) {
		super();
		this.r = r;
		this.sRate = r.getSampleFreq();
		try {
			setUp(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		super.run();

		while (writting) {
			try {
				r.full.acquire();
				payloadSize += r.getByteBuffer().length; // Fill buffer
				randomAccessFile.write(r.getByteBuffer()); // Write buffer to file
			} catch (Exception e) {
				release();
			}
			maxAmplitude = Short.MIN_VALUE;
			for (Short sample: r.getBuffer()) {
				maxAmplitude = (short) Math.max(maxAmplitude, sample);
			}
			r.free.release();
		}
	}

	/**
	 * Finishes file and stops thread.
	 */
	public void release() {
		writting = false;

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

	private void setUp(String file) throws FileNotFoundException, IOException {
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
