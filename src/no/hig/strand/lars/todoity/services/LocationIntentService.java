package no.hig.strand.lars.todoity.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.TaskContext;
import no.hig.strand.lars.todoity.data.TaskContract.ContextEntry;
import no.hig.strand.lars.todoity.data.TasksDatabase;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities;
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.SparseArray;

import com.google.android.gms.location.LocationClient;

public class LocationIntentService extends IntentService {
	
	private Location mLocation;
	private ArrayList<Task> mActiveTasks;
	private SparseArray<List<TaskContext>> mContexts;
	private TasksDatabase mTasksDb;
	
	public LocationIntentService() {
		super("LocationIntentService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mTasksDb = TasksDatabase.getInstance(this);
		mActiveTasks = mTasksDb.getActiveTasks();
		mContexts = new SparseArray<List<TaskContext>>();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mLocation = (Location) intent.getParcelableExtra(
				LocationClient.KEY_LOCATION_CHANGED);
		if (mLocation != null) {
			// For each task, get the previously stored contexts (if any).
			for (Task task : mActiveTasks) {
				
				ArrayList<TaskContext> contexts = mTasksDb.getContextByTaskId(
						task.getId(), ContextEntry.TYPE_LOCATION);
				mContexts.put(task.getId(), contexts);
			}
			
			String detail = mLocation.getLatitude() + " " + 
					        mLocation.getLongitude();
			
			// Get the address of the new location.
			Geocoder geocoder = new Geocoder(this);
			List<Address> addresses = null;
			try {
				addresses = geocoder.getFromLocation(
						mLocation.getLatitude(), mLocation.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				String addressText = "";
				for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
					addressText += address.getAddressLine(i);
					if (i < address.getMaxAddressLineIndex() -1) {
						addressText += ", ";
					}
				}
				
				// Check if this address has been stored for each task before.
				for (Task task : mActiveTasks) {
					boolean newLocation = true;
					for (TaskContext taskContext : mContexts.get(task.getId())) {
						if (addressText.equals(taskContext.getContext())) {
							newLocation = false;
						}
						String details = taskContext.getDetails();
						String[] data = details.split("\\s+");
						double latitude = Double.valueOf(data[0]);
						double longitude = Double.valueOf(data[1]);
						if (latitude == mLocation.getLatitude() && 
								longitude == mLocation.getLongitude()) {
							newLocation = false;
						}
					}
					
					// New location context for this task, save to db.
					if (newLocation) {
						TaskContext taskContext = new TaskContext();
						taskContext.setTaskId(task.getId());
						taskContext.setType(ContextEntry.TYPE_LOCATION);
						taskContext.setContext(addressText);
						taskContext.setDetails(detail);
						DatabaseUtilities.saveContext(this, taskContext);
					}
				}
			}
		}
	}
}
