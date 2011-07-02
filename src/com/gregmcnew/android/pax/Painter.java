package com.gregmcnew.android.pax;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class Painter {
	
	private static Painter sLastPainter = null;
	private static float sCameraRotationDegrees = 0;
	
	public static void setCameraRotationDegrees(float degrees) {
		sCameraRotationDegrees = degrees;
	}
	
	public Painter(GL10 gl, boolean vboSupport, Bitmap bitmap) {
		
		mVBOSupport = vboSupport;
		
		ByteBuffer byteBuffer;
		byteBuffer = ByteBuffer.allocateDirect(vertices.length * Short.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuffer.asShortBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
		
		byteBuffer = ByteBuffer.allocateDirect(texture.length * Short.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mTextureBuffer = byteBuffer.asShortBuffer();
		mTextureBuffer.put(texture);
		mTextureBuffer.position(0);
		
		// Generate texture IDs.
		int[] textureIDs = new int[1];
		gl.glGenTextures(textureIDs.length, textureIDs, 0);
		mTextureID = textureIDs[0];
		
		if (mVBOSupport) {
			GL11 gl11 = (GL11) gl;
			
			// Generate buffer IDs.
			int[] bufferIDs = new int[2];
			gl11.glGenBuffers(bufferIDs.length, bufferIDs, 0);
			mVertexBufferObjectID = bufferIDs[0];
			mTextureBufferObjectID = bufferIDs[1];

			// Upload the vertex data
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectID);
			mVertexBuffer.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mVertexBuffer.capacity(), mVertexBuffer, GL11.GL_STATIC_DRAW);
            
			// Upload the texture vertices
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureBufferObjectID);
			mTextureBuffer.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, mTextureBuffer.capacity(), mTextureBuffer, GL11.GL_STATIC_DRAW);
		}
		
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
		float sizeX = maxX - minX;
		float sizeY = maxY - minY;
		draw(gl, centerX, centerY, sizeX, sizeY, rotateDegrees, alpha);
	}
	
	public void draw(GL10 gl, float moveX, float moveY, float sizeX, float sizeY, float rotateDegrees, float alpha) {
		
        // Make sure we're not using any transformations left over from the
		// the last draw().
		gl.glLoadIdentity();
		
		// Rotate about the Z-axis.
		gl.glRotatef(sCameraRotationDegrees, 0f, 0f, 1f);

		gl.glColor4f(1f, 1f, 1f, alpha);

		// We don't need to rebind everything if we were the last painter to
		// draw.
		if (sLastPainter != this) {
			sLastPainter = this;
			
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
			
			// Point to our vertex and texture buffers.
			if (mVBOSupport) {
				GL11 gl11 = (GL11) gl;
				
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectID);
				gl11.glVertexPointer(2, GL10.GL_SHORT, 0, 0);
				
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureBufferObjectID);
				gl11.glTexCoordPointer(2, GL10.GL_SHORT, 0, 0);
			}
			else {
				gl.glVertexPointer(2, GL10.GL_SHORT, 0, mVertexBuffer);
				gl.glTexCoordPointer(2, GL10.GL_SHORT, 0, mTextureBuffer);
			}
		}
		
		gl.glTranslatef(moveX, moveY, 0f);
		
		// Rotate about the Z-axis.
		gl.glRotatef(rotateDegrees, 0f, 0f, 1f);
		
		// The vertices buffer describes a 2x2 square, not a 1x1 square, so each
		// scale value needs to be half of the size.
		gl.glScalef(sizeX / 2, sizeY / 2, 0f);

		// We use 2D vertices, so every vertex is represented by 2 values in
		// 'vertices'.
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 2);
	}
	
	private short vertices[] = {
			-1, -1, // bottom-left    2     4
			-1,  1, // top-left
			 1, -1, // bottom-right
			 1,  1, // top-right      1     3
	};
	
	private short texture[] = {
			 0,  1, // top left     (vertex 2)
			 0,  0, // bottom left  (vertex 1)
			 1,  1, // top right    (vertex 4)
			 1,  0, // bottom right (vertex 3)
	};
	
	private int mVertexBufferObjectID;
	private int mTextureBufferObjectID;
	private int mTextureID;
	private boolean mVBOSupport;
	
	private ShortBuffer mVertexBuffer;
	private ShortBuffer mTextureBuffer;
}
