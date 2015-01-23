package com.gregmcnew.android.pax;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

public class Painter {
	
	private static boolean sSharedBuffersInitialized = false;
	private static float sCameraRotationDegrees = 0;

	public static float[][] TEAM_COLORS = {{0, .4f, .8f}, {.8f, 0, 0}};
	
	public static void setCameraRotationDegrees(float degrees) {
		sCameraRotationDegrees = degrees;
	}
	
	public Painter(GL10 gl, Renderer renderer, Context context, int resourceID) {
		mRenderer = renderer;
		mContext = context;
		mResourceID = resourceID;
		mInitialized = false;
		
		mRendererStateID = mRenderer.getStateID();
		
		initialize(gl);
	}
	
	private void initialize(GL10 gl) {
		
		if (mInitialized) {
			return;
		}
		
		mInitialized = true;
		
		Bitmap bitmap = loadBitmap(mResourceID);
		
		// Generate texture IDs.
		int[] textureIDs = new int[1];
		gl.glGenTextures(textureIDs.length, textureIDs, 0);
		mTextureID = textureIDs[0];
		
		loadBuffers(gl);
		
		// Send the bitmap to the video device.
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		
		bitmap.recycle();
	}
	
	public static void invalidateSharedBuffers() {
		sSharedBuffersInitialized = false;
	}
	
