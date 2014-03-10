package edu.uchicago.proprio.draggr.transfer;

import android.os.AsyncTask;
import android.util.Log;

public class ConnectDeviceTask extends AsyncTask<Void, Void, Void> {

	private Device device;
	public ConnectDeviceTask (Device d) {
		device = d;
	}
	@Override
	protected Void doInBackground(Void... params) {
		Log.d(device.getName(), "Trying to connect");
		if(device.tryConnect()) {
			Log.d(device.getName(), "Connected successfully");
		} else {
			Log.e(device.getName(), "Unable to connect");
		}
		return null;
	}

}
