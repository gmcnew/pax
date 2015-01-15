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
    
    public GameRenderer(Context context, Game game, boolean landscapeDevice) {
    	super(context);
    	mGame = game;
    	mLandscapeDevice = landscapeDevice;
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
		mParticlePainters[Emitter.UPGRADE_EFFECT] 	= getPainter(gl, R.drawable.icon_upgrade);

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
		mMinusPainter = getPainter(gl, R.drawable.char_minus);
		mPeriodPainter = getPainter(gl, R.drawable.char_period);

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

		// Draw smoke only.
		drawParticles(gl, Emitter.SMOKE, Emitter.SMOKE + 1);
		
		// Draw fighter trails!
		for (Player player : mGame.mPlayers) {
		
			for (Entity entity : player.mEntities[Entity.FIGHTER]) {
				Fighter fighter = (Fighter) entity;
				mHighlight.drawTrail(gl, fighter.mTrailVertices, fighter.mVertexColors);
			}
		}
		
		if (Constants.sShowCollisionBoxes) {
			for (int i = 0; i < 2; i++) {
				QuadtreePainter.draw(gl, mGame.mPlayers[i].mEntities[Ship.FIGHTER].mBodies, mLineVertices, i == 0, mRotation);
			}
		}
		
		if (Constants.sShowShips) {
			
			mPrimitivePainter.setStrokeColor(1, 1, 1, 0.5f);
			mPrimitivePainter.setFillColor(1, 1, 1, 0);

			final float minShieldWidth = mPixelSize * 3;

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
							float shieldWidth = entity.diameter * 0.15f * ((float) entity.health) / entity.originalHealth;
							float shieldStrength = 1;
							if (shieldWidth < minShieldWidth) {
								shieldStrength = shieldWidth / minShieldWidth;
								shieldWidth = minShieldWidth;
							}
							for (int j = 0; j < 3; j++) {
								sShieldColors[j] = c[i][j] * (1 - shieldStrength) + shieldStrength;
							}
							mCircle.draw(gl, entity, sShieldColors[0], sShieldColors[1], sShieldColors[2]);
							mCircle.draw(gl, entity, entity.diameter - shieldWidth, c[i][0], c[i][1], c[i][2]);
						}
					}
				}
			}
		}

		// Draw everything -but- smoke.
		drawParticles(gl, 0, Emitter.SMOKE);
		drawParticles(gl, Emitter.SMOKE + 1, Emitter.TYPES.length);
        
        if (mGame.getState() == Game.State.IN_PROGRESS) { 
        	drawButtons(gl);
        }
		
		if (Constants.sDebugMode) {
			float ex = DIGIT_SPACING * 2 - mGameWidth / 2;
			float x = -(ex + DIGIT_WIDTH);
			float y = (mGameHeight / 2) - 100;
			float dy = -(DIGIT_HEIGHT + LINE_SPACING);
			drawNumber(gl, x, y,          FramerateCounter.getFPS(),          0.6f, 1);
			drawNumber(gl, x, y + dy,     FramerateCounter.getRecentJitter(), 0.4f);
			drawNumber(gl, x, y + dy * 2, FramerateCounter.getMaxJitter(),    0.4f);

			int n1 = AIWeights.NUM_WEIGHTS;
			int n2 = Entity.TYPES.length;

			for (int i = 0; i < 2; i++) {

				float a = 1;
				float r = (Painter.TEAM_COLORS[i][0] + 1) / 2;
				float g = (Painter.TEAM_COLORS[i][1] + 1) / 2;
				float b = (Painter.TEAM_COLORS[i][2] + 1) / 2;

				x = ex + DIGIT_WIDTH * 7 + DIGIT_SPACING * 6;
				y = LINE_SPACING / 2 - dy * (n1 + n2) * i;

				if (mGame.mPlayers[i].isAI()) {
					// build scores
					float[] buildScores = mGame.mPlayers[i].getAIBuildScores();
					int n = buildScores.length;
					float xRight = -(ex + DIGIT_WIDTH);
					float yRight = LINE_SPACING / 2 - dy * n * i;
					for (int j = 0; j < n; j++) {
						drawNumber(gl, xRight, yRight, buildScores[j], a, r, g, b);
						yRight += dy;
					}

					// AI weights
					AIWeights weights = mGame.mPlayers[i].getAIWeights();
					for (int j = 0; j < n1; j++) {
						drawNumber(gl, x, y, weights.w[j], a, r, g, b);
						y += dy;
					}
				}
				else {
					y += dy * n1;
				}

				// entity counts
				for (int j = 0; j < n2; j++) {
					drawNumber(gl, x, y, mGame.mPlayers[i].mEntities[j].size(), a, r, g, b);
					y += dy;
				}
			}
		}
	}

	private void drawNumber(GL10 gl, float x, float y, float number, float alpha) {
		drawNumber(gl, x, y, number, alpha, 1, 1, 1, -1);
	}

	private void drawNumber(GL10 gl, float x, float y, float number, float alpha, int precision) {
		drawNumber(gl, x, y, number, alpha, 1, 1, 1, precision);
	}

	private void drawNumber(GL10 gl, float x, float y, float number, float alpha, float r, float g, float b) {
		drawNumber(gl, x, y, number, alpha, r, g, b, -1);
	}

	private void drawNumber(GL10 gl, float x, float y, float number, float alpha, float r, float g, float b, int precision) {
		String str = (number == (long) number)
				? String.format("%d", (long)number)
				: String.format((precision >= 0
					? "%." + precision + "f"
					: "%s"), number);

		if (number == 0) {
			alpha *= 0.5;
		}

		for (int i = str.length() - 1; i >= 0; i--) {
			char c = str.charAt(i);
			Painter p = mPeriodPainter;
			if (c >= '0' && c <= '9') {
				p = mDigitPainters[c - '0'];
			}
			else if (c == '-') {
				p = mMinusPainter;
			}

			p.draw(gl, x, y, DIGIT_WIDTH, DIGIT_HEIGHT, 0, alpha, r, g, b);
			x -= DIGIT_WIDTH + DIGIT_SPACING;
			number /= 10;
		}
	}
	
	private void drawParticles(GL10 gl, int startType, int endType) {
		if (Constants.sShowParticles && startType < endType) {
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_DST_ALPHA);

			for (int type = startType; type < endType; type++) {
				Painter painter = mParticlePainters[type];
				for (int player = 0; player < Game.NUM_PLAYERS; player++) {
					Emitter emitter = mGame.mPlayers[player].mEmitters[type];
					for (int i = emitter.mStart; i != emitter.mEnd; i = (i + 1) % emitter.mCapacity) {
						Particle p = emitter.mParticles[i];
						float youth = (float) p.life / emitter.mInitialLifeMs;
						float scale = (2f - youth) * p.scale;
						painter.draw(gl, p.x, p.y, scale, scale, 0f, youth);
					}
				}
			}

			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
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

		for (int player = 0; player < Game.NUM_PLAYERS; player++) {
			
			float rot = (player == 0) ? 0 : 180;
			if (mLandscapeDevice) {
				rot -= 90;
			}
			
			float flip = (player == 1) ? -1 : 1;

			// Draw buttons along the bottom of the screen
			float dx = mLandscapeDevice ? 0 : (mGameWidth / 4);
			float dy = mLandscapeDevice ? (-mGameHeight / 4) : 0;
			float xOffset = mLandscapeDevice ? mButtonSize : dx;
			float yOffset = mLandscapeDevice ? -dy : mButtonSize;
			float x = flip * (xOffset - mGameWidth) / 2;
			float y = flip * (yOffset - mGameHeight) / 2;
			if (mLandscapeDevice) {
				y *= -1;
			}
			
			dx *= flip;
			dy *= flip;
			
			for (int i = 0; i < 4; i++) {
			
				float buildProgress = mGame.mPlayers[player].money / Player.BuildCosts[i];
				if (buildProgress > 1) {
					buildProgress = 1;
				}

				float bs = flip * mButtonSize;

				float buttonWidth  = mLandscapeDevice ? bs : dx;
				float buttonHeight = mLandscapeDevice ? dy : bs;
				
				float buttonMinY = y - buttonHeight / 2;
				float buttonMaxY = y + buttonHeight / 2;
				float buttonMinX = x - buttonWidth / 2;
				float buttonMaxX = x + buttonWidth / 2;

				// The width of the progress bar is 1/3 of its maximum height.
				float progressMaxX = buttonMinX + (mLandscapeDevice ? (bs * buildProgress) : (bs / 3));
				float progressMaxY = buttonMinY + (mLandscapeDevice ? (-bs / 3) : (bs * buildProgress));
				
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

	private static final float DIGIT_WIDTH = 20, DIGIT_HEIGHT = 20, LINE_SPACING = 4, DIGIT_SPACING = -3.2f;

	private static float[] sShieldColors = { 1, 1, 1 };
	
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
	private Painter mMinusPainter;
	private Painter mPeriodPainter;

	private Painter mCircle;
	
	private float mButtonSize;
	private final boolean mLandscapeDevice;
}
