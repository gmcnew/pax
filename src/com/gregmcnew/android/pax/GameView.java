package com.gregmcnew.android.pax;

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
				switch (ship.type) {
					case FIGHTER: 
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fighter_p1);
						break;
					case BOMBER: 
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bomber_p1);
						break;
					case FRIGATE: 
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.frigate_p1);
						break;
					case FACTORY: 
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.factory_p1);
						break;
				}
				
				if (bitmap != null) {
					// Scale the image so that its smallest dimension fills the circle.
					// (Its largest dimension may spill outside the circle.)
					Matrix matrix = new Matrix();
					float scaleX = ship.size / bitmap.getWidth();
					float scaleY = ship.size / bitmap.getHeight();
					float scale = Math.max(scaleX, scaleY);
					matrix.postScale(scale, scale);
					matrix.postTranslate(ship.location.x - (scale / scaleX) * (ship.size / 2), ship.location.y - (scale / scaleY) * ship.size / 2);
					matrix.postRotate(ship.heading, ship.location.x, ship.location.y);
					
					canvas.drawBitmap(bitmap, matrix, fp);
				}
				
				canvas.drawCircle(ship.location.x, ship.location.y, ship.size / 2, paints[i % paints.length]);
			}
			i++;
		}
	}
	
	private Game mGame;
}
