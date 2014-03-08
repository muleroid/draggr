package edu.uchicago.proprio.draggr.shapes;

import java.util.Vector;

import com.qualcomm.vuforia.ImageTarget;

import android.graphics.PointF;
import android.opengl.Matrix;
import android.util.Log;

// class which manages display of various files
public class DraggrFolderBase {
	private static final String LOGTAG = "DraggrFolderBase";
	
	// this string is used for equality checking
	// it is equal to the TBD
	private final String mId;
	
	private DraggrFile mFile;
	private DraggrFile mFile2;
	private DraggrFile[][] mFiles;
	private DraggrFile draggedFile = null;
	
	private static final int numRows = 3;
	private static final int numCols = 2;
	
	private static float lastDragX;
	private static float lastDragY;
	
	private boolean onScreen = false;
	
	public DraggrFolderBase(String id) {
		// cols and rows are backwards because i can't figure out how to change orientation
		mId = id;
		mFiles = new DraggrFile[numCols][numRows];
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++) {
				PointF relocate = calcLocation(i, j);
				//Log.d(LOGTAG, "File at (" + relocate.x +"," + relocate.y + ")");
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
	
	public void draw(float[] modelViewMatrix, float[] projectionMatrix, ImageTarget t) {
		//Log.d(LOGTAG, "onScreen");
		float tX = t.getSize().getData()[0];
		float tY = t.getSize().getData()[1];
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++) {
				float[] modelViewClone = modelViewMatrix.clone();
				PointF whereToPut = placeInSpace(i, j, tX, tY);
				//Log.d(LOGTAG, "whereToPut: " + whereToPut.x + "," + whereToPut.y);
				Matrix.translateM(modelViewClone, 0, whereToPut.x, whereToPut.y, 3.0f);
				Matrix.scaleM(modelViewClone, 0, tX * 0.5f, tY * 0.5f, 1.0f);
				float[] modelViewProjection = new float[16];
				Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewClone, 0);
				if(draggedFile != mFiles[i][j]) {
					mFiles[i][j].mModelViewMatrix.setData(modelViewClone);
					mFiles[i][j].mProjectionMatrix.setData(projectionMatrix.clone());
					mFiles[i][j].draw(modelViewProjection);
				}
			}
		/*if(mFile2 != draggedFile)
			mFile2.draw(mvpMatrix.clone());
		if(mFile != draggedFile)
			mFile.draw(mvpMatrix.clone());*/
	}
	
	// overload that is only used when dragging
	public void draw(float[] mProjectionMatrix, float[] mViewMatrix) {
		float[] modelViewProjection = new float[16];
		Matrix.multiplyMM(modelViewProjection, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		if(draggedFile != null)
			draggedFile.draw(modelViewProjection);
	}
	
	public void onScreen() {
		onScreen = true;
	}
	
	public void offScreen() {
		onScreen = false;
	}
	
	public boolean isOnScreen() {
		return onScreen;
	}
	
	// functions that handle drag functionality
	public void onTouch(PointF screenP) {
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++) {
				//mFiles[i][j].onTouch(screenP.x, screenP.y);
				if(mFiles[i][j].onTouch(screenP.x, screenP.y)) {
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
	
	public boolean startDrag(PointF screenP) {
		lastDragX = screenP.x;
		lastDragY = screenP.y;
		Log.d(LOGTAG, "Drag started at (" + lastDragX + "," + lastDragY + ")");
		if(draggedFile != null) {
			draggedFile.translate(lastDragX, lastDragY);
			return true;
		}
		return false;
	}
	
	public void inDrag(PointF screenP) {
		float newX = screenP.x;
		float newY = screenP.y;
		float dx = newX - lastDragX;
		float dy = newY - lastDragY;
		Log.d(LOGTAG, "Drag (" + lastDragX + "," + lastDragY + ")");
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
	
	// do a thing
	private PointF placeInSpace(int i, int j, float x, float y) {
		float dy = (float) (j - 1) * y * 0.3f;
		float dx = (float) (i - 1) * x * 0.3f;
		
		return new PointF(dx, dy);
	}
	
	// given retrieved file information, generate files
	private void populateFiles() {
		
	}
	
	// override Equals and HashCode functions
	public boolean equals(Object other) {
		if (other == this) return true;
		if (other == null) return false;
		if (getClass() != other.getClass()) return false;
		DraggrFolderBase b = (DraggrFolderBase) other;
		return b.mId.equals(mId);
	}
	
	public int hashCode() {
		return mId.hashCode();
	}
}
