package no.hig.strand.lars.todoity.data;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.data.TaskContract.ContextEntry;
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
		open();
	}
	
	
	
	private void open() {
		mDb = mDbHelper.getWritableDatabase();
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
	
	
	
	private long getListIfLastTask(int taskId) {
		Cursor c1 = mDb.query(TaskEntry.TABLE_NAME, null, 
				TaskEntry._ID + " = ? ", 
				new String[] { Integer.toString(taskId) },
				null, null, null);
		if (c1.moveToFirst()) {
			long list = c1.getLong(c1.getColumnIndexOrThrow(
					TaskEntry.COLUMN_NAME_LIST));
			c1 = mDb.query(TaskEntry.TABLE_NAME, null, 
					TaskEntry.COLUMN_NAME_LIST + " = ?", 
					new String[] { Long.toString(list) },
					null, null, null);
			if (c1.getCount() == 1) {
				return list;
			}
		}
		
		return -1;
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
		long listId = -1;
		Cursor c1 = fetchListByDate(date);
		if (c1.moveToFirst()) {
			listId = c1.getLong(c1.getColumnIndexOrThrow(ListEntry._ID));
		}
		
		return listId;
	}
	
	
	
	public ArrayList<String> getListDates() {
		ArrayList<String> dates = new ArrayList<String>();
		
		Cursor c1 = mDb.query(ListEntry.TABLE_NAME, null, null, 
				null, null, null, null);
		if (c1.moveToFirst()) {
			do {
				dates.add(c1.getString(c1.getColumnIndexOrThrow(
						ListEntry.COLUMN_NAME_DATE)));
			} while (c1.moveToNext());
		}
		
		return dates;
	}
	
	
	
	public ArrayList<Task> getTasksByDate(String date) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		
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
		
		return tasks;
	}
	
	
	
	public ArrayList<Task> getTasks() {
		ArrayList<Task> tasks = new ArrayList<Task>();
		
		Cursor c1 = mDb.query(TaskEntry.TABLE_NAME, null, null,
				null, null, null, null);
		
		if (c1.moveToFirst()) {
			Task task;
			do {
				task = getTaskFromCursor(c1, false);
				tasks.add(task);
			} while (c1.moveToNext());
		}
		
		return tasks;
	}
	
	
	
	public ArrayList<Task> getActiveTasks() {
		ArrayList<Task> tasks = new ArrayList<Task>();
		
		Cursor c1 = mDb.query(TaskEntry.TABLE_NAME, null, 
				TaskEntry.COLUMN_NAME_IS_ACTIVE + " = ? ",
				new String[] { "1" },
				null, null, null);
		
		if (c1.moveToFirst()) {
			Task task;
			do {
				task = getTaskFromCursor(c1, false);
				tasks.add(task);
			} while (c1.moveToNext());
		}
		
		return tasks;
	}
	
	
	
	public ArrayList<Task> getTaskHistory() {
		ArrayList<Task> tasks = new ArrayList<Task>();
		
		Cursor c1 = mDb.query(TaskEntry.TABLE_NAME, null, 
				TaskEntry.COLUMN_NAME_IS_FINISHED + " = ? ",
				new String[] { "1" },
				null, null, null);
		
		if (c1.moveToFirst()) {
			Task task;
			do {
				task = getTaskFromCursor(c1, false);
				tasks.add(task);
			} while (c1.moveToNext());
		}
		
		return tasks;
	}
	
	
	
	public Task getTaskById(String taskId) {
		Cursor c1 = mDb.query(TaskEntry.TABLE_NAME, null, 
				TaskEntry._ID + " = ? ",
				new String[] { taskId }, null, null, null);
		Task task = getTaskFromCursor(c1, false);
		
		return task;
	}
	
	
	
	private Cursor fetchTimesByTaskId(int taskId) {
		Cursor c1 = mDb.query(TimesEntry.TABLE_NAME, null,
				TimesEntry.COLUMN_NAME_TASK_ID + " = ?", 
				new String[] { Integer.toString(taskId) }, null, null, null);
		return c1;
	}
	
	
	
	public ArrayList<TaskContext> getContextByTaskId(int taskId, String type) {
		ArrayList<TaskContext> contexts = new ArrayList<TaskContext>();
		
		Cursor c1 = mDb.query(ContextEntry.TABLE_NAME, null, 
				ContextEntry.COLUMN_NAME_TASK + " = ? AND " + 
				ContextEntry.COLUMN_NAME_TYPE + " = ? ", 
				new String[] { Integer.toString(taskId), type },
				null, null, null);
		
		if (c1.moveToFirst()) {
			do {
				TaskContext taskContext = new TaskContext();
				taskContext.setTaskId(taskId);
				taskContext.setType(type);
				String context = c1.getString(c1.getColumnIndexOrThrow(
						ContextEntry.COLUMN_NAME_CONTEXT));
				taskContext.setContext(context);
				String details = c1.getString(c1.getColumnIndexOrThrow(
						ContextEntry.COLUMN_NAME_DETAILS));
				taskContext.setDetails(details);
				
				contexts.add(taskContext);
			} while (c1.moveToNext());
		}
		
		return contexts;
	}
	
	
	
	//*************** INSERTION QUERIES ***************
	
	public long insertList(String date) {
		ContentValues values = new ContentValues();
		values.put(ListEntry.COLUMN_NAME_DATE, date);
		long listId = mDb.insert(ListEntry.TABLE_NAME, null, values);
		
		return listId; 
	}
	
	
	
	public long insertTask(long listId, Task task) {
		ContentValues values = getContentCaluesFromTask(task);
		values.put(TaskEntry.COLUMN_NAME_LIST, listId);
		
		long taskId = mDb.insert(TaskEntry.TABLE_NAME, null, values);
		
		if (! task.getFixedStart().isEmpty()) {
			insertTimes(taskId, task.getFixedStart(), task.getFixedEnd());
		}
		
		return taskId;
	}
	
	
	
	private long insertTimes(long taskId, String start, String end) {
		ContentValues values = new ContentValues();
		values.put(TimesEntry.COLUMN_NAME_TASK_ID, taskId);
		values.put(TimesEntry.COLUMN_NAME_START_TIME, start);
		values.put(TimesEntry.COLUMN_NAME_END_TIME, end);
		return mDb.insert(TimesEntry.TABLE_NAME, null, values);
	}
	
	
	
	public long insertContext(TaskContext context) {
		ContentValues values = new ContentValues();
		values.put(ContextEntry.COLUMN_NAME_TASK, context.getTaskId());
		values.put(ContextEntry.COLUMN_NAME_TYPE, context.getType());
		values.put(ContextEntry.COLUMN_NAME_CONTEXT, context.getContext());
		values.put(ContextEntry.COLUMN_NAME_DETAILS, context.getDetails());
		long id = mDb.insert(ContextEntry.TABLE_NAME, null, values);
		
		return id;
	}
	
	
	
	//*************** DELETION QUERIES ***************
	
	public boolean deleteTaskById(int taskId) {
		long listId = getListIfLastTask(taskId);
		if (listId != -1) {
			mDb.delete(ListEntry.TABLE_NAME, 
					ListEntry._ID + " = ?", 
					new String[] { Long.toString(listId) } );
		}
		
		deleteTimes(taskId);
		
		// TODO delete contexts also???
		
		int result = mDb.delete(TaskEntry.TABLE_NAME,
				TaskEntry._ID + " = ? ",
				new String[] { Integer.toString(taskId) });
		
		return result > 0 ? true : false;
	}
	
	
	
	private boolean deleteTimes(int taskId) {
		return mDb.delete(TimesEntry.TABLE_NAME, 
				TimesEntry.COLUMN_NAME_TASK_ID + " = ? ", 
				new String[] { Integer.toString(taskId) }) > 0 ? true : false;
	}
	
	
	
	
	//*************** UPDATE QUERIES ***************
	
	public boolean updateTask(Task task) {
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
		
		return result > 0 ? true : false;
	}
	
	
	
	public boolean moveTaskToList(int taskId, long toListId) {
		long fromListId = getListIfLastTask(taskId);
		if (fromListId != -1) {
			mDb.delete(ListEntry.TABLE_NAME, 
					ListEntry._ID + " = ?", 
					new String[] { Long.toString(fromListId) } );
		}
		ContentValues values = new ContentValues();
		values.put(TaskEntry.COLUMN_NAME_LIST, toListId);
		int result = mDb.update(TaskEntry.TABLE_NAME, values,
				TaskEntry._ID + " = ?", 
				new String[] { Integer.toString(taskId) });
		
		return result > 0 ? true : false;
	}
}
