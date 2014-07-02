package com.gregmcnew.android.pax;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLU;

public class GameRenderer extends Renderer {
	
	// The size of the screen's largest dimension, measured in game units. 
	public static final float GAME_VIEW_SIZE = 1000f;
	
	// Draw factories at the bottom, with frigates above them, bombers above
	// frigates, and fighters above everything.
	private static final int ENTITY_LAYERS[] = {
			Entity.FACTORY, Entity.FRIGATE, Entity.BOMBER, Entity.FIGHTER,
			Entity.LASER, Entity.BOMB, Entity.MISSILE
			};
    
    public GameRenderer(Context context, Game game) {
    	super(context);
    	mGame = game;
    	mStarField = new StarField();
    }
    
    public void updateRotation(int rotation) {
    	super.updateRotation(rotation);
    	
    	mGameWidth  = (mRotation % 2 == 0) ? mScreenWidth  : mScreenHeight;
    	mGameHeight = (mRotation % 2 == 0) ? mScreenHeight : mScreenWidth;
    	
        // Make sure the largest screen dimension is equal to GAME_VIEW_SIZE
        // game units.
        float maxDimension = Math.max(mScreenWidth, mScreenHeight);
        mPixelSize = GAME_VIEW_SIZE / maxDimension;
        mGameWidth  *= mPixelSize;
        mGameHeight *= mPixelSize;

    }
    
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		super.onSurfaceCreated(gl, config);

		mShipOutlinePainter = getPainter(gl, R.drawable.ship_outline);

		mCircle = getPainter(gl, R.drawable.circle);
		
