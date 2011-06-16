package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

public class GameView extends View {

	public GameView(Context context, Game game) {
		super(context);
		mGame = game;
		mOrientation = Configuration.ORIENTATION_PORTRAIT;
		
		mPlayerEntityBitmaps = new HashMap<Player, Bitmap[]>();
		
		// Load all bitmaps
		for (int i = 0; i < Game.NUM_PLAYERS; i++) {
			Bitmap[] playerBitmaps = new Bitmap[Entity.TYPES.length];
			Resources res = getResources();
			
			switch (i) {
				case 0:
					playerBitmaps[Entity.FIGHTER] = BitmapFactory.decodeResource(res, R.drawable.fighter_p1);
					playerBitmaps[Entity.BOMBER]  = BitmapFactory.decodeResource(res, R.drawable.bomber_p1);
					playerBitmaps[Entity.FRIGATE] = BitmapFactory.decodeResource(res, R.drawable.frigate_p1);
					playerBitmaps[Entity.FACTORY] = BitmapFactory.decodeResource(res, R.drawable.factory_p1);
					break;
				case 1:
					playerBitmaps[Entity.FIGHTER] = BitmapFactory.decodeResource(res, R.drawable.fighter_p2);
					playerBitmaps[Entity.BOMBER]  = BitmapFactory.decodeResource(res, R.drawable.bomber_p2);
					playerBitmaps[Entity.FRIGATE] = BitmapFactory.decodeResource(res, R.drawable.frigate_p2);
					playerBitmaps[Entity.FACTORY] = BitmapFactory.decodeResource(res, R.drawable.factory_p2);
					break;
			}
			
			playerBitmaps[Entity.LASER]   = BitmapFactory.decodeResource(res, R.drawable.laser);
			playerBitmaps[Entity.BOMB]    = BitmapFactory.decodeResource(res, R.drawable.bomb);
			playerBitmaps[Entity.MISSILE] = BitmapFactory.decodeResource(res, R.drawable.missile);
			
			mPlayerEntityBitmaps.put(game.mPlayers[i], playerBitmaps);
		}
		

		mBoundsPaints = new Paint[2];
		mBoundsPaints[0] = new Paint();
		mBoundsPaints[1] = new Paint();
		mBoundsPaints[0].setARGB(192, 0, 64, 255);
		mBoundsPaints[1].setARGB(192, 192, 0, 0);
		for (Paint paint : mBoundsPaints) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(2);
		}
		
		mLaserPaint = new Paint();
		mLaserPaint.setARGB(255, 255, 255, 255);
		mLaserPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mBitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	}
	
	public void updateOrientation(int orientation) {
		if (orientation != mOrientation) {
			mOrientation = orientation;

			mAngleFudge = (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
				? (float) (-Math.PI / 2)
				: 0.0f;
		}
	}
	
	private Paint[] mBoundsPaints;
	private Paint mLaserPaint;
	private Paint mBitmapPaint;
	
	// Draw factories at the bottom, with frigates above them, bombers above
	// frigates, and fighters above everything.
	private int ENTITY_LAYERS[] = {
			Entity.FACTORY, Entity.FRIGATE, Entity.BOMBER, Entity.FIGHTER,
			Entity.LASER, Entity.BOMB, Entity.MISSILE
			};

	@Override
	protected void onDraw(Canvas canvas) {
		// Update the game.
		mGame.update();

		for (int entityType : ENTITY_LAYERS) {
			float radius = Entity.Radii[entityType];
			radius *= 2; // some ships are bigger than their circles
			
			// TODO: Figure out window bounds intelligently.
			float minXDrawable = 0 - radius;
			float maxXDrawable = 320 + radius;
			float minYDrawable = 0 - radius;
			float maxYDrawable = 480 + radius;
			
			for (int i = 0; i < Game.NUM_PLAYERS; i++) {
				Player player = mGame.mPlayers[i];
				Bitmap[] entityBitmaps = mPlayerEntityBitmaps.get(player);
			
				for (Entity entity : player.mEntities[entityType]) {
					
					if (entity.body.center.x > minXDrawable && entity.body.center.x < maxXDrawable && entity.body.center.y > minYDrawable && entity.body.center.y < maxYDrawable) {
					
						Bitmap bitmap = entityBitmaps[entity.type];
						
						float posX;
						float posY;
						if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
							posX = entity.body.center.x;
							posY = entity.body.center.y;
						}
						else {
							posY = 320 - entity.body.center.x;
							posX = entity.body.center.y;
						}
						
						if (bitmap != null) {
							// Scale the image so that its smallest dimension fills the circle.
							// (Its largest dimension may spill outside the circle.)
							Matrix matrix = new Matrix();
							float scaleX = entity.diameter / bitmap.getWidth();
							float scaleY = entity.diameter / bitmap.getHeight();
							float scale = Math.max(scaleX, scaleY);
							matrix.postScale(scale, scale);
							matrix.postTranslate(posX - (scale / scaleX) * entity.radius, posY - (scale / scaleY) * entity.radius);
							matrix.postRotate((float) Math.toDegrees(entity.heading + mAngleFudge), posX, posY);
							
							canvas.drawBitmap(bitmap, matrix, mBitmapPaint);
						}
						
						canvas.drawCircle(posX, posY, entity.radius, mBoundsPaints[i]);
					}
				}
			}
		}
	}
	
	private Map<Player, Bitmap[]> mPlayerEntityBitmaps;
	
	private Game mGame;
	
	private int mOrientation;
	
	// Added to an entity's heading when rotating its bitmap.
	private float mAngleFudge;
}
