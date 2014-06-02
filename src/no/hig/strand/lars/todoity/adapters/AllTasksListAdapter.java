package no.hig.strand.lars.todoity.adapters;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.ListActivity;
import no.hig.strand.lars.todoity.activities.MainActivity;
import no.hig.strand.lars.todoity.activities.TaskActivity;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.fragments.AllTasksFragment;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.DeleteTask;
import no.hig.strand.lars.todoity.helpers.DatePickerFragment;
import no.hig.strand.lars.todoity.helpers.Utilities;
import no.hig.strand.lars.todoity.helpers.Utilities.OnConfirmListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

/**
 * Class used for handling the logic in the list that displays all tasks.
 * @author LarsErik
 *
 */
public class AllTasksListAdapter extends BaseExpandableListAdapter implements 
		OnMenuItemClickListener {
	
	private Context mContext;
	private List<String> mDates;
	private HashMap<String, List<Task>> mTasks;
	private long mTodayInMillis;
	private LayoutInflater mInflater;
	private ChildViewHolder mCurrentHolder;
	private Task mCurrentTask;
	
	// Edit button functionality.
	private OnClickListener mEditListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Get the data in the group list item.
			GroupViewHolder holder = (GroupViewHolder) 
					((View) v.getParent()).getTag();
			
			// Start the TaskActivity for editing the task.
			Intent intent = new Intent(mContext, ListActivity.class);
			intent.putExtra(Constant.DATE_EXTRA, 
					holder.dateText.getText().toString());
			
			((MainActivity) mContext).startActivity(intent);
		}
	};
	
	// Delete button functionality.
	private OnClickListener mDeleteListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Get data in group list item.
			final GroupViewHolder holder = (GroupViewHolder) 
					((View) v.getParent()).getTag();
			
			// Create a confirm dialog, asking for confirmation of deletion.
			String dialogTitle = mContext.getString(R.string.delete) + " " + 
					mTasks.get(mDates.get(holder.position)).size() + " " + 
					mContext.getString(R.string.tasks);
			String dialogMessage = mContext.getString(
					R.string.delete_list_message_pre) + " " + 
					mDates.get(holder.position) + " " + 
					mContext.getString(R.string.delete_list_message);
			Utilities.showConfirmDialog(mContext, dialogTitle, dialogMessage, 
					mContext.getString(R.string.delete), new OnConfirmListener() {
				@Override
				public void onConfirm(DialogInterface dialog, int id) {
					// When user confirms deletion, remove tasks from list and
					//  delete them from database.
					List<Task> tasks = mTasks.remove(mDates.get(holder.position));
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
	
	// Options button functionality. For individual tasks.
	private OnClickListener mOptionsListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Update reference to the list item (to be used later).
			mCurrentHolder = (ChildViewHolder) ((View) v.getParent()).getTag();
			mCurrentTask = (Task) getChild(mCurrentHolder.groupPosition, 
					mCurrentHolder.childPosition);
			showPopupMenu(v);
		}
	};
	
	
	
	public AllTasksListAdapter(Context context, List<String> dates, 
			HashMap<String, List<Task>> tasks) {
		mContext = context;
		mDates = dates;
		mTasks = tasks;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		mTodayInMillis = c.getTimeInMillis();
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
		// Use holder pattern (for more efficient rendering with large lists).
		ChildViewHolder holder;
		
		// Create list item if it does not exist.
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.adapter_expandablelist_child, parent, false);
			
			holder = new ChildViewHolder();
			holder.taskText = (TextView) convertView.findViewById(
					R.id.child_task_text);
			holder.subText = (TextView) convertView.findViewById(
					R.id.child_sub_text);
			holder.optionsButton = (ImageButton) convertView.findViewById(
					R.id.child_options_button);
			holder.groupPosition = groupPosition;
			holder.childPosition = childPosition;
			
			convertView.setTag(holder);
		} else {
			holder = (ChildViewHolder) convertView.getTag();
		}
		
		Task task = (Task) getChild(groupPosition, childPosition);
		
		holder.taskText.setText(Task.getTaskTextFromTask(task));
		holder.subText.setText(Task.getSubTextFromTask(task));
		
		// Set task finished status.
		if (task.isFinished()) {
			holder.optionsButton.setVisibility(View.GONE);
			setStrikeThrough(holder, true);
		} else {
			holder.optionsButton.setVisibility(View.VISIBLE);
			holder.optionsButton.setOnClickListener(mOptionsListener);
		}
		
		if (task.isActive()) {
			// TODO: Highlight task?
		}
		
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
	
	
	
	/**
	 * Function that returns the group view that holds a set of tasks.
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// Use holder pattern (for more efficient rendering with large lists).
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
		
		// Find the date of the tasks in milliseconds. 
		//  The buttons should be hidden for tasks older than today
		//  (we don't want the user to delete the task history, as this is
		//  important for the research).
		long dateInMillis = Utilities.dateToMillis(date);
		
		if (mTodayInMillis > dateInMillis) {
			holder.editButton.setVisibility(View.GONE);
			holder.deleteButton.setVisibility(View.GONE);
		} else {
			holder.editButton.setVisibility(View.VISIBLE);
			holder.deleteButton.setVisibility(View.VISIBLE);
			holder.editButton.setOnClickListener(mEditListener);
			holder.deleteButton.setOnClickListener(mDeleteListener);
		}
		
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
	
	
	
	public void updateData(List<String> dates, HashMap<String, List<Task>> tasks) {
		mDates = dates;
		mTasks = tasks;
	}
	
	
	
	// Set strikethrough on the holder passed as parameter (finished tasks).
	private void setStrikeThrough(ChildViewHolder holder, boolean isFinished) {
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
	
	
	
	private static class ChildViewHolder {
		public TextView taskText;
		public TextView subText;
		public ImageButton optionsButton;
		public int groupPosition;
		public int childPosition;
	}
	
	
	
	private static class GroupViewHolder {
		public TextView dateText;
		public ImageButton editButton;
		public ImageButton deleteButton;
		public int position;
	}

	
	
	/**
	 * Displays a popup menu anchored at the view given as parameter.
	 * @param v - The View to attach the popup menu to.
	 */
	private void showPopupMenu(View v) {
		PopupMenu popup = new PopupMenu(mContext, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.options, popup.getMenu());
		popup.setOnMenuItemClickListener(this);
		popup.show();
	}
	


	/**
	 * Handle popup menu item clicks.
	 */
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.options_edit:
			// Start TaskActivity to edit the given task.
			Intent intent = new Intent(mContext, TaskActivity.class);
			intent.putExtra(Constant.TASK_EXTRA, mCurrentTask);
			intent.putExtra(Constant.POSITION_EXTRA, mCurrentHolder.childPosition);
			((AllTasksFragment) ((MainActivity) mContext).getFragmentAt(2))
					.startActivityForResult(intent, Constant.EDIT_TASK_REQUEST);
			return true;
			
		case R.id.options_move:
			// Display a DatePicker for moving the task to another date.
			DialogFragment datePicker = new DatePickerFragment();
			Bundle args = new Bundle();
			args.putString(Constant.DATEPICKER_TITLE_EXTRA, 
					mContext.getString(R.string.move_task));
			args.putParcelable(Constant.TASK_EXTRA, mCurrentTask);
			datePicker.setArguments(args);
			// Set the target fragment for the result. Communication should
			//  go via MainActivity.
			datePicker.setTargetFragment(((MainActivity) mContext)
					.getFragmentAt(2), Constant.MOVE_TASK_REQUEST);
			datePicker.show(((MainActivity) mContext)
					.getSupportFragmentManager(), "datePicker");
			return true;
			
		case R.id.options_delete:
			// Show dialog to the user asking for confirmation of deletion.
			String dialogMessage = mCurrentHolder.taskText.getText().toString() 
					+ " " +  mContext.getString(R.string.delete_list_message);
			Utilities.showConfirmDialog(mContext, null, dialogMessage, 
					mContext.getString(R.string.delete), 
					new OnConfirmListener() {
				@Override
				public void onConfirm(DialogInterface dialog, int id) {
					String date = (String) getGroup(mCurrentHolder.groupPosition);
					if (mTasks.get(date).size() == 1) {
						mDates.remove(date);
						mTasks.remove(date);
					} else {
						mTasks.get(date).remove(mCurrentTask);
					}
					notifyDataSetChanged();
					new DeleteTask(mContext, mCurrentTask).execute();
					((MainActivity) mContext).updateNeighborFragments();
				}
			});
			return true;
			
		default:
			return false;
		}
	}
}
