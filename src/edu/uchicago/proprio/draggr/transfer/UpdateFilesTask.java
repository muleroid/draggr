package edu.uchicago.proprio.draggr.transfer;

import java.io.IOException;

import edu.uchicago.proprio.draggr.shapes.DraggrFolderBase;

import android.os.AsyncTask;
import android.util.Log;

public class UpdateFilesTask extends AsyncTask<Void, Void, Void> {
	private Device device;
	private String filter;
	private DraggrFolderBase parent;
	
	public UpdateFilesTask(Device d, String f, DraggrFolderBase p) {
		device = d;
		filter = f;
		parent = p;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d(device.getName(), "Attempting to get file info");
		if (!device.blockUntilConnected())
			return null;
		try {
			Log.d(device.getName(), "Unblocked");
			device.updateFiles(filter);
			parent.populateFiles();
		} catch (IOException e) {
		}
		return null;
	}
}
