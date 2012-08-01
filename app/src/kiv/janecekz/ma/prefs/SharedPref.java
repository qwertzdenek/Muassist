/*
Musicians Assistant
    Copyright (C) 2012  Zdeněk Janeček <jan.zdenek@gmail.com>

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

package kiv.janecekz.ma.prefs;

import kiv.janecekz.ma.R;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    public final static String PREFS_NAME = "prefs";

    // /**
    // * If is metronome started, this variable will be true. Controlled by the
    // * Preference Activity.
    // *
    // * @param context
    // * Application Context
    // * @return saved value from the Shared Preferences.
    // */
    // public static boolean getNotify(Context context) {
    // SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    // return prefs.getBoolean(context.getString(R.string.pref_key_notify),
    // true);
    // }

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
                context.getString(R.string.pref_key_bg), "0"));
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
     * Value change speed coeficient. Higher means slower speed.
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static int getSpeed(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return Integer.parseInt(prefs.getString(
                context.getString(R.string.pref_key_speed), "70"));
    }

    /**
     * Base frequency for A tone.
     * 
     * @param context
     *            Application Context
     * @return saved value from the Shared Preferences.
     */
    public static int getBaseFreq(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return Integer.parseInt(prefs.getString(
                context.getString(R.string.pref_key_tone), "440"));
    }
    
    /**
     * Metronome tempo. Setted by user, and freely modifiable.
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
     * Metronome tempo setter. Setted by user, and freely modifiable.
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
     * Metronom time meter getter. Setted by user and freely modifiable.
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
     * Metronom time meter setter. Setted by user and freely modifiable.
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

    // /**
    // * Tone generator frequency. Setted by user and freely modifiable.
    // *
    // * @param context
    // * Application Context
    // * @return saved value from the Shared Preferences.
    // */
    // public static int getFreq(Context context) {
    // SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    // return prefs.getInt("freq", 440);
    // }
    //
    // /**
    // * Tone generator frequency. Setted by user and freely modifiable.
    // *
    // * @param context
    // * Application Context
    // * @return saved value from the Shared Preferences.
    // */
    // public static void setFreq(Context context, int value) {
    // SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    // SharedPreferences.Editor edit = prefs.edit();
    // edit.putInt("freq", value);
    // edit.commit();
    // }

    // /**
    // * Indicates running metronome. Setted by the Metronome fragment.
    // *
    // * @param context
    // * Application Context
    // * @return saved value from the Shared Preferences.
    // */
    // public static boolean getPlay(Context context) {
    // SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    // return prefs.getBoolean("play", false);
    // }
    //
    // /**
    // * Indicates running metronome. Setted by the Metronome fragment.
    // *
    // * @param context
    // * Application Context
    // * @return saved value from the Shared Preferences.
    // */
    // public static void setPlay(Context context, boolean value) {
    // SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
    // SharedPreferences.Editor edit = prefs.edit();
    // edit.putBoolean("play", value);
    // edit.commit();
    // }
}
