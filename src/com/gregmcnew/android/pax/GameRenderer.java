package com.gregmcnew.android.pax;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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
					painters[Entity.FACTORY] = painterMap.get(R.drawable.factory_p1);
					break;
				case 1:
				default:
					painters[Entity.FIGHTER] = painterMap.get(R.drawable.fighter_p2);
					painters[Entity.BOMBER]  = painterMap.get(R.drawable.bomber_p2);
					painters[Entity.FRIGATE] = painterMap.get(R.drawable.frigate_p2);
					painters[Entity.FACTORY] = painterMap.get(R.drawable.factory_p2);
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
			

			// Generate and bind a texture pointer.
			gl.glGenTextures(1, textures, 0);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
			Log.v("EntityPainter", String.format("bound to texture %d", textures[0]));

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

			// Turn the bitmap into a 2D texture.
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			
			ByteBuffer byteBuffer;
			byteBuffer = ByteBuffer.allocateDirect(vertices.length * Float.SIZE);
			byteBuffer.order(ByteOrder.nativeOrder());
			vertexBuffer = byteBuffer.asFloatBuffer();
			vertexBuffer.put(vertices);
			vertexBuffer.position(0);

			byteBuffer = ByteBuffer.allocateDirect(texture.length * Float.SIZE);
			byteBuffer.order(ByteOrder.nativeOrder());
			textureBuffer = byteBuffer.asFloatBuffer();
			textureBuffer.put(texture);
			textureBuffer.position(0);
			
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
	        
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

			// Set face rotation.
			gl.glFrontFace(GL10.GL_CW);

			// Point to our vertex and texture buffers.
			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
			
			gl.glTranslatef(moveX, moveY, 0f);
			gl.glScalef(radius, radius, 0f);
			
			// Rotate about the Z-axis.
			gl.glRotatef(rotateDegrees, 0f, 0f, 1f);

			// We use 2D vertices, so every vertex is represented by 2 floats in
			// 'vertices'.
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 2);
		}
		
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
		
		private int[] textures = new int[1];
	}

	private Context mContext; 
	private Game mGame;
	private Map<Player, EntityPainter[]> mPlayerEntityPainters;
}
