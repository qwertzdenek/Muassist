package kiv.janecekz.ma.prefs;

import kiv.janecekz.ma.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SetupFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(SharedPref.PREFS_NAME);
        addPreferencesFromResource(R.xml.prefereces);
    }
}
