package com.gregmcnew.android.pax;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class UpgradeActivity extends Activity implements DialogInterface.OnClickListener {

	private static final String APP_NAME = "Pax";
	private static final String SOURCE_DIR_URL = "http://philosoph.us/misc/android";
	private static final String UPDATE_FILE_URL = "http://philosoph.us/misc/android/latest.txt";
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
	}
    
    private static final String CHECK_TITLE = "Checking";
    private static final String CHECK_MESSAGE = "Checking for new versions of " + APP_NAME;
    
    private static final String DOWNLOAD_TITLE = "Downloading";
    private static final String DOWNLOAD_MESSAGE = "Getting the latest version of " + APP_NAME;
    
    private static final int CHECK_ERROR = -1;
    private static final int DOWNLOAD_ERROR = -2;
    private static final int UP_TO_DATE = 201;
    private static final int DOWNLOAD_STARTING = 202;
    
    public void onStart() {
    	super.onStart();
    	mProgressDialog = new ProgressDialog(this);
    	mProgressDialog.setTitle(CHECK_TITLE);
    	mProgressDialog.setMessage(CHECK_MESSAGE);
    	
    	mProgressDialog.setIndeterminate(true);
    	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }
    
    public void onResume() {
    	super.onResume();
    	
    	mProgressDialog.show();
    	
    	mDestination = Environment.getExternalStorageDirectory() + "/" + APP_NAME + ".apk";
    	
    	Downloader downloadFile = new Downloader();
    	downloadFile.execute(SOURCE_DIR_URL, mDestination);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mProgressDialog.dismiss();
    }
    
    protected ProgressDialog mProgressDialog;
    private String mDestination;
    
    private void runInstall() {

		Intent upgradeIntent = new Intent(Intent.ACTION_VIEW);
    	upgradeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		upgradeIntent.setDataAndType(
				Uri.fromFile(new File(mDestination)),
				"application/vnd.android.package-archive");
	    startActivity(upgradeIntent);
	    finish();
    }

	@Override
	public void onClick(DialogInterface dialog, int which) {
		finish();
	}
	
	private String[] checkLatestVersion() {
		
		InputStream input;
		try {
			URL url = new URL(UPDATE_FILE_URL);
			URLConnection connection = url.openConnection();
	        connection.connect();
			
			input = new BufferedInputStream(url.openStream());
		}
		catch (MalformedURLException e1) {
			return null;
		}
		catch (IOException e) {
			return null;
		}

		byte data[] = new byte[1024];
		
		StringBuffer output = new StringBuffer();
		
		int count;
		int bytesTransferred = 0;
		
		try {
			while ((count = input.read(data)) != -1) {
				bytesTransferred += count;
				
				for (int i = 0; i < count; i++) {
					output.append((char) data[i]);
				}
			}
		}
		catch (IOException e) {
		}
		
		Log.v(Pax.TAG, "file contents (" + bytesTransferred + " bytes): " + output.toString());
		String[] outputTokens = output.toString().split("\\s");
		
		return outputTokens;
	}
	
	private boolean versionStringIsNewer(String latestVersion) throws Exception {
		boolean upgrade = false;
		
		PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
		String version = packageInfo.versionName;
		
		Log.v(Pax.TAG, String.format("current version: '%s'", version));
		Log.v(Pax.TAG, String.format(" latest version: '%s'", latestVersion));
		
		String[] versionTokens = version.split("\\.");
		String[] latestVersionTokens = latestVersion.split("\\.");
		if (versionTokens.length != latestVersionTokens.length) {
			throw new Exception(String.format(
					"versions '%s' and '%s' aren't of equal length", version, latestVersion));
		}
		
		for (int i = 0; (i < versionTokens.length) && !upgrade; i++) {
			if (Integer.decode(versionTokens[i]) < Integer.decode(latestVersionTokens[i])) {
				upgrade = true;
			}
		}
		
		Log.v(Pax.TAG, String.format(upgrade
				? "current version is out of date"
				: "current version is up to date"));
        
        return upgrade;
	}
	
	private void showDialog(String title, String message) {
		AlertDialog d = new AlertDialog.Builder(this).create();
		d.setTitle(title);
		d.setMessage(message);
		d.setButton("OK", this);
		d.show();
	}
	
	private void updateProgress(int progress) {
    	if (CHECK_ERROR == progress) {
    		showDialog("Error", "Could not determine the latest version.");
    	}
    	else if (DOWNLOAD_ERROR == progress) {
    		showDialog("Error", "Could not download the latest version.");
    	}
    	else if (UP_TO_DATE == progress) {
    		showDialog(null, APP_NAME + " is up to date!");
    	}
    	else if (DOWNLOAD_STARTING == progress) {
    		mProgressDialog.setTitle(DOWNLOAD_TITLE);
    		mProgressDialog.setMessage(DOWNLOAD_MESSAGE);
        	mProgressDialog.setMax(100);
        	mProgressDialog.setProgress(0);
    	}
    	else {
        	mProgressDialog.setIndeterminate(false);
    		mProgressDialog.setProgress(progress);
    		
    		if (progress == 100) {
    			runInstall();
    		}
    	}
	}
    
    private class Downloader extends AsyncTask<String, Integer, String> {
    	
    	@Override
    	protected void onProgressUpdate(Integer... values) {
    		updateProgress(values[0]);
    	}
    	
		@Override
		protected String doInBackground(String... args) {
		    int count;
			
			int bytesTransferred = 0;

	        String sourceDir = args[0];
	        String destination = args[1];
	        
	        boolean upgrade = false;
	        String latestFile;
	        try {
	        	String[] tokens = checkLatestVersion();
	        	String latestVersion = tokens[0];
	        	latestFile = tokens[1];
	        	upgrade = versionStringIsNewer(latestVersion);
	        }
	        catch (Exception e) {
	        	Log.e(Pax.TAG, e.toString());
			    publishProgress(CHECK_ERROR);
			    return null;
	        }
	        
		    if (!upgrade) {
		    	publishProgress(UP_TO_DATE);
		    }
		    else {
			    publishProgress(DOWNLOAD_STARTING);
			    try {
			        URL url = new URL(sourceDir + "/" + latestFile);
			        
			        URLConnection connection = url.openConnection();
			        connection.connect();
			        
					int fileSize = connection.getContentLength();
					
					InputStream input = new BufferedInputStream(url.openStream());
					OutputStream output = new FileOutputStream(destination);
					
					byte data[] = new byte[1024];
					
					while ((count = input.read(data)) != -1) {
						bytesTransferred += count;
						
					    output.write(data, 0, count);
					    
					    publishProgress(bytesTransferred * 100 / fileSize);
					}
					
			        output.flush();
			        output.close();
			        input.close();
				    
				    publishProgress(100);
			    }
			    catch (MalformedURLException e) {
			    	Log.v(Pax.TAG, e.toString());
				    publishProgress(DOWNLOAD_ERROR);
			    }
			    catch (IOException e) {
			    	Log.v(Pax.TAG, e.toString());
				    publishProgress(DOWNLOAD_ERROR);
				}
	        }
	        
    		return null;
		}
    }
}
