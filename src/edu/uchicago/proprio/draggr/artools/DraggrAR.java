package edu.uchicago.proprio.draggr.artools;

import java.util.Vector;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;

import edu.uchicago.proprio.draggr.R;
import edu.uchicago.proprio.draggr.R.layout;
import edu.uchicago.proprio.draggr.R.menu;
import edu.uchicago.proprio.draggr.shapes.Texture;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class DraggrAR extends Activity implements DraggrARControl {
	private static final String LOGTAG = "DraggrAR";
	
	// stuff about the target dataset
	private DataSet mDataset;
	private String mDatasetString = "DraggrDB.xml";
	
	DraggrARSession vuforiaAppSession;
	// open GL resources, view and renderer
	private DraggrGLView mGLView;
	private DraggrRenderer mRenderer;
	
	// textures
	private Vector<Texture> mTextures;
	
	private RelativeLayout mUILayout;
	
	LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_draggr_ar);
		Log.d(LOGTAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		vuforiaAppSession = new DraggrARSession(this);
		startLoadingAnimation();
		
		vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// load textures
		mTextures = new Vector<Texture>();
		loadTextures();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.draggr_ar, menu);
		return true;
	}
	
	private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
            null, false);
        
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
        
    }
	
	protected void onResume() {
		Log.d(LOGTAG, "onResume");
		super.onResume();
		
		try {
			vuforiaAppSession.resumeAR();
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}
		
		// resume GLView
		if(mGLView != null) {
			mGLView.setVisibility(View.VISIBLE);
			mGLView.onResume();
		}
	}
	
	public void onConfigurationChanged(Configuration config) {
		Log.d(LOGTAG, "onConfigurationChanged");
		super.onConfigurationChanged(config);
		
		vuforiaAppSession.onConfigurationChanged();
	}
	
	protected void onPause() {
		Log.d(LOGTAG, "onPause");
		super.onPause();
		
		if (mGLView != null) {
			mGLView.setVisibility(View.INVISIBLE);
			mGLView.onPause();
		}
		
		try {
			vuforiaAppSession.pauseAR();
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}
	}
	
	protected void onDestroy() {
		Log.d(LOGTAG, "onDestroy");
		super.onDestroy();
		
		try {
			vuforiaAppSession.stopAR();
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}
		
		System.gc();
	}
	
	@Override
	public boolean doInitTrackers() {
		// Indicate if the trackers were initialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;
        
        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ImageTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                LOGTAG,
                "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
	}

	@Override
	public boolean doLoadTrackersData() {
		TrackerManager tManager = TrackerManager.getInstance();
		ImageTracker imageTracker = (ImageTracker) tManager.getTracker(ImageTracker.getClassType());
		if(imageTracker == null)
			return false;
		
		if(mDataset == null)
			mDataset = imageTracker.createDataSet();
		
		if(mDataset == null)
			return false;
		
		if(!mDataset.load(mDatasetString, DataSet.STORAGE_TYPE.STORAGE_APPRESOURCE))
			return false;
		
		if(!imageTracker.activateDataSet(mDataset))
			return false;
		
		int numTrackables = mDataset.getNumTrackables();
		
		for(int count = 0; count < numTrackables; count++) {
			Trackable trackable = mDataset.getTrackable(count);
			String name = "Current Dataset: " + trackable.getName();
			trackable.setUserData(name);
			Log.d(LOGTAG, "UserData:Set the following user data " + (String)trackable.getUserData());
		}
		
		return true;
	}

	@Override
	public boolean doStartTrackers() {
		// Indicate if the trackers were started correctly
        boolean result = true;
        
        Tracker imageTracker = TrackerManager.getInstance().getTracker(
            ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.start();
        
        return result;
	}

	@Override
	public boolean doStopTrackers() {
		// Indicate if the trackers were stopped correctly
        boolean result = true;
        
        Tracker imageTracker = TrackerManager.getInstance().getTracker(
            ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.stop();
		return result;
	}

	@Override
	public boolean doUnloadTrackersData() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean doDeinitTrackers() {
		// Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ImageTracker.getClassType());
        
        return result;
	}

	@Override
	public void onQCARUpdate(State state) {
		// TODO Auto-generated method stub
		
	}
	
	// initialize AR components
	private void initApplicationAR() {
		mGLView = new DraggrGLView(this);
		
		mRenderer = new DraggrRenderer(this, vuforiaAppSession);
		mRenderer.setTextures(mTextures);
		mGLView.setRenderer(mRenderer);
		// cheap hack
		mGLView.passInRenderer(mRenderer);
	}

	@Override
	public void onInitARDone(SampleApplicationException exception) {
		// TODO Auto-generated method stub
		if(exception == null) {
			initApplicationAR();
			
			mRenderer.mIsActive = true;
			addContentView(mGLView, new LayoutParams(LayoutParams.MATCH_PARENT,
	                LayoutParams.MATCH_PARENT));
			
			mUILayout.bringToFront();
			
			mUILayout.setBackgroundColor(Color.TRANSPARENT);
			
			try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }
			
			boolean result = CameraDevice.getInstance().setFocusMode(
	                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
			
			if (!result)
                Log.e(LOGTAG, "Unable to enable continuous autofocus");
		} else {
			Log.e(LOGTAG, exception.getString());
			finish();
		}
	}
	
	private void loadTextures() {
		mTextures.add(Texture.loadTextureFromApk("file_texture.png", getAssets()));
	}
}
