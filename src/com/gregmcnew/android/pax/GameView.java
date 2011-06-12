package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import com.gregmcnew.android.pax.Ship.ShipType;

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
		
		mPlayerShipBitmaps = new HashMap<Player, Map<ShipType, Bitmap>>();
		
		// Load all bitmaps
		for (int i = 0; i < Game.NUM_PLAYERS; i++) {
			HashMap<ShipType, Bitmap> playerBitmaps = new HashMap<ShipType, Bitmap>();
			
			switch (i) {
				case 0:
					playerBitmaps.put(ShipType.FIGHTER, BitmapFactory.decodeResource(getResources(), R.drawable.fighter_p1));
					playerBitmaps.put(ShipType.BOMBER, BitmapFactory.decodeResource(getResources(), R.drawable.bomber_p1));
					playerBitmaps.put(ShipType.FRIGATE, BitmapFactory.decodeResource(getResources(), R.drawable.frigate_p1));
					playerBitmaps.put(ShipType.FACTORY, BitmapFactory.decodeResource(getResources(), R.drawable.factory_p1));
					break;
				case 1:
					playerBitmaps.put(ShipType.FIGHTER, BitmapFactory.decodeResource(getResources(), R.drawable.fighter_p2));
					playerBitmaps.put(ShipType.BOMBER, BitmapFactory.decodeResource(getResources(), R.drawable.bomber_p2));
					playerBitmaps.put(ShipType.FRIGATE, BitmapFactory.decodeResource(getResources(), R.drawable.frigate_p2));
					playerBitmaps.put(ShipType.FACTORY, BitmapFactory.decodeResource(getResources(), R.drawable.factory_p2));
					break;
			}
			
			mPlayerShipBitmaps.put(game.mPlayers[i], playerBitmaps);
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
		
		Paint fp = new Paint(Paint.FILTER_BITMAP_FLAG);
		for (Player player : mGame.mPlayers) {
			for (Ship ship : player.mShips) {
				
				Bitmap bitmap = null;
				
				Map<ShipType, Bitmap> shipBitmaps = mPlayerShipBitmaps.get(player);
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
				
				canvas.drawCircle(ship.location.x, ship.location.y, ship.radius, paints[i % paints.length]);
			}
			i++;
		}
	}
	
	private HashMap<Player, Map<ShipType, Bitmap>> mPlayerShipBitmaps;
	
	private Game mGame;
}
