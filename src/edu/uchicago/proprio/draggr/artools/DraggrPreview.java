package edu.uchicago.proprio.draggr.artools;

import edu.uchicago.proprio.draggr.R;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DraggrPreview extends Activity {
	private static final String LOGTAG = "DraggrPreview";
	
	private ImageView mImageView;

	// TODO: Figure out how to get the device into this activity, since the PreviewTask requires the
	// image view
	// otherwise, need another way to get the preview
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_draggr_preview);
	    super.onCreate(savedInstanceState);
	    Bundle extras = getIntent().getExtras();
	    TextView tView = (TextView) findViewById(R.id.preview_fn);
	    mImageView = (ImageView) findViewById(R.id.preview_img);
	    tView.setText(extras.getString("filename"));
	}

}
