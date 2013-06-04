package webdad.apps.verbshaker;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private ListPreference lang;
	private ListPreference color;
	private EditTextPreference fsize;
	public SharedPreferences sharedPref ;
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref =  PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.preferences);
        lang = (ListPreference)getPreferenceScreen().findPreference("pref_language");
        color = (ListPreference)getPreferenceScreen().findPreference("pref_font_color");
        fsize = (EditTextPreference)getPreferenceScreen().findPreference("pref_font_size");
        
        lang.setSummary(lang.getEntry());
        
        color.setSummary(color.getEntry());
        

        fsize.setSummary(sharedPref.getString("pref_font_size", getString(R.string.pref_font_size_default)));
    }
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		  if (key.equals("pref_language")) {
			 lang.setSummary(lang.getEntry());
	        }
		  else if (key.equals("pref_font_size")){
			  fsize.setSummary(sharedPreferences.getString("pref_font_size", getString(R.string.pref_font_size_default)));
		  }
		  else if (key.equals("pref_font_color")){
			  color.setSummary(color.getEntry());
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
