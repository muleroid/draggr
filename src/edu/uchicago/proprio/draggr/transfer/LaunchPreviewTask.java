package edu.uchicago.proprio.draggr.transfer;

import java.io.File;
import java.io.IOException;

import edu.uchicago.proprio.draggr.artools.DraggrPreview;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class LaunchPreviewTask extends AsyncTask<Context, Void, Void> {
	private String filename;
	private Device device;
	
	public LaunchPreviewTask(Device d, String s) {
		filename = s;
		device = d;
	}

	@Override
	protected Void doInBackground(Context... arg0) {
		if (device.blockUntilConnected()) {
			try {
				File f = device.preview(filename);
				Context mContext = arg0[0];
				Intent intent = new Intent(mContext, DraggrPreview.class);
				intent.putExtra("filename", filename);
				intent.putExtra("previewPath", f.getAbsolutePath());
				mContext.startActivity(intent);
			} catch (IOException e) {
			}
		}
		return null;
	}

}
