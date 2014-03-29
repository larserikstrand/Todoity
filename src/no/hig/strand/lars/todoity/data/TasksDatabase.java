package no.hig.strand.lars.todoity.data;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.data.TaskContract.ListEntry;
import no.hig.strand.lars.todoity.data.TaskContract.TaskEntry;
import no.hig.strand.lars.todoity.data.TaskContract.TimesEntry;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TasksDatabase {

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	public static TasksDatabase sInstance = null;
	
	
	public static TasksDatabase getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new TasksDatabase(context);
		}
		return sInstance;
	}
	
	
	
	public TasksDatabase(Context context) {
		mDbHelper = new DatabaseHelper(context);
	}
	
	
	
	private void open() {
		mDb = mDbHelper.getWritableDatabase();
	}
	
	
	
	private void close() {
		mDbHelper.close();
	}
	
	
	
	private Task getTaskFromCursor(Cursor c, boolean hasKnownDate) {
		Task task = new Task();
		
		int taskId = c.getInt(c.getColumnIndexOrThrow(TaskEntry._ID));
		task.setId(taskId);
		
		if (! hasKnownDate) {
			// Find the date of the list the task is belonging to.
			int listId = c.getInt(c.getColumnIndexOrThrow(
					TaskEntry.COLUMN_NAME_LIST));
			Cursor c1 = fetchListById(listId);
			if (c1.moveToFirst()) {
				task.setDate(c1.getString(c1.getColumnIndexOrThrow(
						ListEntry.COLUMN_NAME_DATE)));
			}
		}
		
		task.setCategory(c.getString(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_CATEGORY)));
		task.setDescription(c.getString(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_DESCRIPTION)));
		task.setLatitude(c.getDouble(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_LOCATION_LAT)));
		task.setLongitude(c.getDouble(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_LOCATION_LNG)));
		task.setAddress(c.getString(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_ADDRESS)));
		task.setPriority(c.getInt(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_PRIORITY)));
		task.setActive(c.getInt(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_IS_ACTIVE)) > 0 ? true : false);
		task.setTempStart(c.getLong(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_TEMP_START)));
		task.setTimeStarted(c.getLong(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_TIME_START)));
		task.setTimeEnded(c.getLong(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_TIME_END)));
		task.setTimeSpent(c.getLong(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_TIME_SPENT)));
		task.setFinished(c.getInt(c.getColumnIndexOrThrow(
				TaskEntry.COLUMN_NAME_IS_FINISHED)) > 0 ? true : false);
		
		// Check if this task has fixed times (separate table).
		Cursor c2 = fetchTimesByTaskId(taskId);
		if (c2.moveToFirst()) {
			// Has fixed times.
			task.setFixedStart(c2.getString(c2.getColumnIndexOrThrow(
					TimesEntry.COLUMN_NAME_START_TIME)));
			task.setFixedEnd(c2.getString(c2.getColumnIndexOrThrow(
					TimesEntry.COLUMN_NAME_END_TIME)));
		}
		
		return task;
	}
	
	
	
	private ContentValues getContentCaluesFromTask(Task task) {
		ContentValues values = new ContentValues();
		
		values.put(TaskEntry.COLUMN_NAME_CATEGORY, task.getCategory());
		values.put(TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
		values.put(TaskEntry.COLUMN_NAME_LOCATION_LAT, task.getLatitude());
		values.put(TaskEntry.COLUMN_NAME_LOCATION_LNG, task.getLongitude());
		values.put(TaskEntry.COLUMN_NAME_ADDRESS, task.getAddress());
		values.put(TaskEntry.COLUMN_NAME_PRIORITY, task.getPriority());
		values.put(TaskEntry.COLUMN_NAME_IS_ACTIVE, (task.isActive() ? 1 : 0));
		values.put(TaskEntry.COLUMN_NAME_TEMP_START, task.getTempStart());
		values.put(TaskEntry.COLUMN_NAME_TIME_START, task.getTimeStarted());
		values.put(TaskEntry.COLUMN_NAME_TIME_END, task.getTimeEnded());
		values.put(TaskEntry.COLUMN_NAME_TIME_SPENT, task.getTimeSpent());
		values.put(TaskEntry.COLUMN_NAME_IS_FINISHED, 
				(task.isFinished() ? 1 : 0));
		
		return values;
	}
	
	
	
	//*************** RETRIEVAL QUERIES ***************
	
	private Cursor fetchListById(int listId) {
		Cursor c1 = mDb.query(ListEntry.TABLE_NAME, null, 
				ListEntry._ID + " = ?", 
				new String[] { Integer.toString(listId) }, null, null, null);
		return c1;
	}
	
	
	
	private Cursor fetchListByDate(String date) {
		// Get the list with the specific date.
		Cursor c1 = mDb.query(ListEntry.TABLE_NAME, null,
				ListEntry.COLUMN_NAME_DATE + " = ?", 
				new String[] { date }, null, null, null);

		return c1;
	}
	
	
	
	public long getListIdByDate(String date) {
		open();
		
		long listId = -1;
		Cursor c1 = fetchListByDate(date);
		if (c1.moveToFirst()) {
			listId = c1.getLong(c1.getColumnIndexOrThrow(ListEntry._ID));
		}
		
		close();
		return listId;
	}
	
	
	
	public ArrayList<String> getListDates() {
		ArrayList<String> dates = new ArrayList<String>();
		open();
		
		Cursor c1 = mDb.query(ListEntry.TABLE_NAME, null, null, 
				null, null, null, null);
		if (c1.moveToFirst()) {
			do {
				dates.add(c1.getString(c1.getColumnIndexOrThrow(
						ListEntry.COLUMN_NAME_DATE)));
			} while (c1.moveToNext());
		}
		
		close();
		return dates;
	}
	
	
	
	public ArrayList<Task> getTasksByDate(String date) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		open();
		
		Cursor c1 = fetchListByDate(date);
		if (c1.moveToFirst()) {
			
			String listId = c1.getString(c1.getColumnIndexOrThrow(
					ListEntry._ID));
			c1 = mDb.query(TaskEntry.TABLE_NAME, null, 
					TaskEntry.COLUMN_NAME_LIST + " = ?", 
					new String[] { listId }, null, null, null);
			
			if (c1.moveToFirst()) {
				
				Task task;
				do {
					task = getTaskFromCursor(c1, true);
					task.setDate(date);
					tasks.add(task);
				} while (c1.moveToNext());
			}
		}
		
		close();
		return tasks;
	}
	
	
	
	public ArrayList<Task> getTasks() {
		ArrayList<Task> tasks = new ArrayList<Task>();
		open();
		
		Cursor c1 = mDb.query(TaskEntry.TABLE_NAME, null, null,
				null, null, null, null);
		
		if (c1.moveToFirst()) {
			
			Task task;
			do {
				task = getTaskFromCursor(c1, false);
				tasks.add(task);
			} while (c1.moveToNext());
		}
		
		close();
		return tasks;
	}
	
	
	
	private Cursor fetchTimesByTaskId(int taskId) {
		Cursor c1 = mDb.query(TimesEntry.TABLE_NAME, null,
				TimesEntry.COLUMN_NAME_TASK_ID + " = ?", 
				new String[] { Integer.toString(taskId) }, null, null, null);
		return c1;
	}
	
	
	
	//*************** INSERTION QUERIES ***************
	
	public long insertList(String date) {
		open();
		
		ContentValues values = new ContentValues();
		values.put(ListEntry.COLUMN_NAME_DATE, date);
		long listId = mDb.insert(ListEntry.TABLE_NAME, null, values);
		
		close();
		return listId; 
	}
	
	
	
	public long insertTask(long listId, Task task) {
		open();
		
		ContentValues values = getContentCaluesFromTask(task);
		values.put(TaskEntry.COLUMN_NAME_LIST, listId);
		
		long taskId = mDb.insert(TaskEntry.TABLE_NAME, null, values);
		
		if (! task.getFixedStart().isEmpty()) {
			insertTimes(taskId, task.getFixedStart(), task.getFixedEnd());
		}
		
		close();
		return taskId;
	}
	
	
	
	private long insertTimes(long taskId, String start, String end) {
		ContentValues values = new ContentValues();
		values.put(TimesEntry.COLUMN_NAME_TASK_ID, taskId);
		values.put(TimesEntry.COLUMN_NAME_START_TIME, start);
		values.put(TimesEntry.COLUMN_NAME_END_TIME, end);
		return mDb.insert(TimesEntry.TABLE_NAME, null, values);
	}
	
	
	
	//*************** DELETION QUERIES ***************
	
	public boolean deleteTaskById(int taskId) {
		open();
		
		// Get the list id of the task.
		Cursor c1 = mDb.query(TaskEntry.TABLE_NAME, null, 
				TaskEntry._ID + " = ? ", 
				new String[] { Integer.toString(taskId) }, null, null, null);
		if (c1.moveToFirst()) {
			// Get all other tasks in the same list.
			long listId = c1.getLong(c1.getColumnIndexOrThrow(
					TaskEntry.COLUMN_NAME_LIST));
			c1 = mDb.query(TaskEntry.TABLE_NAME, null, 
					TaskEntry.COLUMN_NAME_LIST + " = ? ", 
					new String[] { Long.toString(listId) }, null, null, null);
			// If the task is the last in a list, also delete the list.
			if (c1.getCount() == 1) {
				mDb.delete(ListEntry.TABLE_NAME, 
						ListEntry._ID + " = ?", 
						new String[] { Long.toString(listId) } );
			}
		}
		
		deleteTimes(taskId);
		
		// TODO delete contexts also???
		
		int result = mDb.delete(TaskEntry.TABLE_NAME,
				TaskEntry._ID + " = ? ",
				new String[] { Integer.toString(taskId) });
		
		close();
		return result > 0 ? true : false;
	}
	
	
	
	private boolean deleteTimes(int taskId) {
		return mDb.delete(TimesEntry.TABLE_NAME, 
				TimesEntry.COLUMN_NAME_TASK_ID + " = ? ", 
				new String[] { Integer.toString(taskId) }) > 0 ? true : false;
	}
	
	
	
	
	//*************** UPDATE QUERIES ***************
	
	public boolean updateTask(Task task) {
		open();
		
		ContentValues values = getContentCaluesFromTask(task);
		int result = mDb.update(TaskEntry.TABLE_NAME, values,
				TaskEntry._ID + " = ?", 
				new String[] { Integer.toString(task.getId()) });
		
		if (! task.getFixedStart().isEmpty()) {
			values.clear();
			values.put(TimesEntry.COLUMN_NAME_START_TIME, 
					task.getFixedStart());
			values.put(TimesEntry.COLUMN_NAME_END_TIME, task.getFixedEnd());
			
			Cursor c = fetchTimesByTaskId(task.getId());
			if (c.moveToFirst()) {
				mDb.update(TimesEntry.TABLE_NAME, values,
						TimesEntry.COLUMN_NAME_TASK_ID + " = ?", 
						new String[] { Integer.toString(task.getId()) });
			} else {
				values.put(TimesEntry.COLUMN_NAME_TASK_ID, task.getId());
				mDb.insert(TimesEntry.TABLE_NAME, null, values);
			}
			
		} else {
			deleteTimes(task.getId());
		}
		
		close();
		return result > 0 ? true : false;
	}
}
