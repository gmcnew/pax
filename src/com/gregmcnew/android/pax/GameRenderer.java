package com.gregmcnew.android.pax;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

public class GameRenderer implements GLSurfaceView.Renderer {
	
	private static final float BG_RED   = 0.094f;
	private static final float BG_GREEN = 0.137f;
	private static final float BG_BLUE  = 0.145f;
	
	// The size of the screen's largest dimension, measured in game units. 
	public static final float GAME_VIEW_SIZE = 1000f;
	
	// Draw factories at the bottom, with frigates above them, bombers above
	// frigates, and fighters above everything.
	private static final int ENTITY_LAYERS[] = {
			Entity.FACTORY, Entity.FRIGATE, Entity.BOMBER, Entity.FIGHTER,
			Entity.LASER, Entity.BOMB, Entity.MISSILE
			};
    
    public GameRenderer(Context context, Game game) {
    	mContext = context;
    	mGame = game;
    	
    	mPainters = new HashMap<Integer, Painter>();
    }
    
    public void updateRotation(int rotation) {
    	mRotation = rotation;
    	Log.v(Pax.TAG, String.format("rotation is now %d", rotation));
    }
    
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
    	gl.glEnable(GL10.GL_TEXTURE_2D);
    	gl.glShadeModel(GL10.GL_SMOOTH);
    	gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
    	
    	gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    	
    	// Enable the use of vertex and texture arrays.
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		// Enable texture transparency.
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		// Print information about the OpenGL driver.
		{
			Log.v("GameViewGL.onSurfaceCreated", "OpenGL version: " + gl.glGetString(GL10.GL_VERSION));
			
			Log.v("GameViewGL.onSurfaceCreated", "OpenGL extensions:");
			String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
			for (String extension : extensions.split(" ")) {
				Log.v("GameViewGL.onSurfaceCreated", "  " + extension);
			}
			
			Log.v("GameViewGL.onSurfaceCreated", "OpenGL renderer: " + gl.glGetString(GL10.GL_RENDERER));
		}
		
		// TODO: Add VBO support, then read the OpenGL extensions list to
		// determine whether the driver supports it.
    	mVBOSupport = false;
		
		mPlayerEntityPainters = new HashMap<Player, Painter[]>();
		for (int player = 0; player < Game.NUM_PLAYERS; player++) {
			Painter[] painters = new Painter[Entity.TYPES.length];
			
			switch (player) {
				case 0:
					painters[Entity.FIGHTER] = getPainter(gl, R.drawable.ohblue);
					painters[Entity.BOMBER]  = getPainter(gl, R.drawable.ohblue);
					painters[Entity.FRIGATE] = getPainter(gl, R.drawable.ohblue);
					painters[Entity.FACTORY] = getPainter(gl, R.drawable.ohblue);
					break;
				case 1:
				default:
					painters[Entity.FIGHTER] = getPainter(gl, R.drawable.ohred);
					painters[Entity.BOMBER]  = getPainter(gl, R.drawable.ohred);
					painters[Entity.FRIGATE] = getPainter(gl, R.drawable.ohred);
					painters[Entity.FACTORY] = getPainter(gl, R.drawable.ohred);
					break;
			}
			
			painters[Entity.LASER]   = getPainter(gl, R.drawable.laser);
			painters[Entity.BOMB]    = getPainter(gl, R.drawable.bomb);
			painters[Entity.MISSILE] = getPainter(gl, R.drawable.missile);

    		mPlayerEntityPainters.put(mGame.mPlayers[player], painters);
    	}
		
		mParticlePainters = new Painter[Emitter.TYPES.length];
		mParticlePainters[Emitter.SMOKE]       		= getPainter(gl, R.drawable.smoke);
		mParticlePainters[Emitter.SPARK]       		= getPainter(gl, R.drawable.bomb);
		mParticlePainters[Emitter.LASER_HIT]   		= getPainter(gl, R.drawable.bomb);
		mParticlePainters[Emitter.MISSILE_HIT] 		= getPainter(gl, R.drawable.bomb);
		mParticlePainters[Emitter.BOMB_HIT]    		= getPainter(gl, R.drawable.bomb);
		mParticlePainters[Emitter.SHIP_EXPLOSION] 	= getPainter(gl, R.drawable.bomb);
		
		mDigitPainters = new Painter[10];
		mDigitPainters[0] = getPainter(gl, R.drawable.char_0);
		mDigitPainters[1] = getPainter(gl, R.drawable.char_1);
		mDigitPainters[2] = getPainter(gl, R.drawable.char_2);
		mDigitPainters[3] = getPainter(gl, R.drawable.char_3);
		mDigitPainters[4] = getPainter(gl, R.drawable.char_4);
		mDigitPainters[5] = getPainter(gl, R.drawable.char_5);
		mDigitPainters[6] = getPainter(gl, R.drawable.char_6);
		mDigitPainters[7] = getPainter(gl, R.drawable.char_7);
		mDigitPainters[8] = getPainter(gl, R.drawable.char_8);
		mDigitPainters[9] = getPainter(gl, R.drawable.char_9);

