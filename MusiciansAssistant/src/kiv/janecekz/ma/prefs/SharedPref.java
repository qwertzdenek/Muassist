package kiv.janecekz.ma.prefs;

import kiv.janecekz.ma.R;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    public final static String PREFS_NAME = "prefs";

    /**
     * If is metronome started, this variable will be true. Controlled by the
     * Preference Activity.
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static boolean getNotify(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(context.getString(R.string.pref_key_notify),
                true);
    }

    /**
     * If is screen locked in the portrait mode, this variable will be true.
     * Controlled by the Preference Activity.
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static boolean getOrient(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.pref_key_orientation), true);
    }

    /**
     * This variable consists the integer values that represents one background
     * color. Controlled by the Preference Activity.
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static int getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return Integer.parseInt(prefs.getString(
                context.getString(R.string.pref_key_bg), "2"));
    }

    /**
     * This variable consists the integer values that represents one sound
     * theme. Controlled by the Preference Activity.
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static int getSound(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return Integer.parseInt(prefs.getString(
                context.getString(R.string.pref_key_sound), "0"));
    }

    /**
     * Metronome tempo. Set by user, and freely modifiable.
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static int getBPM(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getInt("bpm", 80);
    }

    /**
     * Metronome tempo setter. Set by user, and freely modifiable.
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static void setBPM(Context context, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("bpm", value);
        edit.commit();
    }

    /**
     * 
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static int getTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getInt("time", 1);
    }

    /**
     * 
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static void setTime(Context context, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("time", value);
        edit.commit();
    }

    /**
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static boolean getPlay(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean("play", false);
    }

    /**
     * 
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static void setPlay(Context context, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("play", value);
        edit.commit();
    }
}
