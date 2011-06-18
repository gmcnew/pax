package com.gregmcnew.android.pax;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

public class GameRenderer implements GLSurfaceView.Renderer {
	
	// The size of the screen's largest dimension, measured in game units. 
	public static final float GAME_VIEW_SIZE = 480.0f;
	
    
    private static final int[] RESOURCES_TO_LOAD = {
    		R.drawable.bomb,
    		R.drawable.laser,
    		R.drawable.missile,
    		
    		R.drawable.bomber_p1,
    		R.drawable.factory_p1,
    		R.drawable.fighter_p1,
    		R.drawable.frigate_p1,
    		
    		R.drawable.bomber_p2,
    		R.drawable.factory_p2,
    		R.drawable.fighter_p2,
    		R.drawable.frigate_p2,
    		
    		R.drawable.ohyeah,
    };

    
    public GameRenderer(Context context, Game game) {
    	mContext = context;
    	mGame = game;
    }
    
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
    	gl.glEnable(GL10.GL_TEXTURE_2D);
    	gl.glShadeModel(GL10.GL_SMOOTH);
    	gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
    	
    	gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    	
    	// Enable the use of vertex and texture arrays.
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// Print information about the OpenGL driver.
		{
			Log.v("GameViewGL.onSurfaceCreated", "OpenGL version: " + gl.glGetString(GL10.GL_VERSION));
			
			Log.v("GameViewGL.onSurfaceCreated", "OpenGL extensions:");
			String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
			for (String extension : extensions.split(" ")) {
				Log.v("GameViewGL.onSurfaceCreated", "  " + extension);
			}
			
			Log.v("GameViewGL.onSurfaceCreated", "OpenGL renderer: " + gl.glGetString(GL10.GL_RENDERER));
		}

		// Resources! Resources for everyone!
    	Map<Integer, EntityPainter> painterMap = new HashMap<Integer, EntityPainter>();
		Resources resources = mContext.getResources();
		for (int resource : RESOURCES_TO_LOAD) {
			painterMap.put(resource, new EntityPainter(gl, BitmapFactory.decodeResource(resources, resource))); 
		}
		
    	mPlayerEntityPainters = new HashMap<Player, EntityPainter[]>();
    	for (int player = 0; player < Game.NUM_PLAYERS; player++) {
    		EntityPainter[] painters = new EntityPainter[Entity.TYPES.length];
    	
			switch (player) {
				case 0:
					painters[Entity.FIGHTER] = painterMap.get(R.drawable.fighter_p1);
					painters[Entity.BOMBER]  = painterMap.get(R.drawable.bomber_p1);
					painters[Entity.FRIGATE] = painterMap.get(R.drawable.frigate_p1);
					painters[Entity.FACTORY] = painterMap.get(R.drawable.ohyeah);
					break;
				case 1:
				default:
					painters[Entity.FIGHTER] = painterMap.get(R.drawable.fighter_p2);
					painters[Entity.BOMBER]  = painterMap.get(R.drawable.bomber_p2);
					painters[Entity.FRIGATE] = painterMap.get(R.drawable.frigate_p2);
					painters[Entity.FACTORY] = painterMap.get(R.drawable.ohyeah);
					break;
			}
			
			painters[Entity.LASER]   = painterMap.get(R.drawable.laser);
			painters[Entity.BOMB]    = painterMap.get(R.drawable.bomb);
			painters[Entity.MISSILE] = painterMap.get(R.drawable.missile);

    		mPlayerEntityPainters.put(mGame.mPlayers[player], painters);
    	}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        
        // Make sure the largest screen dimension is equal to 480 game units.
        float halfScale = Math.max(width, height) / (GAME_VIEW_SIZE * 2);
        float halfX = width * halfScale;
        float halfY = height * halfScale;
        
