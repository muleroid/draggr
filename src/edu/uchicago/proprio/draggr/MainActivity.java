package edu.uchicago.proprio.draggr;

import java.io.IOException;

import edu.uchicago.proprio.draggr.transfer.Device;
import edu.uchicago.proprio.draggr.artools.DraggrAR;
import edu.uchicago.proprio.draggr.artools.DraggrGLView;
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
		setContentView(R.layout.activity_main);
		/*new Thread(new Runnable() {
			public void run() {
				Device testDevice = new Device("arch_nathan");
				if(testDevice.tryConnect())
					Log.d("MainActivity", "successfully connected to arch_nathan");
				else
					Log.e("MainActivity", "connection unsuccessful");
				
				String motd;
				try {
					motd = testDevice.motd();
					Log.d("MainActivity", motd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("motd", e.toString());
				}
				
				try {
					testDevice.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();*/
		
		
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
