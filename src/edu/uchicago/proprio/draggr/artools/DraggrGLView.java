package edu.uchicago.proprio.draggr.artools;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.WindowManager;

public class DraggrGLView extends GLSurfaceView {
	private static final String LOGTAG = "DraggrGLView";
	private static DraggrRenderer mRenderer;
	
	// fields to store where in the view was touched
	// necessary to pass information in OnLongClick
	// to Drag handler
	private static float lastX;
	private static float lastY;
	
	private static int scrnWidth;
	private static int scrnHeight;
	
	public DraggrGLView(Context context) {
		super(context);
		
		setEGLContextClientVersion(2);
		
		// make it translucent so we can see the camera
		Log.d(LOGTAG, "Set GLView to be translucent");
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		
		Point size = new Point();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getSize(size);
		scrnWidth = size.x;
		scrnHeight = size.y;
		
		String msg = "Screen size is " + scrnWidth + " pixels wide and " + scrnHeight + " pixels high";
		Log.d(LOGTAG, msg);
		
		// set anonymous listener for touch
		setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = event.getX();
					lastY = event.getY();
					break;
				}
				return false;
			}
		});
		
		setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d(LOGTAG, "onClick");
				mRenderer.onTouch(lastX, lastY);
				String result = mRenderer.onClick();
				if(result == null)
					Log.d(LOGTAG, "empty");
				else {
					Context mContext = getContext();
					Intent intent = new Intent(mContext, DraggrPreview.class);
					intent.putExtra("filename", result);
					mContext.startActivity(intent);
				}
					
			}
		});
		
		// set listener
		setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				String msg = "onLongClick at: " + lastX + ", " + lastY;
				Log.d(LOGTAG, msg);
				mRenderer.onTouch(lastX, lastY);
				// create the clipdata
				ClipData.Item item = new ClipData.Item("test");
				ClipData dragData = new ClipData("herp", 
						new String[] {ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
				
				View.DragShadowBuilder mShadow = new View.DragShadowBuilder();
				
				// here should check if our touch location bounds one of the files being displayed
				// on the screen
				v.startDrag(dragData, mShadow, null, 0);
				return true;
			}
		});
		
		setOnDragListener(new DragListener());
	}
	
	private class DragListener implements OnDragListener {

		@Override
		public boolean onDrag(View v, DragEvent event) {
			switch(event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				Log.d(LOGTAG, "DRAG_STARTED (" + event.getX() + "," + event.getY() + ")");
				mRenderer.startDrag(event.getX(), event.getY());
				break;
				
			case DragEvent.ACTION_DRAG_ENTERED:
				//Log.d(LOGTAG, "DRAG_ENTERED");
				break;
				
			case DragEvent.ACTION_DRAG_EXITED:
				//Log.d(LOGTAG, "DRAG_EXITED");
				break;
				
			case DragEvent.ACTION_DRAG_LOCATION:
				//Log.d(LOGTAG, "DRAG_LOC (" + event.getX() + "," + event.getY() + ")");
				mRenderer.inDrag(event.getX(), event.getY());
				break;
			
			case DragEvent.ACTION_DRAG_ENDED:
				mRenderer.endDrag();
				break;
			}
			return true;
		}
	}
	
	public void passInRenderer(DraggrRenderer renderer) {
		mRenderer = renderer;
	}
}
