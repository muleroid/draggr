package edu.uchicago.proprio.draggr.transfer;

import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class DownloadPreviewTask extends AsyncTask<Void, Void, File> {
	private String filename;
	private Device device;
	private ImageView view;
	
	public DownloadPreviewTask(Device d, String s, ImageView v) {
		filename = s;
		device = d;
		view = v;
	}

	@Override
	protected File doInBackground(Void... arg0) {
		try {
			return device.preview(filename);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(File f) {
		if (f != null) {
			Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
			view.setImageBitmap(bm);
		}
	}
}
