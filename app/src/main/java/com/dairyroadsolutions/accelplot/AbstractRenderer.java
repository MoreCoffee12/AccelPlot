package com.dairyroadsolutions.accelplot;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

/* Class that implements a general Open GL renderer */


public abstract class AbstractRenderer implements GLSurfaceView.Renderer
{	
	float mRatio;
	
	
	public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) 
	{
		gl.glDisable(GL10.GL_DITHER);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,GL10.GL_FASTEST);
		gl.glClearColor(0, 0, 0, 0);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}		
	
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		gl.glViewport(0, 0, w, h);
		mRatio = (float) w / h;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(mRatio, -mRatio, -1, 1, 3, 7);
	}
	public void onDrawFrame(GL10 gl)
	{
		gl.glDisable(GL10.GL_DITHER);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, 0, 0, -3.01f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		draw(gl);
	}
	protected abstract void draw(GL10 gl);
}
