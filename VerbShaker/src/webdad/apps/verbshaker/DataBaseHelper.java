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

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
    private static String TABLE_NAME_DE = "verbs_de";
    private static String TABLE_NAME_EN = "verbs_en";
    private static String TABLE_NAME_ES = "verbs_es";
	private static final String DATABASE_NAME = "proverbs";

	private Context myContext;

    DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i("DB","Database object opened. VERSION:"+DATABASE_VERSION);
        myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.i("DB","Database is going to be created.");
        db.execSQL("CREATE TABLE "+TABLE_NAME_DE+" (id INT auto_increment, front TEXT, back TEXT)"); 
        db.execSQL("CREATE TABLE "+TABLE_NAME_EN+" (id INT auto_increment, front TEXT, back TEXT)"); 
        db.execSQL("CREATE TABLE "+TABLE_NAME_ES+" (id INT auto_increment, front TEXT, back TEXT)"); 
        AssetManager mngr = myContext.getAssets();
        initialInsert(db, TABLE_NAME_DE, mngr);
        initialInsert(db, TABLE_NAME_EN, mngr);
        initialInsert(db, TABLE_NAME_ES, mngr);
        //mngr.close();
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		Log.i("DB","Ran into upgrade scenario. OLD:"+arg1+" NEW:"+arg2);
		if(arg2<arg1)return;
		if(arg0.isReadOnly())return;
		if(arg2==2){
			 arg0.execSQL("CREATE TABLE "+TABLE_NAME_ES+" (id INT auto_increment, front TEXT, back TEXT)"); 
			 AssetManager mngr = myContext.getAssets();
		    initialInsert(arg0, TABLE_NAME_ES, mngr);
		}
	}
	
	public void CreateMe(){
		try {
		SQLiteDatabase d = this.getWritableDatabase();
		Log.i("DB", "Opened Database. VERSION: "+d.getVersion());
		d.close();
		}
		catch(Exception e){
			Log.e("DB",e.getMessage());
		}
	}
	
	public void initialInsert(SQLiteDatabase db, String table, AssetManager mngr){
		Log.i("DB","Inserting initial Content");
		
	    try {
			InputStream is = mngr.open(table+".csv");
			// foreach line split it at | and then insert it
			InputStreamReader inputStreamReader = new InputStreamReader(is);
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    String line;
		    while ((line = bufferedReader.readLine()) != null) {
		    	
		        String[] tmp = line.split("\\|");
		        ContentValues c = new ContentValues();
		        c.put("front", tmp[0]);
		        c.put("back", tmp[1]);
		        try{
		        db.insert(table, null, c);
		        Log.i("DB","INSERT INTO "+table+" (front, back) VALUES ('"+tmp[0]+"', '"+tmp[1]+"')");
		        }
		        catch(SQLException se){
		        	Log.e("DB",se.getMessage());
		        }
		    }
		    bufferedReader.close();
		    inputStreamReader.close();
		    is.close();
		} catch (IOException e) {
			Log.e("DB","Could not open "+table+".csv");
		}			
	}
	
	private Boolean Sync(String table){
		SQLiteDatabase d = this.getWritableDatabase();

		   Log.i("Sync", "Getting HTTP");
		   HttpClient client = new DefaultHttpClient();
		   HttpGet request = new HttpGet("http://verbshaker.webdad.eu/sync.php?t="+table);
		   HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				Log.e("Sync","CPE "+e.getMessage());
				return false;
			} catch (IOException e) {
				Log.e("Sync","IOE "+e.getMessage());
				return false;
			}
		   InputStream in = null;
			try {
				in = response.getEntity().getContent();
			} catch (IllegalStateException e) {
				Log.e("Sync","ISE "+e.getMessage());
				return false;
			} catch (IOException e) {
				Log.e("Sync","IOE "+e.getMessage());
				return false;
			}
			InputStreamReader inputStreamReader = new InputStreamReader(in);
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    String line;
		    Log.i("Sync", "Truncating "+table);
			d.execSQL("DELETE FROM "+table);
		    try{
		    while ((line = bufferedReader.readLine()) != null) {
		        String[] tmp = line.split("\\|");
		        ContentValues c = new ContentValues();
		        c.put("front", tmp[0]);
		        c.put("back", tmp[1]);
		        try{
		        d.insert(table, null, c);
		        Log.i("DB","INSERT INTO "+table+" (front, back) VALUES ('"+tmp[0]+"', '"+tmp[1]+"')");
		        }
		        catch(SQLException se){
		        	Log.e("DB",se.getMessage());
		        }
		        Log.i("Sync","INSERT INTO "+table+" (front, back) VALUES ('"+tmp[0]+"', '"+tmp[1]+"')");
		    }
		    bufferedReader.close();
		    inputStreamReader.close();
		    in.close();
		    }
		    catch(Exception e){
		    	Log.e("Sync",e.getMessage());
		    	return false;
		    }
		d.close();
		Log.i("Sync", "Sync Complete");
		return true;
	}
	
	public Boolean Sync(){ //TODO: Correct Error Management...
		Boolean c = true;
		c = Sync(TABLE_NAME_DE);
		c = Sync(TABLE_NAME_EN);
		c = Sync(TABLE_NAME_ES);
		return c;
	}
	
	public String getProVerb(String language){
		String r = "";
		String table="verbs_de";
		if(language.equals("de"))table="verbs_de";
		if(language.equals("en"))table="verbs_en";
		if(language.equals("es"))table="verbs_es";
		try{
		SQLiteDatabase d = this.getWritableDatabase();
		Cursor rf = d.rawQuery("SELECT front FROM "+table+" ORDER BY RANDOM() ASC LIMIT 1", null);
		Cursor rb = d.rawQuery("SELECT back FROM "+table+" ORDER BY RANDOM() ASC LIMIT 1", null);
		
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
