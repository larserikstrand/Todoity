package no.hig.strand.lars.todoity.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.MainActivity;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.fragments.TodayFragment;
import no.hig.strand.lars.todoity.helpers.AppEngineUtilities.UpdateTask;
import no.hig.strand.lars.todoity.views.DynamicListView;
import android.content.Context;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TodayListAdapter extends ArrayAdapter<Task> {
	private Context mContext;
	private ArrayList<Task> mTasks;
	private LayoutInflater mInflater;
	
	private HashMap<Task, Integer> mIdMap = new HashMap<Task, Integer>();
	
	private final int INVALID_ID = -1;
	
	private OnClickListener mStartStopListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) ((View) v.getParent()).getTag();
			
			Task task = mTasks.get(holder.position);
			
			// If task is currently active, we need to pause it and update
			//  the priorities.
			if (task.isActive()) {
				int lastActivePosition = holder.position;
				for (int i = holder.position; i < mTasks.size() - 1; i++) {
					if (mTasks.get(i + 1).isActive()) {
						mTasks.get(i + 1).setPriority(i + 1);
						lastActivePosition = i + 1;
					}
				}
				task.setPriority(lastActivePosition + 1);
				((TodayFragment) ((MainActivity) mContext)
						.getFragmentAt(0)).stopTask(task);
			// Task is not active. Start the task, move it to front and
			//  update the priorities.
			} else {
				task.setPriority(1);
				for (int i = 0; i < holder.position; i++) {
					mTasks.get(i).setPriority(i+2);
				}
				((TodayFragment) ((MainActivity) mContext)
						.getFragmentAt(0)).startTask(task);
			}
		}
	};
	
	private OnCheckedChangeListener mFinishedListener = 
			new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ViewHolder holder = (ViewHolder) ((View) buttonView
					.getParent()).getTag();
			
			Task task = mTasks.get(holder.position);
			
			if (isChecked) {
				task.setFinished(true);
				task.setPriority(mTasks.size());
				for (int i = holder.position + 1; i < mTasks.size(); i++) {
					mTasks.get(i).setPriority(i);
				}
				((TodayFragment) ((MainActivity) mContext)
						.getFragmentAt(0)).stopTask(task);
			} else {
				int lastFinishedPosition = holder.position;
				for (int i = holder.position; i > 0; i--) {
					if (mTasks.get(i - 1).isFinished()) {
						mTasks.get(i - 1).setPriority(lastFinishedPosition + 1);
						lastFinishedPosition = i - 1;
					}
				}
				task.setPriority(lastFinishedPosition + 1);
				((TodayFragment) ((MainActivity) mContext)
						.getFragmentAt(0)).startTask(task);
			}
		}
	};
	
	private OnTouchListener mDragListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ViewHolder holder = (ViewHolder) ((View) v.getParent()).getTag();
			Task task = mTasks.get(holder.position);
			if (! task.isActive() && ! task.isFinished()) {
				((TodayFragment) ((MainActivity) mContext)
						.getFragmentAt(0)).stopActionMode();
				DynamicListView listView;
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
					listView = (DynamicListView) v.getParent().getParent();
					listView.touchEventsCancelled();
					break;
				case MotionEvent.ACTION_DOWN:
					listView = (DynamicListView) v.getParent().getParent();
					listView.startDrag(v, event);
					break;
				}
			}
			return false;
		}
	};
	
    
	public TodayListAdapter(Context context, ArrayList<Task> tasks) {
		super(context, R.layout.adapter_today, tasks);
		mContext = context;
		mTasks = tasks;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < tasks.size(); ++i) {
            mIdMap.put(tasks.get(i), i);
        }
	}
	
	
	
	@Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }
        Task item = getItem(position);
        return mIdMap.get(item);
    }
	
	
	
    @Override
    public boolean hasStableIds() {
        return true;
    }
    
    
    
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.adapter_today, parent, false);
			
			holder = new ViewHolder();
			holder.taskText = (TextView) convertView.findViewById(
					R.id.today_item_task_text);
			holder.subText = (TextView) convertView.findViewById(
					R.id.today_item_sub_text);
			holder.dragButton = (ImageView) convertView.findViewById(
					R.id.drag_button);
			holder.finishedCheck = (CheckBox) convertView.findViewById(
					R.id.finish_check);
			holder.startPauseButton = (Button) convertView.findViewById(
					R.id.start_pause_button);
			holder.position = position;
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Task task = mTasks.get(position);
		
		holder.taskText.setText(Task.getTaskTextFromTask(task));
		holder.subText.setText(Task.getSubTextFromTask(task));
		
		// Color the layout if the task is active
		if (task.isActive()) {
			setRowBackground(holder, true);
		}
		holder.startPauseButton.setOnClickListener(mStartStopListener);
		
		// Cross out the task if it is finished.
		if (task.isFinished()) {
			holder.finishedCheck.setChecked(true);
			setStrikeThrough(holder, true);
			holder.startPauseButton.setEnabled(false);
		}
		holder.finishedCheck.setOnCheckedChangeListener(mFinishedListener);
		
		// Enable drag and drop reordering.
		holder.dragButton.setOnTouchListener(mDragListener);
		
		return convertView;
	}
	
	
	
	public void dragEnded() {
		for (int i = 0; i < mTasks.size(); i++) {
			mTasks.get(i).setPriority(i+1);
			new UpdateTask(mContext, mTasks.get(i)).execute();
		}
		((MainActivity) mContext).updateNeighborFragments();
	}
	
	
	
	private void setStrikeThrough(ViewHolder holder, boolean isFinished) {
		if (isFinished) {
			holder.taskText.setPaintFlags(holder.taskText.getPaintFlags() 
					| Paint.STRIKE_THRU_TEXT_FLAG);
			holder.subText.setPaintFlags(holder.subText.getPaintFlags() 
					| Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			holder.taskText.setPaintFlags(holder.taskText.getPaintFlags() 
					& (~Paint.STRIKE_THRU_TEXT_FLAG));
			holder.subText.setPaintFlags(holder.subText.getPaintFlags() 
					& (~Paint.STRIKE_THRU_TEXT_FLAG));
		}
	}


	
	private void setRowBackground(ViewHolder holder, boolean activate) {
		LinearLayout row = (LinearLayout) holder.dragButton.getParent();
		if (activate) {
			holder.startPauseButton.setText(mContext.getString(R.string.pause));
			row.setBackgroundColor(mContext.getResources()
					.getColor(android.R.color.holo_green_dark));
		} else {
			holder.startPauseButton.setText(mContext.getString(R.string.start));
			TypedValue typedValue = new TypedValue();
			mContext.getTheme().resolveAttribute(android.R.attr
					.activatedBackgroundIndicator, typedValue, true);
			row.setBackgroundResource(typedValue.resourceId);
		}
	}
	
	
	
	private static class ViewHolder {
		public TextView taskText;
		public TextView subText;
		public ImageView dragButton;
		public CheckBox finishedCheck;
		public Button startPauseButton;
		public int position;
	}
	
}
