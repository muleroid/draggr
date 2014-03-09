package edu.uchicago.proprio.draggr.transfer;

import android.os.AsyncTask;

public class ConnectDeviceTask extends AsyncTask<Void, Void, Void> {

	private Device device;
	public ConnectDeviceTask (Device d) {
		device = d;
	}
	@Override
	protected Void doInBackground(Void... params) {
		device.tryConnect();
		return null;
	}

}
