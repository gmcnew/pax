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

		String benchmarkModeString = getString(R.string.benchmark_mode);
		Preference pref = findPreference(benchmarkModeString);
		
		// Enable/disable options based on the value of the "benchmark mode" option.
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		updateBenchmarkModePreference(settings.getBoolean(benchmarkModeString, false));
		
		// Enable/disable other options whenever the "benchmark mode" option changes.
		pref.setOnPreferenceChangeListener(
			new OnPreferenceChangeListener() {
		        public boolean onPreferenceChange(Preference preference, Object newValue) {
		        	updateBenchmarkModePreference((Boolean) newValue);
		            return true;
		        }
			}
		);
	}
	
	public void updateBenchmarkModePreference(boolean benchmarkMode) {
	}
}
