package kiv.janecekz.ma.common;

public class SharedData {
    /**
     * Array of bytes in Little endian (WAV format)
     */
    public byte[] byteBuffer;

    /**
     * Samples in 16bit short
     */
    public short[] shortBuffer;

    /**
     * True if data available
     */
    public boolean available = false;

    public SharedData(int windowSize) {
        byteBuffer = new byte[windowSize * 2];
        shortBuffer = new short[windowSize];
    }
}
