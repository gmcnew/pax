package com.gregmcnew.android.pax;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class PrimitivePainter {

	public PrimitivePainter(GL10 gl, Renderer renderer) {
		
		mRenderer = renderer;
		mRendererStateID = mRenderer.getStateID();
		
		mCircleVertices = null;
		mLineVertices = null;
		
		mFillRed = mFillGreen = mFillBlue = mFillAlpha = 0f;
		mStrokeRed = mStrokeGreen = mStrokeBlue = mStrokeAlpha = 0f;

		float lineVertices[] = { -0.5f, 0, 0.5f, 0 };
		
		float circleVertices[] = new float[CIRCLE_POINTS * 2];
		
		for (int i = 0; i < CIRCLE_POINTS; i++) {
			float angle = (float) (Math.PI * i * 2) / CIRCLE_POINTS;
			circleVertices[i * 2]     = (float) Math.cos(angle) / 2;
			circleVertices[i * 2 + 1] = (float) Math.sin(angle) / 2;
		}
		
		ByteBuffer byteBuffer;
		
		byteBuffer = ByteBuffer.allocateDirect(lineVertices.length * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mLineVertices = byteBuffer.asFloatBuffer();
		mLineVertices.put(lineVertices);
		mLineVertices.position(0);
		
		byteBuffer = ByteBuffer.allocateDirect(circleVertices.length * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mCircleVertices = byteBuffer.asFloatBuffer();
		mCircleVertices.put(circleVertices);
		mCircleVertices.position(0);
		
		if (Constants.sVertexBufferObjects) {
			GL11 gl11 = (GL11) gl;
			
			// Generate buffer IDs.
			int[] bufferIDs = new int[2];
			gl11.glGenBuffers(bufferIDs.length, bufferIDs, 0);
			mCircleVertexBufferObjectID = bufferIDs[0];
			mLineVertexBufferObjectID = bufferIDs[1];

			// Upload the vertex data
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mCircleVertexBufferObjectID);
			mCircleVertices.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mCircleVertices.capacity(), mCircleVertices, GL11.GL_STATIC_DRAW);
			
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mLineVertexBufferObjectID);
			mLineVertices.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mLineVertices.capacity(), mLineVertices, GL11.GL_STATIC_DRAW);
		}
	}
    
    public void setFillColor(float r, float g, float b, float a) {
    	mFillRed   = r;
    	mFillGreen = g;
    	mFillBlue  = b;
    	mFillAlpha = a;
    }
    
    public void setStrokeColor(float r, float g, float b, float a) {
    	mStrokeRed   = r;
    	mStrokeGreen = g;
    	mStrokeBlue  = b;
    	mStrokeAlpha = a;
    }
	
	public void drawCircle(GL10 gl, float x, float y, float radius) {
		prepareCircle(gl, x, y, radius, 0);
		
		if (mFillAlpha != 0) {
			gl.glColor4f(mFillRed, mFillGreen, mFillBlue, mFillAlpha);
			gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, CIRCLE_POINTS);
		}
		
		if (mStrokeAlpha != 0) {
			gl.glColor4f(mStrokeRed, mStrokeGreen, mStrokeBlue, mStrokeAlpha);
			gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, CIRCLE_POINTS);
		}
	}
	
	public void drawDottedCircle(GL10 gl, float x, float y, float radius, float rotation, int segments) {
		prepareCircle(gl, x, y, radius, rotation);
		
		if (mFillAlpha != 0) {
			gl.glColor4f(mFillRed, mFillGreen, mFillBlue, mFillAlpha);
			gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, CIRCLE_POINTS);
		}
		
		if (mStrokeAlpha != 0) {
			gl.glColor4f(mStrokeRed, mStrokeGreen, mStrokeBlue, mStrokeAlpha);
			for (int i = 0; i < segments; i++) {
				gl.glDrawArrays(GL10.GL_LINE_STRIP,
						CIRCLE_POINTS * i / segments,
						CIRCLE_POINTS / (2 * segments));
			}
		}
	}
    
    private void prepareCircle(GL10 gl, float x, float y, float radius, float rotation) {
		
    	if (mRenderer.stateLost(mRendererStateID)) {
			gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
	    	
			if (Constants.sVertexBufferObjects) {
				GL11 gl11 = (GL11) gl;
				
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mCircleVertexBufferObjectID);
				gl11.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);
			}
			else {
				gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mCircleVertices);
			}
    	}
			
		gl.glLoadIdentity();
		gl.glLineWidth(1f);
		gl.glTranslatef(x, y, 0);
		gl.glRotatef(rotation, 0, 0, 1);
		gl.glScalef(radius * 2, radius * 2, 0);
    }

	private int mCircleVertexBufferObjectID;
	private int mLineVertexBufferObjectID;
	private FloatBuffer mCircleVertices;
	private FloatBuffer mLineVertices;
	
	private float mFillRed, mFillGreen, mFillBlue, mFillAlpha;
	private float mStrokeRed, mStrokeGreen, mStrokeBlue, mStrokeAlpha;
	
	private static final int CIRCLE_POINTS = 60;
	
	private Renderer mRenderer;
	private int mRendererStateID;
}
