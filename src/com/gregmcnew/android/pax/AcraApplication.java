package com.gregmcnew.android.pax;

import org.acra.*;
import org.acra.annotation.*;

import android.app.Application;

@ReportsCrashes(formKey = "dHZFcmN2UFpxZzNsS1EwRHZNWDVNZ3c6MQ")
public class AcraApplication extends Application {
	
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();
	}
}