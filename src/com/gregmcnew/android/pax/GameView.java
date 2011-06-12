package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import com.gregmcnew.android.pax.Entity.Type;

import android.content.Context;
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
			
			switch (i) {
				case 0:
					playerBitmaps.put(Type.FIGHTER, BitmapFactory.decodeResource(getResources(), R.drawable.fighter_p1));
					playerBitmaps.put(Type.BOMBER, BitmapFactory.decodeResource(getResources(), R.drawable.bomber_p1));
					playerBitmaps.put(Type.FRIGATE, BitmapFactory.decodeResource(getResources(), R.drawable.frigate_p1));
					playerBitmaps.put(Type.FACTORY, BitmapFactory.decodeResource(getResources(), R.drawable.factory_p1));
					break;
				case 1:
					playerBitmaps.put(Type.FIGHTER, BitmapFactory.decodeResource(getResources(), R.drawable.fighter_p2));
					playerBitmaps.put(Type.BOMBER, BitmapFactory.decodeResource(getResources(), R.drawable.bomber_p2));
					playerBitmaps.put(Type.FRIGATE, BitmapFactory.decodeResource(getResources(), R.drawable.frigate_p2));
					playerBitmaps.put(Type.FACTORY, BitmapFactory.decodeResource(getResources(), R.drawable.factory_p2));
					break;
			}
			
			mPlayerEntityBitmaps.put(game.mPlayers.get(i), playerBitmaps);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Update the game.
		mGame.update();
		
		/*
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setARGB(255, 192, 0, 0);
		Rect targetRect = new Rect(0, 0, 0, canvas.getHeight());
		
		int width = canvas.getWidth() / 4;
		targetRect.left = mGame.mPlayers[0].buildTarget.ordinal() * width;
		targetRect.right = targetRect.left + width;
		canvas.drawRect(targetRect, paint);
		*/
		
		Paint[] paints = new Paint[2];
		paints[0] = new Paint();
		paints[1] = new Paint();
		paints[0].setARGB(192, 0, 64, 255);
		paints[1].setARGB(192, 192, 0, 0);
		for (Paint paint : paints) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(2);
		}
		int i = 0;
		
		Paint laserPaint = new Paint();
		laserPaint.setARGB(255, 255, 255, 255);
		laserPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		Paint fp = new Paint(Paint.FILTER_BITMAP_FLAG);
		for (Player player : mGame.mPlayers) {
			for (Entity ship : player.mEntities) {
				
				if (ship == null) {
					continue;
				}
				
				Bitmap bitmap = null;
				
				Map<Type, Bitmap> shipBitmaps = mPlayerEntityBitmaps.get(player);
				if (shipBitmaps != null) {
					bitmap = shipBitmaps.get(ship.type);
				}
				
				if (bitmap != null) {
					// Scale the image so that its smallest dimension fills the circle.
					// (Its largest dimension may spill outside the circle.)
					Matrix matrix = new Matrix();
					float scaleX = ship.diameter / bitmap.getWidth();
					float scaleY = ship.diameter / bitmap.getHeight();
					float scale = Math.max(scaleX, scaleY);
					matrix.postScale(scale, scale);
					matrix.postTranslate(ship.location.x - (scale / scaleX) * ship.radius, ship.location.y - (scale / scaleY) * ship.radius);
					matrix.postRotate(ship.heading, ship.location.x, ship.location.y);
					
					canvas.drawBitmap(bitmap, matrix, fp);
				}
				
				if (ship.isShip) { 
					canvas.drawCircle(ship.location.x, ship.location.y, ship.radius, paints[i % paints.length]);
				}
				else {
					canvas.drawCircle(ship.location.x, ship.location.y, ship.radius, laserPaint);
				}
			}
			i++;
		}
	}
	
	private HashMap<Player, Map<Type, Bitmap>> mPlayerEntityBitmaps;
	
	private Game mGame;
}
