/*
Musicians Assistant
    Copyright (C) 2012-2014  Zdeněk Janeček <jan.zdenek@gmail.com>

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
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements
		OnSharedPreferenceChangeListener {

	private static final String STATE_SELECTED_TAB = "selected_tab";

	public static final String TAG = "MA";

	public TouchControl touchCon;

	private AlertDialog helpDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);

		getSharedPreferences(SharedPref.PREFS_NAME, MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(this);

		touchCon = TouchControl.getInstance();

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab tab = actionBar
				.newTab()
				.setText(R.string.title_section_metronome)
				.setTabListener(
						new TabListener<MetronomeFragment>(this, "metronome",
								MetronomeFragment.class));
		actionBar.addTab(tab);

		tab = actionBar
				.newTab()
				.setText(R.string.title_section_tone)
				.setTabListener(
						new TabListener<ToneFragment>(this, "tone",
								ToneFragment.class));
		actionBar.addTab(tab);

		tab = actionBar
				.newTab()
				.setText(R.string.title_section_tuner)
				.setTabListener(
						new TabListener<TunerFragment>(this, "tuner",
								TunerFragment.class));
		actionBar.addTab(tab);

		tab = actionBar
				.newTab()
				.setText(R.string.title_section_recorder)
				.setTabListener(
						new TabListener<RecorderFragment>(this, "rec",
								RecorderFragment.class));
		actionBar.addTab(tab);

		// set desired orientation
		if (SharedPref.getOrient(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}

		// Help dialog
		AlertDialog.Builder help = new AlertDialog.Builder(this);
		help.setTitle(R.string.help);
		help.setMessage(R.string.helpText);
		help.setIcon(android.R.drawable.ic_menu_info_details);
		helpDialog = help.create();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(STATE_SELECTED_TAB)) {
			getSupportActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_TAB));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_SELECTED_TAB, getSupportActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);

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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(getResources().getString(R.string.pref_key_orientation))) {
			if (SharedPref.getOrient(this)) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			}
		}
	}
}
