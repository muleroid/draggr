package edu.uchicago.proprio.draggr;

import edu.uchicago.proprio.draggr.transfer.DraggrPassive;
import edu.uchicago.proprio.draggr.artools.DraggrAR;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends Activity {
	private final static String LOGTAG = "MainActivity";
	private String trackable = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button arStart = (Button) this.findViewById(R.id.start_ar);
		arStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), DraggrAR.class);
				v.getContext().startActivity(intent);
			}
		});
		
		Button passiveStart = (Button) this.findViewById(R.id.start_passive);
		passiveStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(trackable == null)
					return;
				Intent intent = new Intent(v.getContext(), DraggrPassive.class);
				intent.putExtra("trackable_name", trackable);
				v.getContext().startActivity(intent);
			}
			
		});
		
		Spinner spinner = (Spinner) this.findViewById(R.id.trackable_selector);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.trackable_list, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int pos, long id) {
				trackable = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				trackable = null;
			}
		});
	}

	/*public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

}