		mHighlight = getPainter(gl, R.drawable.white);
		
		mBuildTargetPaintersBlue = new Painter[4];
		mBuildTargetPaintersBlue[0] = getPainter(gl, R.drawable.fighter_outline);
		mBuildTargetPaintersBlue[1] = getPainter(gl, R.drawable.bomber_outline);
		mBuildTargetPaintersBlue[2] = getPainter(gl, R.drawable.frigate_outline);
		mBuildTargetPaintersBlue[3] = getPainter(gl, R.drawable.upgrade_outline);
		
		mBuildTargetPaintersRed = new Painter[4];
		mBuildTargetPaintersRed[0] = getPainter(gl, R.drawable.fighter_outline_red);
		mBuildTargetPaintersRed[1] = getPainter(gl, R.drawable.bomber_outline_red);
		mBuildTargetPaintersRed[2] = getPainter(gl, R.drawable.frigate_outline_red);
		mBuildTargetPaintersRed[3] = getPainter(gl, R.drawable.upgrade_outline_red);

        // Initialize the background image.
		if (Pax.BACKGROUND_IMAGE) {
			mBackgroundPainter = getPainter(gl, R.drawable.background);
        }
		
		FramerateCounter.start();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        Log.v(Pax.TAG, String.format("GameRenderer.onSurfaceChanged with width %d, height %d", width, height));
		
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        
        // Make sure the largest screen dimension is equal to GAME_VIEW_SIZE
        // game units.
        float maxDimension = Math.max(width, height);
        
        mGameWidth = GAME_VIEW_SIZE * width / maxDimension;
        mGameHeight = GAME_VIEW_SIZE * height / maxDimension;
        
        float halfX = mGameWidth / 2;
        float halfY = mGameHeight / 2;
        
        GLU.gluOrtho2D(gl, -halfX, halfX, -halfY, halfY);
 
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
		
		mButtonSize = Math.max(mGameWidth, mGameHeight) / 15;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		
		long dt = FramerateCounter.tick();
		mGame.update(dt);
		
        if (mBackgroundPainter != null) {
        	mBackgroundPainter.draw(gl, 0, 0, mGameWidth, mGameHeight, 0, 1f);
        }
        else {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        	gl.glClearColor(BG_RED, BG_GREEN, BG_BLUE, 1.0f);
        }
        
        drawParticles(gl, Emitter.SMOKE);
		
		for (int entityType : ENTITY_LAYERS) {
			for (int i = 0; i < Game.NUM_PLAYERS; i++) {
				
				Player player = mGame.mPlayers[i];
				Painter[] painters = mPlayerEntityPainters.get(player);
			
				for (Entity entity : player.mEntities[entityType]) {
					
					painters[entityType].setCameraRotationDegrees(90 * mRotation);
					painters[entityType].draw(gl, entity);
				}
			}
		}
        
    	drawParticles(gl, Emitter.SPARK);
    	drawParticles(gl, Emitter.LASER_HIT);
    	drawParticles(gl, Emitter.MISSILE_HIT);
    	drawParticles(gl, Emitter.BOMB_HIT);
    	drawParticles(gl, Emitter.SHIP_EXPLOSION);
		
        if (mGame.getState() == Game.State.IN_PROGRESS) { 
        	drawButtons(gl);
        }
		
