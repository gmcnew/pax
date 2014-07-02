package com.gregmcnew.android.pax;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.Toast;

public class IntroActivity extends ActivityWithMenu {
	
	private static final int COUNTDOWN_SECONDS = Constants.SELF_BENCHMARK ? 0 : 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mView = new IntroView(this);
    	mTimer = new Timer();
        
        setContentView(mView);
        mView.requestFocus();
        mView.setFocusableInTouchMode(true);
        
    	reset();
    }
    
    private void reset() {
    	mPlayerOneAI = true;
    	mPlayerTwoAI = true;
		mTimerIsRunning = false;
		mMenuOpen = false;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	mView.onResume();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mView.onPause();
    	stopTimer();
    	reset();
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.restart_game).setVisible(false);
    	stopTimer();
    	reset();
    	mMenuOpen = true;
        return true;
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu) {
    	super.onOptionsMenuClosed(menu);
    	mMenuOpen = false;
    }
    
    private void startGame()
    {
    	Intent intent = new Intent(this, Pax.class);
    	intent.putExtra(Pax.PLAYER_ONE_AI, mPlayerOneAI);
    	intent.putExtra(Pax.PLAYER_TWO_AI, mPlayerTwoAI);
    	startActivity(intent);
    }
    
    private void startTimer() {
    	if (!mTimerIsRunning) {
    		mTimerIsRunning = true;
    		int countdownMs = COUNTDOWN_SECONDS * 1000;
    		mGameStartTime = SystemClock.uptimeMillis() + countdownMs;
    		mTimer.schedule(new StartGameTask(), COUNTDOWN_SECONDS * 1000);
    	}
    }
    
    private void stopTimer() {
    	// Cancel the countdown.
        if (mTimerIsRunning) {
        	mTimerIsRunning = false;
        	mTimer.cancel();
        	mTimer = new Timer();
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        mView.updateRotation();
    }
    
    public void resetPreferences() {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }
    
    private class StartGameTask extends TimerTask {
		@Override
		public void run() {
			startGame();
		}
    }
    
    private boolean mMenuOpen;
    private IntroView mView;
    private Timer mTimer;
    private long mGameStartTime;
    private boolean mTimerIsRunning;
    private boolean mPlayerOneAI;
    private boolean mPlayerTwoAI;
    
    
    // Private classes
    
    private class IntroRenderer extends Renderer {

		public IntroRenderer(IntroActivity activity) {
			super(activity);
			mActivity = activity;
			mStarField = new StarField();
		}
		
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			super.onSurfaceCreated(gl, config);
			mStarPainter = getPainter(gl, R.drawable.star);
			mTitlePainter = getPainter(gl, R.drawable.title);
			mCirclePainter = getPainter(gl, R.drawable.circle);
			mSmokePainter = getPainter(gl, R.drawable.smoke);
			mNumberPainters = new Painter[10];
			mNumberPainters[0] = getPainter(gl, R.drawable.char_gold_0);
			mNumberPainters[1] = getPainter(gl, R.drawable.char_gold_1);
			mNumberPainters[2] = getPainter(gl, R.drawable.char_gold_2);
			mNumberPainters[3] = getPainter(gl, R.drawable.char_gold_3);
			mNumberPainters[4] = getPainter(gl, R.drawable.char_gold_4);
			mNumberPainters[5] = getPainter(gl, R.drawable.char_gold_5);
			mNumberPainters[6] = getPainter(gl, R.drawable.char_gold_6);
			mNumberPainters[7] = getPainter(gl, R.drawable.char_gold_7);
			mNumberPainters[8] = getPainter(gl, R.drawable.char_gold_8);
			mNumberPainters[9] = getPainter(gl, R.drawable.char_gold_9);
		}
		
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			super.onSurfaceChanged(gl, width, height);
			
	        gl.glMatrixMode(GL10.GL_PROJECTION);
	        gl.glLoadIdentity();
	        
	        float halfX = width / 2;
	        float halfY = height / 2;
	        
	        GLU.gluOrtho2D(gl, -halfX, halfX, -halfY, halfY);
	        
	        gl.glMatrixMode(GL10.GL_MODELVIEW);
	        gl.glLoadIdentity();
		}
		
		@Override
		public void onDrawFrame(GL10 gl) {
			long dt = FramerateCounter.tick();
			if (!mActivity.mMenuOpen) {
				mStarField.update(dt);
			}
			
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

	        // This seems to be necessary to avoid camera offset problems when the
	        // screen is rotated while the game is paused (and rendering is thus
	        // paused as well). TODO: Figure this out.
	        gl.glViewport(0, 0, (int) mScreenWidth, (int) mScreenHeight);
	        
			float minDimension = mScreenWidth < mScreenHeight ? mScreenWidth : mScreenHeight;
			float maxDimension = mScreenWidth > mScreenHeight ? mScreenWidth : mScreenHeight;
			
			float rotationDegrees = -mRotation * 90;
			
			float buttonSize = maxDimension / 8;
			
			float fadeAlpha = 1f;
			float countdownAlpha = 0f;
			int secondsLeft = 0;
			
			if (mActivity.mTimerIsRunning) {
				long msLeft = mActivity.mGameStartTime - SystemClock.uptimeMillis();
				if (msLeft > 0) {
					
					countdownAlpha = ((float) (msLeft % 1000)) / 1000;
					secondsLeft = (int) Math.ceil(((float) msLeft) / 1000);
					
					if (msLeft < 1000) {
						fadeAlpha = countdownAlpha;
					}
				}
				else {
					fadeAlpha = 0f;
				}
			}
			
			if (!Constants.sFadeOutIntro) {
				fadeAlpha = 1f;
			}
			
			drawStars(gl, mStarField, mStarPainter, mScreenWidth, mScreenHeight, fadeAlpha);
			
			float buttonXPos = 0;
			float buttonYPos = (mRotation % 2 == 0 ? mScreenHeight: mScreenWidth) / 3;
			
			// Draw a glow behind each button that represents a human player.
			float glowSize = (buttonSize * 3);
			if (!mPlayerOneAI) {
				mSmokePainter.draw(gl, -buttonXPos, -buttonYPos, glowSize, glowSize, 0, fadeAlpha);
			}
			if (!mPlayerTwoAI) {
				mSmokePainter.draw(gl, buttonXPos, buttonYPos, glowSize, glowSize, 0, fadeAlpha);
			}
			
			// Draw the title text
			mTitlePainter.draw(gl, 0, 0, minDimension / 2, minDimension / 2, rotationDegrees, fadeAlpha);

			float flip = (mRotation < 2) ? 1 : -1;
			float numberSize = maxDimension / 20;
			float numberXPos = flip * ((mRotation % 2 == 0) ? 0 : -maxDimension / 6);
			float numberYPos = flip * ((mRotation % 2 != 0) ? 0 : -maxDimension / 6);
			
			// Draw the countdown.
			mNumberPainters[secondsLeft].draw(gl, numberXPos, numberYPos, numberSize, numberSize, rotationDegrees, countdownAlpha);
			
			// Draw buttons
			float[][] c = Painter.TEAM_COLORS;
			for (int i = 0; i < 2; i++) {
				int rot = (i == 1 ? 1 : -1);
				mCirclePainter.draw(gl, buttonXPos * rot, buttonYPos * rot, buttonSize, buttonSize, 180 * i, 1f);
				mCirclePainter.draw(gl, buttonXPos * rot, buttonYPos * rot, buttonSize * .85f, buttonSize * .85f, 180 * i, 1f, c[i][0], c[i][1], c[i][2]);

				// Draw a black circle on top to fade the button out.
				mCirclePainter.draw(gl, buttonXPos * rot, buttonYPos * rot, buttonSize, buttonSize, 180 * i, 1 - fadeAlpha, 0, 0, 0);
			}
		}
		
		private StarField mStarField;
		private IntroActivity mActivity;
		private Painter mStarPainter;
		private Painter mNumberPainters[];
		private Painter mTitlePainter;
		private Painter mCirclePainter;
		private Painter mSmokePainter;
    }
    
    private class IntroView extends GLSurfaceView {

		private IntroView(IntroActivity activity) {
			super(activity);
			mActivity = activity;
			
			mRenderer = new IntroRenderer(activity);
			
			setEGLConfigChooser(false);
			setRenderer(mRenderer);
		}
		
		private void updateRotation() {
			Display display = mActivity.getWindowManager().getDefaultDisplay();
			mRotation = display.getOrientation();
			mRenderer.updateRotation(mRotation);
			requestRender();
		}
		
		@Override
		public void onResume() {
			super.onResume();
			updateRotation();
		}

		private void clearTutorialSetting(SharedPreferences settings) {
			Editor editor = settings.edit();
			editor.putBoolean(getString(R.string.tutorial_prompt_setting), false);
			editor.commit();
		}

		private boolean showTutorial() {
			final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

			boolean tutorialPrompt = settings.getBoolean(getString(R.string.tutorial_prompt_setting), true);
			if (!tutorialPrompt) {
				return false;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setMessage(getString(R.string.tutorial_prompt));
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					// TODO: Launch a tutorial activity.
					Toast.makeText(mActivity, "Sorry, buddy, you're on your own!", Toast.LENGTH_LONG).show();
					dialog.cancel();
					clearTutorialSetting(settings);
				}
			});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					clearTutorialSetting(settings);
				}
			});
			AlertDialog alert = builder.create();
			alert.show();

			return true;
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {

			// We don't care which pointer was pressed.
			int action = event.getAction() & MotionEvent.ACTION_MASK;

			if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
					&& !mActivity.mMenuOpen
					&& !showTutorial()) {

				for (int i = 0; i < event.getPointerCount(); i++) {

					// Divide the screen into three sections.
					// A touch in section 0 means player 2 should be human.
					// A touch in section 2 means player 1 should be human.
					int xSection = (int) (event.getX(i) * 3 / getWidth());
					int ySection = (int) (event.getY(i) * 3 / getHeight());
					int section = (mRotation % 2 == 0 ? ySection : xSection);

					if (mRotation >= 2) {
						section = 2 - section;
					}

					if (section == 1) {
						// The user clicked in the middle of the screen, even
						// though that doesn't do anything. They must not know
						// what they're doing -- maybe the options menu will
						// help.
						openOptionsMenu();
					}
					else {
						if (section == 0) {
			    			mActivity.mPlayerTwoAI = false;
						}
						else {
			    			mActivity.mPlayerOneAI = false;
						}

			    		mActivity.startTimer();
						setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
					}
				}
			}

			// We consumed the event.
			return true;
		}

		private int mRotation;
		private final IntroRenderer mRenderer;
		private final IntroActivity mActivity;
    }
}
