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
	
	public static Painter Create(GL10 gl, boolean vboSupport, Bitmap bitmap) {
		return new Painter(gl, vboSupport, bitmap, bitmap.getWidth(), bitmap.getHeight());
	}
	
	public static Painter CreateMinSize(GL10 gl, boolean vboSupport, Bitmap bitmap, float minSize) {
		float originalWidth = bitmap.getWidth();
		float originalHeight = bitmap.getHeight();
		
		float minDimension = Math.min(originalWidth, originalHeight);
		float width = minSize * originalWidth / minDimension;
		float height = minSize * originalHeight / minDimension;
		
		return new Painter(gl, vboSupport, bitmap, width, height);
	}
	
	private Painter(GL10 gl, boolean vboSupport, Bitmap bitmap, float width, float height) {
		
		mVBOSupport = vboSupport;
		
		float halfWidth = width / 2;
		vertices[0] = vertices[2] = -halfWidth; // bottom-left, top-left;
		vertices[4] = vertices[6] =  halfWidth; // bottom-right, top-right;

		float halfHeight = height / 2;
		vertices[1] = vertices[5] = -halfHeight; // bottom-left, bottom-right
		vertices[3] = vertices[7] =  halfHeight; // top-left, top-right
		
		ByteBuffer byteBuffer;
		byteBuffer = ByteBuffer.allocateDirect(vertices.length * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuffer.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		byteBuffer = ByteBuffer.allocateDirect(vertices.length * 2);
		byteBuffer.order(ByteOrder.nativeOrder());
		indexBuffer = byteBuffer.asCharBuffer();
		for (int i = 0; i < vertices.length; i++) {
			indexBuffer.put((char) i);
		}
		indexBuffer.position(0);
		
		byteBuffer = ByteBuffer.allocateDirect(texture.length * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuffer.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
		
		// Generate texture IDs.
		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		mTextureID = textureIDs[0];
		
		if (mVBOSupport) {
			GL11 gl11 = (GL11) gl;
			
			// Generate buffer IDs.
			int[] bufferIDs = new int[3];
			gl11.glGenBuffers(2, bufferIDs, 0);
			mVertexBufferObjectID = bufferIDs[0];
			mElementBufferObjectID = bufferIDs[1];
			mTextureBufferObjectID = bufferIDs[2];

			// Upload the vertex data
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectID);
			vertexBuffer.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexBuffer.capacity(), vertexBuffer, GL11.GL_STATIC_DRAW);

			// Upload the index data
			gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectID);
			indexBuffer.position(0);
			gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 2, indexBuffer, GL11.GL_STATIC_DRAW);
            
			// Upload the texture vertices
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureBufferObjectID);
			textureBuffer.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, textureBuffer.capacity(), textureBuffer, GL11.GL_STATIC_DRAW);
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
		draw(gl, entity.body.center.x, entity.body.center.y, (float) Math.toDegrees(entity.heading));
	}
	
	// A bitmap's vertices go from -1 to 1 in the bitmap's largest dimension.
	public void draw(GL10 gl, float moveX, float moveY, float rotateDegrees) {
		
        // Make sure we're not using any transformations left over from the
		// the last draw().
		gl.glLoadIdentity();
        
		gl.glFrontFace(GL10.GL_CW);

		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

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
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		}
		
		gl.glTranslatef(moveX, moveY, 0f);
		
		// Rotate about the Z-axis.
		gl.glRotatef(rotateDegrees, 0f, 0f, 1f);

		// We use 2D vertices, so every vertex is represented by 2 floats in
		// 'vertices'.
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 2);
	}

    private CharBuffer  indexBuffer;
	private FloatBuffer vertexBuffer;
	private FloatBuffer textureBuffer;
	
	private float vertices[] = {
			-1.0f, -1.0f, // bottom-left    2     4
			-1.0f,  1.0f, // top-left
			 1.0f, -1.0f, // bottom-right
			 1.0f,  1.0f, // top-right      1     3
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
}
