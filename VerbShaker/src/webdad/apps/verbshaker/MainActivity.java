package webdad.apps.verbshaker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	public DataBaseHelper db;
	public TextView txt_m;
	private ProgressDialog pd;
	
	//IN
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("App", "Starting...");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		db = new DataBaseHelper(getApplicationContext());
		Log.i("App", "Create DB if needed...");
		db.CreateMe();
		txt_m = (TextView)findViewById(R.id.txt_mixed);
		Log.i("App", "Ready!");
	}
	
	//created
	
	@Override
	protected void onStart(){
		super.onStart();
	}
	
	//started(visible)
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	//resumed (visible)
	
	@Override
	protected void onPause(){
		super.onPause();
	}
	
	//paused (maybe visible)
	
	@Override
	protected void onStop(){
		super.onStop();
	}
	
	//stopped
	
	@Override
	protected void onDestroy(){
		db.close();
		super.onDestroy();
	}
	
	//OUT
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		try{
			getMenuInflater().inflate(R.menu.activity_main, menu);
		}
		catch(Exception e){
			Log.e("App",e.getMessage());
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_settings:
	        	/*try
	        	{
	        	Intent in = new Intent(MainActivity.this, preferences.class);
	        	MainActivity.this.startActivity(in);
	        	}
	        	catch(Exception e)
	        	{
	        	Log.e("Menu", e.getMessage());
	        	}*/
	        	
	            return true;
	        case R.id.menu_sync:
	        	try
	        	{
	        		sync();
	        	}
	        	catch(Exception e)
	        	{
	        		Log.e("Menu", e.getMessage());
	        	}
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void btn_getNewMix_onclick(View view) {
	     getMix();
	 }
	
	public void getMix(){
		txt_m.setText(db.getProVerb());
	}
	
	private void sync(){
		pd = ProgressDialog.show(MainActivity.this, "", "Syncing with Online DB", true, false);
		SyncThread st = new SyncThread();
	    st.start();
	}
	
	private class SyncThread extends Thread {
        public SyncThread() {
        }

        @Override
        public void run() {         
            db.Sync();
            handler.sendEmptyMessage(0);
        }

		private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                pd.dismiss();
            }
        };
    }
}
