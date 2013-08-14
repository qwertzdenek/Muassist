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

package kiv.janecekz.ma;

import kiv.janecekz.ma.prefs.Setup;
import kiv.janecekz.ma.prefs.SharedPref;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends Activity implements
        ActionBar.OnNavigationListener, OnSharedPreferenceChangeListener {
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    public static final String TAG = "MA";

    private TouchControl touchCon;
    private AlertDialog helpDialog;
    private WakeLock wl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSharedPreferences(SharedPref.PREFS_NAME, MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);

        touchCon = TouchControl.getInstance();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1, new String[] {
                                getString(R.string.title_section_metronome),
                                getString(R.string.title_section_tone),
                                getString(R.string.title_section_tuner),
                                getString(R.string.title_section_recorder) }),
                this);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "metronome");

        // Help dialog
        AlertDialog.Builder help = new AlertDialog.Builder(this);
        help.setTitle(R.string.help);
        help.setMessage(R.string.helpText);
        help.setIcon(android.R.drawable.ic_menu_info_details);
        helpDialog = help.create();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
                .getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        menu.findItem(R.id.menu_settings).setShowAsAction(
                MenuItem.SHOW_AS_ACTION_IF_ROOM);

        Intent prefsIntent = new Intent(getApplicationContext(), Setup.class);

        menu.findItem(R.id.menu_settings).setIntent(prefsIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_help:
            helpDialog.show();
            ((TextView) helpDialog.findViewById(android.R.id.message))
                    .setMovementMethod(LinkMovementMethod.getInstance());
            return true;

        case R.id.menu_settings:
            startActivity(item.getIntent());
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given tab is selected, show the tab contents in the
        // container
        Fragment fragment = null;

        switch (position) {
        case 0:
            fragment = new MetronomeFragment();
            break;
        case 1:
            fragment = new ToneFragment();
            break;
        case 2:
            fragment = new TunerFragment();
            break;
        case 3:
            fragment = new RecorderFragment();
            break;
        default:
            break;
        }

        Fragment f = this.getFragmentManager().findFragmentByTag("metronome");
        if (f == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment).commit();
        } else {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_out, android.R.anim.fade_in);
            ft.replace(R.id.container, fragment).commit();
        }
        
        touchCon.registerOnMyEvent((IControlable) fragment);
        return true;
    }

    /**
     * Utility method to get current choosed background resource.
     * 
     * @return Resource ID.
     */
    public int getBgRes() {
        int result;
        int s = SharedPref.getTheme(this);
        switch (s) {
        case 0:
            result = R.drawable.bg_morning;
            break;
        case 1:
            result = R.drawable.bg_sunset;
            break;
        case 2:
            result = R.drawable.bg_night;
            break;

        default:
            result = -1;
            break;
        }
        return result;
    }

    public WakeLock getWakeLock() {
        return wl;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals(getResources().getString(R.string.pref_key_orientation))) {
            if (SharedPref.getOrient(this)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            }
        }
    }
}
