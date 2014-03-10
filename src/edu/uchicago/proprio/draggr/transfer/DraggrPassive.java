package edu.uchicago.proprio.draggr.transfer;

import java.io.IOException;

import edu.uchicago.proprio.draggr.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class DraggrPassive extends Activity {
	private final static String LOGTAG = "DraggrPassive";
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOGTAG, "onCreate");
		setContentView(R.layout.activity_draggr_passive);
		Bundle extras = this.getIntent().getExtras();
		String trackable_name = extras.getString("trackable_name");
		ImageView trackable = (ImageView) this.findViewById(R.id.trackable);
		try {
			Bitmap toDisplay = BitmapFactory.decodeStream(this.getAssets().open(trackable_name+".jpg"));
			trackable.setImageBitmap(toDisplay);
			Log.d(LOGTAG, "loaded");
		} catch(IOException e) {
			Log.e(LOGTAG, "Could not load image: " + trackable_name);
			Log.e(LOGTAG, e.getMessage());
		}
	}
}
