package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

public class GameView extends View {

	public GameView(Context context, Game game) {
		super(context);
		mGame = game;
		mContext = context;
		mHeight = 0;
		mWidth = 0;
		
		mPlayerEntityBitmaps = new HashMap<Player, Bitmap[]>();
		
		// Load all bitmaps
		Resources res = getResources();
		for (int i = 0; i < Game.NUM_PLAYERS; i++) {
			Bitmap[] playerBitmaps = new Bitmap[Entity.TYPES.length];
			
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
		
		mBackgroundBitmap = BitmapFactory.decodeResource(res, R.drawable.background);
		

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
	
	public void updateOrientation() {
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        
		mRotation = display.getRotation();
		
		switch (mRotation) {
			case Surface.ROTATION_0:
				mAngleFudge = 0;
				break;
			case Surface.ROTATION_90:
				mAngleFudge = (float) -Math.PI * 0.5f;
				break;
			case Surface.ROTATION_180:
				mAngleFudge = (float) -Math.PI;
				break;
			case Surface.ROTATION_270:
				mAngleFudge = (float) -Math.PI * 1.5f;
				break;
		}
		
		mWidth = display.getWidth();
		mHeight = display.getHeight();
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
		
		if (mWidth == 0) {
			updateOrientation();
		}

		Matrix matrix = new Matrix();
		{
			float scaleX = (float) mWidth / mBackgroundBitmap.getWidth();
			float scaleY = (float) mHeight / mBackgroundBitmap.getHeight();
			float scale = Math.max(scaleX, scaleY);
			matrix.postScale(scale, scale);
			matrix.postTranslate((scaleX - scale) * (mBackgroundBitmap.getWidth() / 2),
					(scaleY - scale) * (mBackgroundBitmap.getHeight() / 2));
			canvas.drawBitmap(mBackgroundBitmap, matrix, mBitmapPaint);
		}

		for (int entityType : ENTITY_LAYERS) {
			float radius = Entity.Radii[entityType];
			radius *= 2; // some ships are bigger than their circles
			
			// TODO: Figure out window bounds intelligently.
			float minXDrawable = 0 - radius;
			float maxXDrawable = mWidth + radius;
			float minYDrawable = 0 - radius;
			float maxYDrawable = mHeight + radius;
			
			for (int i = 0; i < Game.NUM_PLAYERS; i++) {
				Player player = mGame.mPlayers[i];
				Bitmap[] entityBitmaps = mPlayerEntityBitmaps.get(player);
			
				for (Entity entity : player.mEntities[entityType]) {
					
					float posX;
					float posY;
					switch (mRotation) {
						case Surface.ROTATION_0:
							posX = entity.body.center.x;
							posY = entity.body.center.y;
							break;
						case Surface.ROTATION_90:
							posX = entity.body.center.y;
							posY = mHeight - entity.body.center.x;
							break;
						case Surface.ROTATION_180:
							posX = mWidth - entity.body.center.x;
							posY = mHeight - entity.body.center.y;
							break;
						case Surface.ROTATION_270:
						default:
							posX = mWidth - entity.body.center.y;
							posY = entity.body.center.x;
							break;
					}
					
					if (posX > minXDrawable && posX < maxXDrawable && posY > minYDrawable && posY < maxYDrawable) {
					
						Bitmap bitmap = entityBitmaps[entity.type];
						
						if (bitmap != null) {
							// Scale the image so that its smallest dimension fills the circle.
							// (Its largest dimension may spill outside the circle.)
							matrix.reset();
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
	
	private Bitmap mBackgroundBitmap;
	
	private Map<Player, Bitmap[]> mPlayerEntityBitmaps;
	
	private Game mGame;
	private Context mContext;
	
	private int mRotation;
	
	// Added to an entity's heading when rotating its bitmap.
	private float mAngleFudge;
	
	private int mHeight;
	private int mWidth;
}
