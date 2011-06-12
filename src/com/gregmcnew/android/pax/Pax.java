package com.gregmcnew.android.pax;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class Pax extends Activity implements OnClickListener, OnKeyListener, OnTouchListener {
    
    public final static boolean SELF_BENCHMARK = false;
    public final static boolean BENCHMARK = false;
	
	static int UPDATE_INTERVAL_MS = 40;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (SELF_BENCHMARK) {
        	Debug.startMethodTracing("dmtrace.trace", 32 * 1024 * 1024);
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	if (BENCHMARK) {
    		try {
				Thread.sleep(3000, 0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
        
        mGame = new Game();
        
        mView = new GameView(this, mGame);
        
        setContentView(mView);
        mView.setOnClickListener(this);
        mView.setOnKeyListener(this);
        mView.setOnTouchListener(this);
        
        mView.setFocusable(true);
        mView.setFocusableInTouchMode(true);
        
        //mLastResult = Dish.Result.IN_PROGRESS;
        
        mHandler = new Handler();
        mHandler.removeCallbacks(mUpdateViewTask);
        
        mRandom = new Random();
		mRandom.setSeed(0);
		
		mLastState = Game.State.IN_PROGRESS;
    	
    	mFrames = 0;
    	
        mHandler.postDelayed(mUpdateViewTask, 0);
        
    	mGame.setBuildTarget(1, Player.BuildTarget.BOMBER);
    }
    
    private Random mRandom;
    
    private Runnable mUpdateViewTask = new Runnable() {
    	public void run() {
    		
    		if (SELF_BENCHMARK) {
    			for (mFrames = 0; mFrames < 100; mFrames++) {
        	    	//Log.i("Petri:run", String.format("frame %d", mFrames));
	    			if (mFrames == 10) {
	    				float gridWidth = 2;
	    				float gridHeight = 3;
	    				for (int x = 0; x < gridWidth; x++) {
	    					for (int y = 0; y < gridHeight; y++) {
	    						float xPos = mView.getWidth() * ((0.5f + x) / gridWidth);
	    						float yPos = mView.getHeight() * ((0.5f + y) / gridHeight);
	    						xPos += mRandom.nextInt(30) - 15;
	    						yPos += mRandom.nextInt(30) - 15;
	    						//addBlob(xPos, yPos); 
	    					}
	    				}
	    			}
	    			mView.invalidate();
    			}
    			
    			updateState(Game.State.TIE);
    			return;
    		}
    		else if (BENCHMARK) {
    			
    			if (mFrames % 10 == 1) {
    				//addBlob(mRandom.nextInt(mView.getWidth()), mRandom.nextInt(mView.getHeight()));
    			}
    			
        		mView.invalidate();
	    		mHandler.postDelayed(this, UPDATE_INTERVAL_MS);
	    		
	    		updateState(mGame.getState());
	    		
	    		if (mFrames > 200) {
	    			updateState(Game.State.TIE);
	    		}
	    		
	    		mFrames++;
    		}
    		else {
        		mView.invalidate();
	    		mHandler.postDelayed(this, UPDATE_INTERVAL_MS);
	    		
	    		updateState(mGame.getState());
    		}
    	}
    };

    @Override
    public void onDestroy() {
    	if (SELF_BENCHMARK) {
    		Debug.stopMethodTracing();
    	}
    	else if (BENCHMARK) {
    		try {
				Thread.sleep(3000, 0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	super.onDestroy();
    }
    
    public void updateState(Game.State state) {
		if (mLastState == Game.State.IN_PROGRESS) {
    		if (state != mLastState) {
    			mLastState = state;
    			Toast.makeText(this, "The game is over!", Toast.LENGTH_LONG).show();
    			finish();
    		}
		}
    }
    
    public boolean onTouch(View v, MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
    		Log.i("Petri:onTouch", String.format("event has %d pointers", event.getPointerCount()));
	    	for (int i = 0; i < event.getPointerCount(); i++) {
		    	float x = event.getX(i);
		    	//float y = event.getY(i);
		    	
		    	// Ignore the "NONE" build target.
		    	int numBuildTargets = Player.BuildTarget.values().length - 1;
		    	
		    	// TODO: Allow landscape mode to work reasonably.
		    	int selection = (int) (x * numBuildTargets / mView.getWidth());
		    	
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
    	
    	// We consumed the event.
    	return true;
    }
    
    public void onClick(View v) {
    	//Log.i("onClick", "Click detected");
    }

	public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
		//Log.i("onKey", String.format("%d, %s", keyCode, keyEvent.toString()));
		finish();
		return true;
	}
	
	/*
	private boolean addBlob(float x, float y) {
		Blob.Team[] teams = Blob.Team.values();
		Blob.Team team = teams[mNextTeamIndex];
		mNextTeamIndex++;
		if (mNextTeamIndex > Blob.LAST_TEAM_INDEX) {
			mNextTeamIndex = Blob.FIRST_TEAM_INDEX;
		}
		
		return mDish.addBlob(team, x, y);
	}
	*/

	// Benchmarking variables
    private int mFrames;
    
    private Game mGame;
    private GameView mView;
    private Handler mHandler;
    private Game.State mLastState;
}