package com.gregmcnew.android.pax;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Update the version string.
        TextView versionInfoField = (TextView) findViewById(R.id.version);
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
		    versionInfoField.setText(String.format("version %s", packageInfo.versionName));
		} catch (NameNotFoundException e) {
			// Leave the version info string unchanged.
		}

        View v = findViewById(R.id.about_title);

        if (v != null) {
            v.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            Context c = getBaseContext();
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean(getString(R.string.debug_mode), true);
                            editor.commit();
                            Toast.makeText(c, "Debug mode enabled!", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
            );
        }
    }
}