	private void loadBuffers(GL10 gl) {
		
		if (sSharedBuffersInitialized) {
			return;
		}
		Log.v(Pax.TAG, "initializing shared buffers");
		sSharedBuffersInitialized = true;

		ByteBuffer byteBuffer;
		byteBuffer = ByteBuffer.allocateDirect(vertices.length * Short.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		sVertexBuffer = byteBuffer.asShortBuffer();
		sVertexBuffer.put(vertices);
		sVertexBuffer.position(0);
		
		byteBuffer = ByteBuffer.allocateDirect(texture.length * Short.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		sTextureBuffer = byteBuffer.asShortBuffer();
		sTextureBuffer.put(texture);
		sTextureBuffer.position(0);
		
		if (Constants.sVertexBufferObjects) {
			GL11 gl11 = (GL11) gl;
			
			// Generate buffer IDs.
			int[] bufferIDs = new int[2];
			gl11.glGenBuffers(bufferIDs.length, bufferIDs, 0);
			sVertexBufferObjectID = bufferIDs[0];
			sTextureBufferObjectID = bufferIDs[1];

			// Upload the vertex data
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, sVertexBufferObjectID);
			sVertexBuffer.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, sVertexBuffer.capacity(), sVertexBuffer, GL11.GL_STATIC_DRAW);
            
			// Upload the texture vertices
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, sTextureBufferObjectID);
			sTextureBuffer.position(0);
			gl11.glBufferData(GL11.GL_ARRAY_BUFFER, sTextureBuffer.capacity(), sTextureBuffer, GL11.GL_STATIC_DRAW);
		}
	}

	public void drawFillBounds(GL10 gl, float minX, float maxX, float minY, float maxY, float rotateDegrees, float alpha) {
		if (minX > maxX) {
			float temp = maxX;
			maxX = minX;
			minX = temp;
		}
		if (minY > maxY) {
			float temp = maxY;
			maxY = minY;
			minY = temp;
		}
		float centerX = (maxX + minX) / 2;
		float centerY = (maxY + minY) / 2;
		float sizeX = maxX - minX;
		float sizeY = maxY - minY;
		draw(gl, centerX, centerY, sizeX, sizeY, rotateDegrees, alpha);
	}
	
	public void draw(GL10 gl, Entity entity) {
		draw(gl, entity.body.center.x, entity.body.center.y, entity.length, entity.diameter, (float) Math.toDegrees(entity.heading), 1f);
	}

	public void draw(GL10 gl, Entity entity, float r, float g, float b) {
		draw(gl, entity.body.center.x, entity.body.center.y, entity.length, entity.diameter, (float) Math.toDegrees(entity.heading), 1f, r, g, b);
	}

	public void draw(GL10 gl, Entity entity, float scale, float r, float g, float b) {
		draw(gl, entity.body.center.x, entity.body.center.y, scale, scale, (float) Math.toDegrees(entity.heading), 1f, r, g, b);
	}

	public void draw(GL10 gl, float moveX, float moveY, float sizeX, float sizeY, float rotateDegrees, float alpha) {
		draw(gl, moveX, moveY, sizeX, sizeY, rotateDegrees, alpha, 1f, 1f, 1f);
	}

	public void draw(GL10 gl, float moveX, float moveY, float sizeX, float sizeY, float rotateDegrees, float alpha, float r, float g, float b) {
		
		if (!mRenderer.inBounds(moveX, sizeX, moveY, sizeY)) {
			return;
		}

        // Make sure we're not using any transformations left over from the
		// last draw().
		gl.glLoadIdentity();
		
		// Rotate about the Z-axis.
		gl.glRotatef(sCameraRotationDegrees, 0f, 0f, 1f);

		gl.glColor4f(r, g, b, alpha);

		if (mRenderer.stateLost(mRendererStateID) || mTextureBuffer != null) {
			
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
			
			// Point to our vertex and texture buffers.

			if (Constants.sVertexBufferObjects) {
				GL11 gl11 = (GL11) gl;
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, sVertexBufferObjectID);
				gl11.glVertexPointer(2, GL10.GL_SHORT, 0, 0);

				if (mTextureBuffer != null) {
					gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
				}
			}
			else {
				gl.glVertexPointer(2, GL10.GL_SHORT, 0, sVertexBuffer);
			}

			if (mTextureBuffer != null) {
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
			}
			else if (Constants.sVertexBufferObjects) {
				GL11 gl11 = (GL11) gl;
				gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, sTextureBufferObjectID);
				gl11.glTexCoordPointer(2, GL10.GL_SHORT, 0, 0);
			}
			else {
				gl.glTexCoordPointer(2, GL10.GL_SHORT, 0, sTextureBuffer);
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
	
	public void drawTrail(GL10 gl, ShortBuffer trailVertices, FloatBuffer vertexColors) {
			
		float sizeX = 2f;
		float sizeY = 2f;
		float alpha = 1.0f;
		float moveX = 0.0f;
		float moveY = 0.0f;
		float rotateDegrees = 0.0f;
		
        // Make sure we're not using any transformations left over from the
		// last draw().
		gl.glLoadIdentity();
		
		// Rotate about the Z-axis.
		gl.glRotatef(sCameraRotationDegrees, 0f, 0f, 1f);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		gl.glColor4f(1f, 1f, 1f, alpha);

		// Force the next painter to rebind.
		mRenderer.loseAllState();
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		
		// Point to our vertex and texture buffers.
		
		if (Constants.sVertexBufferObjects) {
			GL11 gl11 = (GL11) gl;
			
			gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		}
		int originalLimit = trailVertices.limit();
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, vertexColors);

		// Skip (0,0) points at the end of the buffer.
		int i;
		for (i = trailVertices.limit() - 2; i >= 0; i -= 2) {
			short x = trailVertices.get(i);
			short y = trailVertices.get((short) (i + 1));
			if (x != 0 || y != 0) {
				break;
			}
		}
		trailVertices.limit(i + 2);
	
		gl.glVertexPointer(2, GL10.GL_SHORT, 0, trailVertices);
		gl.glTexCoordPointer(2, GL10.GL_SHORT, 0, sTextureBuffer);
		
		gl.glTranslatef(moveX, moveY, 0f);
		
		// Rotate about the Z-axis.
		gl.glRotatef(rotateDegrees, 0f, 0f, 1f);
		
		// The vertices buffer describes a 2x2 square, not a 1x1 square, so each
		// scale value needs to be half of the size.
		gl.glScalef(sizeX / 2, sizeY / 2, 0f);

		// We use 2D vertices, so every vertex is represented by 2 values in
		// 'vertices'.
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, trailVertices.remaining() / 2);
		
		trailVertices.limit(originalLimit);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	}
	
	private Bitmap loadBitmap(int resourceID) {
		Resources resources = mContext.getResources();
		InputStream is = resources.openRawResource(resourceID);
		return BitmapFactory.decodeStream(is);
	}

	public void setTextureBuffer(FloatBuffer buffer) {
		mTextureBuffer = buffer;
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
	
	private static int sVertexBufferObjectID;
	private static int sTextureBufferObjectID;
	private static ShortBuffer sVertexBuffer;
	private static ShortBuffer sTextureBuffer;

	private FloatBuffer mTextureBuffer;

	private boolean mInitialized;
	private int mTextureID;

	private Renderer mRenderer;
	private int mRendererStateID;
	private Context mContext;

	private int mResourceID;
}
