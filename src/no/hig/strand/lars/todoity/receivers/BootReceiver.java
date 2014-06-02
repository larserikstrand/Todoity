package no.hig.strand.lars.todoity.receivers;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.TasksDatabase;
import no.hig.strand.lars.todoity.services.ContextService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	
	private TasksDatabase mTasksDb;

	@Override
	public void onReceive(Context context, Intent intent) {
		// Check if there are active tasks. If so, start context service.
		mTasksDb = TasksDatabase.getInstance(context);
		ArrayList<Task> tasks = mTasksDb.getActiveTasks();
		
		if (! tasks.isEmpty()) {
			Intent contextServiceIntent = 
					new Intent(context, ContextService.class);
			context.startService(contextServiceIntent);
		}
				
	}
}
