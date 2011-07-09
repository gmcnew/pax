package com.gregmcnew.android.pax;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
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

public class IntroActivity extends Activity {
	
	private static final int COUNTDOWN_SECONDS = 3;
	
	public IntroActivity() {
		super();
	}
	
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
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	mView.updateRotation();
    	mView.onResume();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mView.onPause();
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
        
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        return true;
    }
    
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);

    	// Stop the countdown.
    	stopTimer();
    	
        Intent i = new Intent(this, PrefsActivity.class);
        startActivity(i);
        
        return true;
    }
    
    private class StartGameTask extends TimerTask {
		@Override
		public void run() {
			startGame();
		}
    }
    
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
		}
		
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			super.onSurfaceCreated(gl, config);
			mBackgroundPainter = getPainter(gl, R.drawable.background);
			mTitlePainter = getPainter(gl, R.drawable.title);
			mBlueButtonPainter = getPainter(gl, R.drawable.ohblue);
			mRedButtonPainter = getPainter(gl, R.drawable.ohred);
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
			float minDimension = mScreenWidth < mScreenHeight ? mScreenWidth : mScreenHeight;
			float maxDimension = mScreenWidth > mScreenHeight ? mScreenWidth : mScreenHeight;
			
			float rotationDegrees = -mRotation * 90;
			
			float numberSize = maxDimension / 20;
			float buttonSize = maxDimension / 8;
			
			// Draw the background
			float halfWidth = mScreenWidth / 2;
			float halfHeight = mScreenHeight / 2;
			mBackgroundPainter.drawFillBounds(gl, -halfWidth, halfWidth, -halfHeight, halfHeight, rotationDegrees, 1f);
			
			float flip = (mRotation >= 2) ? -1 : 1;
			float buttonXPos = flip * 0;
			float buttonYPos = flip * (mRotation % 2 == 0 ? mScreenHeight: mScreenWidth) / 3;
			
			// Draw a glow behind each button that represents a human player.
			float glowSize = (float) (buttonSize * 4);
			if (!mPlayerOneAI) {
				mSmokePainter.draw(gl, -buttonXPos, -buttonYPos, glowSize, glowSize, 0, 1f);
			}
			if (!mPlayerTwoAI) {
				mSmokePainter.draw(gl, buttonXPos, buttonYPos, glowSize, glowSize, 0, 1f);
			}
			
			// Draw buttons
			mBlueButtonPainter.draw(gl, -buttonXPos, -buttonYPos, buttonSize, buttonSize, 180, 1f);
			mRedButtonPainter.draw(gl, buttonXPos, buttonYPos, buttonSize, buttonSize, 0, 1f);
			
			// Draw the title text
			mTitlePainter.draw(gl, 0, 0, minDimension / 2, minDimension / 2, rotationDegrees, 1f);
			
			// Draw the countdown indicator
			if (mActivity.mTimerIsRunning) {
				long msLeft = mActivity.mGameStartTime - SystemClock.uptimeMillis();
				if (msLeft > 0) {
					float numberXPos = flip * ((mRotation % 2 == 0) ? 0 : -maxDimension / 6);
					float numberYPos = flip * ((mRotation % 2 != 0) ? 0 : -maxDimension / 6);
					
					float alpha = ((float) (msLeft % 1000)) / 1000;
					int secondsLeft = (int) Math.ceil(((float) msLeft) / 1000);
					
					mNumberPainters[secondsLeft].draw(gl, numberXPos, numberYPos, numberSize, numberSize, rotationDegrees, alpha);
				}
			}
		}
		
		private IntroActivity mActivity;
		private Painter mNumberPainters[];
		private Painter mBackgroundPainter;
		private Painter mTitlePainter;
		private Painter mBlueButtonPainter;
		private Painter mRedButtonPainter;
		private Painter mSmokePainter;
    }
    
    private class IntroView extends GLSurfaceView {

		private IntroView(IntroActivity activity) {
			super(activity);
			mActivity = activity;
			
			mRenderer = new IntroRenderer(activity);
			
			setEGLConfigChooser(false);
			setRenderer(mRenderer);
			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
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
		
		@Override
		public void onPause() {
			super.onPause();
			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
			mActivity.reset();
		}
		
		@Override
	    public boolean onTouchEvent(MotionEvent event) {
			
	    	// We don't care which pointer was pressed.
	    	int action = event.getAction() & MotionEvent.ACTION_MASK;
	    	
	    	if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
	    		
		    	for (int i = 0; i < event.getPointerCount(); i++) {
		    		
		    		// Divide the screen into three sections.
		    		// A touch in section 0 means player 2 should be human. 
		    		// A touch in section 2 means player 1 should be human.
		    		int xSection = (int) (event.getX() * 3 / getWidth());
		    		int ySection = (int) (event.getY() * 3 / getHeight());
		    		int section = (mRotation % 2 == 0 ? ySection : xSection);
		    		
		    		if (section != 1) {
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
