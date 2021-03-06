package com.gregmcnew.android.pax;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class QuadtreePainter {

	public static void draw(GL10 gl, Quadtree q, FloatBuffer lineVertices, boolean bluePlayer, int rotation) {
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, lineVertices);
		draw(gl, q, bluePlayer, rotation);
	}

	public static void draw(GL10 gl, Quadtree q, boolean bluePlayer, int rotation) {

		float minX = q.getMinX(), maxX = q.getMaxX(), minY = q.getMinY(), maxY = q.getMaxY();

		if (rotation % 2 != 0) {
			float temp = minX;
			minX = minY;
			minY = temp;
			temp = maxX;
			maxX = maxY;
			maxY = temp;

			if (rotation == 1) {
				minX *= -1;
				maxX *= -1;
			}
			else {
				minY *= -1;
				maxY *= -1;
			}
		}

		float delta = (maxX - minX) * GameRenderer.GAME_VIEW_SIZE / 2;

		float alpha = 1f;

		gl.glLoadIdentity();
		if (bluePlayer) {
			gl.glColor4f(0f, 1f, 1f, alpha);
		}
		else {
			gl.glColor4f(1f, 1f, 0f, alpha);
		}
		gl.glTranslatef(minX, minY, 0);
		gl.glScalef(delta, delta, delta);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);

		gl.glLoadIdentity();
		if (bluePlayer) {
			gl.glColor4f(0f, 0.5f, 1f, alpha);
		}
		else {
			gl.glColor4f(1f, 0.5f, 0f, alpha);
		}
		gl.glTranslatef(maxX, maxY, 0);
		gl.glScalef(delta, delta, delta);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);

		if (!q.getIsLeaf()) {
			draw(gl, q.getLow(), bluePlayer, rotation);
			draw(gl, q.getHigh(), bluePlayer, rotation);
		}
	}
}
