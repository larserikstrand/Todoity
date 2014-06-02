package no.hig.strand.lars.todoity.activities;

import java.util.ArrayList;
import java.util.Collections;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.adapters.ListAdapter;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.Task.TaskPriorityComparator;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.MoveTask;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.OnTasksLoadedListener;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.SaveTask;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.UpdateTask;
import no.hig.strand.lars.todoity.helpers.DatePickerFragment;
import no.hig.strand.lars.todoity.helpers.DatePickerFragment.OnDateSetListener;
import no.hig.strand.lars.todoity.helpers.Utilities;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity for creating new lists of tasks or editing existing lists of tasks.
 * @author LarsErik
 * 
 */
public class ListActivity extends FragmentActivity implements 
		OnDateSetListener, OnTasksLoadedListener {

	private ArrayList<Task> mTasks;
	private ListAdapter mAdapter;
	private ListView mListView;
	private TextView mDate;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		mDate = (TextView) findViewById(R.id.date_text);
		mTasks = new ArrayList<Task>();
		
		Intent data = getIntent();
		// If a date was passed to the Activity, display that date.
		if (data.hasExtra(Constant.DATE_EXTRA)) {
			mDate.setText(data.getStringExtra(Constant.DATE_EXTRA));
		// No date passed to Activity. Display default (today) date.
		} else {
			mDate.setText(Utilities.getTodayDate());
		}
		
		mAdapter = new ListAdapter(this, mTasks);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		
		// Get tasks by a given date. 'OnTasksLoaded' is called when finished.
		new DatabaseUtilities.GetTasksByDateTask(
				this, mDate.getText().toString()).execute();
	}

	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_list, menu);
        return true;
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Home (back) button is pressed.
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		// Change date button is pressed.
		case R.id.action_change_date:
			// Display a DatePicker fragment. 'OnDateSet' is called when a
			//  date has been selected.
			DialogFragment datePicker = new DatePickerFragment();
			datePicker.show(getSupportFragmentManager(), "datePicker");
			return true;
		// New task button is pressed.
		case R.id.action_new_task:
			Intent intent = new Intent(ListActivity.this, TaskActivity.class);
			startActivityForResult(intent, Constant.NEW_TASK_REQUEST);
			return true;
		// Done button is pressed.
		case R.id.action_done:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		switch (requestCode) {
		// A new task is returned from TaskActivity.
		case Constant.NEW_TASK_REQUEST:
			if (resultCode == RESULT_OK) {
				Task task = data.getParcelableExtra(Constant.TASK_EXTRA);
				task.setDate(mDate.getText().toString());
				
				mTasks.add(task);
				task.setPriority(mTasks.size());
				mAdapter.notifyDataSetChanged();
				
				// Save task to databases.
				new SaveTask(this, task).execute();
			}
			return;
		// An edited task is returned from TaskActivity.
		case Constant.EDIT_TASK_REQUEST:
			if (resultCode == RESULT_OK) {
				Task task = data.getParcelableExtra(Constant.TASK_EXTRA);
				int position = data.getIntExtra(Constant.POSITION_EXTRA, -1);
				
				mTasks.set(position, task);
				mAdapter.notifyDataSetChanged();
				
				// Update task in databases.
				new UpdateTask(this, task).execute();
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	
	/**
	 * Called from the DatePickerFragment when a date is selected.
	 * Receives the selected date, and arguments passed to the DatePicker.
	 */
	@Override
	public void onDateSet(String date, Fragment target, Bundle args) {
		// If selected date is different from current date.
		if (! mDate.getText().toString().equals(date)) {
			// Check if arguments (task) were returned from the DatePicker,
			//  ie. the date for a task should be changed.
			if (args != null) {
				Task task = args.getParcelable(Constant.TASK_EXTRA);
				task.setDate(date);
				mTasks.remove(task);
				mAdapter.notifyDataSetChanged();
				new MoveTask(this, task, date).execute();
			// No specific task in DatePicker, simply load all tasks for
			//  the selected date.
			} else {
				mDate.setText(date);
				new DatabaseUtilities.GetTasksByDateTask(this, date).execute();
			}
		}
	}



	/**
	 * Called when the tasks for the current date have been loaded
	 *  from the database.
	 */
	@Override
	public void onTasksLoaded(ArrayList<Task> tasks) {
		// Sort tasks by priority and add them to the list.
		Collections.sort(tasks, new TaskPriorityComparator());
		mTasks.clear();
		mTasks.addAll(tasks);
		mAdapter.notifyDataSetChanged();
	}
}
