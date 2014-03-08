package edu.uchicago.proprio.draggr.artools;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Vuforia;

import edu.uchicago.proprio.draggr.shapes.DraggrFolderBase;
import edu.uchicago.proprio.draggr.shapes.Texture;

import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.opengl.GLES20;

public class DraggrRenderer implements GLSurfaceView.Renderer{
    private static final String LOGTAG = "DraggrRenderer";
    
    private DraggrARSession vuforiaAppSession;
    private DraggrAR mActivity;
    
    private Vector<Texture> mTextures;
    
	// Vuforia specific renderer for drawing video backgrounds and 3D objects
	private Renderer mRenderer;
	
	boolean mIsActive = false;
	
	private DraggrFolderBase mFolder;
	
	private int mProgram;
	
	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];
	
	public DraggrRenderer(DraggrAR activity,
			DraggrARSession session) {
		mActivity = activity;
		vuforiaAppSession = session;
	}
	
	public void setTextures(Vector<Texture> textures) {
		mTextures = textures;
	}
	
	public boolean isTouched(float touchX, float touchY) {
		return mFolder.isTouched(screenToWorld(touchX, touchY));
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		renderFrame();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		GLES20.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		//Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
		Matrix.orthoM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, -1, 1);
		vuforiaAppSession.onSurfaceChanged(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		initRendering();
		
		vuforiaAppSession.onSurfaceCreated();
	}
	
	// shamelessly ripped from Vuforia sample app
	private void initRendering() {
		mFolder = new DraggrFolderBase();
		
		mRenderer = Renderer.getInstance();
		
		// sets background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
				: 1.0f);
		
		// set up file texture
		for(Texture t : mTextures) {
			GLES20.glGenTextures(1, t.mTextureID, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, 
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, 
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
					t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
					GLES20.GL_UNSIGNED_BYTE, t.mData);
		}
		
		mFolder.setFileTexture(mTextures.firstElement());
		
		// oh yeah should probably change this otherwise lol
		mProgram = GLTools.createProgramFromShaders(mFolder.vertexShader(),
				mFolder.fragmentShader());
		checkGlError("glLinkProgram");
		
		// set up view matrix
		Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		
		mActivity.loadingDialogHandler
        	.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
	}
	
	private void renderFrame() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		State state = mRenderer.begin();
		mRenderer.drawVideoBackground();
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		//GLES20.glEnable(GLES20.GL_BLEND);
		//GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		//if(state.getNumTrackableResults() > 0)
			//mFolder.draw(mProgram);
		//Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, vuforiaAppSession.getProjectionMatrix().getData(), 
				0, mViewMatrix, 0);
		mFolder.draw(mProgram, mMVPMatrix);
		mRenderer.end();
	}
	
	private PointF screenToWorld(float touchX, float touchY) {
		float screenWidth = (float) vuforiaAppSession.getScreenWidth();
		float screenHeight = (float) vuforiaAppSession.getScreenHeight();
		
		float[] invertedMatrix, transformMatrix,
        	normalizedInPoint, outPoint;
		invertedMatrix = new float[16];
		transformMatrix = new float[16];
		normalizedInPoint = new float[4];
		outPoint = new float[4];
		
		int oglTouchY = (int) (screenHeight - touchY);
		
		normalizedInPoint[0] = (float) ((touchX) * 2.0f / screenWidth - 1.0);
		normalizedInPoint[1] = (float) ((oglTouchY) * 2.0f / screenHeight - 1.0);
		normalizedInPoint[2] = - 1.0f;
		normalizedInPoint[3] = 1.0f;
		Matrix.multiplyMM(transformMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		Matrix.invertM(invertedMatrix, 0, transformMatrix, 0);
		
		Matrix.multiplyMV(outPoint, 0, invertedMatrix, 0, normalizedInPoint, 0);
		
		if(outPoint[3] == 0.0) {
			Log.e(LOGTAG, "divide by zero error in calculating world coords");
			return new PointF();
		}
		
		float worldX = -1 * outPoint[0] / outPoint[3];
		float worldY = outPoint[1] / outPoint[3];
		
		Log.d(LOGTAG, "Touch at: " + worldX + ", " + worldY);
		return new PointF(worldX, worldY);
	}
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(LOGTAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
