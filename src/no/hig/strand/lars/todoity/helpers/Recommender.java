package no.hig.strand.lars.todoity.helpers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.TaskContext;
import no.hig.strand.lars.todoity.data.TaskContract.ContextEntry;
import no.hig.strand.lars.todoity.data.TasksDatabase;
import no.hig.strand.lars.todoity.fragments.TodayFragment;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

public final class Recommender {
	
	public Recommender() {}
	
	
	public static class RecommendTask extends AsyncTask<Void, Void, Void> 
			implements ConnectionCallbacks, OnConnectionFailedListener {

		private TodayFragment mFragment;
		private Context mContext;
		
		private TasksDatabase mTasksDb;
		
		private LocationClient mLocationClient;
		private Location mLastKnownLocation;
		
		private long mTimeOfCalculation;
		private HashMap<String, Float> mRecommendationMap;
		
		private Task mRecommendedTask;
		private ArrayList<Task> mTaskHisory;
		private ArrayList<Task> mPlannedTasks;
		private ArrayList<Task> mRecommendedList;
		
		
		
		public RecommendTask(TodayFragment fragment) {
			mFragment = fragment;
			mContext = fragment.getActivity();
			
			mTasksDb = TasksDatabase.getInstance(mContext);
			mTaskHisory = mTasksDb.getTaskHistory();
			mPlannedTasks = mTasksDb.getTasksByDate(Utilities.getTodayDate());
		}
		
		
		
		@Override
		protected void onPreExecute() {
			mFragment.getProgressBar().setIndeterminate(true);
		}



		@Override
		protected Void doInBackground(Void... params) {
			
			mLocationClient = new LocationClient(mContext, this, this);
			mLocationClient.connect();
			mLastKnownLocation = null;
			
			// Get calculation time (since midnight).
			mTimeOfCalculation = Utilities.getTimeSinceMidnight(
					Calendar.getInstance().getTimeInMillis());
			mRecommendationMap = new HashMap<String, Float>();
			mRecommendedTask = null;
			mRecommendedList = new ArrayList<Task>();
			
			// Put the currently active tasks first (should be on top of list).
			for (int i = mPlannedTasks.size() - 1; i >= 0; i--) {
				if (mPlannedTasks.get(i).isActive()) {
					mRecommendedList.add(mPlannedTasks.get(i));
					mPlannedTasks.remove(i);
				}
			}
			
			while (! mLocationClient.isConnected()) {}
			
			mLastKnownLocation = mLocationClient.getLastLocation();
			recommend(mTimeOfCalculation);
			updatePriorities();
			
			return null;
		}
		
		
		
		@Override
		protected void onPostExecute(Void result) {
			mFragment.getProgressBar().setIndeterminate(false);
		}

		
		
		// Recommend a task for the time provided as parameter
		private void recommend(long recommendationTime) {
				
			mRecommendedTask = null;
			mRecommendationMap.clear();
			timeOfDayRecommendation(recommendationTime);
			timeOfDayAndDayOfWeekRecommendation(recommendationTime);
			locationRecommendation();
			timeOfDayAndLocationRecommendation(recommendationTime);
			timeOfDayAndDayOfWeekAndLocationRecommendation(recommendationTime);
			
			float probability = 0;
			for (Entry<String, Float> entry : mRecommendationMap.entrySet()) {
				if (entry.getValue() > probability) {
					for (Task task : mPlannedTasks) {
						if (! task.isFinished() && 
								task.getCategory().equals(entry.getKey())) {
							mRecommendedTask = task;
						}
					}
				}
			}
			
			
			// A recommendation has been found.
			if (mRecommendedTask != null) {
				
				// Check if there is a task with a fixed time that may interfere
				//  with the task to be recommended.
				Task fixedTask = null;
				long fixedTaskStartTime = 0;
				for (Task task : mPlannedTasks) {
					if (! task.getFixedStart().isEmpty()) {
						long taskStartTime = Utilities.timeToMillis(
								task.getFixedStart());
						
						// If the start time of the task is later than 'now' and if
						//  the task is sooner than a previously found task or if
						//  a task have not been found.
						if (taskStartTime - recommendationTime > 0 && 
								( taskStartTime < fixedTaskStartTime
								|| fixedTaskStartTime == 0 )) {
							fixedTaskStartTime = taskStartTime;
							fixedTask = task;
						}
					}
				}
				
				// If a task with a fixed start time has been found, we must
				//  account for this.
				long avgTimeSpent  = getAverageTimeSpentOnTask(mRecommendedTask);
				if (fixedTask != null) {
					long timeToFixedStart = fixedTaskStartTime - recommendationTime;
						
					// If able to find an average time and this time is less 
					//  than the time until the fixed task is to be started. 
					//  Recommend task and perform new recommendation with
					//  new time.
					if (avgTimeSpent > 0 && avgTimeSpent < timeToFixedStart) {
						mRecommendedList.add(mRecommendedTask);
						mPlannedTasks.remove(mRecommendedTask);
						mTimeOfCalculation += avgTimeSpent;
						recommend(mTimeOfCalculation);
					
					// If not able to find an average time, or there is not enough
					//  time before the fixed task.
					} else {
						mRecommendedList.add(fixedTask);
						mPlannedTasks.remove(fixedTask);
						if (! fixedTask.getFixedEnd().isEmpty()) {
							long timeToNextTask = Utilities.timeToMillis(
									fixedTask.getFixedEnd());
							mTimeOfCalculation = timeToNextTask;
						} else {
							avgTimeSpent = getAverageTimeSpentOnTask(fixedTask);
							mTimeOfCalculation += avgTimeSpent > 0 ? 
									avgTimeSpent : Constant.DEFAULT_AVERAGE_TIME;
						}
						recommend(mTimeOfCalculation);
					}
						
				} else {
					mRecommendedList.add(mRecommendedTask);
					mPlannedTasks.remove(mRecommendedTask);
					mTimeOfCalculation += avgTimeSpent > 0 ? 
							avgTimeSpent : Constant.DEFAULT_AVERAGE_TIME;
					recommend(mTimeOfCalculation);
				}
			}
		}
		
		
		
		/*
		 * Calculate the type of task (category) that is completed most often
		 *  at the time of day of the calculation.
		 */
		private void timeOfDayRecommendation(long recommendationTime) {
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			long startTimeTask;
			long endTimeTask;
			for (Task task : mTaskHisory) {
				if (task.getTimeStarted() > 0) {
					// Get start and end time since midnight of the task.
					startTimeTask = Utilities.getTimeSinceMidnight(
							task.getTimeStarted());
					endTimeTask = Utilities.getTimeSinceMidnight(
							task.getTimeEnded());
					
					if (startTimeTask < recommendationTime && 
							endTimeTask > recommendationTime) {
						int occurrences = 1;
						if (categoryOccurrences.containsKey(task.getCategory())) {
							occurrences = categoryOccurrences
									.get(task.getCategory()) + 1;
						}
						categoryOccurrences.put(task.getCategory(), occurrences);
					}
				}
			}

			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		private void timeOfDayAndDayOfWeekRecommendation(long recommendationTime) {
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			int dayNow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			long startTimeTask;
			long endTimeTask;
			for (Task task : mTaskHisory) {
				if (task.getTimeStarted() > 0) {
					// Get start and end time since midnight of the task.
					int dayTask = Utilities.getDayOfWeek(task.getTimeStarted());
					startTimeTask = Utilities.getTimeSinceMidnight(
							task.getTimeStarted());
					endTimeTask = Utilities.getTimeSinceMidnight(
							task.getTimeEnded());
					
					if (startTimeTask < recommendationTime && 
							endTimeTask > recommendationTime && 
							dayNow == dayTask) {
						int occurrences = 1;
						if (categoryOccurrences.containsKey(task.getCategory())) {
							occurrences = categoryOccurrences
									.get(task.getCategory()) + 1;
						}
						categoryOccurrences.put(task.getCategory(), occurrences);
					}
				}
			}
			
			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		private void locationRecommendation() {
			if (mLocationClient.isConnected()) {
				mLastKnownLocation = mLocationClient.getLastLocation();
			} else if (mLastKnownLocation == null) {
				return;
			}
			
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			ArrayList<TaskContext> taskContexts;
			for (Task task : mTaskHisory) {
				
				boolean hasSameLocation = false;
				taskContexts = mTasksDb.getContextByTaskId(task.getId(),
						ContextEntry.TYPE_LOCATION);
				
				for (TaskContext taskContext : taskContexts) {
					String[] latLng = taskContext.getDetails().split("\\s+");
					double latitude = Double.valueOf(latLng[0]);
					double longitude = Double.valueOf(latLng[1]);
					
					// Check if location where task was performed is the 
					//  same as the current location.
					float[] result = new float[3];
					Location.distanceBetween(mLastKnownLocation.getLatitude(), 
							mLastKnownLocation.getLongitude(), 
							latitude, longitude, result);
					float distance = result[0];
					
					if (! hasSameLocation && distance <= 
							Constant.MAX_DISTANCE_LOCATION_RECOMMENDATION) {
						int occurrences = 1;
						if (categoryOccurrences.containsKey(task.getCategory())) {
							occurrences = categoryOccurrences
									.get(task.getCategory()) + 1;
						}
						categoryOccurrences.put(task.getCategory(), occurrences);
						hasSameLocation = true;
					}
				}
			}
			
			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		/*
		 * Calculates and recommends the type of task (category) that is completed 
		 * most often at the time of the calculation and the current location.
		 */
		private void timeOfDayAndLocationRecommendation(long recommendationTime) {
			if (mLocationClient.isConnected()) {
				mLastKnownLocation = mLocationClient.getLastLocation();
			} else if (mLastKnownLocation == null) {
				return;
			}
			
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			long startTimeTask;
			long endTimeTask;
			ArrayList<TaskContext> taskContexts;
			for (Task task : mTaskHisory) {
				
				boolean hasSameContext = false;
				taskContexts = mTasksDb.getContextByTaskId(task.getId(),
						ContextEntry.TYPE_LOCATION);
				
				for (TaskContext taskContext : taskContexts) {
					String[] latLng = taskContext.getDetails().split("\\s+");
					double latitude = Double.valueOf(latLng[0]);
					double longitude = Double.valueOf(latLng[1]);
					
					// Check if location where task was performed is the 
					//  same as the current location.
					float[] result = new float[3];
					Location.distanceBetween(mLastKnownLocation.getLatitude(), 
							mLastKnownLocation.getLongitude(), 
							latitude, longitude, result);
					float distance = result[0];
					
					if (! hasSameContext && distance <= 
							Constant.MAX_DISTANCE_LOCATION_RECOMMENDATION &&
							task.getTimeStarted() > 0) {
						// Get start and end time since midnight of the task.
						startTimeTask = Utilities.getTimeSinceMidnight(
								task.getTimeStarted());
						endTimeTask = Utilities.getTimeSinceMidnight(
								task.getTimeEnded());
						
						if (startTimeTask < recommendationTime && 
								endTimeTask > recommendationTime) {
							int occurrences = 1;
							if (categoryOccurrences.containsKey(task.getCategory())) {
								occurrences = categoryOccurrences
										.get(task.getCategory()) + 1;
							}
							categoryOccurrences.put(task.getCategory(), occurrences);
							hasSameContext = true;
						}
					}
				}
			}
			
			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		private void timeOfDayAndDayOfWeekAndLocationRecommendation(
				long recommendationTime) {
			if (mLocationClient.isConnected()) {
				mLastKnownLocation = mLocationClient.getLastLocation();
			} else if (mLastKnownLocation == null) {
				return;
			}
			
			HashMap<String, Integer> categoryOccurrences = 
					new HashMap<String, Integer>();
			
			int dayNow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			long startTimeTask;
			long endTimeTask;
			ArrayList<TaskContext> taskContexts;
			for (Task task : mTaskHisory) {
				
				boolean hasSameContext = false;
				taskContexts = mTasksDb.getContextByTaskId(task.getId(),
						ContextEntry.TYPE_LOCATION);
				
				for (TaskContext taskContext : taskContexts) {
					String[] latLng = taskContext.getDetails().split("\\s+");
					double latitude = Double.valueOf(latLng[0]);
					double longitude = Double.valueOf(latLng[1]);
					
					// Check if location where task was performed is the 
					//  same as the current location.
					float[] result = new float[3];
					Location.distanceBetween(mLastKnownLocation.getLatitude(), 
							mLastKnownLocation.getLongitude(), 
							latitude, longitude, result);
					float distance = result[0];
					
					if (! hasSameContext && distance <= 
							Constant.MAX_DISTANCE_LOCATION_RECOMMENDATION &&
							task.getTimeStarted() > 0) {
						// Get start and end time since midnight of the task.
						int dayTask = Utilities.getDayOfWeek(task.getTimeStarted());
						startTimeTask = Utilities.getTimeSinceMidnight(
								task.getTimeStarted());
						endTimeTask = Utilities.getTimeSinceMidnight(
								task.getTimeEnded());
						
						if (startTimeTask < recommendationTime && 
								endTimeTask > recommendationTime &&
								dayTask == dayNow) {
							int occurrences = 1;
							if (categoryOccurrences.containsKey(task.getCategory())) {
								occurrences = categoryOccurrences
										.get(task.getCategory()) + 1;
							}
							categoryOccurrences.put(task.getCategory(), occurrences);
							hasSameContext = true;
						}
					}
				}
			}
			
			addRecommendationsFromMap(categoryOccurrences);
		}
		
		
		
		private void addRecommendationsFromMap(HashMap<String, Integer> map) {
			float total = 0;
			for (Entry<String, Integer> entry : map.entrySet()) {
				total += entry.getValue();
			}
			
			float probability;
			for (Entry<String, Integer> entry : map.entrySet()) {
				probability = (float) entry.getValue() / total;
				if (! mRecommendationMap.containsKey(entry.getKey()) ||
						mRecommendationMap.get(entry.getKey()) < probability) {
					mRecommendationMap.put(entry.getKey(), probability);
				}
			}
			
		}
		
		
		
		private long getAverageTimeSpentOnTask(Task task) {
			long totalTimeSpent = 0;
			int numberOfTasks = 0;
			for (Task t : mTaskHisory) {
				if (t.getCategory().equals(task.getCategory()) && 
						t.getTimeStarted() > 0) {
					numberOfTasks += 1;
					totalTimeSpent += task.getTimeSpent();
				}
			}
			return numberOfTasks > 0 ? totalTimeSpent / numberOfTasks : -1;
		}
		
		
		
		private void updatePriorities() {
			for (int i = 0; i < mRecommendedList.size(); i++) {
				mRecommendedList.get(i).setPriority(i+1);
				new DatabaseUtilities.UpdateTask(
						mContext, mRecommendedList.get(i)).execute();
			}
			for (int i = 0; i < mPlannedTasks.size(); i++) {
				mPlannedTasks.get(i).setPriority(mRecommendedList.size() + i + 1);
				new DatabaseUtilities.UpdateTask(
						mContext, mPlannedTasks.get(i)).execute();
			}
		}
		
		
		
		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			cancel(true);
		}
		
		
		
		@Override
		public void onConnected(Bundle bundle) {}
		
		
		
		@Override
		public void onDisconnected() {
			mLocationClient = null;
			cancel(true);
		}
		
	}
	
	
	
	public static void recommend(TodayFragment fragment) {
		new RecommendTask(fragment).execute();
	}
}

