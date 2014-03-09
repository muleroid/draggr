package edu.uchicago.proprio.draggr.transfer;

import java.io.IOException;

import android.os.AsyncTask;

public class UpdateFilesTask extends AsyncTask<Void, Void, Void> {
	private Device device;
	private String filter;
	
	public UpdateFilesTask(Device d, String f) {
		device = d;
		filter = f;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			device.updateFiles(filter);
		} catch (IOException e) {
		}
		return null;
	}
}
