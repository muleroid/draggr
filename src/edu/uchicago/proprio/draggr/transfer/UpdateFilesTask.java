package edu.uchicago.proprio.draggr.transfer;

import java.io.IOException;

import edu.uchicago.proprio.draggr.shapes.DraggrFolderBase;

import android.os.AsyncTask;

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
		if (!device.blockUntilConnected())
			return null;
		try {
			device.updateFiles(filter);
			parent.populateFiles();
		} catch (IOException e) {
		}
		return null;
	}
}
