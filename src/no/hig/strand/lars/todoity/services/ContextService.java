package no.hig.strand.lars.todoity.services;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.MainActivity;
import no.hig.strand.lars.todoity.data.Constant;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class ContextService extends Service implements ConnectionCallbacks, OnConnectionFailedListener {
	
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	private PendingIntent mLocationPendingIntent;
	private boolean mLocationInProgress;
	
	private ActivityRecognitionClient mActivityRecognitionClient;
	private PendingIntent mActivityPendingIntent;
	private boolean mActivityInProgress;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mLocationClient = new LocationClient(this, this, this);
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(
				LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(Constant.LOCATION_UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(Constant.FASTEST_LOCATION_INTERVAL);
		mLocationPendingIntent = PendingIntent.getService(
				this, 0, new Intent(this, LocationIntentService.class), 
				PendingIntent.FLAG_UPDATE_CURRENT);
		mLocationInProgress = false;
		
		mActivityRecognitionClient = 
				new ActivityRecognitionClient(this, this, this);
		mActivityPendingIntent = PendingIntent.getService(
				this, 0, new Intent(this, ActivityIntentService.class), 
				PendingIntent.FLAG_UPDATE_CURRENT);
		mActivityInProgress = false;
	}



	@Override
	public void onDestroy() {
		if (mLocationClient != null) {
			mLocationClient.removeLocationUpdates(mLocationPendingIntent);
			mLocationClient.disconnect();
		}
		if (mActivityRecognitionClient != null) {
			mActivityRecognitionClient.removeActivityUpdates(
					mActivityPendingIntent);
			mActivityRecognitionClient.disconnect();
		}
		
		super.onDestroy();
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		startServiceInForeground();
		
		if (! mLocationClient.isConnecting() && 
				! mLocationClient.isConnected()) {
			mLocationClient.connect();
		}
		if (! mActivityRecognitionClient.isConnecting() && 
				! mActivityRecognitionClient.isConnected()) {
			mActivityRecognitionClient.connect();
		}
		
		return START_REDELIVER_INTENT;
	}


	
	private void startServiceInForeground() {
		Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
        		Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
        		this, 0, intent, 0);
        
        final Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(
                		getString(R.string.contextservice_notification_title))
                .setContentText(
                		getString(R.string.contextservice_notification_message))
                .setSmallIcon(R.drawable.animation_context_service)
                .setContentIntent(pendingIntent)
                .build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        
        startForeground(Constant.CONTEXT_NOTIFICATION_ID, notification);
	}



	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {}



	@Override
	public void onConnected(Bundle bundle) {
		if (mLocationClient.isConnected() && ! mLocationInProgress) {
			mLocationClient.requestLocationUpdates(
					mLocationRequest, mLocationPendingIntent);
			mLocationInProgress = true;
		}
		if (mActivityRecognitionClient.isConnected() && ! mActivityInProgress) {
			mActivityRecognitionClient.requestActivityUpdates(
					Constant.ACTIVITY_DETECTION_INTERVAL, mActivityPendingIntent);
			mActivityInProgress = true;
		}
	}



	@Override
	public void onDisconnected() {
		mLocationInProgress = false;
		mActivityInProgress = false;
	}
	
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
