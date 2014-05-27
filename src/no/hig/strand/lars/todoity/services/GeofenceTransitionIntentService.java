package no.hig.strand.lars.todoity.services;

import java.util.ArrayList;
import java.util.List;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.R.drawable;
import no.hig.strand.lars.todoity.R.string;
import no.hig.strand.lars.todoity.activities.MainActivity;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.TasksDatabase;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class GeofenceTransitionIntentService extends IntentService {

	private TasksDatabase mTasksDb;
	
	public GeofenceTransitionIntentService() {
		super("GeofenceTransitionIntentService");
		init();
	}
	
	
	
	public GeofenceTransitionIntentService(String name) {
		super(name);
		init();
	}
	
	
	
	private void init() {
		mTasksDb = TasksDatabase.getInstance(this);
	}

	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (LocationClient.hasError(intent)) {
			int errorCode = LocationClient.getErrorCode(intent);
			Log.e(getClass().getName(), "Location Services error: " +
					Integer.toString(errorCode));
			
		} else {
			int transitionType = LocationClient.getGeofenceTransition(intent);
			if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
				
				List<Geofence> geofenceList = LocationClient.getTriggeringGeofences(intent);
				ArrayList<String> triggerIds = new ArrayList<String>();
				
				for (Geofence geofence : geofenceList) {
					triggerIds.add(geofence.getRequestId());
				}
				pushNotification(triggerIds);
			} else {
				Log.e(getClass().getName(), "Geofence transition error: " + 
						Integer.toString(transitionType));
			}
		}
	}
	
	
	
	private void pushNotification(ArrayList<String> triggerIds) {
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
				Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = 
				PendingIntent.getActivity(this, 0, intent, 0);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.app_name));
		if (triggerIds.size() > 1) {
			builder.setContentText(getString(
					R.string.geofenceservice_multiple_entered));
		} else {
			Task task = mTasksDb.getTaskById(triggerIds.get(0));
			builder.setContentText(
					getString(R.string.geofenceservice_single_entered1) + " " +
					Task.getTaskTextFromTask(task) + 
					getString(R.string.geofenceservice_single_entered2));
		}
		builder.setSmallIcon(R.drawable.ic_notification_todoity);
		builder.setContentIntent(pendingIntent);
		
		// Display notification.
		final Notification note = builder.build();
		note.flags |= Notification.FLAG_AUTO_CANCEL;
		NotificationManager notificationManager = (NotificationManager) 
				getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(Constant.GEOFENCE_NOTIFICATION_ID, note);
		
		// Notification sound.
		try {
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
