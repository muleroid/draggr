package edu.uchicago.proprio.draggr.shapes;

import java.util.Vector;

import android.graphics.PointF;
import android.util.Log;

// class which manages display of various files
public class DraggrFolderBase {
	private static final String LOGTAG = "DraggrFolderBase";
	
	private DraggrFile mFile;
	private DraggrFile mFile2;
	private DraggrFile[][] mFiles;
	private DraggrFile draggedFile = null;
	
	private static final int numRows = 3;
	private static final int numCols = 2;
	
	private static float lastDragX;
	private static float lastDragY;
	
	private boolean onScreen = false;
	
	public DraggrFolderBase() {
		// cols and rows are backwards because i can't figure out how to change orientation
		mFiles = new DraggrFile[numCols][numRows];
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++) {
				PointF relocate = calcLocation(i, j);
				Log.d(LOGTAG, "File at (" + relocate.x +"," + relocate.y + ")");
				mFiles[i][j] = new DraggrFile(relocate.x, relocate.y);
			}
		//mFile = new DraggrFile(0, -0.3f);
		//mFile2 = new DraggrFile(0, 0.3f);
	}
	
	public void setFileTexture(Texture fTexture) {
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++)
				mFiles[i][j].setTexture(fTexture);
		//mFile.setTexture(fTexture);
		//mFile2.setTexture(fTexture);
	}
	
	public void draw(float[] mvpMatrix) {
		if(onScreen)
			for(int i = 0; i < numCols; i++)
				for(int j = 0; j < numRows; j++) {
					if(draggedFile != mFiles[i][j])
						mFiles[i][j].draw(mvpMatrix.clone());
				}
		/*if(mFile2 != draggedFile)
			mFile2.draw(mvpMatrix.clone());
		if(mFile != draggedFile)
			mFile.draw(mvpMatrix.clone());*/
		
		if(draggedFile != null)
			draggedFile.draw(mvpMatrix);
	}
	
	public void onScreen() {
		onScreen = true;
	}
	
	public void offScreen() {
		onScreen = false;
	}
	
	// functions that handle drag functionality
	public void onTouch(PointF screenP) {
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++) {
				if(mFiles[i][j].isTouched(screenP.x, screenP.y)) {
					draggedFile = mFiles[i][j];
					return;
				}
			}
		/*if(mFile.isTouched(screenP.x, screenP.y)) {
			Log.d(LOGTAG, "mFile touched");
			draggedFile = mFile;
		}
		if(mFile2.isTouched(screenP.x, screenP.y)) {
			Log.d(LOGTAG, "mFile2 touched");
			draggedFile = mFile2;
		}*/
	}
	
	public void startDrag(PointF screenP) {
		lastDragX = screenP.x;
		lastDragY = screenP.y;
		Log.d(LOGTAG, "Drag started at (" + lastDragX + "," + lastDragY + ")");
	}
	
	public void inDrag(PointF screenP) {
		float newX = screenP.x;
		float newY = screenP.y;
		float dx = newX - lastDragX;
		float dy = newY - lastDragY;
		//Log.d(LOGTAG, "Drag (" + lastDragX + "," + lastDragY + ")");
		//if(dx > 0 || dy > 0)
		if(draggedFile != null)
			draggedFile.translate(dx, dy);
		lastDragX = newX;
		lastDragY = newY;
	}
	
	public void releaseFile() {
		if(draggedFile != null) {
			draggedFile.resetPosition();
			draggedFile = null;
		}
		//mFile.resetPosition();
	}
	
	// given an index into the 2D array of files, calculate where in space to translate that file
	private PointF calcLocation(int i, int j) {
		float dy = (float) (j - 1) * 0.3f;
		float dx = (float) (i - 1) * 0.3f;
		
		return new PointF(dx, dy);
	}
	
	// given retrieved file information, generate files
	private void populateFiles() {
		
	}
}
