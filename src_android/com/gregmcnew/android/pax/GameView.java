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

	public GameView(ActivityWithMenu activity, Game game) {
		
		super(activity);
		mGame = game;
		mContext = activity;
		mRenderer = new GameRenderer(activity, mGame, mContext.mLandscapeDevice);
		
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
		
    	// We don't care which pointer was pressed.
    	int action = event.getAction() & MotionEvent.ACTION_MASK;
    	
    	if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
    		Log.i("Pax:onTouch", String.format("event has %d pointers", event.getPointerCount()));
    		
    		if (mGame.getState() == Game.State.IN_PROGRESS) {
    	    	for (int i = 0; i < event.getPointerCount(); i++) {
    	    		handleGameTouchEvent(event.getX(i), event.getY(i));
    	    	}
    		}
    		else {
    			// The game is over, but we may need to wait before starting
    			// the next one.
    			if (mGame.getMsSinceEnd() > WAIT_BETWEEN_GAMES_MS) {
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
	
	private void handleGameTouchEvent(float x, float y) {
		
    	// Ignore touches in benchmark mode.
    	if (Constants.sBenchmarkMode) {
    		return;
    	}
    	
    	// Ignore the "NONE" build target.
    	int numBuildTargets = Player.sBuildTargetValues.length - 1;

		if (!Constants.sAllowUpgrades)
		{
			numBuildTargets--;
		}

    	if (mContext.mLandscapeDevice) {
    		y = getHeight() - y;
    	}

    	int selection = 0;
    	boolean inRange = true;
    	int player = -1;

		float rx = x / getWidth();
		float ry = y / getHeight();

		if (!mContext.mLandscapeDevice) {
			if ((mRotation % 2 == 0)) {
				int gridPos = (int) (ry * 4);
				switch (gridPos) {
					case 0:
						player = 1;
						rx = 1 - rx;
						break;
					case 3:
						player = 0;
						break;
					default:
						inRange = false;
				}

				selection = (int) (rx * numBuildTargets);

				if (mRotation == 2) {
					player = 1 - player;
				}
			}
			else {
				int gridPos = (int) (rx * 4);
				switch (gridPos) {
					case 0:
						player = 1;
						break;
					case 3:
						player = 0;
						ry = 1 - ry;
						break;
					default:
						inRange = false;
				}

				selection = (int) (ry * numBuildTargets);

				if (mRotation == 3) {
					player = 1 - player;
				}
			}
		}

		Log.i("Pax:onTouch", String.format("click target: %d, %f, %f", mRotation, x, y));
		Log.i("Pax:onTouch", String.format("click target: %d, %d, %d (%d)", inRange ? 1 : 0, player, selection, numBuildTargets));

    	if (player != -1 && inRange) {
	    	Log.i("Pax:onTouch", String.format("build target: %d", selection));

	    	mGame.setBuildTargetIfHuman(player, Player.sBuildTargetValues[selection]);
    	}
	}
	
	private Game mGame;
	private ActivityWithMenu mContext;
	private GameRenderer mRenderer;
	private int mRotation;
}
