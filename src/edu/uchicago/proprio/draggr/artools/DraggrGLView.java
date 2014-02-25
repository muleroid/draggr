package edu.uchicago.proprio.draggr.artools;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class DraggrGLView extends GLSurfaceView {
	
	public DraggrGLView(Context context) {
		super(context);
		
		setEGLContextClientVersion(2);
	}
}
