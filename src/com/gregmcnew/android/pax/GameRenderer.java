package com.gregmcnew.android.pax;
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
	
	public static final boolean SUPPORTS_GL11 = false;
	
	private static final float[] BG_RGB = { 0.094f, 0.137f, 0.145f };
	
	// The size of the screen's largest dimension, measured in game units. 
	public static final float GAME_VIEW_SIZE = 1000f;
	
    
    private static final int[] RESOURCES_TO_LOAD = {
    		R.drawable.bomb,
    		R.drawable.laser,
    		R.drawable.missile,
    		
    		R.drawable.bomber_p1,
    		R.drawable.factory_p1,
    		R.drawable.fighter_p1,
    		R.drawable.frigate_p1,
    		
    		R.drawable.bomber_p2,
    		R.drawable.factory_p2,
    		R.drawable.fighter_p2,
    		R.drawable.frigate_p2,

    		R.drawable.ohblue,
    		R.drawable.ohred,
    		
    		R.drawable.fighter_outline,
    		R.drawable.bomber_outline,
    		R.drawable.frigate_outline,
    		R.drawable.upgrade_outline,
    		R.drawable.white20,
    };

    
    public GameRenderer(Context context, Game game) {
    	mContext = context;
    	mGame = game;
    	mVBOSupport = SUPPORTS_GL11;
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
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

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

		Resources resources = mContext.getResources();
		Map<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>();
		for (int resourceID : RESOURCES_TO_LOAD) {
			bitmaps.put(resourceID, BitmapFactory.decodeResource(resources, resourceID));
		}
		
		mPlayerEntityPainters = new HashMap<Player, Painter[]>();
		for (int player = 0; player < Game.NUM_PLAYERS; player++) {
			Painter[] painters = new Painter[Entity.TYPES.length];
			
			switch (player) {
				case 0:
					painters[Entity.FIGHTER] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohblue), Fighter.DIAMETER);
					painters[Entity.BOMBER]  = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohblue), Bomber.DIAMETER);
					painters[Entity.FRIGATE] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohblue), Frigate.DIAMETER);
					painters[Entity.FACTORY] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohblue), Factory.DIAMETER);
					break;
				case 1:
				default:
					painters[Entity.FIGHTER] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred), Fighter.DIAMETER);
					painters[Entity.BOMBER]  = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred), Bomber.DIAMETER);
					painters[Entity.FRIGATE] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred), Frigate.DIAMETER);
					painters[Entity.FACTORY] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred), Factory.DIAMETER);
					break;
			}
			
			painters[Entity.LASER]   = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.laser),   Laser.DIAMETER);
			painters[Entity.BOMB]    = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.bomb),    Bomb.DIAMETER);
			painters[Entity.MISSILE] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.missile), Missile.DIAMETER);

    		mPlayerEntityPainters.put(mGame.mPlayers[player], painters);
    	}
		
		for (Bitmap bitmap : bitmaps.values()) {
			bitmap.recycle();
		}
		
		// We delay loading the background image until the first call to onSurfaceChanged().
		// This lets us scale the background image once, based on the screen size, and forget about it afterwards.
		mBackgroundPainter = null;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        Log.v(Pax.TAG, String.format("GameRenderer.onSurfaceChanged with width %d, height %d", width, height));
		
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        
        mWidth = width;
        mHeight = height;
        
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
		
		// Create the button highlight image.
		float highlightWidth = Math.min(mGameWidth, mGameHeight) / 4;
		mButtonSize = Math.max(mGameWidth, mGameHeight) / 15;
        
        // Initialize the background image.
		
		Resources res = mContext.getResources();
		if (Pax.BACKGROUND_IMAGE) {
			mBackgroundPainter = Painter.CreateMinSize(gl, mVBOSupport, BitmapFactory.decodeResource(res, R.drawable.background), Math.max(mGameWidth, mGameHeight));
        }

		mHighlight = Painter.CreateSize(gl, mVBOSupport, BitmapFactory.decodeResource(res, R.drawable.white20), highlightWidth, mButtonSize);
		
		mBuildTargetPainters = new Painter[4];
		mBuildTargetPainters[0] = Painter.CreateMinSize(gl, mVBOSupport, BitmapFactory.decodeResource(res, R.drawable.fighter_outline), mButtonSize);
		mBuildTargetPainters[1] = Painter.CreateMinSize(gl, mVBOSupport, BitmapFactory.decodeResource(res, R.drawable.bomber_outline), mButtonSize);
		mBuildTargetPainters[2] = Painter.CreateMinSize(gl, mVBOSupport, BitmapFactory.decodeResource(res, R.drawable.frigate_outline), mButtonSize);
		mBuildTargetPainters[3] = Painter.CreateMinSize(gl, mVBOSupport, BitmapFactory.decodeResource(res, R.drawable.upgrade_outline), mButtonSize);
	}
	
	// Draw factories at the bottom, with frigates above them, bombers above
	// frigates, and fighters above everything.
	private static final int ENTITY_LAYERS[] = {
			Entity.FACTORY, Entity.FRIGATE, Entity.BOMBER, Entity.FIGHTER,
			Entity.LASER, Entity.BOMB, Entity.MISSILE
			};
	
	@Override
	public void onDrawFrame(GL10 gl) {
		mGame.update();
		
        if (mBackgroundPainter != null) {
        	mBackgroundPainter.draw(gl, 0, 0, 0f);
        }
        else {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        	gl.glClearColor(BG_RGB[0], BG_RGB[1], BG_RGB[2], 1.0f);
        }
		
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
		
		// Draw UI elements along a short edge of the screen.
		
		float dx = mGameWidth / 4;
		float dy = mGameHeight / 4;
		float x = (dx - mGameWidth) / 2;
		float y = (dy - mGameHeight) / 2;
		
		if (mRotation % 2 == 0) {
			// Draw buttons along the bottom of the screen
			dy = 0;
			y = (mButtonSize - mGameHeight) / 2;
		}
		else {
			// Draw buttons along the right side of the screen.
			dx = 0;
			x = (mGameWidth - mButtonSize) / 2;
		}
		for (int i = 0; i < 4; i++) {
			if (i == mGame.mPlayers[0].mBuildTarget.ordinal()) {
				mHighlight.draw(gl, x, y, 90 * mRotation);
			}
			
			mBuildTargetPainters[i].draw(gl, x, y, 90 * mRotation);
			x += dx;
			y += dy;
		}
	}
	
	private Context mContext; 
	private Game mGame;
	private int mWidth;
	private int mHeight;
	
	private float mGameWidth;
	private float mGameHeight;
	
	private Map<Player, Painter[]> mPlayerEntityPainters;
	private Painter mBackgroundPainter;
	private boolean mVBOSupport;
	
	private Painter mHighlight;
	private Painter[] mBuildTargetPainters;
	
	private int mRotation;
	private float mButtonSize;
}
