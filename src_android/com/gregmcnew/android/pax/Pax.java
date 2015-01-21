package com.gregmcnew.android.pax;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Pax extends ActivityWithMenu {
    
    public static final int LOG_FRAMERATE_INTERVAL_UPDATES = -1; // in ms; make negative to disable
	public static final int UPDATE_INTERVAL_MS = 40;
	
	public static final String PLAYER_ONE_AI = "playerOneAI";
	public static final String PLAYER_TWO_AI = "playerTwoAI";
    
    // To be used for log messages.
    public static final String TAG = "Pax";

    static {
    	if (Constants.SELF_BENCHMARK) {
    		// Make randomness deterministic in benchmark mode.
    		Game.sRandom.setSeed(0);
    	}
    }
	
	private int mRedWins;
	private int mBlueWins;
	private int mTies;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // These strings describe game-over states.
        mGameResultStrings = new HashMap<Game.State, String>();
        mGameResultStrings.put(Game.State.TIE,       "Tie game!");
        mGameResultStrings.put(Game.State.RED_WINS,  "Red wins!");
        mGameResultStrings.put(Game.State.BLUE_WINS, "Blue wins!");
        
        if (Constants.SELF_BENCHMARK) {
        	Debug.startMethodTracing("dmtrace.trace", 64 * 1024 * 1024);
        }
        
        GameSounds.initialize(this);
        
        mGame = new Game(mLandscapeDevice ? (float) -Math.PI / 2 : 0);
		
		mLastState = Game.State.IN_PROGRESS;

        mHandler = new Handler();
        
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        
        mNumUpdates = 0;

        if (Constants.SELF_BENCHMARK) {
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
            
            if (Constants.MUSIC) {
            	//mMusic = MediaPlayer.create(this, R.raw.music);
            }
        }
    	
    	mRedWins = mBlueWins = mTies = 0;
    }
    
	@Override
	public void onResume() {
    	super.onResume();

		if (Constants.sBenchmarkMode || Constants.sBenchmarkMode != mLastBenchmarkMode) {
			mLastBenchmarkMode = Constants.sBenchmarkMode;
			mGame.restart();
		}

		if (Constants.sBenchmarkMode) {
			FramerateCounter.start();
			mGame.mPlayers[0].setAI(true);
			mGame.mPlayers[1].setAI(true);

			// Preferences are reapplied in every call to onResume() in
			// ActivityWithMenu (our superclass), so it's okay to override them
			// here.
			Constants.sGameSpeed = Constants.GAME_SPEED_INSANE;
			Constants.sAIDifficulty = AI.Difficulty.MEDIUM;
			Constants.sSound = false;
			Constants.sDebugMode = true;
		}
		else {
			for (int i = 0; i < Game.NUM_PLAYERS; i++) {
				mGame.mPlayers[i].setAI(mPlayerIsAI[i]);
			}
		}

		mGame.setAIDifficulty(Constants.sAIDifficulty);

		if (!Constants.SELF_BENCHMARK) {
			if (Constants.MUSIC) {
				mMusic.start();
			}

			mView.onResume();
			setScreenPowerState(mGame.getState());
		}

		mHandler.postDelayed(mUpdateViewTask, 0);
	}

    @Override
    public void onPause() {
    	super.onPause();
        mHandler.removeCallbacks(mUpdateViewTask);
    	if (!Constants.SELF_BENCHMARK) {
    		mView.onPause();
        	if (Constants.MUSIC) {
        		mMusic.pause();
        	}
    	}
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu) {
    	super.onOptionsMenuClosed(menu);
    	mGame.resume();
    	setScreenPowerState(mGame.getState());
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tutorial:
            	// TODO: Add a tutorial.
            	Toast.makeText(this, "Sorry, buddy, you're on your own!", Toast.LENGTH_LONG).show();
            	break;
            case R.id.settings:
                Intent prefsIntent = new Intent(this, PrefsActivity.class);
                startActivity(prefsIntent);
                break;
            case R.id.restart_game:
            	mGame.restart();
            	break;
        }
        return true;
    }
    
    /*
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
    */
    
    private Runnable mUpdateViewTask = new Runnable() {
    	public void run() {
    		
    		if (Constants.SELF_BENCHMARK) {
    			for (int frames = 0; frames < 1000; frames++) {
    				mGame.update(UPDATE_INTERVAL_MS);
    				
    				if (frames % 25 == 0 && mGame.getState() != Game.State.IN_PROGRESS) {
    					mGame.restart();
    				}
    			}
    			
        		updateState(Game.State.TIE);
    		}
    		else if (Constants.SIMPLE_BALANCE_TEST) {
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
    		else if (Constants.AI_TRAINING_MODE) {
    			for (Player player : mGame.mPlayers) {
    				player.setAI(true);
    			}
				mGame.mPlayers[1].setAIDifficulty(AI.Difficulty.MEDIUM);
    			
    			AIWeights x = mGame.mPlayers[1].getAIWeights();
    			Log.v(TAG, String.format("other AI is using weights %f, %f, %f, %f, %f", x.w[0], x.w[1], x.w[2], x.w[3], x.w[4]));
    			
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
    				
    				mGame.mPlayers[0].randomizeAIWeights();
    				
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

	    			AIWeights weights = mGame.mPlayers[0].getAIWeights();

	    			String outString = String.format("score %f with weights %f, %f, %f, %f, %f\n", score,
	    					weights.w[0], weights.w[1], weights.w[2], weights.w[3], weights.w[4]);
	    			
	    			Log.v(TAG, outString);
	    		    try {
	    		    	fw.write(outString);
	    		    	fw.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.v(TAG, "error writing to file!");
						e.printStackTrace();
					}
	    			
	    			mGame.restart();
    			}
    		}
    		else {
    			// Actual game updates are performed by the rendering thread.
    			// Each time a frame is drawn, the game is updated first.
    			mView.requestRender();
	    		mHandler.postDelayed(this, UPDATE_INTERVAL_MS);
	    		
	    		updateState(mGame.getState());
	    		
	    		if (Constants.sBenchmarkMode) {
	    			if (mNumUpdates > 500) {
	    				updateState(Game.State.TIE);
	    				mGame.restart();
	    				mNumUpdates = 0;
	    			}
	    		}
	    		
	    		mNumUpdates++;
    		}
    	}
    };

    @Override
    public void onDestroy() {
    	if (Constants.SELF_BENCHMARK) {
    		Debug.stopMethodTracing();
    	}
    	super.onDestroy();
    }
    
    public void updateState(Game.State state) {
		if (state != mLastState) {
			mLastState = state;
			
			setScreenPowerState(state);
			
			String resultString = mGameResultStrings.get(state);
			if (resultString != null) {
    			Log.v(TAG, resultString);

				if (Constants.sBenchmarkMode) {
					mToast.setText(String.format("Average framerate: %.2f", FramerateCounter.getTotalFPS()));
					mToast.show();
				}
    			
    			if (Constants.SELF_BENCHMARK) {
    				finish();
    			}
    		}
		}
    }
    
    private void setScreenPowerState(Game.State state) {
    	mView.setKeepScreenOn(Game.State.IN_PROGRESS == state);
    }
    
    private boolean mPlayerIsAI[] = { false, false };
    
    private Game mGame;
    private GameView mView;
    private Game.State mLastState;
    private MediaPlayer mMusic;
    private Handler mHandler;
    
    // Keep track of the last state of the "benchmark mode" preference.
    private boolean mLastBenchmarkMode;
    
	private Map<Game.State, String> mGameResultStrings;
    
    private Toast mToast;
    
    private int mNumUpdates;
}