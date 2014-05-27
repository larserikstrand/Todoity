package no.hig.strand.lars.todoity.data;

public class Constant {
	
	
	// *************** App requests ***************
	
	public final static int NEW_LIST_REQUEST = 1;
	public final static int EDIT_LIST_REQUEST = 2;
	public final static int NEW_TASK_REQUEST = 3;
	public final static int EDIT_TASK_REQUEST = 4;
	public final static int MAP_REQUEST = 5;
	public final static int MOVE_TASK_REQUEST = 6;
	
	
	// *************** String keys passed between activities ***************
	
	public final static String TASK_EXTRA = "no.hig.strand.lars.todoity.TASK";
	public final static String TASKS_EXTRA = "no.hig.strand.lars.todoity.TASKS";
	public final static String DATE_EXTRA  = "no.hig.strand.lars.todoity.DATE";
	public final static String LOCATION_EXTRA = 
			"no.hig.strand.lars.todoity.LOCATION";
	public final static String POSITION_EXTRA = 
			"no.hig.strand.lars.todoity.POSITION";
	public final static String DATEPICKER_TITLE_EXTRA = 
			"no.hig.strand.lars.todoity.DATEPICKER_TITLE";
	
	
	// *************** Miscellaneous ***************
	
	// The date and time formats used in the app.
	public final static String DATE_FORMAT = "EEE, MMM dd, yyyy";
	public final static String TIME_FORMAT = "HH:mm";
	
	// Request code for failure in connecting to Google Play Services.
	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	// The number of days (including 'today') to display tasks for in
	//  the Week list.
	public final static int TASKS_TO_DISPLAY_IN_WEEK = 7;
	
	// The minimum amount of time in milliseconds before a task is considered 
	//  as actually started. (We don't want it to count as started when the
	//  user just randomly starts and stops the task.)
	public final static int MIN_TIME_TASK_START = 1000 * 10;
	
	
	// *************** Context Service ***************
	
	// Polling interval for location updates (every five minutes).
	public final static long LOCATION_UPDATE_INTERVAL = 1000 * 60 * 5;
	// Fastest polling interval for location (every minute).
	public final static long FASTEST_LOCATION_INTERVAL = 1000 * 60 * 1;
	// Polling interval for detecting user activity (every two minutes).
	public final static long ACTIVITY_DETECTION_INTERVAL = 1000 * 60 * 2;

	// Notification to display when currently collecting context.
	public final static int CONTEXT_NOTIFICATION_ID = 4723;
	
	
	// *************** Recommender ***************
	
	// The maximum distance between current location and the context location
	//  of a task for the locations to be counted as 'equal'.
	public final static int MAX_DISTANCE_LOCATION_RECOMMENDATION = 100;
	
	// Default time if task to use if recommender is not able to calculate
	//  the average time spent on a task and task has no 'end time'.
	public final static long DEFAULT_AVERAGE_TIME = 1000 * 60 * 60;
	
	
	// *************** Geofences ***************
	
	// The number of days forward in time in which to apply geofences for
	//  the tasks, including 'today' (i.e. only 'today' means 1).
	public final static int NUMBER_OF_GEOFENCE_DAYS = 3;
	
	// Radius of geofences (in meters).
	public final static float GEOFENCE_RADIUS = 500;
	
	// Amount of time before a Geofence is removed.
	public final static long GEOFENCE_DURATION = 
				1000 * 60 * 60 * 24 * NUMBER_OF_GEOFENCE_DAYS;
	
	// Id for the notification to be displayed when a user enters a geofence.
	public final static int GEOFENCE_NOTIFICATION_ID = 1251;

}
