package edu.uchicago.proprio.draggr.transfer;

import java.io.IOException;

import android.os.AsyncTask;

public class TransferTask extends AsyncTask<Void, Void, Void> {

	private Device device;
	private String filename;
	private Device otherDevice;
	
	public TransferTask(Device d, String f, Device o) {
		device = d;
		filename = f;
		otherDevice = o;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if (!device.blockUntilConnected())
			return null;
		try {
			device.transfer(filename, otherDevice);
		} catch (IOException e) {}
		
		return null;
	}

}
