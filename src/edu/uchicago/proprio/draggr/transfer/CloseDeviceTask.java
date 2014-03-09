package edu.uchicago.proprio.draggr.transfer;

import java.io.IOException;

import android.os.AsyncTask;

public class CloseDeviceTask extends AsyncTask<Void,Void,Void> {

	private Device device;
	public CloseDeviceTask(Device d) {
		device = d;
	}
	@Override
	protected Void doInBackground(Void... params) {
		try { device.close(); }
		catch (IOException e) {}
		return null;
	}

}
