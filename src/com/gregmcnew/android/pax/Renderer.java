package com.gregmcnew.android.pax;

import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

public abstract class Renderer implements GLSurfaceView.Renderer {
	
	public Renderer(Context context) {
		mContext = context;
    	
    	mPainters = new HashMap<Integer, Painter>();
	}

	@Override
	public abstract void onDrawFrame(GL10 gl);

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        
        mScreenWidth = width;
        mScreenHeight = height;
        
        gl.glViewport(0, 0, width, height);
        Log.v(Pax.TAG, String.format("Renderer.onSurfaceChanged with width %d, height %d", width, height));
		
	}
	
	public void updateRotation(int rotation) {
    	mRotation = rotation;
    	Painter.setCameraRotationDegrees(90 * mRotation);
    	Log.v(Pax.TAG, String.format("rotation is now %d", rotation));
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
    	// If the surface has been recreated, all textures will need to be
    	// reloaded. This means we should start over with new painters.
		Painter.resetSharedBuffers();
    	mPainters.clear();
		
    	gl.glEnable(GL10.GL_TEXTURE_2D);
    	gl.glShadeModel(GL10.GL_SMOOTH);
    	gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
    	
    	gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    	
    	// Enable the use of vertex and texture arrays.
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		// Enable texture transparency.
		gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		String version = gl.glGetString(GL10.GL_VERSION);
		String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
		String renderer = gl.glGetString(GL10.GL_RENDERER);
		
		// Print information about the OpenGL driver.
		{
			Log.v("GameViewGL.onSurfaceCreated", "OpenGL version: " + version);
			
			Log.v("GameViewGL.onSurfaceCreated", "OpenGL extensions:");
			for (String extension : extensions.split(" ")) {
				Log.v("GameViewGL.onSurfaceCreated", "  " + extension);
			}
			
			Log.v("GameViewGL.onSurfaceCreated", "OpenGL renderer: " + renderer);
		}
        
		// The device supports VBOs if (1) its version isn't 1.0 (since VBOs are
		// standard in 1.1 and above) or (2) its extensions list includes
		// "vertex_buffer_object".
		mVBOSupport = (!version.contains("1.0")) || extensions.contains("vertex_buffer_object");
		Log.v(Pax.TAG, mVBOSupport ? "device supports VBOs" : "device doesn't support VBOs");
		
		FramerateCounter.start();
	}
    
    // Returns a Painter for a resource (creating a new one if necessary).
    protected Painter getPainter(GL10 gl, int resourceID) {
    	
    	Painter painter = mPainters.get(resourceID);
    	
    	if (painter == null) {
    		painter = new Painter(gl, mContext, mVBOSupport, resourceID);
    		
    		mPainters.put(resourceID, painter);
    	}
    	
    	return painter;
    }
	
	public void drawStars(GL10 gl, StarField stars, Painter starPainter, float width, float height) {
		float scale = Math.max(width, height);
		float size = 5f;
		for (StarField.Star star : stars.mStars) {
			float alpha = star.mAge < 1000 ? ((float) star.mAge / 1000) : 1f;
			starPainter.draw(gl, star.mX * scale, star.mY * scale, size, size, 0f, alpha);
		}
	}
	
	// The screen's width and height, in pixels.
	// These values -will- change when the screen is rotated.
	protected float mScreenWidth;
	protected float mScreenHeight;
	protected int mRotation;
	
    private Map<Integer, Painter> mPainters;
	private Context mContext;
	private boolean mVBOSupport;
}
