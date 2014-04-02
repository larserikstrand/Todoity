package no.hig.strand.lars.todoity.data;

public class Constant {
	
	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
		
	public final static int NEW_LIST_REQUEST = 1;
	public final static int EDIT_LIST_REQUEST = 2;
	public final static int NEW_TASK_REQUEST = 3;
	public final static int EDIT_TASK_REQUEST = 4;
	public final static int MAP_REQUEST = 5;
	public final static int MOVE_TASK_REQUEST = 6;
	
	public final static String TASK_EXTRA = "no.hig.strand.lars.todoity.TASK";
	public final static String TASKS_EXTRA = "no.hig.strand.lars.todoity.TASKS";
	public final static String DATE_EXTRA  = "no.hig.strand.lars.todoity.DATE";
	public final static String LOCATION_EXTRA = 
			"no.hig.strand.lars.todoity.LOCATION";
	public final static String POSITION_EXTRA = 
			"no.hig.strand.lars.todoity.POSITION";
	public final static String DATEPICKER_TITLE_EXTRA = 
			"no.hig.strand.lars.todoity.DATEPICKER_TITLE";
	
	public final static String DATE_FORMAT = "EEE, MMM dd, yyyy";
	public final static String TIME_FORMAT = "HH:mm";
	
	public final static int TASKS_TO_DISPLAY_IN_WEEK = 7;
	
	// The minimum amount of time in milliseconds before a task is considered 
	//  as actually started.
	public final static int MINIMUM_TASK_START = 1000 * 10;

}
