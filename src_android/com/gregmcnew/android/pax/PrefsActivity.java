package com.gregmcnew.android.pax;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

public class PrefsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		// Add debug options if debug mode is enabled.
		if (settings.getBoolean(getString(R.string.debug_mode), false)) {
			addPreferencesFromResource(R.xml.preferences_debug);
		}
	}
}
