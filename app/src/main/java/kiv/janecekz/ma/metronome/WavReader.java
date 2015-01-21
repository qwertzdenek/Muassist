package kiv.janecekz.ma.metronome;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;

import kiv.janecekz.ma.MainActivity;

public class WavReader {
    public static short[] readFile(Context c, int resId) {
        byte[] mint = new byte[4];
        BufferedInputStream f = new BufferedInputStream(c.getResources()
                .openRawResource(resId));

        byte[] samples;
        short[] samplRes;
        try {
            f.skip(40);
            f.read(mint);

            int size = (int) (u_byte(mint[0]) | (u_byte(mint[1]) << 8)
                    | (u_byte(mint[2]) << 16) | (u_byte(mint[3]) << 24));

            samples = new byte[size];
            samplRes = new short[size >> 1];
            f.read(samples);
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "WavReader: file cannot be opened");
            return null;
        }
        prepareResults(samples, samplRes);

        return samplRes;
    }

    /**
     * Transform endianity.
     *
     * @param src input wave
     * @param dst output wave
     */
    public static void prepareResults(byte[] src, short[] dst) {
        for (int i = 0, j = 0; i < src.length; i += 2, j++) {
            dst[j] = (short) (u_byte(src[i]) | (u_byte(src[i + 1]) << 8));
        }
    }

    /**
     * Converts unsigned byte to int
     *
     * @param b byte value
     * @return integer value
     */
    private static int u_byte(byte b) {
        return b & 0xFF;
    }
}
