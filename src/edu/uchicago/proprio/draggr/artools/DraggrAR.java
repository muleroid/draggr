package edu.uchicago.proprio.draggr.artools;

import com.qualcomm.vuforia.State;

import edu.uchicago.proprio.draggr.R;
import edu.uchicago.proprio.draggr.R.layout;
import edu.uchicago.proprio.draggr.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class DraggrAR extends Activity implements DraggrARControl {
	private static final String LOGTAG = "DraggrAR";
	
	DraggrARSession vuforiaAppSession;
	// open GL resources, view and renderer
	private DraggrGLView mGLView;
	private DraggrRenderer mRenderer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_draggr_ar);
		Log.d(LOGTAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		vuforiaAppSession = new DraggrARSession(this);
		
		vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.draggr_ar, menu);
		return true;
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
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean doLoadTrackersData() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean doStartTrackers() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean doStopTrackers() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean doUnloadTrackersData() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean doDeinitTrackers() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onQCARUpdate(State state) {
		// TODO Auto-generated method stub
		
	}
	
	// initialize AR components
	private void initApplicationAR() {
		mGLView = new DraggrGLView(this);
		
		mRenderer = new DraggrRenderer(this);
		mGLView.setRenderer(mRenderer);
		
		addContentView(mGLView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
	}

	@Override
	public void onInitARDone(SampleApplicationException e) {
		// TODO Auto-generated method stub
		if(e == null) {
			initApplicationAR();
		} else {
			Log.e(LOGTAG, e.getString());
			finish();
		}
	}
}