		float lineVertices[] = { -0.5f, 0, 0.5f, 0 };
		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(lineVertices.length * Short.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mLineVertices = byteBuffer.asFloatBuffer();
		mLineVertices.put(lineVertices);
		mLineVertices.position(0);
		
		mPlayerEntityPainters = new HashMap<Player, Painter[]>();
		for (int player = 0; player < Game.NUM_PLAYERS; player++) {
			Painter[] painters = new Painter[Entity.TYPES.length];
			
			painters[Entity.LASER]   = getPainter(gl, R.drawable.laser);
			painters[Entity.BOMB]    = getPainter(gl, R.drawable.bomb);
			painters[Entity.MISSILE] = getPainter(gl, R.drawable.missile);

    		mPlayerEntityPainters.put(mGame.mPlayers[player], painters);
    	}
		
		mStarPainter = getPainter(gl, R.drawable.star);
		
		mParticlePainters = new Painter[Emitter.TYPES.length];
		mParticlePainters[Emitter.SMOKE]       		= getPainter(gl, R.drawable.smoke);
		mParticlePainters[Emitter.SPARK]       		= getPainter(gl, R.drawable.bomb);
		mParticlePainters[Emitter.LASER_HIT]   		= getPainter(gl, R.drawable.bomb);
		mParticlePainters[Emitter.MISSILE_HIT] 		= getPainter(gl, R.drawable.bomb);
		mParticlePainters[Emitter.BOMB_HIT]    		= getPainter(gl, R.drawable.bomb);
		mParticlePainters[Emitter.SHIP_EXPLOSION] 	= getPainter(gl, R.drawable.bomb);
		mParticlePainters[Emitter.UPGRADE_EFFECT] 	= getPainter(gl, R.drawable.upgrade_effect);

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

		mBuildTargetPainters = new Painter[4];
		mBuildTargetPainters[0] = getPainter(gl, R.drawable.icon_fighter);
		mBuildTargetPainters[1] = getPainter(gl, R.drawable.icon_bomber);
		mBuildTargetPainters[2] = getPainter(gl, R.drawable.icon_frigate);
		mBuildTargetPainters[3] = getPainter(gl, R.drawable.icon_upgrade);

        // Initialize the background image.
		if (Constants.BACKGROUND_IMAGE) {
			//mBackgroundPainter = getPainter(gl, R.drawable.background);
        }
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		super.onSurfaceChanged(gl, width, height);
		
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        updateRotation(mRotation);
        
        float halfX = ((mRotation % 2 == 0) ? mGameWidth  : mGameHeight) / 2;
        float halfY = ((mRotation % 2 == 0) ? mGameHeight : mGameWidth ) / 2;
        
        GLU.gluOrtho2D(gl, -halfX, halfX, -halfY, halfY);
 
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
		
		mButtonSize = Math.max(mGameWidth, mGameHeight) / 15;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
        
        long dt = FramerateCounter.tick();
        if (Constants.sBenchmarkMode) {
        	dt = Pax.UPDATE_INTERVAL_MS;
        }
        mGame.update(dt);
		
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        if (!mGame.isPaused()) {
        	mStarField.update(dt);
        }
        
        drawStars(gl, mStarField, mStarPainter, mGameWidth, mGameHeight);
        
        if (Constants.sShowParticles) {
	        drawParticles(gl, Emitter.SMOKE);
        }
		
		// Draw fighter trails!
		for (Player player : mGame.mPlayers) {
		
			for (Entity entity : player.mEntities[Entity.FIGHTER]) {
				Fighter fighter = (Fighter) entity;
				mHighlight.drawTrail(gl, fighter.mTrailVertices, fighter.mVertexColors);
			}
		}
		
		if (Constants.sShowCollisionBoxes) {
			mGame.mPlayers[0].mEntities[Ship.FIGHTER].mBodies.draw(gl, mLineVertices, true, mRotation);
			mGame.mPlayers[1].mEntities[Ship.FIGHTER].mBodies.draw(gl, mLineVertices, false, mRotation);
		}
		
		if (Constants.sShowShips) {
			
			mPrimitivePainter.setStrokeColor(1, 1, 1, 0.5f);
			mPrimitivePainter.setFillColor(1, 1, 1, 0);

			final float minShieldWidth = mPixelSize * 2;

			float[][] c = Painter.TEAM_COLORS;

			for (int entityType : ENTITY_LAYERS) {
				for (int i = 0; i < Game.NUM_PLAYERS; i++) {

					Player player = mGame.mPlayers[i];
					Painter[] painters = mPlayerEntityPainters.get(player);

					for (Entity entity : player.mEntities[entityType]) {
						if (painters[entityType] != null) {
							painters[entityType].draw(gl, entity);
						}
						else {
							float[] shieldColors = { 1, 1, 1 };
							float shieldWidth = entity.diameter * 0.15f * ((float) entity.health) / entity.originalHealth;
							if (shieldWidth < minShieldWidth) {
								float shieldStrength = shieldWidth / minShieldWidth;
								shieldWidth = minShieldWidth;
								for (int j = 0; j < 3; j++) {
									shieldColors[j] = c[i][j] * (1 - shieldStrength) + shieldStrength;
								}
							}
							mCircle.draw(gl, entity, shieldColors[0], shieldColors[1], shieldColors[2]);
							mCircle.draw(gl, entity, entity.diameter - shieldWidth, c[i][0], c[i][1], c[i][2]);
						}
					}
				}
			}
		}
		
        if (Constants.sShowParticles) {
	    	drawParticles(gl, Emitter.SPARK);
	    	drawParticles(gl, Emitter.LASER_HIT);
	    	drawParticles(gl, Emitter.MISSILE_HIT);
	    	drawParticles(gl, Emitter.BOMB_HIT);
	    	drawParticles(gl, Emitter.SHIP_EXPLOSION);
	    	drawParticles(gl, Emitter.UPGRADE_EFFECT);
		}
        
        if (mGame.getState() == Game.State.IN_PROGRESS) { 
        	drawButtons(gl);
        }
		
		if (Constants.sShowFPS) {
			float x = (mGameWidth / 2) - 100;
			float y = (mGameHeight / 2) - 100;
			drawNumber(gl, x, y, (long) FramerateCounter.getFPS(), 0.2f);
			drawNumber(gl, x, y - 35, FramerateCounter.getRecentJitter(), 0.1f);
			drawNumber(gl, x, y - 70, FramerateCounter.getMaxJitter(), 0.1f);
		}
	}
	
