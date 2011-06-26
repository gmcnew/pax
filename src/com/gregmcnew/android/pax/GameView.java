package com.gregmcnew.android.pax;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends GLSurfaceView {
	
	public static final long WAIT_BETWEEN_GAMES_MS = 1000;

	public GameView(Activity activity, Game game) {
		
		super(activity);
		mGame = game;
		mContext = activity;
		mRenderer = new GameRenderer(activity, mGame);
		
		setEGLConfigChooser(false);
		setRenderer(mRenderer);
		updateRotation();
	}
	
	public void updateRotation() {
		Display display = mContext.getWindowManager().getDefaultDisplay();
		mRotation = display.getOrientation();
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
			    	
			    	int selection = 0;
			    	int player = -1;
			    	int xGridPos = (int) (x * 4 / getWidth());
			    	int yGridPos = (int) (y * 4 / getHeight());
			    	
			    	if (mRotation % 2 == 0) {
			    		// Ignore clicks in the center of the screen.
			    		if (yGridPos == 0 || yGridPos == 3) {
				    		player = ((mRotation == 0) ^ (yGridPos < 2)) ? 0 : 1;
				    		selection = (int) (x * numBuildTargets / getWidth());
			    		}
			    	}
			    	else {
			    		// Ignore clicks in the center of the screen.
			    		if (xGridPos == 0 || xGridPos == 3) {
			    			player = ((mRotation == 1) ^ (xGridPos < 2)) ? 0 : 1;
			    			selection = (numBuildTargets - 1) - (int) (y * numBuildTargets / getHeight());
			    		}
			    	}
			    	
			    	if (player != -1) {
				    	Log.i("Pax:onTouch", String.format("build target: %d", selection));
				    	
				    	if ((player == 1) ^ (mRotation >= 2)) {
				    		selection = (numBuildTargets - 1) - selection;
				    	}
				    	mGame.setBuildTarget(player, Player.BuildTarget.values()[selection]);
			    	}
		    	}
    		}
    		else {
    			// The game is over, but we may need to wait before starting
    			// the next one.
    			if (mGame.mEndedTime + WAIT_BETWEEN_GAMES_MS <= SystemClock.uptimeMillis()) {
    				mGame.restart();
    			}
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
