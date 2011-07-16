package com.gregmcnew.android.pax;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (Pax.sBlackBackground) { 
        	ImageView backgroundImage = (ImageView) findViewById(R.id.background_image);
        	backgroundImage.setVisibility(View.INVISIBLE);
        }
        
        // Update the version string.
        TextView versionInfoField = (TextView) findViewById(R.id.version);
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
		    versionInfoField.setText(String.format("version %s", packageInfo.versionName));
		} catch (NameNotFoundException e) {
			// Leave the version info string unchanged.
		}
    }
}
