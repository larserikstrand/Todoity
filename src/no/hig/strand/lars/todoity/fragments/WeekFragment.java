package no.hig.strand.lars.todoity.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.MainActivity;
import no.hig.strand.lars.todoity.adapters.WeekListAdapter;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.Task.TaskPriorityComparator;
import no.hig.strand.lars.todoity.data.TasksDatabase;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.MoveTask;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.UpdateTask;
import no.hig.strand.lars.todoity.helpers.DatePickerFragment.OnDateSetListener;
import no.hig.strand.lars.todoity.helpers.Utilities;
import no.hig.strand.lars.todoity.helpers.Utilities.DateComparator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class WeekFragment extends Fragment implements OnDateSetListener {
	
	private View mRootView;
	private ExpandableListView mListView;
	private WeekListAdapter mAdapter;
	private List<String> mDates;
	private HashMap<String, List<Task>> mTasks;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_week,
				container, false);
		
		mDates = new ArrayList<String>();
		mTasks = new HashMap<String, List<Task>>();
		mListView = (ExpandableListView) mRootView.findViewById(R.id.week_list);
		mAdapter = new WeekListAdapter(getActivity(), mDates, mTasks);
		mListView.setAdapter(mAdapter);
		
		return mRootView;
	}
	
	
	
	@Override
	public void onResume() {
		update();
		super.onResume();
	}
	
	
	
	public void update() {
		new GetTasksTask(getActivity()).execute();
	}
	
	
	
	public void updateList(ArrayList<Task> tasks) {
		mDates.clear();
		mTasks.clear();
		
		for (Task task : tasks) {
			String date = task.getDate();
			if (! mDates.contains(date)) {
				mDates.add(date);
				mTasks.put(date, new ArrayList<Task>());
			}
			mTasks.get(date).add(task);
		}
		
		mAdapter.notifyDataSetChanged();
	}
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constant.EDIT_TASK_REQUEST:
			if (resultCode == Activity.RESULT_OK) {
				Task task = data.getParcelableExtra(Constant.TASK_EXTRA);
				int position = data.getIntExtra(Constant.POSITION_EXTRA, -1);
				
				mTasks.get(task.getDate()).set(position, task);
				new UpdateTask(getActivity(), task).execute();
				mAdapter.notifyDataSetChanged();
				((MainActivity) getActivity()).updateNeighborFragments();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}



	private class GetTasksTask extends 
		AsyncTask<Void, Void, ArrayList<Task>> {
		
		private TasksDatabase tasksDb;

		public GetTasksTask(Context context) {
			tasksDb = TasksDatabase.getInstance(context);
		}

		@Override
		protected ArrayList<Task> doInBackground(Void... params) {
			ArrayList<Task> weekTasks = new ArrayList<Task>();
			
			ArrayList<String> dates = Utilities.getDates(
					Constant.TASKS_TO_DISPLAY_IN_WEEK);
			TaskPriorityComparator comparator = new TaskPriorityComparator();
			ArrayList<Task> dateTasks;
			for (String date : dates) {
				dateTasks = tasksDb.getTasksByDate(date);
				Collections.sort(dateTasks, comparator);
				weekTasks.addAll(dateTasks);
			}
			
			return weekTasks;
		}

		@Override
		protected void onPostExecute(ArrayList<Task> result) {
			updateList(result);
		}
	}
	
	

	@Override
	public void onDateSet(String date, Fragment target, Bundle args) {
		// Because new items are created, we reload the entire adapter.
		Task task = args.getParcelable(Constant.TASK_EXTRA);
		String oldDate = task.getDate();
		if (! date.equals(task.getDate())) {
			ArrayList<Task> tasks = (ArrayList<Task>) mTasks.get(oldDate);
			ArrayList<String> dates = Utilities.getDates(
					Constant.TASKS_TO_DISPLAY_IN_WEEK);
			if (dates.contains(date)) {
				if (! mDates.contains(date)) {
					mDates.add(date);
					mTasks.put(date, new ArrayList<Task>());
				}
				mTasks.get(date).add(task);
			}
			if (tasks.size() == 1) {
				mTasks.remove(oldDate);
				mDates.remove(oldDate);
			} else {
				mTasks.get(oldDate).remove(task);
			}
			Collections.sort(mDates, new DateComparator());
			TaskPriorityComparator comparator = new TaskPriorityComparator();
			for (String d : mDates) {
				Collections.sort(mTasks.get(d), comparator);
			}
			
			task.setDate(date);
			mAdapter = new WeekListAdapter(getActivity(), mDates, mTasks);
			mListView.setAdapter(mAdapter);;
			new MoveTask(getActivity(), task, date).execute();
			((MainActivity) getActivity()).updateNeighborFragments();
		}
	}
	
}
