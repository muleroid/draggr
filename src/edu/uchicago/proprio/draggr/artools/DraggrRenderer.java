package edu.uchicago.proprio.draggr.artools;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Vuforia;

import edu.uchicago.proprio.draggr.shapes.Triangle;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.opengl.GLES20;

public class DraggrRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "DraggrRenderer";
    
    private DraggrAR mActivity;
    
	// Vuforia specific renderer for drawing video backgrounds and 3D objects
	private Renderer mRenderer;
	
	private Triangle mTriangle;
	
	private int mProgram;
	
	private int mPositionHandle;
	
	private int mColorHandle;
	
	public DraggrRenderer(DraggrAR activity) {
		mActivity = activity;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		checkGlError("glClear");
		renderFrame();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		GLES20.glViewport(0, 0, width, height);
		checkGlError("glViewport");
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		initRendering();
	}
	
	// shamelessly ripped from Vuforia sample app
	private void initRendering() {
		mTriangle = new Triangle();
		
		//mRenderer = Renderer.getInstance();
		
		// sets background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
				: 1.0f);
		
		// oh yeah should probably change this otherwise lol
		mProgram = GLTools.createProgramFromShaders(mTriangle.vertexShader(),
				mTriangle.fragmentShader());
		checkGlError("glLinkProgram");
		//mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		//mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
	}
	
	private void renderFrame() {
		//State state = mRenderer.begin();
		//mRenderer.drawVideoBackground();
		//GLES20.glUseProgram(mProgram);
		mTriangle.draw(mProgram);
		//mRenderer.end();
	}
	
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
