package no.hig.strand.lars.todoity.adapters;

import java.util.HashMap;
import java.util.List;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.ListActivity;
import no.hig.strand.lars.todoity.activities.MainActivity;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.DeleteTask;
import no.hig.strand.lars.todoity.helpers.Utilities;
import no.hig.strand.lars.todoity.helpers.Utilities.OnConfirmListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class WeekListAdapter extends BaseExpandableListAdapter {
	
	private Context mContext;
	private List<String> mDates;
	private HashMap<String, List<Task>> mTasks;
	private LayoutInflater mInflater;

	private OnClickListener mEditListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			GroupViewHolder holder = (GroupViewHolder) 
					((View) v.getParent()).getTag();
			
			Intent intent = new Intent(mContext, ListActivity.class);
			intent.putExtra(Constant.DATE_EXTRA, 
					holder.dateText.getText().toString());
			
			((MainActivity) mContext).startActivity(intent);
		}
	};
	
	private OnClickListener mDeleteListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final GroupViewHolder holder = (GroupViewHolder) 
					((View) v.getParent()).getTag();
			
			String dialogTitle = mContext.getString(R.string.delete) + " " + 
					mTasks.get(mDates.get(holder.position)).size() + " " + 
					mContext.getString(R.string.tasks);
			String dialogMessage = mContext.getString(
					R.string.delete_list_message1) + " " + 
					mDates.get(holder.position) + " " + 
					mContext.getString(R.string.delete_list_message2);
			Utilities.showConfirmDialog(mContext, dialogTitle, dialogMessage, 
					mContext.getString(R.string.delete), new OnConfirmListener() {
				@Override
				public void onConfirm(DialogInterface dialog, int id) {
					List<Task> tasks = mTasks.remove(
							mDates.get(holder.position));
					mDates.remove(holder.position);
					notifyDataSetChanged();
					
					for (Task task : tasks) {
						new DeleteTask(mContext, task).execute();
					}
					((MainActivity) mContext).updateNeighborFragments();
				}
			});
		}
	};
	
	public WeekListAdapter(Context context, List<String> dates, 
			HashMap<String, List<Task>> tasks) {
		mContext = context;
		mDates = dates;
		mTasks = tasks;
		mInflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
	}
	
	
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mTasks.get(mDates.get(groupPosition)).get(childPosition);
	}
	
	
	
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
	
	
	
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ChildViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.adapter_expandablelist_child, parent, false);
			
			holder = new ChildViewHolder();
			holder.taskText = (TextView) convertView.findViewById(
					R.id.child_task_text);
			holder.subText = (TextView) convertView.findViewById(
					R.id.child_sub_text);
			holder.groupPosition = groupPosition;
			holder.childPosition = childPosition;
			
			convertView.setTag(holder);
		} else {
			holder = (ChildViewHolder) convertView.getTag();
		}
		
		Task task = (Task) getChild(groupPosition, childPosition);
		
		holder.taskText.setText(Task.getTaskTextFromTask(task));
		holder.subText.setText(Task.getSubTextFromTask(task));
		
		return convertView;
	}
	
	
	
	@Override
	public int getChildrenCount(int groupPosition) {
		return mTasks.get(mDates.get(groupPosition)).size();
	}
	
	
	
	@Override
	public Object getGroup(int groupPosition) {
		return mDates.get(groupPosition);
	}
	
	
	
	@Override
	public int getGroupCount() {
		return mDates.size();
	}
	
	
	
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.adapter_expandablelist_group, parent, false);
			
			holder = new GroupViewHolder();
			holder.dateText = (TextView) convertView.findViewById(
					R.id.group_date_text);
			holder.editButton = (ImageButton) convertView.findViewById(
					R.id.group_edit_button);
			holder.deleteButton = (ImageButton) convertView.findViewById(
					R.id.group_delete_button);
			holder.position = groupPosition;
			
			convertView.setTag(holder);
		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}
		
		String date = (String) getGroup(groupPosition);
		holder.dateText.setText(date);
		
		holder.editButton.setOnClickListener(mEditListener);
		holder.deleteButton.setOnClickListener(mDeleteListener);
		
		return convertView;
	}
	
	
	
	@Override
	public boolean hasStableIds() {
		return false;
	}
	
	
	
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	
	
	private static class ChildViewHolder {
		public TextView taskText;
		public TextView subText;
		public int groupPosition;
		public int childPosition;
	}
	
	
	
	private static class GroupViewHolder {
		public TextView dateText;
		public ImageButton editButton;
		public ImageButton deleteButton;
		public int position;
	}

}
