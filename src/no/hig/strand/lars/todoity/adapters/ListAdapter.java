package no.hig.strand.lars.todoity.adapters;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.ListActivity;
import no.hig.strand.lars.todoity.activities.TaskActivity;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<Task> {

	private Context mContext;
	private ArrayList<Task> mTasks;
	private LayoutInflater mInflater;
	
	private OnClickListener mEditListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) ((View) v.getParent()).getTag();
			
			Intent intent = new Intent(mContext, TaskActivity.class);
			intent.putExtra(Constant.TASK_EXTRA, mTasks.get(holder.position));
			intent.putExtra(Constant.POSITION_EXTRA, holder.position);
			
			((ListActivity) mContext).startActivityForResult(
					intent, Constant.EDIT_TASK_REQUEST);
		}
	};
	
	private OnClickListener mDeleteListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			/*LinearLayout layout = (LinearLayout) v.getParent();
			ListView listView = (ListView) layout.getParent();
			int position = (Integer) layout.getTag();
			Task task = mTasks.get(position);
			mTasks.remove(position);
			listView.setAdapter(new TaskListAdapter(context, mTasks));
			new DatabaseUtilities.DeleteTask(
					ListActivity.this, task, null).execute();
			new AppEngineUtilities.RemoveTask(
					ListActivity.this, task).execute();*/
		}
	};
	
	
	public ListAdapter(Context context, ArrayList<Task> tasks) {
		super(context, R.layout.adapter_list, tasks);
		mContext = context;
		mTasks = tasks;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.adapter_list, parent, false);
			
			holder = new ViewHolder();
			holder.taskText = (TextView) convertView.findViewById(
					R.id.list_item_task_text);
			holder.subText = (TextView) convertView.findViewById(
					R.id.list_item_sub_text);
			holder.editButton = (ImageButton) convertView.findViewById(
					R.id.list_item_edit_button);
			holder.deleteButton = (ImageButton) convertView.findViewById(
					R.id.list_item_delete_button);
			holder.position = position;
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Task task = mTasks.get(position);
		
		holder.taskText.setText(Task.getTaskTextFromTask(task));
		holder.subText.setText(Task.getSubTextFromTask(task));
		
		if (task.isFinished()) {
			holder.editButton.setVisibility(View.GONE);
			holder.deleteButton.setVisibility(View.GONE);
			setStrikeThrough(holder);
		}
		
		holder.editButton.setOnClickListener(mEditListener);
		holder.deleteButton.setOnClickListener(mDeleteListener);
		
		return convertView;
	}
	
	
	
	private void setStrikeThrough(ViewHolder holder) {
		holder.taskText.setPaintFlags(holder.taskText.getPaintFlags() 
				| Paint.STRIKE_THRU_TEXT_FLAG);
		holder.subText.setPaintFlags(holder.subText.getPaintFlags() 
				| Paint.STRIKE_THRU_TEXT_FLAG);
	}
	
	
	
	private static class ViewHolder {
		public TextView taskText;
		public TextView subText;
		public ImageButton editButton;
		public ImageButton deleteButton;
		public int position;
	}
	
}
