package webdad.apps.verbshaker;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements SensorEventListener {

	public DataBaseHelper db;
	public TextView txt_m;
	public TextView txt_ad;
	private ProgressDialog pd;
	
	private Boolean sync_onstart;
	private Boolean sync_wlan_only;
	private Boolean t2s;
	private String language;
	private String font_size;
	private String font_color;
	
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private Boolean sensor_ok;
	private static final int SHAKE_THRESHOLD = 800;
	private long lastUpdate=0;
	float x, y, z, last_x=0.0f, last_y=0.0f, last_z = 0.0f, gravity_x=0.0f, gravity_y=0.0f, gravity_z=0.0f;
	final float alpha = (float) 0.8;
	
	private Vibrator vib;
	private Boolean vib_ok;
	private OnSharedPreferenceChangeListener listener;
	private AdView mAdView;
	
	private TextToSpeech myT2S;

	private Locale ES;
	//IN
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("App", "Starting...");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		sync_onstart = sharedPref.getBoolean("pref_sync_onstart",sharedPref.getBoolean("pref_sync_onstart_default", true));
		sync_wlan_only = sharedPref.getBoolean("pref_ws",sharedPref.getBoolean("pref_ws_default", true));
		
		language = sharedPref.getString("pref_language",sharedPref.getString("pref_language_default", getResources().getConfiguration().locale.getDisplayName()));
		font_size = sharedPref.getString("pref_font_size",sharedPref.getString("pref_font_size_default", "30"));
		font_color = sharedPref.getString("pref_font_color",sharedPref.getString("pref_font_color_default", "#009a00"));
		
		
		t2s = sharedPref.getBoolean("pref_t2s",sharedPref.getBoolean("pref_t2s_default", true));
		db = new DataBaseHelper(getApplicationContext());
		

		ES = new Locale("es","ES");
		
		Log.i("App", "Create DB if needed...");
		db.CreateMe();
		
		txt_m = (TextView)findViewById(R.id.txt_mixed);
		
		Log.i("TXT", "Fontsize = "+font_size);
		try{
		txt_m.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(font_size));
		}
		catch(NumberFormatException e){
			txt_m.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30.0f);
		}
		Log.i("TXT","Set textsize to "+txt_m.getTextSize());
		
		Log.i("TXT", "Font color = "+font_color);
		txt_m.setTextColor(Color.parseColor(font_color));
		Log.i("TXT"," Set fontcolor to "+txt_m.getTextColors().toString());
		
		txt_ad = (TextView)findViewById(R.id.txt_adError);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
		    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		    sensor_ok=true;
		  }
		else {
			sensor_ok=false;
		}
		
		vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if (vib != null){
		    vib_ok=true;
		  }
		else {
			vib_ok=false;
		}
		
		myT2S = new TextToSpeech(this,null);

		if(sync_onstart){
			sync();
		}
		
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			   language = prefs.getString("pref_language",prefs.getString("pref_language_default", getResources().getConfiguration().locale.getDisplayName()));
			   Log.d("Language","Main: Set lang to "+language); 
			   t2s = prefs.getBoolean("pref_t2s",prefs.getBoolean("pref_t2s_default", true));
			   sync_wlan_only = prefs.getBoolean("pref_ws",prefs.getBoolean("pref_ws_default", true));
			   if(t2s)
			   {
				   Log.d("T2S", "T2S set to true");
			   if(language.equals("de")){myT2S.setLanguage(Locale.GERMAN);}
			     else if (language.equals("en")){myT2S.setLanguage(Locale.ENGLISH);} 
			     else if (language.equals("es")){myT2S.setLanguage(ES);} 
			     else if (language.equals("fr")){myT2S.setLanguage(Locale.FRENCH);} 
			     else {myT2S.setLanguage(Locale.GERMAN);}
			   Log.d("T2S","Language of T2S set to "+myT2S.getLanguage());
			   }
			   font_size = prefs.getString("pref_font_size",prefs.getString("pref_font_size", "30"));

			   try{
					txt_m.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(font_size));
					}
					catch(NumberFormatException e){
						txt_m.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30.0f);
					}
				Log.d("TXT","Set textsize to "+txt_m.getTextSize());
				
				font_color = prefs.getString("pref_font_color",prefs.getString("pref_font_color", "#009a00"));
				
				Log.d("TXT", "Font color = "+font_color);
				txt_m.setTextColor(Color.parseColor(font_color));
				Log.d("TXT"," Set fontcolor to "+txt_m.getTextColors().toString());
			  }
			};
		
		mAdView = (AdView) findViewById(R.id.ad);
	    mAdView.setAdListener(new MyAdListener());
	
		AdRequest adRequest = new AdRequest();
	    adRequest.addKeyword("proverbs fun");
	    mAdView.loadAd(adRequest);
	    
	    
		    
	   
		Log.i("App", "Ready!");
	}
	
	//created
	
	@Override
	protected void onStart(){
		super.onStart();
		 if(t2s)
		    {
		    	Log.i("T2S", "Language of App is "+language);
		    	
		    	
		    	 if(language.equals("de")){myT2S.setLanguage(Locale.GERMAN);}
			     else if (language.equals("en")){myT2S.setLanguage(Locale.ENGLISH);} 
			     else if (language.equals("es"))
			     {
			    	 Log.i("T2S",myT2S.isLanguageAvailable(ES)+"");
			    	 if(myT2S.isLanguageAvailable(ES) == TextToSpeech.LANG_COUNTRY_AVAILABLE){
			    		 Log.i("T2S","Spanish is avaible");
			    	 myT2S.setLanguage(ES);
			    	 }
			    	 else {
			    		 Log.i("T2S","Spanish is NOT avaible");
			    		 myT2S.setLanguage(Locale.ENGLISH);
			    		 myT2S.speak("Spanish is not avaiable on your phone", TextToSpeech.QUEUE_FLUSH, null);
			    		 t2s = false;
			    	 }
			     } 
			     else if (language.equals("fr")){myT2S.setLanguage(Locale.FRENCH);} 
			     else {myT2S.setLanguage(Locale.GERMAN);}
		    	
		    	 Log.i("T2S","Language of T2S set to "+myT2S.getLanguage());
		    }
	}
	
	//started(visible)
	
	@Override
	protected void onResume(){
		super.onResume();
		if(sensor_ok){ mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);}
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener);

		
	}
	
	//resumed (visible)
	
	@Override
	protected void onPause(){
		super.onPause();
		if(sensor_ok){mSensorManager.unregisterListener(this);}
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
		myT2S.shutdown();
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
	
	//Layout change
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_settings:
	        	try
	        	{
	        	Intent in = new Intent(MainActivity.this, SettingsActivity.class);
	        	startActivity(in);
	        	}
	        	catch(Exception e)
	        	{
	        	Log.e("Menu", e.getMessage());
	        	}
	        	
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
	        case R.id.menu_share:
	        	try
	        	{
	        		Intent sendIntent = new Intent();
		        	sendIntent.setAction(Intent.ACTION_SEND);
		        	sendIntent.putExtra(Intent.EXTRA_TEXT, txt_m.getText()+"\n\n\nMixUp presented by VerbShaker\nAn App brought to you by <a href=\"http://www.webdad.eu\">WebDaD.eu</a>");
		        	sendIntent.putExtra(Intent.EXTRA_SUBJECT, "VerbShaker Mix!" );
		        	sendIntent.setType("text/plain");
		        	startActivity(sendIntent);
	        	}
	        	catch(Exception e)
	        	{
	        		Log.e("Menu", e.getMessage());
	        	}
	            return true;
	        case R.id.menu_about:
	        	try
	        	{
	        	Intent in = new Intent(MainActivity.this, AboutActivity.class);
	        	MainActivity.this.startActivity(in);
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
	     view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	 }
	
	public void getMix(){
		Log.d("Mix","Get Mix for Language "+language);
		txt_m.setText(db.getProVerb(language));
		Log.d("Mix","Mix is  "+txt_m.getText());
		if(t2s)
		{
			myT2S.speak((String) txt_m.getText(), TextToSpeech.QUEUE_FLUSH, null);
			
		}
	}
	
	private class MyAdListener implements AdListener {

		public void onDismissScreen(Ad arg0) {
			
		}

		public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
			txt_ad.setText("Error getting AD");
			txt_ad.setVisibility(0);

		}

		public void onLeaveApplication(Ad arg0) {

		}

		public void onPresentScreen(Ad arg0) {

		}

		public void onReceiveAd(Ad arg0) {
			txt_ad.setText("");
			txt_ad.setVisibility(8);
		}

	}
	
	private void sync(){
		pd = ProgressDialog.show(MainActivity.this, "", "Syncing with Online DB", true, false);
		SyncThread st = new SyncThread();
	    st.start();
	    st = null;
	}
	
	private class SyncThread extends Thread {
        public SyncThread() {
        }

        @Override
        public void run() {   
        	if(isNetworkAvailable()){
        		if(sync_wlan_only && isWLANAvailable()){
        			db.Sync();
        		}
        	}
            handler.sendEmptyMessage(0);
        }

		@SuppressLint("HandlerLeak")
		private Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                pd.dismiss();
                
            }
        };
    }
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private boolean isWLANAvailable() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    return mWifi != null && mWifi.isConnected();
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do Something for an Acc change
		
	}

	public void onSensorChanged(SensorEvent event) {
		if(sensor_ok){
			Log.i("Sensor",event.toString());
	
			long curTime = System.currentTimeMillis();
		    // only allow one update every 200ms.
		    if ((curTime - lastUpdate) > 200) {
		      long diffTime = (curTime - lastUpdate);
		      lastUpdate = curTime;
	
		      
	
		      // Isolate the force of gravity with the low-pass filter.
		      gravity_x = alpha * gravity_x + (1 - alpha) * event.values[0];
		      gravity_y = alpha * gravity_y + (1 - alpha) * event.values[1];
		      gravity_z = alpha * gravity_z + (1 - alpha) * event.values[2];
	
		      // Remove the gravity contribution with the high-pass filter.
		      x = event.values[0] - gravity_x;
		      y = event.values[1] - gravity_y;
		      z = event.values[2] - gravity_z;
	
	
		      float speed = Math.abs(x+y+z - (last_x + last_y + last_z)) / diffTime * 10000;
	
		      if (speed > SHAKE_THRESHOLD) {
		        Log.d("sensor", "shake detected w/ speed: " + speed);
		        getMix();
		       if(vib_ok){ vib.vibrate(300);}
		      }
		      last_x = x;
		      last_y = y;
		      last_z = z;
		    }
		}
	}


}
