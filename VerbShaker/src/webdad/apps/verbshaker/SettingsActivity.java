package webdad.apps.verbshaker;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private ListPreference lang;
	public SharedPreferences sharedPref ;
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref =  PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.preferences);
        lang = (ListPreference)getPreferenceScreen().findPreference("pref_language");
        String sel_lang = sharedPref.getString("pref_language", getString(R.string.pref_language_default));
        
        String slang = "";
        if(sel_lang.equals("de")){slang="Deutsch";}
        else if (sel_lang.equals("en")){slang="English";} 
        else {slang = "???";}
        
        lang.setSummary(slang);
    }
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		  if (key.equals("pref_language")) {
			  String sel_lang = sharedPreferences.getString("pref_language", getString(R.string.pref_language_default));
		        Log.d("Language", "Settings: Found "+sel_lang);
		        String slang = "";
		        if(sel_lang.equals("de")){slang="Deutsch";}
		        else if (sel_lang.equals("en")){slang="English";} 
		        else {slang = "???";}
		        Log.d("Language", "Settings: Set to  "+slang);
		        lang.setSummary(slang);
	        }

		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
}
