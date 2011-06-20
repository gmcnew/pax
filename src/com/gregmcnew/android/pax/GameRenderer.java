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
	
	public static final boolean SUPPORTS_GL11 = false;
	
	private static final float[] BG_RGB = { 0.094f, 0.137f, 0.145f };
	
	// The size of the screen's largest dimension, measured in game units. 
	public static final float GAME_VIEW_SIZE = 1000f;
	
    
    private static final int[] RESOURCES_TO_LOAD = {

    		R.drawable.ohblue,
    		R.drawable.ohred,
    		
    		R.drawable.fighter_outline,
    		R.drawable.bomber_outline,
    		R.drawable.frigate_outline,
    		R.drawable.upgrade_outline,
    		R.drawable.white20,
    		
    		R.drawable.laser,
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

		Map<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>();
		for (int resourceID : RESOURCES_TO_LOAD) {
			bitmaps.put(resourceID, loadBitmap(resourceID));
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
					painters[Entity.BOMB]    = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohblue),    Bomb.DIAMETER);
					painters[Entity.MISSILE] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohblue), Missile.DIAMETER);
					break;
				case 1:
				default:
					painters[Entity.FIGHTER] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred), Fighter.DIAMETER);
					painters[Entity.BOMBER]  = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred), Bomber.DIAMETER);
					painters[Entity.FRIGATE] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred), Frigate.DIAMETER);
					painters[Entity.FACTORY] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred), Factory.DIAMETER);
					painters[Entity.BOMB]    = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred),    Bomb.DIAMETER);
					painters[Entity.MISSILE] = Painter.CreateMinSize(gl, mVBOSupport, bitmaps.get(R.drawable.ohred), Missile.DIAMETER);
					break;
			}
			
			painters[Entity.LASER] = Painter.CreateSize(gl, mVBOSupport, bitmaps.get(R.drawable.laser), Laser.LENGTH, Laser.DIAMETER);

    		mPlayerEntityPainters.put(mGame.mPlayers[player], painters);
    	}

		mHighlight = Painter.Create(gl, mVBOSupport, bitmaps.get(R.drawable.white20));
		
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
		
		mButtonSize = Math.max(mGameWidth, mGameHeight) / 15;
        
        // Initialize the background image.
		if (Pax.BACKGROUND_IMAGE) {
			//mBackgroundPainter = Painter.CreateMinSize(gl, mVBOSupport, loadBitmap(R.drawable.background), Math.max(mGameWidth, mGameHeight));
        }
		
		mBuildTargetPainters = new Painter[4];
		mBuildTargetPainters[0] = Painter.CreateMinSize(gl, mVBOSupport, loadBitmap(R.drawable.fighter_outline), mButtonSize);
		mBuildTargetPainters[1] = Painter.CreateMinSize(gl, mVBOSupport, loadBitmap(R.drawable.bomber_outline), mButtonSize);
		mBuildTargetPainters[2] = Painter.CreateMinSize(gl, mVBOSupport, loadBitmap(R.drawable.frigate_outline), mButtonSize);
		mBuildTargetPainters[3] = Painter.CreateMinSize(gl, mVBOSupport, loadBitmap(R.drawable.upgrade_outline), mButtonSize);
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
		
		for (int player = 0; player < Game.NUM_PLAYERS; player++) {
			
			float dx = mGameWidth / 4;
			float dy = mGameHeight / 4;
			float x = (dx - mGameWidth) / 2;
			float y = (dy - mGameHeight) / 2;
			
			float buildIndicatorRotation = 90 * mRotation;
			
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
					mHighlight.drawFillBounds(gl, buttonMinX, buttonMaxX, buttonMinY, buttonMaxY, 0);
					
					// Draw a 'progress' box behind part of the button.
					mHighlight.drawFillBounds(gl,
							buttonMinX,
							progressMaxX,
							buttonMinY,
							progressMaxY,
							0);
				}
				
				mBuildTargetPainters[i].draw(gl, x, y, buildIndicatorRotation);
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
