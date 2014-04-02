package no.hig.strand.lars.todoity.helpers;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.TasksDatabase;
import android.content.Context;
import android.os.AsyncTask;

public final class DatabaseUtilities {

	
	public DatabaseUtilities() {}
	
	
	
	public static class SaveTask extends AsyncTask<Void, Void, Void> {
		private TasksDatabase tasksDb;
		private Task task;
		private Context context;
		
		public SaveTask(Context context, Task task) {
			this.task = task;
			this.context = context;
			tasksDb = TasksDatabase.getInstance(context);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// Check if the list exists. If not, insert it.
			long listId = tasksDb.getListIdByDate(task.getDate());
			if (listId < 0) {
				listId = tasksDb.insertList(task.getDate());
			}
			long taskId = tasksDb.insertTask(listId, task);
			task.setId((int) taskId);
			
			// TODO new AppEngineUtilities.SaveTask(context, task).execute();
			
			return null;
		}
	}
	
	
	
	public static class DeleteTask extends AsyncTask<Void, Void, Void> {
		TasksDatabase tasksDb;
		Task task;
		private Context context;
		
		public DeleteTask(Context context, Task task) {
			this.task = task;
			this.context = context;
			tasksDb = TasksDatabase.getInstance(context);
		}

		@Override
		protected Void doInBackground(Void... params) {
			tasksDb.deleteTaskById(task.getId());
			// TODO new AppEngineUtilities.DeleteTask(context, task).execute();
			
			return null;
		}
	}
	
	
	
	public static class UpdateTask extends AsyncTask<Void, Void, Void> {
		private TasksDatabase tasksDb;
		private Task task;
		
		public UpdateTask(Context context, Task task) {
			this.task = task;
			tasksDb = TasksDatabase.getInstance(context);
		}

		@Override
		protected Void doInBackground(Void... params) {
			tasksDb.updateTask(task);
			
			// TODO AppEngine call.
			
			return null;
		}
	}
	
	
	
	public static class MoveTask extends AsyncTask<Void, Void, Void> {
		private TasksDatabase tasksDb;
		private Task task;
		private String date;

		public MoveTask(Context context, Task task, String date) {
			this.task = task;
			tasksDb = TasksDatabase.getInstance(context);
			this.date = date;
		}

		@Override
		protected Void doInBackground(Void... params) {			
			// Move the task to the selected date. 
			//  Create list on that date if none exist.
			long listId = tasksDb.getListIdByDate(date);
			if (listId < 0) {
				listId = tasksDb.insertList(date);
			} 
			
			tasksDb.moveTaskToList(task.getId(), listId);
			
			// TODO AppEngine something...
			
			return null;
		}
	}
	
	
	
	// *************** Class for loading tasks from database ***************
	
	public interface OnTasksLoadedListener {
		public void onTasksLoaded(ArrayList<Task> tasks);
	}
	
	
	
	public static class GetTasksByDateTask extends 
			AsyncTask<Void, Void, ArrayList<Task>> {

		private OnTasksLoadedListener callback;
		private TasksDatabase tasksDb;
		private String date;
		
		public GetTasksByDateTask(Context context, String date) {
			try {
				this.callback = (OnTasksLoadedListener) context;
			} catch (ClassCastException e) {
				throw new ClassCastException(context.toString() + 
						" must implement OnDateSetListener");
			}
			tasksDb = TasksDatabase.getInstance(context);
			this.date = date;
		}
		
		@Override
		protected ArrayList<Task> doInBackground(Void... params) {
			ArrayList<Task> tasks = tasksDb.getTasksByDate(date);
			return tasks;
		}

		@Override
		protected void onPostExecute(ArrayList<Task> result) {
			callback.onTasksLoaded(result);
		}
	}
	
}
