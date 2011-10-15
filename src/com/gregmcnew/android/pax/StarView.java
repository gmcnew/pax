package com.gregmcnew.android.pax;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;

public class StarView extends GLSurfaceView {

	public StarView(Context context, AttributeSet attributes) {
		super(context, attributes);
		
		mRenderer = new StarRenderer(context);
		
		setEGLConfigChooser(false);
		setRenderer(mRenderer);
	}
	
	private StarRenderer mRenderer; 
    
    private class StarRenderer extends com.gregmcnew.android.pax.Renderer {

		public StarRenderer(Context context) {
			super(context);
			mStarField = new StarField();
		}
		
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			super.onSurfaceCreated(gl, config);
			mStarPainter = getPainter(gl, R.drawable.star);
		}
		
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			super.onSurfaceChanged(gl, width, height);
			
	        gl.glMatrixMode(GL10.GL_PROJECTION);
	        gl.glLoadIdentity();
	        
	        float halfX = width / 2;
	        float halfY = height / 2;
	        
	        GLU.gluOrtho2D(gl, -halfX, halfX, -halfY, halfY);
	        
	        gl.glMatrixMode(GL10.GL_MODELVIEW);
	        gl.glLoadIdentity();
		}
		
		@Override
		public void onDrawFrame(GL10 gl) {
			long dt = FramerateCounter.tick();
			mStarField.update(dt);
			
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			// This seems to be necessary to avoid camera offset problems when the
			// screen is rotated while the game is paused (and rendering is thus
			// paused as well). TODO: Figure this out.
			gl.glViewport(0, 0, (int) mScreenWidth, (int) mScreenHeight);
			
			mStarField.draw(gl, mStarPainter, mScreenWidth, mScreenHeight);
		}
		
		private StarField mStarField;
		private Painter mStarPainter;
    }
}
