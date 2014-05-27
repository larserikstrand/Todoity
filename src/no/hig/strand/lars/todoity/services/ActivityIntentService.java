package no.hig.strand.lars.todoity.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityIntentService extends IntentService {

	//private SparseArray<List<String>> mActivities;
	
	public ActivityIntentService() {
		super("ActivityIntentService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = 
					ActivityRecognitionResult.extractResult(intent);
			
			DetectedActivity mostProbableActivity = 
					result.getMostProbableActivity();
			//int confidence = mostProbableActivity.getConfidence();
			int activityType = mostProbableActivity.getType();
			String activityName = getNameFromType(activityType);
			
			Log.d("ACTIVITYINTENTSERVICE", "Activity: " + activityName);
		}
	}
	
	private String getNameFromType(int activityType) {
		switch (activityType) {
		case DetectedActivity.IN_VEHICLE:
			return "in vehicle";
		case DetectedActivity.ON_BICYCLE:
			return "on bicycle";
		case DetectedActivity.ON_FOOT:
			return "on foot";
		case DetectedActivity.STILL:
			return "still";
		case DetectedActivity.TILTING:
			return "tilting";
		case DetectedActivity.UNKNOWN:
			return "unknown";
		}
		return "unknown";
	}
}
