package webdad.apps.verbshaker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
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
        if(sel_lang == "de")slang="Deutsch";
        else if (sel_lang=="en")slang="English";
        else slang = "???";
        
        lang.setSummary(slang);
    }
}
