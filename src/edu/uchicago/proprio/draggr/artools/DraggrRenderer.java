package edu.uchicago.proprio.draggr.artools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.xmlpull.v1.XmlPullParserException;

import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Vuforia;

import edu.uchicago.proprio.draggr.shapes.DraggrFile;
import edu.uchicago.proprio.draggr.shapes.DraggrFolderBase;
import edu.uchicago.proprio.draggr.shapes.Texture;
import edu.uchicago.proprio.draggr.transfer.ConnectDeviceTask;
import edu.uchicago.proprio.draggr.transfer.Device;
import edu.uchicago.proprio.draggr.transfer.UpdateFilesTask;
import edu.uchicago.proprio.draggr.xml.DraggrXmlParser;
import edu.uchicago.proprio.draggr.xml.DraggrXmlParser.DeviceEntry;

import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.opengl.GLES20;
import android.os.AsyncTask;

public class DraggrRenderer implements GLSurfaceView.Renderer{
    private static final String LOGTAG = "DraggrRenderer";
    
    private DraggrARSession vuforiaAppSession;
    private DraggrAR mActivity;
    
    private Vector<Texture> mTextures;
    
	// Vuforia specific renderer for drawing video backgrounds and 3D objects
	private Renderer mRenderer;
	
	boolean mIsActive = false;
	
	//private DraggrFolderBase mFolder;
	private DraggrFolderBase mDraggedFolder = null;
	private HashSet<DraggrFolderBase> onScreenFolders = new HashSet<DraggrFolderBase>();
	private HashMap<String, DraggrFolderBase> mFolders = new HashMap<String, DraggrFolderBase>();
	
	// these two matrices are used to draw the file being dragged
	private final float[] mDragProjMatrix = new float[16];
	private final float[] mDragViewMatrix = new float[16];
	
	public DraggrRenderer(DraggrAR activity,
			DraggrARSession session, Vector<Texture> textures) {
		mActivity = activity;
		vuforiaAppSession = session;
		mTextures = textures;
		//setFolders();
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
			temp.onTouch(new PointF(touchX,touchY));
		}
	}
	
	public String onClick() {
		String result = null;
		if(onScreenFolders.isEmpty())
			return result;
		Iterator<DraggrFolderBase> itr = onScreenFolders.iterator();
		while(itr.hasNext()) {
			DraggrFolderBase temp = (DraggrFolderBase) itr.next();
			result = temp.onClick();
		}
		return result;
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
	}
	
	public void endDrag() {
		if(mDraggedFolder != null) {
			// find onScreenFolders to transfer to
			/*Iterator<DraggrFolderBase> itr = onScreenFolders.iterator();
			while(itr.hasNext()) {
				DraggrFolderBase cur = itr.next();
				if(this.equals(cur))
					continue;
				mDraggedFolder.transfer(cur);
			}*/
			mDraggedFolder.releaseFile();
		}
		mDraggedFolder = null;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		renderFrame();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
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
		//Device test = new Device("arch_nathan");
		/*if(!test.tryConnect())
			Log.e(LOGTAG, "failed to connect to arch_nathan");*/
		//mFolder = new DraggrFolderBase("womp", test, this);
		//mFolder.populateFiles();
		
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
		
		setFolders();
		//mFolder.setFileTexture(mTextures.firstElement());

		// set up view matrix
		Matrix.setLookAtM(mDragViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		
		mActivity.loadingDialogHandler
        	.sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
	}
	
	private void renderFrame() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		State state = mRenderer.begin();

		mRenderer.drawVideoBackground();
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		if(state.getNumTrackableResults() <= 0)
		{
			onScreenFolders.clear();
		}
		for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx ++)
		{
			// TODO: figure out a way to remove folders that aren't on screen
			//onScreenFolders.add(mFolder);
			TrackableResult result = state.getTrackableResult(tIdx);
			Trackable trackable = result.getTrackable();
			// uncomment for multiple folders
			DraggrFolderBase cur = mFolders.get(trackable.getName());
			if(cur == null)
				continue;
			onScreenFolders.add(cur);
			Matrix44F modelViewMatrix_Vuforia = Tool
	                .convertPose2GLMatrix(result.getPose());
	        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
	        
	        // DraggrFolderBase constructs the appropriately translated
	        // projection matrix for each individual file, we just provide it
	        // with the pose, as well as the image target's size, so the files
	        // can be scaled accordingly
            ImageTarget t = (ImageTarget) trackable;
            cur.draw(modelViewMatrix, vuforiaAppSession.getProjectionMatrix().getData(), t);
		}
		
		if(mDraggedFolder != null)
			mDraggedFolder.draw(mDragProjMatrix, mDragViewMatrix);
		
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		mRenderer.end();
	}
	
	// TODO: figure out how to do this, need to somehow get result from each device's connect
	private void setFolders() {
		Device cur;
		DraggrFolderBase newFolder;
		DraggrXmlParser deviceParser = new DraggrXmlParser();
		List<DeviceEntry> entries = null;
		// read in XML? or get information somehow
		try {
			entries = deviceParser.parse(mActivity.getAssets().open("device_mapping.xml"));
			// might want to put this logic in an async task...
			for(DeviceEntry entry : entries) {
				cur = new Device(entry.name);
				new ConnectDeviceTask(cur).execute();
				newFolder = new DraggrFolderBase(entry.trackable, cur, this);
				newFolder.setFileTexture(mTextures.firstElement());
				new UpdateFilesTask(cur, "", newFolder).execute();
				mFolders.put(entry.trackable, newFolder);
				Log.d(LOGTAG, entry.name + ": " + entry.trackable);
			}
		} catch (Exception e) {
			Log.e(LOGTAG, "Error parsing device_mapping.xml");
			Log.e(LOGTAG, e.getMessage());
		}
		// loop over all information, each iteration:
		// create a device w/ given name (and port? if necessary)
		// try to connect the device
		// get the file info (this requires device be connected)
		// get the thumbnails?
		// create a DraggrFolderBase, passing it the name of trackable and the device
	}
	
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(LOGTAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
    
    public void loadTextureToFile(Texture t, DraggrFile f) {
    	if (t != null) {
	    	Texture textureToLoad = t;
	    	DraggrFile targetFile = f;
			GLES20.glGenTextures(1, textureToLoad.mTextureID, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureToLoad.mTextureID[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, 
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, 
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
					textureToLoad.mWidth, textureToLoad.mHeight, 0, GLES20.GL_RGBA,
					GLES20.GL_UNSIGNED_BYTE, textureToLoad.mData);
			targetFile.setTexture(textureToLoad);
			/* old code: new LoadTextureTask(t, f).execute(); */
		}
    }
    
    // this will load the texture in a background thread..., once the result
    // is achieved, set the corresponding DraggrFile's texture to it, using setTexture
    public class LoadTextureTask extends AsyncTask<Void, Void, Void> {
    	private Texture textureToLoad;
    	private DraggrFile targetFile;
    	
    	public LoadTextureTask(Texture t, DraggrFile f) {
    		textureToLoad = t;
    		targetFile = f;
    	}
    	
    	protected Void doInBackground(Void... UNUSED) {
    		GLES20.glGenTextures(1, textureToLoad.mTextureID, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureToLoad.mTextureID[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, 
					GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, 
					GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
					textureToLoad.mWidth, textureToLoad.mHeight, 0, GLES20.GL_RGBA,
					GLES20.GL_UNSIGNED_BYTE, textureToLoad.mData);
			targetFile.setTexture(textureToLoad);
    		return null;
    	}
    }
}
