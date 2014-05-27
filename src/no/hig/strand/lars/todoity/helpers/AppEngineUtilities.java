package no.hig.strand.lars.todoity.helpers;

import java.io.IOException;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.contextentityendpoint.Contextentityendpoint;
import no.hig.strand.lars.todoity.contextentityendpoint.model.ContextEntity;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.TaskContext;
import no.hig.strand.lars.todoity.taskentityendpoint.Taskentityendpoint;
import no.hig.strand.lars.todoity.taskentityendpoint.model.TaskEntity;
import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson.JacksonFactory;

public final class AppEngineUtilities {

	public AppEngineUtilities() {}
	
	public static class SaveTask extends AsyncTask<Void, Void, Void> {
		Task task;
		Context context;
		
		public SaveTask(Context context, Task task) {
			this.task = task;
			this.context = context;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// Save task externally to AppEngine
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			endpointBuilder.setApplicationName(
					context.getString(R.string.app_name));
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			
			TaskEntity taskEntity = getTaskEntityFromTask(task);
			String id = Installation.id(context) + " " + task.getId();
			taskEntity.setId(id);
			try {
				taskEntity = endpoint.insertTaskEntity(taskEntity).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
	}
	
	
	
	public static class GetTask extends AsyncTask<String, Void, TaskEntity> {

		@Override
		protected TaskEntity doInBackground(String... params) {
			String taskId = params[0];
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			try {
				TaskEntity task = endpoint.getTaskEntity(taskId).execute();
				return task;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	
	
	/**
	 * Updates a task in Google AppEngine. The task to be updated is passed
	 * as a variable the constructor.
	 * @author LarsErik
	 *
	 */
	public static class UpdateTask extends AsyncTask<Void, Void, Void> {
		Task task;
		Context context;
		
		public UpdateTask(Context context, Task task) {
			this.task = task;
			this.context = context;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			endpointBuilder.setApplicationName(
					context.getString(R.string.app_name));
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			
			String id = Installation.id(context) + " " + task.getId();
			TaskEntity taskEntity = getTaskEntityFromTask(task);
			taskEntity.setId(id);
			
			try {
				endpoint.updateTaskEntity(taskEntity).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	
	public static class RemoveTask extends AsyncTask<Void, Void, Void> {
		Task task;
		Context context;
		
		public RemoveTask(Context context, Task task) {
			this.task = task;
			this.context = context;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			String id = Installation.id(context) + " " + task.getId();
			Taskentityendpoint.Builder endpointBuilder = 
					new Taskentityendpoint.Builder(
							AndroidHttp.newCompatibleTransport(), 
							new JacksonFactory(), null);
			endpointBuilder.setApplicationName(
					context.getString(R.string.app_name));
			Taskentityendpoint endpoint = CloudEndpointUtils
					.updateBuilder(endpointBuilder).build();
			try {
				endpoint.removeTaskEntity(id).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	
	public static class SaveContextTask extends AsyncTask<Void, Void, Void> {
		Context context;
		TaskContext taskContext;
		
		public SaveContextTask(Context context, TaskContext taskContext) {
			this.context = context;
			this.taskContext = taskContext;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			saveContext(context, taskContext);
			
			return null;
		}
	}
	
	
	
	public static void saveContext(Context context, TaskContext taskContext) {
		// Save context externally to AppEngine
		Contextentityendpoint.Builder endpointBuilder =
				new Contextentityendpoint.Builder(
						AndroidHttp.newCompatibleTransport(),
						new JacksonFactory(), null);
		endpointBuilder.setApplicationName(
				context.getString(R.string.app_name));
		Contextentityendpoint endpoint = CloudEndpointUtils
				.updateBuilder(endpointBuilder).build();
					
		ContextEntity contextEntity = new ContextEntity();
		contextEntity.setTaskId(
				Installation.id(context) + " " + taskContext.getTaskId());
		contextEntity.setType(taskContext.getType());
		contextEntity.setContext(taskContext.getContext());
		contextEntity.setDetails(taskContext.getDetails());
		try {
			endpoint.insertContextEntity(contextEntity).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private static TaskEntity getTaskEntityFromTask(Task task) {
		TaskEntity taskEntity = new TaskEntity();
		
		taskEntity.setDate(task.getDate())
		.setCategory(task.getCategory())
		.setDescription(task.getDescription())
		.setLatitude(task.getLatitude())
		.setLongitude(task.getLongitude())
		.setAddress(task.getAddress())
		.setActive(task.isActive())
		.setTimeStarted(Utilities.millisToDate(task.getTimeStarted()))
		.setTimeEnded(Utilities.millisToDate(task.getTimeEnded()))
		.setTimeSpent(task.getTimeSpent())
		.setFinished(task.isFinished())
		.setFixedStart(task.getFixedStart())
		.setFixedEnd(task.getFixedEnd());
		
		return taskEntity;
	}
	
}
