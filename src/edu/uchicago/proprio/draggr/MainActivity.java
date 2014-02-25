package edu.uchicago.proprio.draggr;

import edu.uchicago.proprio.draggr.artools.DraggrAR;
import edu.uchicago.proprio.draggr.artools.DraggrGLView;
import android.R;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("MainActivity", "onCreate");
		super.onCreate(savedInstanceState);
		
		//setContentView(R.layout.activity_main);
		Intent intent = new Intent(this, DraggrAR.class);
		this.startActivity(intent);
	}

	/*public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

}
