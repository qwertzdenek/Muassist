package kiv.janecekz.ma.common;

public class SharedData {
	public byte[] byteBuffer;
	public short[] shortBuffer;
	public boolean available = false;
	
	public SharedData(int windowSize) {
		byteBuffer = new byte[windowSize * 2];
		shortBuffer = new short[windowSize];
	}
}
