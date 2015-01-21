package com.gregmcnew.android.pax;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class TextPainter {

	public static int WRAP = (1 << 0);
	public static int ALIGN_LEFT = (1 << 1);
	public static int ALIGN_RIGHT = (1 << 2);
	public static int ALIGN_TOP = (1 << 3);
	public static int ALIGN_BOTTOM = (1 << 4);

	public TextPainter(GL10 gl, Renderer renderer) {
		mPainter = renderer.getPainter(gl, R.drawable.characters);
		mTexture = new float[8];
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mTexture.length * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mFloatBuffer = byteBuffer.asFloatBuffer();
	}

	public void setColor(float r, float g, float b, float a) {
		mR = r;
		mG = g;
		mB = b;
		mA = a;
	}

	public void drawText(GL10 gl, String text, float size, float x, float y, int flags) {
		float width = size * text.length();
		float height = size * 16 / 6;

		// Adjust for alignment.
		if ((flags & ALIGN_LEFT) != 0) {
			// do nothing
		}
		else if ((flags & ALIGN_RIGHT) != 0) {
			x -= width;
		}
		else {
			x -= width / 2;
		}

		if ((flags & ALIGN_TOP) != 0) {
			y -= height / 2;
		}
		else if ((flags & ALIGN_BOTTOM) != 0) {
			y += height / 2;
		}

		for (char c : text.toCharArray()) {
			int offset = c - ' ';
			float left = (float) (offset % 16) / 16;
			float right = left + 1f / 16;
			float top = (float) (offset / 16) / 6;
			float bottom = top + 1f / 6;

			mTexture[0] = mTexture[2] = left;
			mTexture[1] = mTexture[5] = bottom;
			mTexture[3] = mTexture[7] = top;
			mTexture[4] = mTexture[6] = right;
			/*
				array offsets:
				[0] left,  [1] bottom,
				[2] left,  [3] top,
				[4] right, [5] bottom,
				[6] right, [7] top
			*/

			mFloatBuffer.put(mTexture);
			mFloatBuffer.position(0);

			mPainter.setTextureBuffer(mFloatBuffer);
			mPainter.draw(gl, x, y, size, height, 0, mA, mR, mG, mB);
			x += size;
		}
	}

	private float[] mTexture;
	private FloatBuffer mFloatBuffer;
	private float mR, mG, mB, mA;
	private Painter mPainter;
}
