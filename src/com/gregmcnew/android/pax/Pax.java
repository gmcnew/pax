package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class Pax extends Activity {
    
    public static final boolean SELF_BENCHMARK = false;
    public static final boolean SIMPLE_BALANCE_TEST = false;
    public static final boolean BACKGROUND_IMAGE = false;
    public static final boolean MUSIC = true;
    public static final boolean FIGHTER_SPAM_TEST = false;
    public static final boolean PARTICLES = true;
    public static final boolean FPS_METER = true;
    
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
	            if (bundle.getBoolean(PLAYER_ONE_AI)) {
	        		mGame.mPlayers[0].setAI(true);
	            }
	            if (bundle.getBoolean(PLAYER_TWO_AI)) {
	        		mGame.mPlayers[1].setAI(true);
	            }
            }
            
            mView = new GameView(this, mGame);
            setContentView(mView);
            mView.requestFocus();
            mView.setFocusableInTouchMode(true);
            
            if (MUSIC) {
            	mMusic = MediaPlayer.create(this, R.raw.music);
            }
        }
    	
    	mRedWins = mBlueWins = mTies = 0;
    }
    
    private void applyPreferences() {
        Resources res = getResources();
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	Map<String, Game.Speed> gameSpeeds = new HashMap<String, Game.Speed>();
    	gameSpeeds.put(res.getString(R.string.game_speed_normal), Game.Speed.NORMAL);
    	gameSpeeds.put(res.getString(R.string.game_speed_fast),   Game.Speed.FAST);
    	gameSpeeds.put(res.getString(R.string.game_speed_insane), Game.Speed.INSANE);
    	Game.Speed gameSpeed = gameSpeeds.get(settings.getString("game_speed_preference", null));
    	mGame.setGameSpeed(gameSpeed == null ? Game.Speed.NORMAL : gameSpeed);
    	
    	Map<String, Player.AIDifficulty> aiDifficulties = new HashMap<String, Player.AIDifficulty>();
    	aiDifficulties.put(res.getString(R.string.ai_difficulty_easy),   Player.AIDifficulty.EASY);
    	aiDifficulties.put(res.getString(R.string.ai_difficulty_medium), Player.AIDifficulty.MEDIUM);
    	aiDifficulties.put(res.getString(R.string.ai_difficulty_hard),   Player.AIDifficulty.HARD);
    	Player.AIDifficulty aiDifficulty = aiDifficulties.get(settings.getString("ai_difficulty_preference", null));
    	mGame.setAIDifficulty(aiDifficulty == null ? Player.AIDifficulty.EASY : aiDifficulty);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	if (!SELF_BENCHMARK) {
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    		if (MUSIC) {
    	    	mMusic.start();
    		}
    		mView.onResume();
    	}
        
        applyPreferences();
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
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        return true;
    }
    
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
        Intent i = new Intent(this, PrefsActivity.class);
        startActivity(i);
        
        return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        mView.updateRotation();
    }
    
    private Runnable mUpdateViewTask = new Runnable() {
    	public void run() {
    		
    		if (SELF_BENCHMARK) {
    			for (int frames = 0; frames < 300; frames++) {
    				mGame.update(UPDATE_INTERVAL_MS);
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
    		else {
        		mView.invalidate();
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
    			Toast.makeText(this, resultString, Toast.LENGTH_LONG).show();
    			if (SELF_BENCHMARK) {
    				finish();
    			}
    		}
		}
    }
    
    // To be used for log messages.
    public static final String TAG = "Pax";
    
    private Game mGame;
    private GameView mView;
    private Game.State mLastState;
    private MediaPlayer mMusic;
    private Handler mHandler;
    
	private Map<Game.State, String> mGameResultStrings;
    
    private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
}