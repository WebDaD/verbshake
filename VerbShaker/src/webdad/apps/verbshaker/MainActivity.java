package webdad.apps.verbshaker;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	public DataBaseHelper db;
	public TextView txt_m;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		db = new DataBaseHelper(getApplicationContext());
		db.CreateMe();
		txt_m = (TextView)findViewById(R.id.txt_mixed);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void btn_getNewMix_onclick(View view) {
	     getMix();
	 }
	
	public void getMix(){
		txt_m.setText(db.getProVerb());
		// TODO: get a new mix from db and fill txt_mixed with is
	}
}
