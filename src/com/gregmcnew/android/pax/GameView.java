package com.gregmcnew.android.pax;

import android.app.Activity;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends GLSurfaceView {

	public GameView(Activity activity, Game game) {
		
		super(activity);
		mGame = game;
		mContext = activity;
		mRenderer = new GameRenderer(activity, mGame);
		
		setEGLConfigChooser(false);
		setRenderer(mRenderer);
		updateRotation();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		updateRotation();
	}
	
	private void updateRotation() {
		Display display = mContext.getWindowManager().getDefaultDisplay();
		mRotation = display.getRotation();
		mRenderer.updateRotation(mRotation);
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
    		Log.i("Pax:onTouch", String.format("event has %d pointers", event.getPointerCount()));
    		
    		if (mGame.getState() == Game.State.IN_PROGRESS) { 
		    	for (int i = 0; i < event.getPointerCount(); i++) {
			    	float x = event.getX(i);
			    	float y = event.getY(i);
			    	Log.i("Pax:onTouch", String.format("(%f, %f)", x, y));
			    	// Ignore the "NONE" build target.
			    	int numBuildTargets = Player.BuildTarget.values().length - 1;
			    	
			    	int selection = numBuildTargets;
			    	if (mRotation % 2 == 0) {
			    		if (y >= getHeight() * 0.67) {
			    			selection = (int) (x * numBuildTargets / getWidth());
			    		}
			    	}
			    	else {
			    		if (x >= getWidth() * 0.67) {
			    			selection = (numBuildTargets - 1) - (int) (y * numBuildTargets / getHeight());
			    		}
			    	}
			    	Log.i("Pax:onTouch", String.format("build target: %d", selection));
			    	
			    	if (selection < numBuildTargets) {
				    	mGame.setBuildTarget(0, Player.BuildTarget.values()[selection]);
			    	}
			    	
			    	/*
			    	if (addBlob(x, y)) {
			    		mView.addGlow(x, y);
			    		mView.invalidate();
			    	}
			    	*/
		    	}
    		}
    		else {
				mGame.restart();
    		}
    	}
    	
    	// We consumed the event.
    	return true;
    }
    
    public void onClick(View v) {
    	Log.i("onClick", "Click detected");
    }

	public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
		Log.i("onKey", String.format("%d, %s", keyCode, keyEvent.toString()));
		mContext.finish();
		return true;
	}
	
	private Game mGame;
	private Activity mContext;
	private GameRenderer mRenderer;
	private int mRotation;
}
