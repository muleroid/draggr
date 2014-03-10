package edu.uchicago.proprio.draggr.shapes;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.qualcomm.vuforia.ImageTarget;

import android.graphics.PointF;
import android.opengl.Matrix;
import android.util.Log;

import edu.uchicago.proprio.draggr.artools.DraggrRenderer;
import edu.uchicago.proprio.draggr.transfer.Device;
import edu.uchicago.proprio.draggr.transfer.TransferTask;

// class which manages display of various files
public class DraggrFolderBase {
	private static final String LOGTAG = "DraggrFolderBase";
	
	private final DraggrRenderer mRenderer;
	
	// this string is used for equality checking
	// it is equal to the trackable name
	private final String mId;
	private final Device mDevice;

	private DraggrFile[][] mFiles;
	private DraggrFile draggedFile = null;
	
	private static final int numRows = 3;
	private static final int numCols = 2;
	
	private static float lastDragX;
	private static float lastDragY;
	
	public DraggrFolderBase(String id, Device device, DraggrRenderer renderer) {
		// cols and rows are backwards because i can't figure out how to change orientation
		mId = id;
		mDevice = device;
		mRenderer = renderer;
		mFiles = new DraggrFile[numCols][numRows];
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++) {
				//Log.d(LOGTAG, "File at (" + relocate.x +"," + relocate.y + ")");
				mFiles[i][j] = new DraggrFile();
				mFiles[i][j].onScreen = true;
			}
	}
	
	public void setFileTexture(Texture fTexture) {
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++)
				mFiles[i][j].setTexture(fTexture);
	}
	
	public void draw(float[] modelViewMatrix, float[] projectionMatrix, ImageTarget t) {
		//Log.d(LOGTAG, "onScreen");
		float tX = t.getSize().getData()[0];
		float tY = t.getSize().getData()[1];
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++) {
				DraggrFile temp = mFiles[i][j];
				if(!temp.onScreen)
					continue;
				float[] modelViewClone = modelViewMatrix.clone();
				PointF whereToPut = placeInSpace(i, j, tX, tY);

				Matrix.translateM(modelViewClone, 0, whereToPut.x, whereToPut.y, 3.0f);
				Matrix.scaleM(modelViewClone, 0, tX * 1.0f, tY * 1.0f, 1.0f);
				float[] modelViewProjection = new float[16];
				Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewClone, 0);
				if(draggedFile != temp) {
					temp.mModelViewMatrix.setData(modelViewClone);
					temp.mProjectionMatrix.setData(projectionMatrix.clone());
					temp.draw(modelViewProjection);
				}
			}
	}
	
	// overload that is only used when dragging
	public void draw(float[] mProjectionMatrix, float[] mViewMatrix) {
		float[] modelViewProjection = new float[16];
		if(draggedFile == null)
			return;
		Matrix.multiplyMM(modelViewProjection, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		draggedFile.draw(modelViewProjection);
	}
	
	public String onClick() {
		String result = null;
		if(draggedFile != null) {
			result = draggedFile.getFilename();
			draggedFile = null;
		}
		return result;
	}
	
	// functions that handle drag functionality
	public void onTouch(PointF screenP) {
		for(int i = 0; i < numCols; i++)
			for(int j = 0; j < numRows; j++) {
				DraggrFile temp = mFiles[i][j];
				if(temp.onScreen && temp.onTouch(screenP.x, screenP.y)) {
					draggedFile = temp;
					return;
				}
			}
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
	}
	
	public String getId() {
		return mId;
	}
	
	public Device getDevice() {
		return mDevice;
	}
	
	public void transfer(DraggrFolderBase dest) {
		if(draggedFile == null) {
			Log.e(LOGTAG, "no file selected");
			return;
		}
		new TransferTask(mDevice, draggedFile.getFilename(), dest.getDevice()).execute();
	}
	
	// do a thing
	private PointF placeInSpace(int i, int j, float x, float y) {
		float dy = (float) (j - 1) * y * 1.0f;
		float dx = (float) (i - 1) * x * 1.0f;
		
		return new PointF(dx, dy);
	}
	
	// given retrieved file information, generate files
	// this can be called anytime we receive a new update from the device
	// TOUSE: uncomment commented block, comment out the HashSet garbage
	public void populateFiles() {
		if(!mDevice.isConnected()) {
			Log.e(LOGTAG, mDevice.getName() + " not connected, could not retrieve files");
			return;
		}
		Iterator<String> itr = mDevice.listFiles().iterator();
		/*HashSet<String> files = new HashSet<String>();
		files.add("game_of_thrones");
		files.add("hunger_games");
		Iterator<String> itr = files.iterator();*/ /* old placeholder code I think - Nathan */
		int f = 0;
		int i = 0;
		int j = 0;
		while(itr.hasNext() && f < numRows * numCols) {
			String file = itr.next();
			mFiles[i][j].setFilename(file);
			// create a new texture from the file thumbnail here?
			File thumbnail = mDevice.thumbnail(file);
			if(thumbnail == null)
				continue;
			Texture texture = Texture.loadTextureFromFile(thumbnail);
			mRenderer.loadTextureToFile(texture, mFiles[i][j]);
			// can set the thumbnail here using setTexture // isn't that done by loadTextureToFile? -Nathan
			mFiles[i][j].onScreen = true;
			j++;
			f++;
			if(j >= numRows) {
				i++;
				j = 0;
			}
		}
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
