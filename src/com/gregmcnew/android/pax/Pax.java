package com.gregmcnew.android.pax;

import java.util.Random;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.widget.Toast;

public class Pax extends Activity {
    
    public static final boolean SELF_BENCHMARK = false;
	
	public static final int UPDATE_INTERVAL_MS = 40;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (SELF_BENCHMARK) {
        	Debug.startMethodTracing("dmtrace.trace", 64 * 1024 * 1024);
        }
    	
    	thisContext = this;
        
        mGame = new Game();
        
        
        mRandom = new Random();
		mRandom.setSeed(0);
		
		mLastState = Game.State.IN_PROGRESS;

        mHandler = new Handler();
        mHandler.removeCallbacks(mUpdateViewTask);
        mHandler.postDelayed(mUpdateViewTask, 0);

        if (SELF_BENCHMARK) {
        	mGame.setBuildTarget(0, Player.BuildTarget.FIGHTER);
        }
        else {
        	mView = new GameView(this, mGame);
            mView.setEGLConfigChooser(false);
            mView.setRenderer(new GameRenderer(this, mGame));
            setContentView(mView);
            mView.requestFocus();
            mView.setFocusableInTouchMode(true);
            mPlayer = MediaPlayer.create(this, R.raw.music);
            mPlayer.start();
        }
    	mGame.setBuildTarget(0, Player.BuildTarget.FIGHTER);
    	mGame.setBuildTarget(1, Player.BuildTarget.BOMBER);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	mPlayer.start();
    	if (!SELF_BENCHMARK) {
    		mView.onResume();
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mPlayer.pause();
    	if (!SELF_BENCHMARK) {
    		mView.onPause();
    	}
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){        
        super.onConfigurationChanged(newConfig);
        mView.onConfigurationChanged(newConfig);
    }
    
    private Random mRandom;
    
    private Runnable mUpdateViewTask = new Runnable() {
    	public void run() {
    		
    		if (SELF_BENCHMARK) {
    			for (int frames = 0; frames < 300; frames++) {
    				mGame.update();
    				/*
	    			mView.invalidate();
		    		mHandler.postDelayed(this, UPDATE_INTERVAL_MS);
		    		*/
    				
    				if (frames % 25 == 0 && mGame.getState() != Game.State.IN_PROGRESS) {
    					mGame.restart();
    				}
    			}
    			
        		updateState(Game.State.TIE);
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
    	super.onDestroy();
    }
    
    public void updateState(Game.State state) {
		if (mLastState == Game.State.IN_PROGRESS) {
    		if (state != mLastState) {
    			mLastState = state;
    			String resultString = null;
    			switch (state) {
    				case TIE:
    					resultString = "Tie game!";
    					break;
    				case RED_WINS:
    					resultString = "Red wins!";
    					break;
    				case BLUE_WINS:
    					resultString = "Blue wins!";
    					break;
    				default:
    					resultString = "The game is over!";
    			}
    			Toast.makeText(this, resultString, Toast.LENGTH_LONG).show();
    			if (SELF_BENCHMARK) {
    				finish();
    			}
    		}
		}
    }
    
    private Game mGame;
    private GameView mView;
    private Game.State mLastState;
    private MediaPlayer mPlayer;
    private Handler mHandler;
    public static Pax thisContext;
}