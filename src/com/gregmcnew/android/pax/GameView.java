package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import com.gregmcnew.android.pax.Entity.Type;

import android.content.Context;
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
		
		mPlayerEntityBitmaps = new HashMap<Player, Map<Type, Bitmap>>();
		
		// Load all bitmaps
		for (int i = 0; i < Game.NUM_PLAYERS; i++) {
			HashMap<Type, Bitmap> playerBitmaps = new HashMap<Type, Bitmap>();
			Resources res = getResources();
			
			switch (i) {
				case 0:
					playerBitmaps.put(Type.FIGHTER, BitmapFactory.decodeResource(res, R.drawable.fighter_p1));
					playerBitmaps.put(Type.BOMBER,  BitmapFactory.decodeResource(res, R.drawable.bomber_p1));
					playerBitmaps.put(Type.FRIGATE, BitmapFactory.decodeResource(res, R.drawable.frigate_p1));
					playerBitmaps.put(Type.FACTORY, BitmapFactory.decodeResource(res, R.drawable.factory_p1));
					break;
				case 1:
					playerBitmaps.put(Type.FIGHTER, BitmapFactory.decodeResource(res, R.drawable.fighter_p2));
					playerBitmaps.put(Type.BOMBER,  BitmapFactory.decodeResource(res, R.drawable.bomber_p2));
					playerBitmaps.put(Type.FRIGATE, BitmapFactory.decodeResource(res, R.drawable.frigate_p2));
					playerBitmaps.put(Type.FACTORY, BitmapFactory.decodeResource(res, R.drawable.factory_p2));
					break;
			}
			
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
	
	private Paint[] mBoundsPaints;
	private Paint mLaserPaint;
	private Paint mBitmapPaint;
	
	// Draw factories at the bottom, with frigates above them, bombers above
	// frigates, and fighters above everything.
	private int SHIP_LAYERS[] = { Ship.FACTORY, Ship.FRIGATE, Ship.BOMBER, Ship.FIGHTER };

	@Override
	protected void onDraw(Canvas canvas) {
		// Update the game.
		mGame.update();

		for (int shipType : SHIP_LAYERS) {
			for (int i = 0; i < Game.NUM_PLAYERS; i++) {
				Player player = mGame.mPlayers[i];
				Map<Type, Bitmap> entityBitmaps = mPlayerEntityBitmaps.get(player);
			
				for (Ship ship : player.mShipLists.get(shipType)) {
					
					Bitmap bitmap = entityBitmaps.get(ship.type);
					
					if (bitmap != null) {
						// Scale the image so that its smallest dimension fills the circle.
						// (Its largest dimension may spill outside the circle.)
						Matrix matrix = new Matrix();
						float scaleX = ship.diameter / bitmap.getWidth();
						float scaleY = ship.diameter / bitmap.getHeight();
						float scale = Math.max(scaleX, scaleY);
						matrix.postScale(scale, scale);
						matrix.postTranslate(ship.body.center.x - (scale / scaleX) * ship.radius, ship.body.center.y - (scale / scaleY) * ship.radius);
						matrix.postRotate((float) Math.toDegrees(ship.heading), ship.body.center.x, ship.body.center.y);
						
						canvas.drawBitmap(bitmap, matrix, mBitmapPaint);
					}
					
					canvas.drawCircle(ship.body.center.x, ship.body.center.y, ship.radius, mBoundsPaints[i]);
				}
			}
		}

		for (Player player : mGame.mPlayers) {
			for (int projectileType : Projectile.TYPES) {
				for (Projectile projectile : player.mProjectileLists.get(projectileType)) {
					canvas.drawCircle(projectile.body.center.x, projectile.body.center.y, projectile.radius, mLaserPaint);
				}
			}
		}
	}
	
	private HashMap<Player, Map<Type, Bitmap>> mPlayerEntityBitmaps;
	
	private Game mGame;
}
