package no.hig.strand.lars.todoity.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskContext implements Parcelable {
	
	private int mTaskId;
	private String mType;
	private String mContext;
	private String mDetails;
	
	public TaskContext() {
		mTaskId = -1;
		mType = mContext = mDetails = "";
	}
	
	public TaskContext(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mTaskId);
		dest.writeString(mType);
		dest.writeString(mContext);
		dest.writeString(mDetails);
	}
	
	private void readFromParcel(Parcel in) {
		mTaskId = in.readInt();
		mType = in.readString();
		mContext = in.readString();
		mDetails = in.readString();
	}
	
	public static final Parcelable.Creator<TaskContext> CREATOR = 
			new Parcelable.Creator<TaskContext>() {

				@Override
				public TaskContext createFromParcel(Parcel source) {
					return new TaskContext(source);
				}

				@Override
				public TaskContext[] newArray(int size) {
					return new TaskContext[size];
				}
	};
	
	public int getTaskId() {
		return mTaskId;
	}
	
	public void setTaskId(int taskId) {
		mTaskId = taskId;
	}
	
	public String getType() {
		return mType;
	}
	
	public void setType(String type) {
		mType = type;
	}
	
	public String getContext() {
		return mContext;
	}
	
	public void setContext(String context) {
		mContext = context;
	}
	
	public String getDetails() {
		return mDetails;
	}
	
	public void setDetails(String details) {
		mDetails = details;
	}
	
	
}
