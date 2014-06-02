package no.hig.strand.lars.todoity.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.MainActivity;
import no.hig.strand.lars.todoity.adapters.AllTasksListAdapter;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.Task.TaskPriorityComparator;
import no.hig.strand.lars.todoity.data.TasksDatabase;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.MoveTask;
import no.hig.strand.lars.todoity.helpers.DatePickerFragment.OnDateSetListener;
import no.hig.strand.lars.todoity.helpers.Utilities.DateComparator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

/**
 * Fragment class used for displaying all tasks (including old tasks).
 * @author LarsErik
 *
 */
public class AllTasksFragment extends Fragment implements OnDateSetListener {

	private View mRootView;
	private ExpandableListView mListView;
	private AllTasksListAdapter mAdapter;
	private List<String> mDates;
	private HashMap<String, List<Task>> mTasks;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_all_tasks,
				container, false);
		
		mDates = new ArrayList<String>();
		mTasks = new HashMap<String, List<Task>>();
		mListView = (ExpandableListView) mRootView
				.findViewById(R.id.all_tasks_list);
		mAdapter = new AllTasksListAdapter(getActivity(), mDates, mTasks);
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
	public void onDateSet(String date, Fragment target, Bundle args) {
		// Because new items are created, we reload the entire adapter.
		Task task = args.getParcelable(Constant.TASK_EXTRA);
		String oldDate = task.getDate();
		if (! date.equals(task.getDate())) {
			ArrayList<Task> tasks = (ArrayList<Task>) mTasks.get(oldDate);
			if (! mDates.contains(date)) {
				mDates.add(date);
				mTasks.put(date, new ArrayList<Task>());
			}
			mTasks.get(date).add(task);
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
			// We need to set the adapter again. (Don't really know why.
			//  Perhaps because new children may have been added to the list.)
			mAdapter = new AllTasksListAdapter(getActivity(), mDates, mTasks);
			mListView.setAdapter(mAdapter);
			new MoveTask(getActivity(), task, date).execute();
			((MainActivity) getActivity()).updateNeighborFragments();
		}
	}
	
	
	
	private class GetTasksTask extends 
			AsyncTask<Void, Void, ArrayList<Task>> {

		private TasksDatabase tasksDb;

		public GetTasksTask(Context context) {
			tasksDb = TasksDatabase.getInstance(context);
		}

		@Override
		protected ArrayList<Task> doInBackground(Void... params) {
			ArrayList<Task> allTasks = new ArrayList<Task>();
			
			ArrayList<String> dates = tasksDb.getListDates();
			Collections.sort(dates, new DateComparator());
			TaskPriorityComparator comparator = new TaskPriorityComparator();
			ArrayList<Task> dateTasks;
			for (String date : dates) {
				dateTasks = tasksDb.getTasksByDate(date);
				Collections.sort(dateTasks, comparator);
				allTasks.addAll(dateTasks);
			}
			
			return allTasks;
		}

		@Override
		protected void onPostExecute(ArrayList<Task> result) {
			updateList(result);
		}
	}
	
}
