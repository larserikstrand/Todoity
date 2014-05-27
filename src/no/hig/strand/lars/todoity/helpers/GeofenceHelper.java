package no.hig.strand.lars.todoity.helpers;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.TasksDatabase;
import no.hig.strand.lars.todoity.services.GeofenceTransitionIntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;


public final class GeofenceHelper  {
	
	public GeofenceHelper() {}
	
	
	public static void updateGeofences(Context context) {
		new UpdateGeofences(context).execute();
	}

	
	
	public static void addGeofence(Context context, Task task) {
		long now = Utilities.dateToMillis(Utilities.getTodayDate());
		long taskTime = Utilities.dateToMillis(task.getDate());
		long diff = taskTime - now;
		if (diff > 0 && diff < Constant.GEOFENCE_DURATION) {
			new UpdateGeofences(context).execute();
		}
	}
	
	
	
	private static class UpdateGeofences extends AsyncTask<Void, Void, Void> 
			implements ConnectionCallbacks, OnConnectionFailedListener,
			OnAddGeofencesResultListener {
		
		private Context mContext;
		
		private TasksDatabase mTasksDb;
		
		private ArrayList<Task> mGeofencedTasks;
		private ArrayList<Geofence> mGeofences;
		
		private LocationClient mLocationClient;
		private boolean mInProgress;
		
		private PendingIntent mTransitionPendingIntent;
		
		
		public UpdateGeofences(Context context) {
			mContext = context;
			mTasksDb = TasksDatabase.getInstance(mContext);
			mLocationClient = new LocationClient(mContext, this, this);
			mInProgress = false;
			mTransitionPendingIntent = PendingIntent.getService(mContext, 0,
					new Intent(mContext, GeofenceTransitionIntentService.class),
					PendingIntent.FLAG_UPDATE_CURRENT);
		}
		
		
		@Override
		protected Void doInBackground(Void... params) {
			if (! mInProgress) {
				mInProgress = true;
				mLocationClient.connect();
				
				ArrayList<String> dates = Utilities
						.getDates(Constant.NUMBER_OF_GEOFENCE_DAYS);
				for (String date : dates) {
					ArrayList<Task> tasks = mTasksDb.getTasksByDate(date);
				}
				
				while (! mLocationClient.isConnected()) {}
			}
			
			return null;
		}

		@Override
		public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
			if (statusCode == LocationStatusCodes.SUCCESS) {
				// All is well, do nothing.
			} else {
				
			}
			
			mInProgress = false;
			mLocationClient.disconnect();
		}

		

		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			// TODO Auto-generated method stub
			
		}

		

		@Override
		public void onConnected(Bundle dataBundle) {
			mLocationClient.addGeofences(
					mGeofences, mTransitionPendingIntent, this);
		}

		

		@Override
		public void onDisconnected() {
			mInProgress = false;
		}
		
	}

}
