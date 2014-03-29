package no.hig.strand.lars.todoity.data;

import no.hig.strand.lars.todoity.data.TaskContract.ContextEntry;
import no.hig.strand.lars.todoity.data.TaskContract.ListEntry;
import no.hig.strand.lars.todoity.data.TaskContract.TaskEntry;
import no.hig.strand.lars.todoity.data.TaskContract.TimesEntry;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private final static String INTEGER_TYPE = " INTEGER";
	private final static String REAL_TYPE = " REAL";
	private final static String TEXT_TYPE = " TEXT";
	private final static String COMMA_SEP = ", ";
	
	private final static String SQL_CREATE_LIST = 
			"CREATE TABLE " + ListEntry.TABLE_NAME + " (" + 
			ListEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
			ListEntry.COLUMN_NAME_DATE + TEXT_TYPE + ")";
	private final static String SQL_DELETE_LIST =
			"DROP TABLE IF EXISTS " + ListEntry.TABLE_NAME;
	
	private final static String SQL_CREATE_TASK =
			"CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
			TaskEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
			TaskEntry.COLUMN_NAME_LIST + INTEGER_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_CATEGORY + TEXT_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_LOCATION_LAT + REAL_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_LOCATION_LNG + REAL_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_PRIORITY + INTEGER_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_IS_ACTIVE + INTEGER_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_TEMP_START + INTEGER_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_TIME_START + INTEGER_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_TIME_END + INTEGER_TYPE + COMMA_SEP + 
			TaskEntry.COLUMN_NAME_TIME_SPENT + INTEGER_TYPE + COMMA_SEP +
			TaskEntry.COLUMN_NAME_IS_FINISHED + INTEGER_TYPE + ")";
	private final static String SQL_DELETE_TASK =
			"DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME;
	
	private final static String SQL_CREATE_CONTEXT =
			"CREATE TABLE " + ContextEntry.TABLE_NAME + " (" +
			ContextEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
			ContextEntry.COLUMN_NAME_TASK + INTEGER_TYPE + COMMA_SEP +
			ContextEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
			ContextEntry.COLUMN_NAME_CONTEXT + TEXT_TYPE + COMMA_SEP +
			ContextEntry.COLUMN_NAME_DETAILS + TEXT_TYPE + ")";
	private final static String SQL_DELETE_CONTEXT =
			"DROP TABLE IF EXISTS " + ContextEntry.TABLE_NAME;
	
	private final static String SQL_CREATE_TIMES =
			"CREATE TABLE " + TimesEntry.TABLE_NAME + " (" +
			TimesEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
			TimesEntry.COLUMN_NAME_TASK_ID + INTEGER_TYPE + COMMA_SEP +
			TimesEntry.COLUMN_NAME_START_TIME + TEXT_TYPE + COMMA_SEP +
			TimesEntry.COLUMN_NAME_END_TIME + TEXT_TYPE + ")";
	private final static String SQL_DELETE_TIMES = 
			"DROP TABLE IF EXISTS " + TimesEntry.TABLE_NAME;
	
	
	public final static int DATABASE_VERSION = 1;
	public final static String DATABASE_NAME = "Tasks.db";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_LIST);
		db.execSQL(SQL_CREATE_TASK);
		db.execSQL(SQL_CREATE_CONTEXT);
		db.execSQL(SQL_CREATE_TIMES);
	}

	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		db.execSQL(SQL_DELETE_TIMES);
		db.execSQL(SQL_DELETE_CONTEXT);
		db.execSQL(SQL_DELETE_TASK);
		db.execSQL(SQL_DELETE_LIST);
		onCreate(db);
	}

}
