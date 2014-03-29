package no.hig.strand.lars.todoity.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.ListActivity;
import no.hig.strand.lars.todoity.adapters.AllTasksListAdapter;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.Task.TaskPriorityComparator;
import no.hig.strand.lars.todoity.data.TasksDatabase;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;

public class AllTasksFragment extends Fragment {

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
		
		setupUI();
		
		return mRootView;
	}
	
	
	
	@Override
	public void onResume() {
		update();
		super.onResume();
	}
	
	
	
	private void setupUI() {
		Button button = (Button) mRootView
				.findViewById(R.id.all_tasks_new_list_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), ListActivity.class));
			}
		});
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
