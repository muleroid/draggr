package edu.uchicago.proprio.draggr.artools;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
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
	private DraggrFolderBase mDraggedFolder = null;
	private HashSet<DraggrFolderBase> onScreenFolders = new HashSet<DraggrFolderBase>();
	
	// these two matrices are used to draw the file being dragged
	private final float[] mDragProjMatrix = new float[16];
	private final float[] mDragViewMatrix = new float[16];
	
	public DraggrRenderer(DraggrAR activity,
			DraggrARSession session) {
		mActivity = activity;
		vuforiaAppSession = session;
	}
	
	public void setTextures(Vector<Texture> textures) {
		mTextures = textures;
	}
	
	// pass world coordinates to mFolder
	public void onTouch(float touchX, float touchY) {
		if(onScreenFolders.isEmpty())
			return;
		Iterator<DraggrFolderBase> itr = onScreenFolders.iterator();
		while(itr.hasNext()) {
			DraggrFolderBase temp = (DraggrFolderBase) itr.next();
			/*temp.onTouch(GLTools.screenToWorld(touchX, touchY, 
					vuforiaAppSession.getScreenWidth(), vuforiaAppSession.getScreenHeight(),
					mDragProjMatrix, mDragViewMatrix));*/
			temp.onTouch(new PointF(touchX,touchY));
		}
	}
	
	public void startDrag(float touchX, float touchY) {
		if(onScreenFolders.isEmpty())
			return;
		Iterator<DraggrFolderBase> itr = onScreenFolders.iterator();
		while(itr.hasNext()) {
			DraggrFolderBase temp = (DraggrFolderBase) itr.next();
			if(temp.startDrag(GLTools.screenToWorld(touchX, touchY, 
					vuforiaAppSession.getScreenWidth(), vuforiaAppSession.getScreenHeight(),
					mDragProjMatrix, mDragViewMatrix))) {
					mDraggedFolder = temp;
					break;
			}
		}
	}
	
	public void inDrag(float touchX, float touchY) {
		if(mDraggedFolder!= null)
			mDraggedFolder.inDrag(GLTools.screenToWorld(touchX, touchY, 
					vuforiaAppSession.getScreenWidth(), vuforiaAppSession.getScreenHeight(),
					mDragProjMatrix, mDragViewMatrix));
		//mFolder.inDrag(screenToWorld(touchX, touchY));
	}
	
	public void endDrag() {
		if(mDraggedFolder != null)
			mDraggedFolder.releaseFile();
		mDraggedFolder = null;
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
		Matrix.frustumM(mDragProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
		vuforiaAppSession.onSurfaceChanged(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		initRendering();
		
		vuforiaAppSession.onSurfaceCreated();
	}
	
	// shamelessly ripped from Vuforia sample app
	private void initRendering() {
		mFolder = new DraggrFolderBase("womp");
		
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

		// set up view matrix
		Matrix.setLookAtM(mDragViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		
		mActivity.loadingDialogHandler
        	.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
	}
	
	private void renderFrame() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		State state = mRenderer.begin();
		// make a shallow copy
		mRenderer.drawVideoBackground();
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		if(state.getNumTrackableResults() <= 0)
		{
			onScreenFolders.clear();
		}
		for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx ++)
		{
			// TODO: figure out a way to remove folders that aren't on screen
			onScreenFolders.add(mFolder);
			TrackableResult result = state.getTrackableResult(tIdx);
			Trackable trackable = result.getTrackable();
			//Log.d(LOGTAG, trackable.getUserData().toString());
			Matrix44F modelViewMatrix_Vuforia = Tool
	                .convertPose2GLMatrix(result.getPose());
	        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
	        
	        // DraggrFolderBase constructs the appropriately translated
	        // projection matrix for each individual file, we just provide it
	        // with the pose, as well as the image target's size, so the files
	        // can be scaled accordingly
            //float[] modelViewProjection = new float[16];
            ImageTarget t = (ImageTarget) trackable;
            /*float tX = t.getSize().getData()[0];
            float tY = t.getSize().getData()[1];
            Matrix.translateM(modelViewMatrix, 0, tX / 2.0f, tY / 2.0f, 3.0f);
            Log.d(LOGTAG, t.getSize().getData()[0] + "," + t.getSize().getData()[1]);
            Matrix.scaleM(modelViewMatrix, 0, t.getSize().getData()[0] * 0.2f, 
            		t.getSize().getData()[1] * 0.2f, 1.0f);*/
            //Matrix.multiplyMM(modelViewProjection, 0, 
            //		vuforiaAppSession.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            //mFolder.onScreen();
            mFolder.draw(modelViewMatrix, vuforiaAppSession.getProjectionMatrix().getData(), t);
		}
		
		if(mDraggedFolder != null)
			mDraggedFolder.draw(mDragProjMatrix, mDragViewMatrix);
		/*if(state.getNumTrackableResults() > 0)
			mFolder.onScreen();
		else
			mFolder.offScreen();*/
			//mFolder.draw(mMVPMatrix);
		
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		mRenderer.end();
	}
	
	/*private PointF screenToWorld(float touchX, float touchY) {
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
		Matrix.multiplyMM(transformMatrix, 0, mDragProjMatrix, 0, mDragViewMatrix, 0);
		Matrix.invertM(invertedMatrix, 0, transformMatrix, 0);
		
		Matrix.multiplyMV(outPoint, 0, invertedMatrix, 0, normalizedInPoint, 0);
		
		if(outPoint[3] == 0.0) {
			Log.e(LOGTAG, "divide by zero error in calculating world coords");
			return new PointF();
		}
		
		float worldX = -1 * outPoint[0] / outPoint[3];
		float worldY = outPoint[1] / outPoint[3];
		
		return new PointF(worldX, worldY);
	}*/
	
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(LOGTAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
