package no.hig.strand.lars.todoity.activities;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.adapters.ListAdapter;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.helpers.DatePickerFragment;
import no.hig.strand.lars.todoity.helpers.DatePickerFragment.OnDateSetListener;
import no.hig.strand.lars.todoity.helpers.Utilities;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ListActivity extends FragmentActivity implements OnDateSetListener{

	private ArrayList<Task> mTasks;
	private ListAdapter mAdapter;
	private ListView mList;
	private TextView mDate;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		mDate = (TextView) findViewById(R.id.date_text);
		Intent data = getIntent();
		if (data.hasExtra(Constant.TASKS_EXTRA)) {
			mTasks = data.getParcelableArrayListExtra(Constant.TASKS_EXTRA);
		} else {
			mTasks = new ArrayList<Task>();
		}
		if (data.hasExtra(Constant.DATE_EXTRA)) {
			mDate.setText(data.getStringExtra(Constant.DATE_EXTRA));
		} else {
			mDate.setText(Utilities.getTodayDate());
		}
		mAdapter = new ListAdapter(this, mTasks);
		
		setupUI();
	}

	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	private void setupUI() {
		mList = (ListView) findViewById(R.id.list);
		mList.setAdapter(mAdapter);
		
		Button button = (Button) findViewById(R.id.date_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment datePicker = new DatePickerFragment();
				datePicker.show(getSupportFragmentManager(), "datePicker");
			}
		});
		
		button = (Button) findViewById(R.id.new_task_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ListActivity.this,
						TaskActivity.class);
				startActivityForResult(intent, Constant.NEW_TASK_REQUEST);
			}
		});
		
		button = (Button) findViewById(R.id.done_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTasks.size() > 0) {
					Intent data = new Intent();
					data.putExtra(Constant.TASKS_EXTRA, mTasks);
					setResult(RESULT_OK, data);
				}
				finish();
			}
		});
	}

	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		switch (requestCode) {
		case Constant.NEW_TASK_REQUEST:
			if (resultCode == RESULT_OK) {
				Task task = data.getParcelableExtra(Constant.TASK_EXTRA);
				task.setDate(mDate.getText().toString());
				
				mTasks.add(task);
				mAdapter.notifyDataSetChanged();
				
				//new DatabaseUtilities.SaveTask(this, task).execute();
				
			}
			return;
		case Constant.EDIT_TASK_REQUEST:
			if (resultCode == RESULT_OK) {
				Task task = data.getParcelableExtra(Constant.TASK_EXTRA);
				int position = data.getIntExtra(Constant.POSITION_EXTRA, -1);
				
				mTasks.set(position, task);
				mAdapter.notifyDataSetChanged();
				//new DatabaseUtilities.UpdateTask(this, task).execute();
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	
	@Override
	public void onDateSet(String date) {
		mDate.setText(date);
		// TODO load dates...
	}
}
