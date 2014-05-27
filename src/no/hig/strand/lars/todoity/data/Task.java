package no.hig.strand.lars.todoity.data;

import java.util.Comparator;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable {

	private int mId;
	
	private String mDate;
	private String mCategory;
	private String mDescription;
	private double mLatitude;
	private double mLongitude;
	private String mAddress;
	private int mPriority;
	// Whether or not the task is currently in progress
	private boolean mIsActive;
	// Temporary time in milliseconds for the task
	private long mTempStart;
	// Actual times for the task
	private long mTimeStarted;
	private long mTimeEnded;
	// Total time spent on task in milliseconds.
	private long mTimeSpent;
	// Whether or not the task is finished (i.e. should sometimes not be displayed).
	private boolean mIsFinished;
	
	// Variables for tasks with fixed start and end times.
	private String mFixedStart;
	private String mFixedEnd;
	
	
	public Task() {
		mId = 0;
		mDate = "";
		mCategory = "";
		mDescription = "";
		mLatitude = 0;
		mLongitude = 0;
		mAddress = "";
		mPriority = 0;
		mIsActive = false;
		mTempStart = 0;
		mTimeStarted = 0;
		mTimeEnded = 0;
		mTimeSpent = 0;
		mIsFinished = false;
		
		mFixedStart = "";
		mFixedEnd = "";
	}
	
	
	
	public Task(Parcel in) {
		readFromParcel(in);
	}
	
	
	
	@Override
	public int describeContents() {
		return 0;
	}

	
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeString(mDate);
		dest.writeString(mCategory);
		dest.writeString(mDescription);
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
		dest.writeString(mAddress);
		dest.writeInt(mPriority);
		dest.writeInt((int) (mIsActive ? 1 : 0));
		dest.writeLong(mTempStart);
		dest.writeLong(mTimeStarted);
		dest.writeLong(mTimeEnded);
		dest.writeLong(mTimeSpent);
		dest.writeInt((int) (mIsFinished ? 1 : 0));
		dest.writeString(mFixedStart);
		dest.writeString(mFixedEnd);
	}
	
	
	
	private void readFromParcel(Parcel in) {
		mId = in.readInt();
		mDate = in.readString();
		mCategory = in.readString();
		mDescription = in.readString();
		mLatitude = in.readDouble();
		mLongitude = in.readDouble();
		mAddress = in.readString();
		mPriority = in.readInt();
		mIsActive = in.readInt() != 0;
		mTempStart = in.readLong();
		mTimeStarted = in.readLong();
		mTimeEnded = in.readLong();
		mTimeSpent = in.readLong();
		mIsFinished = in.readInt() != 0;
		mFixedStart = in.readString();
		mFixedEnd = in.readString();
	}
	
	
	
	public static final Parcelable.Creator<Task> CREATOR = 
			new Parcelable.Creator<Task>() {

				@Override
				public Task createFromParcel(Parcel source) {
					return new Task(source);
				}

				@Override
				public Task[] newArray(int size) {
					return new Task[size];
				}
		
		
	};
	
	
	
	public int getId() {
		return mId;
	}
	
	
	
	public void setId(int id) {
		mId = id;
	}
	
	
	
	// Used for AppEngine purposes.
	public String getDate() {
		return mDate;
	}
	
	
	
	public void setDate(String date) {
		mDate = date;
	}
	
	
	
	public String getCategory() {
		return mCategory;
	}


	
	public void setCategory(String category) {
		mCategory = category;
	}
	
	
	
	public String getDescription() {
		return mDescription;
	}

	
	
	public void setDescription(String description) {
		mDescription = description;
	}
	
	
	
	public double getLatitude() {
		return mLatitude;
	}

	
	
	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}
	
	
	
	public double getLongitude() {
		return mLongitude;
	}

	
	
	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}
	
	
	
	public String getAddress() {
		return mAddress;
	}
	
	
	
	public void setAddress(String address) {
		mAddress = address;
	}
	
	
	
	public int getPriority() {
		return mPriority;
	}
	
	
	
	public void setPriority(int priority) {
		mPriority = priority;
	}
	
	
	
	public boolean isActive() {
		return mIsActive;
	}

	
	
	public void setActive(boolean isActive) {
		mIsActive = isActive;
	}
	
	
	
	public long getTempStart() {
		return mTempStart;
	}
	
	
	
	public void setTempStart(long start) {
		mTempStart = start;
	}
	
	
	
	public long getTimeStarted() {
		return mTimeStarted;
	}

	
	
	public void setTimeStarted(long timeStarted) {
		mTimeStarted = timeStarted;
	}
	
	
	
	public long getTimeEnded() {
		return mTimeEnded;
	}

	
	
	public void setTimeEnded(long timeEnded) {
		mTimeEnded = timeEnded;
	}
	
	
	
	public long getTimeSpent() {
		return mTimeSpent;
	}
	
	
	
	public void setTimeSpent(long timeSpent) {
		mTimeSpent = timeSpent;
	}
	
	
	
	public void updateTimeSpent(long time) {
		mTimeSpent += time;
	}
	
	
	
	public boolean isFinished() {
		return mIsFinished;
	}
	
	
	
	public void setFinished(boolean isFinished) {
		mIsFinished = isFinished;
	}
	
	
	
	public String getFixedStart() {
		return mFixedStart;
	}
	
	
	
	public void setFixedStart(String fixedStart) {
		mFixedStart = fixedStart;
	}
	
	
	
	public String getFixedEnd() {
		return mFixedEnd;
	}
	
	
	
	public void setFixedEnd(String fixedEnd) {
		mFixedEnd = fixedEnd;
	}
	
	
	
	public static String getTaskTextFromTask(Task task) {
		String taskText = "";
		taskText += task.getCategory();
		if (! task.getDescription().isEmpty()) {
			taskText += ": " + task.getDescription();
		}
		return taskText;
	}
	
	
	
	public static String getSubTextFromTask(Task task) {
		String subText = "";
		if (! task.getFixedStart().isEmpty()) {
			subText += task.getFixedStart();
			if (! task.getFixedEnd().isEmpty()) {
				subText += "-" + task.getFixedEnd();
			}
			if (! task.getAddress().isEmpty()) {
				subText += ", ";
			}
		}
		if (! task.getAddress().isEmpty()) {
			subText += task.getAddress();
		}
		return subText;
	}
	
	
	
	public static class TaskPriorityComparator implements Comparator<Task> {
		@Override
		public int compare(Task lhs, Task rhs) {
			return lhs.getPriority() - rhs.getPriority();
		}
		
	}
	
}