        GLU.gluOrtho2D(gl, -halfX, halfX, -halfY, halfY);
 
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
	}
	
	// Draw factories at the bottom, with frigates above them, bombers above
	// frigates, and fighters above everything.
	private static final int ENTITY_LAYERS[] = {
			Entity.FACTORY, Entity.FRIGATE, Entity.BOMBER, Entity.FIGHTER,
			Entity.LASER, Entity.BOMB, Entity.MISSILE
			};
	
	@Override
	public void onDrawFrame(GL10 gl) {
		mGame.update();
		
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		for (int entityType : ENTITY_LAYERS) {
			for (int i = 0; i < Game.NUM_PLAYERS; i++) {
				
				Player player = mGame.mPlayers[i];
				EntityPainter[] painters = mPlayerEntityPainters.get(player);
			
				for (Entity entity : player.mEntities[entityType]) {
					painters[entityType].draw(gl, entity);
				}
			}
		}
	}

	public class EntityPainter {
		
		boolean SUPPORTS_GL11 = true;

		public EntityPainter(GL10 gl, Bitmap bitmap) {
			
			float width = bitmap.getWidth();
			float height = bitmap.getHeight();
			if (width < height) {
				float x = width / height;
				vertices[0] = vertices[2] = -x; // bottom-left, top-left
				vertices[4] = vertices[6] =  x; // bottom-right, top-right
			}
			else {
				float y = height / width;
				vertices[1] = vertices[5] = -y; // bottom-left, bottom-right
				vertices[3] = vertices[7] =  y; // top-left, top-right
			}
			
			ByteBuffer byteBuffer;
			byteBuffer = ByteBuffer.allocateDirect(vertices.length * Float.SIZE);
			byteBuffer.order(ByteOrder.nativeOrder());
			vertexBuffer = byteBuffer.asFloatBuffer();
			vertexBuffer.put(vertices);
			vertexBuffer.position(0);

			int numIndices = 6;
			byteBuffer = ByteBuffer.allocateDirect(numIndices * 2);
			byteBuffer.order(ByteOrder.nativeOrder());
			indexBuffer = byteBuffer.asCharBuffer();
				indexBuffer.put((char) 0);
				indexBuffer.put((char) 1);
				indexBuffer.put((char) 2);
				indexBuffer.put((char) 1);
				indexBuffer.put((char) 2);
				indexBuffer.put((char) 3);
			
			byteBuffer = ByteBuffer.allocateDirect(texture.length * Float.SIZE);
			byteBuffer.order(ByteOrder.nativeOrder());
			textureBuffer = byteBuffer.asFloatBuffer();
			textureBuffer.put(texture);
			textureBuffer.position(0);
			
			if (SUPPORTS_GL11) {
				int[] bufferIDs = new int[2];
				GL11 gl11 = (GL11) gl;
				gl11.glGenBuffers(2, bufferIDs, 0);
				mVertexBufferObjectID = bufferIDs[0];
				mElementBufferObjectID = bufferIDs[1];

	            // Upload the vertex data
	            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectID);
	            vertexBuffer.position(0);
	            gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexBuffer.capacity(), vertexBuffer, GL11.GL_STATIC_DRAW);

	            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectID);
	            indexBuffer.position(0);
	            gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 2, indexBuffer, GL11.GL_STATIC_DRAW);
	            

				gl.glGenTextures(1, mTextureBufferIDs, 0);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureBufferIDs[0]);
				textureBuffer.position(0);
	            gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, textureBuffer.capacity() * 2, textureBuffer, GL11.GL_STATIC_DRAW);
				
			}
			 {
				// Generate and bind a texture pointer.
				Log.v("EntityPainter", String.format("bound to texture %d", mTextureBufferIDs[0]));
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
				gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

				// Send the bitmap to the video device.
				GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			}
			
			bitmap.recycle();
		}
		
		public void draw(GL10 gl, Entity entity) {
			draw(gl, entity.body.center.x, entity.body.center.y, entity.radius, (float) Math.toDegrees(entity.heading));
		}
		
		/*
		 * A bitmap's vertices go from -1 to 1 in the bitmap's largest dimension.
		 */
		public void draw(GL10 gl, float moveX, float moveY, float radius, float rotateDegrees) {
			
	        // Make sure we're not using any transformations left over from the
			// the last draw().
	        gl.glLoadIdentity();

			//Log.v("EntityPainter", "binding texture...");
			//gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureBufferIDs[0]);

			//Log.v("EntityPainter", "setting frontface...");
			// Set face rotation.
			gl.glFrontFace(GL10.GL_CW);

			// Point to our vertex and texture buffers.
			if (SUPPORTS_GL11) {

				gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureBufferIDs[0]);
				
	            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	            
	            //Set the face rotation
	            gl.glFrontFace(GL10.GL_CCW);

				GL11 gl11 = (GL11) gl;
	            
				
				
				
				
	            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectID);
	            gl11.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);

	            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mTextureBufferIDs[0]);
	            gl11.glTexCoordPointer(2, GL10.GL_FLOAT, 0, 0);

	            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectID);
	            
	            
	            
	            
	            
	            
	            /*
	            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferObjectID);
	            gl11.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
				Log.v("EntityPainter", "setting text coord pointer...");
	            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
	            
				Log.v("EntityPainter", "binding to element buffer...");
	            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectID);
	            
				Log.v("EntityPainter", "finished binding to element buffer.");
	            gl.glEnableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES);
	            gl.glEnableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES);
	            */
	            
	            //GL11Ext.glWeightPointerOES(2, GL10.GL_FLOAT, 0, VERTEX_WEIGHT_BUFFER_INDEX_OFFSET  * FLOAT_SIZE);
	            //GL11Ext.glMatrixIndexPointerOES(2, GL10.GL_UNSIGNED_BYTE, 0, VERTEX_PALETTE_INDEX_OFFSET );
			}
			else {
				gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
			}
			
			gl.glTranslatef(moveX, moveY, 0f);
			gl.glScalef(radius, radius, 0f);
			
			// Rotate about the Z-axis.
			gl.glRotatef(rotateDegrees, 0f, 0f, 1f);

			// We use 2D vertices, so every vertex is represented by 2 floats in
			// 'vertices'.
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 2);
			
			/*
            GL11 gl11 = (GL11) gl;
            GL11Ext gl11Ext = (GL11Ext) gl;
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            int VERTEX_SIZE = 8;
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, mVertexBufferIDs[0]);
            gl11.glVertexPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, 0);
            gl11.glTexCoordPointer(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_TEXTURE_BUFFER_INDEX_OFFSET * FLOAT_SIZE);

            gl.glEnableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES);
            gl.glEnableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES);

            gl11Ext.glWeightPointerOES(2, GL10.GL_FLOAT, VERTEX_SIZE, VERTEX_WEIGHT_BUFFER_INDEX_OFFSET  * FLOAT_SIZE);
            gl11Ext.glMatrixIndexPointerOES(2, GL10.GL_UNSIGNED_BYTE, VERTEX_SIZE, VERTEX_PALETTE_INDEX_OFFSET );

            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mElementBufferObjectId);
            gl11.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, 0);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL11Ext.GL_MATRIX_INDEX_ARRAY_OES);
            gl.glDisableClientState(GL11Ext.GL_WEIGHT_ARRAY_OES);
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
            */
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
		
		private int[] mVertexBufferIDs  = new int[2];
		private int[] mTextureBufferIDs = new int[1];
		private int mVertexBufferObjectID;
		private int mElementBufferObjectID;
	}

	private Context mContext; 
	private Game mGame;
	private Map<Player, EntityPainter[]> mPlayerEntityPainters;
}
