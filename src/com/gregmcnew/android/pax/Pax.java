package com.gregmcnew.android.pax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class Pax extends ActivityWithMenu {
    
    public static final boolean SELF_BENCHMARK = false;
    public static final boolean SIMPLE_BALANCE_TEST = false;
    public static final boolean BACKGROUND_IMAGE = false;
    public static final boolean FIGHTER_SPAM_TEST = false;
    public static final boolean MUSIC = false;
    public static final boolean PARTICLES = true;
    public static final boolean AI_TRAINING_MODE = false;
    
    public static final int LOG_FRAMERATE_INTERVAL_UPDATES = -1; // in ms; make negative to disable
	public static final int UPDATE_INTERVAL_MS = 40;
	
	public static final String PLAYER_ONE_AI = "playerOneAI";
	public static final String PLAYER_TWO_AI = "playerTwoAI";
	
	public static final Random sRandom = new Random();

    static {
    	if (SELF_BENCHMARK) {
    		// Make randomness deterministic in benchmark mode.
    		sRandom.setSeed(0);
    	}
    }
	
	private int mRedWins;
	private int mBlueWins;
	private int mTies;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mShakeDetector = new ShakeDetector();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        // These strings describe game-over states.
        mGameResultStrings = new HashMap<Game.State, String>();
        mGameResultStrings.put(Game.State.TIE,       "Tie game!");
        mGameResultStrings.put(Game.State.RED_WINS,  "Red wins!");
        mGameResultStrings.put(Game.State.BLUE_WINS, "Blue wins!");
        
        if (SELF_BENCHMARK) {
        	Debug.startMethodTracing("dmtrace.trace", 64 * 1024 * 1024);
        }
        
        GameSounds.initialize(this);
        
        mGame = new Game();
		
		mLastState = Game.State.IN_PROGRESS;

        mHandler = new Handler();
        mHandler.removeCallbacks(mUpdateViewTask);
        mHandler.postDelayed(mUpdateViewTask, 0);

        if (SELF_BENCHMARK) {
        	for (Player player : mGame.mPlayers) {
        		player.setAI(true);
        	}
        }
        else {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
            	mPlayerIsAI[0] = bundle.getBoolean(PLAYER_ONE_AI);
            	mPlayerIsAI[1] = bundle.getBoolean(PLAYER_TWO_AI);
            }
            else {
            	for (int i = 0; i < mPlayerIsAI.length; i++) {
            		mPlayerIsAI[i] = false;
            	}
            }
            
            mView = new GameView(this, mGame);
            setContentView(mView);
            mView.requestFocus();
            mView.setFocusableInTouchMode(true);
            
            if (MUSIC) {
            	//mMusic = MediaPlayer.create(this, R.raw.music);
            }
        }
    	
    	mRedWins = mBlueWins = mTies = 0;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (sBenchmarkMode || sBenchmarkMode != mLastBenchmarkMode) {
    		mLastBenchmarkMode = sBenchmarkMode;
    		mGame.restart();
    	}
    	
    	if (sBenchmarkMode) {
    		sRandom.setSeed(0);
    		FramerateCounter.start();
    		mGame.mPlayers[0].setAI(true);
    		mGame.mPlayers[1].setAI(true);

    		// Preferences are reapplied in every call to onResume() in
    		// ActivityWithMenu (our superclass), so it's okay to override them
    		// here.
    		sGameSpeed = GAME_SPEED_INSANE;
    		sAIDifficulty = AI.Difficulty.MEDIUM;
    		sSound = false;
    		sShowFPS = true;
    	}
    	else {
    		for (int i = 0; i < Game.NUM_PLAYERS; i++) {
    			mGame.mPlayers[i].setAI(mPlayerIsAI[i]);
    		}
    	}
    	
    	mGame.setAIDifficulty(sAIDifficulty);
		
    	if (!SELF_BENCHMARK) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    		if (MUSIC) {
    	    	mMusic.start();
    		}
    		
    		mView.onResume();
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	if (!SELF_BENCHMARK) {
    		mView.onPause();
        	if (MUSIC) {
        		mMusic.pause();
        	}
            mSensorManager.unregisterListener(mShakeDetector);
    	}
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu) {
    	super.onOptionsMenuClosed(menu);
    	mGame.resume();
    	mView.setKeepScreenOn(true);
    }
    
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	mView.setKeepScreenOn(false);
    	mGame.pause();
        return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        mView.updateRotation();
    }
    
    private float predictScore(float[] features, float[] weights) {
    	float score = 0;
    	for (int i = 0; i < features.length; i++) {
    		if (features[i] > 0) {
    			score += weights[i];
    		}
    		else if (features[i] < 0) {
    			score -= weights[i];
    		}
    	}
    	return (score < 0) ? -1 : ((score > 0) ? 1 : 0);
    }
    
    private Runnable mUpdateViewTask = new Runnable() {
    	public void run() {
    		
    		if (SELF_BENCHMARK) {
    			for (int frames = 0; frames < 1000; frames++) {
    				mGame.update(UPDATE_INTERVAL_MS);
    				
    				if (frames % 25 == 0 && mGame.getState() != Game.State.IN_PROGRESS) {
    					mGame.restart();
    				}
    			}
    			
        		updateState(Game.State.TIE);
    		}
    		else if (SIMPLE_BALANCE_TEST) {
    			for (int i = 0; i < 100; i++) {
    				mGame.mPlayers[0].mBuildTarget = Player.BuildTarget.FIGHTER;
    				mGame.mPlayers[1].mBuildTarget = Player.BuildTarget.FRIGATE;
    				Game.State state = Game.State.IN_PROGRESS;
    				while (state == Game.State.IN_PROGRESS) {
        				mGame.update(UPDATE_INTERVAL_MS);
            			state = mGame.getState();
    				}
    				
	    			switch (state) {
	    				case RED_WINS:
	    					mRedWins++;
	    					break;
	    				case BLUE_WINS:
	    					mBlueWins++;
	    					break;
	    				case TIE:
	    					mTies++;
	    					break;
	    			}
	    			Log.i(TAG, String.format("red %d, blue %d, ties %d", mRedWins, mBlueWins, mTies));
	    			mGame.restart();
    			}
    		}
    		else if (AI_TRAINING_MODE) {
    			float[] w = mGame.mPlayers[0].getAIWeightParameters();
    			float[] f = { 0f, 0f, 0f, 0f, 0f };
    			
    			for (Player player : mGame.mPlayers) {
    				player.setAI(true);
    			}
				mGame.mPlayers[1].setAIDifficulty(AI.Difficulty.MEDIUM);
    			
    			float[] x = mGame.mPlayers[1].getAIWeightParameters();
    			Log.v(TAG, String.format("other AI is using weights %f, %f, %f, %f, %f", x[0], x[1], x[2], x[3], x[4]));
    			
    			//new FileOutputStream(
    			FileWriter fw;
    			try {
					fw = new FileWriter("sdcard/savedData.txt", false);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					return;
				}
    			
    			while (true) {//for (int i = 0; i < 1000; i++) {
    				
    				// Randomize the AI's weights, making sure that weights
    				// 0, 2 and 4 are positive.
    				w[0] = sRandom.nextFloat();
    				w[2] = sRandom.nextFloat();
    				w[4] = sRandom.nextFloat();
    				w[1] = (sRandom.nextFloat() - 0.5f) * 2;
    				w[3] = (sRandom.nextFloat() - 0.5f) * 2;
    				/*
    				for (int j = 0; j < w.length; j++) {
    					w[j] = (sRandom.nextFloat() - 0.5f) * 2;
    				}
    				*/
    				
    				Game.State state = Game.State.IN_PROGRESS;
					while (state == Game.State.IN_PROGRESS) {
	    				mGame.update(25);
	        			state = mGame.getState();
					}
					
					float score = 0f;
	    			switch (state) {
	    				case RED_WINS:
	    					// Our score is the percentage of health the enemy factory has left, negated.
	    					Entity redFactory = mGame.mPlayers[1].mEntities[Ship.FACTORY].get(0);
	    					score = -(float) redFactory.health / (float) Factory.HEALTH;
	    					break;
	    				case BLUE_WINS:
	    					// Our score is the percentage of health our factory has left.
	    					Entity blueFactory = mGame.mPlayers[0].mEntities[Ship.FACTORY].get(0);
	    					score = (float) blueFactory.health / (float) Factory.HEALTH;
	    					break;
	    			}


	    			String outString = String.format("score %f with weights %f, %f, %f, %f, %f\n", score, w[0], w[1], w[2], w[3], w[4]);
	    			Log.v(TAG, outString);
	    		    try {
	    		    	fw.write(outString);
	    		    	fw.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.v(TAG, "error writing to file!");
						e.printStackTrace();
					}
	    			
					/*
	    			// Adjust weights based on the score.
	    			float delta = 0.01f;
	    			float predictedScore = predictScore(w, f);
	    			Log.v(TAG, String.format("predicted score %f (delta: %f)", predictedScore, score - predictedScore));
	    			for (int j = 0; j < w.length; j++) {
	    				f[j] += delta * (score - predictedScore) * w[j];
	    			}
	    			Log.v(TAG, String.format("new feature weights [%f, %f, %f, %f, %f, %f]", f[0], f[1], f[2], f[3], f[4], f[5]));
	    			*/
	    			
	    			mGame.restart();
    			}
    			/*
    			try {
					fw.close();
				} catch (IOException e) {
					Log.v(TAG, "error closing files");
				}
        		updateState(Game.State.TIE);
				*/
    		}
    		else {
    			// Actual game updates are performed by the rendering thread.
    			// Each time a frame is drawn, the game is updated first.
    			mView.requestRender();
	    		mHandler.postDelayed(this, UPDATE_INTERVAL_MS);
	    		
	    		updateState(mGame.getState());
	    		
	    		if (mShakeDetector.isShaking()) {
	    			mGame.restart();
	    		}
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
		if (state != mLastState) {
			mLastState = state;
			String resultString = mGameResultStrings.get(state);
			if (resultString != null) {
    			Log.v(TAG, resultString);
    			
    			String toastString = sBenchmarkMode
    					? String.format("Average framerate: %.2f", FramerateCounter.getTotalFPS())
    					: resultString;
    			
    			Toast.makeText(this, toastString, Toast.LENGTH_LONG).show();
    			if (SELF_BENCHMARK) {
    				finish();
    			}
    		}
		}
    }
    
    // To be used for log messages.
    public static final String TAG = "Pax";
    
    private boolean mPlayerIsAI[] = { false, false };
    
    private Game mGame;
    private GameView mView;
    private Game.State mLastState;
    private MediaPlayer mMusic;
    private Handler mHandler;
    
    // Keep track of the last state of the "benchmark mode" preference.
    private boolean mLastBenchmarkMode;
    
	private Map<Game.State, String> mGameResultStrings;
    
    private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
}