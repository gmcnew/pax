package com.gregmcnew.android.pax;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class Painter {
	
	public Painter(GL10 gl, boolean vboSupport, Bitmap bitmap) {
		
		mVBOSupport = vboSupport;
		
		ByteBuffer byteBuffer;
		byteBuffer = ByteBuffer.allocateDirect(vertices.length * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuffer.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		byteBuffer = ByteBuffer.allocateDirect(vertices.length * 2);
		byteBuffer.order(ByteOrder.nativeOrder());
		mIndexBuffer = byteBuffer.asCharBuffer();
		for (int i = 0; i < vertices.length; i++) {
			mIndexBuffer.put((char) i);
		}
		mIndexBuffer.position(0);
		
		byteBuffer = ByteBuffer.allocateDirect(texture.length * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mTextureBuffer = byteBuffer.asFloatBuffer();
		mTextureBuffer.put(texture);
		mTextureBuffer.position(0);
		
		// Generate texture IDs.
		int[] textureIDs = new int[1];
		gl.glGenTextures(textureIDs.length, textureIDs, 0);
		mTextureID = textureIDs[0];
		
		if (mVBOSupport) {
			GL11 gl11 = (GL11) gl;
			
			// Generate buffer IDs.
			int[] bufferIDs = new int[3];
			gl11.glGenBuffers(bufferIDs.length, bufferIDs, 0);
			mVertexBufferObjectID = bufferIDs[0];
			mElementBufferObjectID = bufferIDs[1];
			mTextureBufferObjectID = bufferIDs[2];

			// Upload the vertex data
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectID);
			mVertexBuffer.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mVertexBuffer.capacity(), mVertexBuffer, GL11.GL_STATIC_DRAW);

			// Upload the index data
			gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectID);
			mIndexBuffer.position(0);
			gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer.capacity() * 2, mIndexBuffer, GL11.GL_STATIC_DRAW);
            
			// Upload the texture vertices
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureBufferObjectID);
			mTextureBuffer.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mTextureBuffer.capacity(), mTextureBuffer, GL11.GL_STATIC_DRAW);
		}
		
		// Set texture filtering parameters.
		//gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		//gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		
		// Send the bitmap to the video device.
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	}
	
	public void draw(GL10 gl, Entity entity) {
		draw(gl, entity.body.center.x, entity.body.center.y, entity.length, entity.diameter, (float) Math.toDegrees(entity.heading), 1f);
	}
	
	public void drawFillBounds(GL10 gl, float minX, float maxX, float minY, float maxY, float rotateDegrees, float alpha) {
		float centerX = (maxX + minX) / 2;
		float centerY = (maxY + minY) / 2;
		float scaleX = maxX - minX;
		float scaleY = maxY - minY;
		draw(gl, centerX, centerY, scaleX, scaleY, rotateDegrees, alpha);
	}
	
	public void draw(GL10 gl, float moveX, float moveY, float scaleX, float scaleY, float rotateDegrees, float alpha) {
		
        // Make sure we're not using any transformations left over from the
		// the last draw().
		gl.glLoadIdentity();
		
		// Rotate about the Z-axis.
		gl.glRotatef(mCameraRotationDegrees, 0f, 0f, 1f);
        
		gl.glFrontFace(GL10.GL_CW);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		gl.glColor4f(1f, 1f, 1f, alpha);

		// Point to our vertex and texture buffers.
		if (mVBOSupport) {
			
			gl.glEnableClientState(GL10.GL_TEXTURE_2D);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			GL11 gl11 = (GL11) gl;
			
			// Point to our buffers
            
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectID);
			gl11.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);
			
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureBufferObjectID);
			gl11.glTexCoordPointer(2, GL10.GL_FLOAT, 0, 0);

			gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectID);
		}
		else {
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
		}
		
		gl.glTranslatef(moveX, moveY, 0f);
		
		// Rotate about the Z-axis.
		gl.glRotatef(rotateDegrees, 0f, 0f, 1f);
		gl.glScalef(scaleX, scaleY, 0f);

		// We use 2D vertices, so every vertex is represented by 2 floats in
		// 'vertices'.
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 2);
	}
	
	public void setCameraRotationDegrees(float degrees) {
		mCameraRotationDegrees = degrees;
	}
	
	private float vertices[] = {
			-0.5f, -0.5f, // bottom-left    2     4
			-0.5f,  0.5f, // top-left
			 0.5f, -0.5f, // bottom-right
			 0.5f,  0.5f, // top-right      1     3
	};
	
	private float texture[] = {
			0.0f, 1.0f, // top left     (vertex 2)
			0.0f, 0.0f, // bottom left  (vertex 1)
			1.0f, 1.0f, // top right    (vertex 4)
			1.0f, 0.0f  // bottom right (vertex 3)
	};
	
	private int mVertexBufferObjectID;
	private int mElementBufferObjectID;
	private int mTextureBufferObjectID;
	private int mTextureID;
	private boolean mVBOSupport;

    private CharBuffer  mIndexBuffer;
	private FloatBuffer mVertexBuffer;
	private FloatBuffer mTextureBuffer;
	
	private float mCameraRotationDegrees;
}
