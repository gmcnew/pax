package com.gregmcnew.android.pax;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.util.Log;
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
			    	
			    	// TODO: Allow landscape mode to work reasonably.
			    	int selection = (int) (x * numBuildTargets / getWidth());
			    	Log.i("Pax:onTouch", String.format("build target: %d", selection));
			    	
			    	Player.BuildTarget buildTarget = Player.BuildTarget.NONE;
			    	if (selection < numBuildTargets) {
			    		buildTarget = Player.BuildTarget.values()[selection];
			    	}
			    	mGame.setBuildTarget(0, buildTarget);
			    	
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
	private Renderer mRenderer;
}