		if (Pax.FPS_METER) {
			int fps = FramerateCounter.getFPS();
			float x = (mGameWidth / 2) - 100;
			float y = (mGameHeight / 2) - 100;
			int digitWidth = 25;
			int digitHeight = 30;
			while (fps > 0) {
				int digit = fps % 10;
				mDigitPainters[digit].draw(gl, x, y, digitWidth, digitHeight, 0, 1f);
				x -= digitWidth;
				fps /= 10;
			}
		}
	}
    
    // Returns a Painter for a resource (creating a new one if necessary).
    private Painter getPainter(GL10 gl, int resourceID) {
    	if (!mPainters.containsKey(resourceID)) {
    		Bitmap bitmap = loadBitmap(resourceID);
    		mPainters.put(resourceID, new Painter(gl, mVBOSupport, bitmap));
    		bitmap.recycle();
    	}
    	
    	return mPainters.get(resourceID);
    }
	
	private void drawParticles(GL10 gl, int emitterType) {
		if (Pax.PARTICLES) {
			Painter painter = mParticlePainters[emitterType];
	    	for (int player = 0; player < Game.NUM_PLAYERS; player++) {
	    		Emitter emitter = mGame.mPlayers[player].mEmitters[emitterType];
	    		for (int i = emitter.mStart; i != emitter.mEnd; i = (i + 1) % emitter.mCapacity) {
	    			Emitter.Particle p = emitter.mParticles[i];
	    			float youth = (float) p.life / emitter.mInitialLifeMs;
	    			float scale = (2f - youth) * p.scale;
	    			painter.draw(gl, p.x, p.y, scale, scale, 0f, youth);
	    		}
	    	}
		}
	}

	// Draw UI elements along a short edge of the screen.
    private void drawButtons(GL10 gl) {
    	for (int player = 0; player < Game.NUM_PLAYERS; player++) {
			
			float dx = mGameWidth / 4;
			float dy = mGameHeight / 4;
			float x = (dx - mGameWidth) / 2;
			float y = (dy - mGameHeight) / 2;
			
			float buildIndicatorRotation = 90 * mRotation;

			Painter[] buildTargetPainters = (player == 0) ? mBuildTargetPaintersBlue : mBuildTargetPaintersRed;
			if ((player == 1) ^ (mRotation >= 2)) {
				dx = -dx;
				dy = -dy;
				x = -x;
				y = -y;
			}
			
			if (player == 1) {
				buildIndicatorRotation += 180;
			}
			
			for (int i = 0; i < 4; i++) {
			
				float buildProgress = mGame.mPlayers[player].money / Player.BuildCosts[i];
				if (buildProgress > 1) {
					buildProgress = 1;
				}
				
				// Oh no...
				
				float buttonMinX;
				float buttonMaxX;
				float buttonMinY;
				float buttonMaxY;
				
				float progressMaxX;
				float progressMaxY;
				
				float flip = ((player == 1) ^ (mRotation >= 2)) ? -1 : 1;
				
				float halfButtonSize = mButtonSize / 2;
				if (mRotation % 2 == 0) {
					// Draw buttons along the bottom of the screen
					dy = 0;
					y = flip * (halfButtonSize - (mGameHeight / 2));
					
					buttonMinY = y - flip * mButtonSize / 2;
					buttonMaxY = y + flip * mButtonSize / 2;
					buttonMinX = x - dx / 2;
					buttonMaxX = x + dx / 2;

					progressMaxX = buttonMinX + flip * mButtonSize / 3;//dx * buildProgress;
					progressMaxY = buttonMinY + flip * mButtonSize * buildProgress;
				}
				else {
					// Draw buttons along the right side of the screen.
					dx = 0;
					x = (mGameWidth / 2) - halfButtonSize;
					if (player != 0) {
						x = -x;
					}
					
					buttonMinY = y - dy / 2;
					buttonMaxY = y + dy / 2;
					buttonMinX = x + flip * mButtonSize / 2;
					buttonMaxX = x - flip * mButtonSize / 2;
					
					progressMaxX = buttonMinX - flip * (mButtonSize * buildProgress);
					progressMaxY = buttonMinY + flip * mButtonSize / 3;//dy * buildProgress;
				}
				
				if (i == mGame.mPlayers[player].mBuildTarget.ordinal()) {
	
					// Draw a 'selected' box behind the entire button.
					mHighlight.drawFillBounds(gl, buttonMinX, buttonMaxX, buttonMinY, buttonMaxY, 0, 0.2f);
					
					// Draw a 'progress' box behind part of the button.
					mHighlight.drawFillBounds(gl,
							buttonMinX,
							progressMaxX,
							buttonMinY,
							progressMaxY,
							0,
							0.2f);
				}
				
				buildTargetPainters[i].draw(gl, x, y, mButtonSize, mButtonSize, buildIndicatorRotation, 1f);
				x += dx;
				y += dy;
			}
		}
    }
	
	private Bitmap loadBitmap(int resourceID) {
		Resources resources = mContext.getResources();
		InputStream is = resources.openRawResource(resourceID);
		return BitmapFactory.decodeStream(is);
	}
	
	private Context mContext; 
	private Game mGame;
	
	private float mGameWidth;
	private float mGameHeight;
	
	private boolean mVBOSupport;

    private Map<Integer, Painter> mPainters;
	private Map<Player, Painter[]> mPlayerEntityPainters;
	private Painter mBackgroundPainter;
	
	private Painter mHighlight;
	private Painter[] mBuildTargetPaintersBlue;
	private Painter[] mBuildTargetPaintersRed;
	private Painter[] mParticlePainters;
	private Painter[] mDigitPainters;
	
	private int mRotation;
	private float mButtonSize;
}
