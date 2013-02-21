package webdad.apps.verbshaker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
    private static String DICTIONARY_TABLE_NAME = "verbs_de";
	private static final String DATABASE_NAME = "proverbs";

	private Context myContext;

    DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        myContext = context;
        DICTIONARY_TABLE_NAME = context.getResources().getString(R.string.db_table);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.i("DB","Database is going to be created.");
        db.execSQL("CREATE TABLE "+DICTIONARY_TABLE_NAME+" (id INT auto_increment, front TEXT, back TEXT)"); 
        initialInsert(db);
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		if(arg2<arg1)return;
		if(arg0.isReadOnly())return;
		//TODO upgrade when we wrote an update and chaned DBVERSION(Arg2)
	}
	
	public void CreateMe(){
		try {
		SQLiteDatabase d = this.getWritableDatabase();
		d.close();
		}
		catch(Exception e){
			Log.e("DB",e.getMessage());
		}
	}
	
	public void initialInsert(SQLiteDatabase db){
		Log.i("DB","Inserting initial Content");
		AssetManager mngr = myContext.getAssets();
	    try {
			InputStream is = mngr.open(DICTIONARY_TABLE_NAME+".csv");
			// foreach line split it at | and then insert it
			InputStreamReader inputStreamReader = new InputStreamReader(is);
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    String line;
		    while ((line = bufferedReader.readLine()) != null) {
		        String[] tmp = line.split("\\|");
		        db.execSQL("INSERT INTO "+DICTIONARY_TABLE_NAME+" (front, back) VALUES ('"+tmp[0]+"', '"+tmp[1]+"')"); 
		        Log.i("DB","INSERT INTO "+DICTIONARY_TABLE_NAME+" (front, back) VALUES ('"+tmp[0]+"', '"+tmp[1]+"')");
		    }
		    bufferedReader.close();
		    inputStreamReader.close();
		    is.close();
		    mngr.close();
		} catch (IOException e) {
			Log.e("DB","Could not open "+DICTIONARY_TABLE_NAME+".csv");
		}			
	}
	
	public Boolean Sync(){
		Boolean ok = true;
		SQLiteDatabase d = this.getWritableDatabase();

		

		   Log.i("Sync", "Getting HTTP");
		   HttpClient client = new DefaultHttpClient();
		   HttpGet request = new HttpGet("http://verbshaker.webdad.eu/sync.php");
		   HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				Log.e("Sync","CPE "+e.getMessage());
			} catch (IOException e) {
				Log.e("Sync","IOE "+e.getMessage());
			}
		   InputStream in = null;
			try {
				in = response.getEntity().getContent();
			} catch (IllegalStateException e) {
				Log.e("Sync","ISE "+e.getMessage());
			} catch (IOException e) {
				Log.e("Sync","IOE "+e.getMessage());
			}
			InputStreamReader inputStreamReader = new InputStreamReader(in);
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    String line;
		    Log.i("Sync", "Truncating "+DICTIONARY_TABLE_NAME);
			d.execSQL("DELETE FROM "+DICTIONARY_TABLE_NAME);
		    try{
		    while ((line = bufferedReader.readLine()) != null) {
		        String[] tmp = line.split("\\|");
		        d.execSQL("INSERT INTO "+DICTIONARY_TABLE_NAME+" (front, back) VALUES ('"+tmp[0]+"', '"+tmp[1]+"')"); 
		        Log.i("Sync","INSERT INTO "+DICTIONARY_TABLE_NAME+" (front, back) VALUES ('"+tmp[0]+"', '"+tmp[1]+"')");
		    }
		    bufferedReader.close();
		    inputStreamReader.close();
		    in.close();
		    }
		    catch(Exception e){
		    	Log.e("Sync",e.getMessage());
		    }
		d.close();
		Log.i("Sync", "Sync Complete");
		return ok;
	}
	
	public String getProVerb(){
		String r = "";
		try{
		SQLiteDatabase d = this.getWritableDatabase();
		Cursor rf = d.rawQuery("SELECT front FROM "+DICTIONARY_TABLE_NAME+" ORDER BY RANDOM() ASC LIMIT 1", null);
		Cursor rb = d.rawQuery("SELECT back FROM "+DICTIONARY_TABLE_NAME+" ORDER BY RANDOM() ASC LIMIT 1", null);
		
		rf.moveToFirst();
		rb.moveToFirst();
		
		r += rf.getString(0) + " "+ rb.getString(0);
		
		rf.close();
		rb.close();
		d.close();
		}
		catch(Exception e){
			Log.e("DB", e.getMessage());
			r = "An Error has occured...";
		}
		return r;
	}

}
