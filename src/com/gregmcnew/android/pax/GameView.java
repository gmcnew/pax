package com.gregmcnew.android.pax;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class GameView extends View {

	public GameView(Context context, Game game) {
		super(context);
		mGame = game;
		// TODO Auto-generated constructor stub
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
		paints[0].setARGB(128, 192, 0, 0);
		paints[1].setARGB(128, 0, 64, 255);
		for (Paint paint : paints) {
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			paint.setStrokeWidth(2);
		}
		int i = 0;
		for (Player player : mGame.mPlayers) {
			for (Ship ship : player.mShips) {
				canvas.drawCircle(ship.location.x, ship.location.y, ship.size / 2, paints[i % paints.length]);
			}
			i++;
		}
	}
	
	private Game mGame;
}
