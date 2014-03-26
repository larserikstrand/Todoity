package no.hig.strand.lars.todoity.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.views.DraggableListView;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TodayListAdapter extends ArrayAdapter<Task> {
	private Context mContext;
	private ArrayList<Task> mTasks;
	private LayoutInflater mInflater;
	
	private HashMap<Task, Integer> mIdMap = new HashMap<Task, Integer>();
	
	private final static int INVALID_ID = -1;
	
    
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
			holder.dragButton = (ImageButton) convertView.findViewById(
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
		
		//rowView.setTag(position);
		//registerForContextMenu(rowView);
		Task task = mTasks.get(position);
		
		holder.taskText.setText(Task.getTaskTextFromTask(task));
		holder.subText.setText(Task.getSubTextFromTask(task));
		
		// Color the layout if the task is active
		if (task.isActive()) {
			convertView.setBackgroundColor(mContext.getResources()
					.getColor(R.color.lightgreen));
			holder.startPauseButton.setText(mContext.getString(R.string.pause));
		}
		
		holder.startPauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*LinearLayout layout = (LinearLayout) v.getParent();
				int position = (Integer) layout.getTag();
				Task task = mTasks.get(position);
				String text = ((Button) v).getText().toString();
				if (text.equals(getString(R.string.start))) {
					((Button) v).setText(getString(R.string.pause));
					layout.setBackgroundColor(getResources()
							.getColor(R.color.lightgreen));
					startTask(task);
				} else {
					((Button) v).setText(getString(R.string.start));
					layout.setBackgroundResource(0);
					pauseTask(task);
				}*/
			}
		});
		
		// Cross out the task if it is finished.
		if (task.isFinished()) {
			holder.finishedCheck.setOnCheckedChangeListener(null);
			holder.finishedCheck.setChecked(true);
			setStrikeThrough(holder, true);
			holder.startPauseButton.setEnabled(false);
		}
		holder.finishedCheck.setOnCheckedChangeListener(
				new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, 
					boolean isChecked) {
				LinearLayout layout = (LinearLayout) buttonView.getParent();
				ViewHolder holder = (ViewHolder) layout.getTag();
				
				Task task = mTasks.get(holder.position);
				// When the task is checked and finished.
				if (isChecked) {
					task.setFinished(true);
					holder.startPauseButton.setText(mContext.getString(
							R.string.start));
					layout.setBackgroundResource(0);
					setStrikeThrough(holder, true);
					holder.startPauseButton.setEnabled(false);
					//pauseTask(task);
				} else {
					task.setFinished(false);
					//new DatabaseUtilities.UpdateTask(
					//		getActivity(), task).execute();
					//new AppEngineUtilities.UpdateTask(
					//		getActivity(), task).execute();
					setStrikeThrough(holder, false);
					holder.startPauseButton.setEnabled(true);
					//((MainActivity)getActivity()).updateGeofences();
					//recommend();
				}
			}
		});
		
		// Enable drag and drop reordering.
		holder.dragButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				DraggableListView listView = (DraggableListView) v
						.getParent().getParent();
				listView.startDrag();
				return true;
			}
		});
		
		return convertView;
	}
	
	
	
	public void onDragEnd() {
		for (int i = 0; i < mTasks.size(); i++) {
			mTasks.get(i).setPriority(i+1);
			//new DatabaseUtilities.UpdateTask(
			//		getActivity(), tasks.get(i)).execute();
		}
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
	
	
	
	private static class ViewHolder {
		public TextView taskText;
		public TextView subText;
		public ImageButton dragButton;
		public CheckBox finishedCheck;
		public Button startPauseButton;
		public int position;
	}
	
}