	private void drawNumber(GL10 gl, float x, float y, long number, float alpha) {
		int digitWidth = 25;
		int digitHeight = 30;
		while (number > 0) {
			int digit = (int) number % 10;
			mDigitPainters[digit].draw(gl, x, y, digitWidth, digitHeight, 0, alpha);
			x -= digitWidth;
			number /= 10;
		}
	}
	
	private void drawParticles(GL10 gl, int emitterType) {
		if (Constants.PARTICLES) {
			Painter painter = mParticlePainters[emitterType];
	    	for (int player = 0; player < Game.NUM_PLAYERS; player++) {
	    		Emitter emitter = mGame.mPlayers[player].mEmitters[emitterType];
	    		for (int i = emitter.mStart; i != emitter.mEnd; i = (i + 1) % emitter.mCapacity) {
	    			Particle p = emitter.mParticles[i];
	    			float youth = (float) p.life / emitter.mInitialLifeMs;
	    			float scale = (2f - youth) * p.scale;
	    			painter.draw(gl, p.x, p.y, scale, scale, 0f, youth);
	    		}
	    	}
		}
	}

	// Draw UI elements along a short edge of the screen.
    private void drawButtons(GL10 gl) {
    	
    	// Don't draw buttons in benchmark mode, since they change very quickly
    	// and can't be controlled anyway.
    	if (Constants.sBenchmarkMode) {
    		return;
    	}

		// Draw the build target icon.
		float[][] c = Painter.TEAM_COLORS;
		float[][] h = { { 0, 0, 0}, { 0, 0, 0 } };

		for (int player = 0; player < Game.NUM_PLAYERS; player++) {

			for (int j = 0; j < 3; j++) {
				h[player][j] = (c[player][j] + 1) / 2;
			}
			
			float rot = (player == 0) ? 0 : 180;
			
			float flip = (player == 1) ? -1 : 1;

			// Draw buttons along the bottom of the screen
			float dx = mGameWidth / 4;
			float dy = 0;
			float x = flip * (dx - mGameWidth) / 2;
			float y = flip * (mButtonSize - mGameHeight) / 2;
			
			dx *= flip;
			dy *= flip;
			
			for (int i = 0; i < 4; i++) {
			
				float buildProgress = mGame.mPlayers[player].money / Player.BuildCosts[i];
				if (buildProgress > 1) {
					buildProgress = 1;
				}
				
				float buttonMinY = y - flip * mButtonSize / 2;
				float buttonMaxY = y + flip * mButtonSize / 2;
				float buttonMinX = x - dx / 2;
				float buttonMaxX = x + dx / 2;

				// The width of the progress bar is 1/3 of its maximum height.
				float progressMaxX = buttonMinX + flip * mButtonSize / 3;
				float progressMaxY = buttonMinY + flip * mButtonSize * buildProgress;
				
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

				mBuildTargetPainters[i].draw(gl, x, y, mButtonSize, mButtonSize, rot, 1f);
				mBuildTargetPainters[i].draw(gl, x, y, mButtonSize, mButtonSize, rot, .33f, c[player][0], c[player][1], c[player][2]);
				
				x += dx;
				y += dy;
			}
		}
    }
	
	private Game mGame;
	
	private StarField mStarField;
	
	private FloatBuffer mLineVertices;
	
	// The width and height of the displayed game area, in game units.
	// These values will -not- change when the screen is rotated.
	private float mGameWidth;
	private float mGameHeight;
	private float mPixelSize; // in game units

	private Map<Player, Painter[]> mPlayerEntityPainters;

	private Painter mStarPainter;
	
	private Painter mHighlight;
	private Painter[] mBuildTargetPainters;
	private Painter[] mParticlePainters;
	private Painter[] mDigitPainters;

	private Painter mShipOutlinePainter;
	private Painter mCircle;
	
	private float mButtonSize;
}
