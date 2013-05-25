package com.aungthiha.geekalarm;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

public class QuestionData {

	private static final String DB_NAME = "mathq.db";
	private static final int DB_VERSION = 1;
	


	public static final String TABLE = "easy";
	public static final String D_ID = BaseColumns._ID;
	public static final String QUESTION = "question";
	public static final String ANSWER = "answer";
	public static final String CHOICE_1 = "choice_1";
	public static final String CHOICE_2 = "choice_2";
	public static final String CHOICE_3 = "choice_3";
	public static final String CHOICE_4 = "choice_4";
	public static final String CHOICE_5 = "choice_5";
	public static final String SOLUTION = "solution";
	private static final String TAG = "QuestionData";

	Context context;
	DbHelper dbHelper;
	SQLiteDatabase db;

	public QuestionData(Context context) throws IOException {
		this.context = context;
		dbHelper = new DbHelper();
		dbHelper.createDataBase();
	}

	public void insert(ContentValues values) {
		new SaveQuestionAsync().execute(values);
	}

	private class SaveQuestionAsync extends
			AsyncTask<ContentValues, Void, Void> {

		@Override
		protected Void doInBackground(ContentValues... values) {
			db = dbHelper.getWritableDatabase();
			db.insert(TABLE, null, values[0]);
			Log.d(TAG, "values inserted ");

			return null;
		}

	}

	public Cursor query() {
		db = dbHelper.getReadableDatabase();
		Log.d(TAG, "on query");
		Cursor cursor = db.query(TABLE, null, null, null, null, null,
				"RANDOM()", "1");
		
		return cursor;
	}
	
	public Cursor query(long id) {
		db = dbHelper.getReadableDatabase();
		Log.d(TAG, "on query id");
		Cursor cursor = db.query(TABLE, null, D_ID +" = " +id ,null , null,
				null,null);
		
		return cursor;
	}

	// public Cursor queryAsync(){
	// new QueryCursor();
	// }

//	private class QueryCursor extends AsyncTask<Void, Void, Void> {
//
//		@Override
//		protected Void doInBackground(Void... params) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//	}

	public class DbHelper extends SQLiteOpenHelper {

	   
	    private static final String DB_PATH = "/data/data/com.aungthiha.geekalarm/databases/";
	 
	    
	 
	    private SQLiteDatabase myDataBase; 
	 
	    
	 
	 
	 
	  /**
	     * Creates a empty database on the system and rewrites it with your own database.
	     * */
	    public void createDataBase() throws IOException{
	 
	    	boolean dbExist = checkDataBase();
	 
	    	if(dbExist){
	    		Log.d(TAG, "db exists");

	    	}else{
	    		
	    		Log.d(TAG, "copy db");
	 
	    		//By calling this method and empty database will be created into the default system path
	               //of your application so we are gonna be able to overwrite that database with our database.
	        	this.getReadableDatabase();
	 
	        	try {
	 
	    			copyDataBase();
	 
	    		} catch (IOException e) {
	 
	        		throw new Error("Error copying database");
	 
	        	}
	    	}
	 
	    }
	 
	    /**
	     * Check if the database already exist to avoid re-copying the file each time you open the application.
	     * @return true if it exists, false if it doesn't
	     */
	    private boolean checkDataBase(){
	 
	    	SQLiteDatabase checkDB = null;
	 
	    	try{
	    		String myPath = DB_PATH + DB_NAME;
	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	 
	    	}catch(SQLiteException e){
	 
	    		Log.d(TAG, "checkDatabase() , not exist yet");
	 
	    	}
	 
	    	if(checkDB != null){
	 
	    		checkDB.close();
	 
	    	}
	 
	    	return checkDB != null ? true : false;
	    }
	    
	    private void copyDataBase() throws IOException{
	    	 
	    	//Open your local db as the input stream
	    	InputStream myInput = context.getAssets().open(DB_NAME);
	 
	    	// Path to the just created empty db
	    	String outFileName = DB_PATH + DB_NAME;
	 
	    	//Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream(outFileName);
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	 
	    }
	 
	    public void openDataBase() throws SQLException{
	 
	    	//Open the database
	        String myPath = DB_PATH + DB_NAME;
	    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	 
	    }
	 
	    @Override
		public synchronized void close() {
	 
	    	    if(myDataBase != null)
	    		    myDataBase.close();
	 
	    	    super.close();
	 
		}
	 
		public DbHelper() {
			super(context,DB_NAME, null, DB_VERSION);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
//			String sql = String
//					.format("create table %s "
//							+ "(%s integer primary key autoincrement, %s text, %s text, %s text, %s text, %s text , %s text, %s text) ",
//							TABLE, D_ID, QUESTION, ANSWER, CHOICE_1, CHOICE_2,
//							CHOICE_3, CHOICE_4, CHOICE_5);
//			Log.d(TAG, "on created with SQL: " + sql);
//			Log.d(TAG, "" + QUESTION + "  " + ANSWER);
//			db.execSQL(sql);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL("drop  if exists" + TABLE);
			onCreate(db);

		}

	}
}
