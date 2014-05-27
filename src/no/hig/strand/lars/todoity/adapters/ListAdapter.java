package no.hig.strand.lars.todoity.adapters;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.ListActivity;
import no.hig.strand.lars.todoity.activities.TaskActivity;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<Task> implements 
		OnMenuItemClickListener {

	private Context mContext;
	private ArrayList<Task> mTasks;
	private LayoutInflater mInflater;
	private ViewHolder mCurrentHolder;
	private Task mCurrentTask;
	
	private OnClickListener mOptionsListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mCurrentHolder = (ViewHolder) ((View) v.getParent()).getTag();
			mCurrentTask = mTasks.get(mCurrentHolder.position);
			showPopupMenu(v);
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
			holder.optionsButton = (ImageButton) convertView.findViewById(
					R.id.list_item_options_button);
			holder.position = position;
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Task task = mTasks.get(position);
		
		holder.taskText.setText(Task.getTaskTextFromTask(task));
		holder.subText.setText(Task.getSubTextFromTask(task));
		
		if (task.isFinished()) {
			holder.optionsButton.setVisibility(View.GONE);
			setStrikeThrough(holder);
		}
		
		holder.optionsButton.setOnClickListener(mOptionsListener);
		
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
		public ImageButton optionsButton;
		public int position;
	}


	
	private void showPopupMenu(View v) {
		PopupMenu popup = new PopupMenu(mContext, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.options, popup.getMenu());
		popup.setOnMenuItemClickListener(this);
		popup.show();
	}
	
	

	@Override
	public boolean onMenuItemClick(MenuItem item) {
switch (item.getItemId()) {
		
		case R.id.options_edit:
			Intent intent = new Intent(mContext, TaskActivity.class);
			intent.putExtra(Constant.TASK_EXTRA, mCurrentTask);
			intent.putExtra(Constant.POSITION_EXTRA, mCurrentHolder.position);
			
			((ListActivity) mContext).startActivityForResult(
					intent, Constant.EDIT_TASK_REQUEST);
			return true;
			
		case R.id.options_move:
			DialogFragment datePicker = new DatePickerFragment();
			Bundle args = new Bundle();
			args.putString(Constant.DATEPICKER_TITLE_EXTRA, 
					mContext.getString(R.string.move_task));
			args.putParcelable(Constant.TASK_EXTRA, mCurrentTask);
			datePicker.setArguments(args);
			datePicker.show(((ListActivity) mContext)
					.getSupportFragmentManager(), "datePicker");
			return true;
			
		case R.id.options_delete:
			// Show dialog to the user asking for confirmation of deletion.
			String dialogMessage = 
					mCurrentHolder.taskText.getText().toString() + " " +  
					mContext.getString(R.string.delete_list_message);
			Utilities.showConfirmDialog(mContext, null, 
					dialogMessage, 
					mContext.getString(R.string.delete), 
					new OnConfirmListener() {
				@Override
				public void onConfirm(DialogInterface dialog, int id) {
					Task task = mTasks.remove(mCurrentHolder.position);
					notifyDataSetChanged();
					new DeleteTask(mContext, task).execute();
				}
			});
			return true;
			
		default:
			return false;
		}
	}
	
}
