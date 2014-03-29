package no.hig.strand.lars.todoity.fragments;

import java.util.ArrayList;
import java.util.Collections;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.ListActivity;
import no.hig.strand.lars.todoity.activities.MainActivity;
import no.hig.strand.lars.todoity.adapters.TodayListAdapter;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.data.Task.TaskPriorityComparator;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.DeleteTask;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.GetTasksByDateTask;
import no.hig.strand.lars.todoity.helpers.Utilities;
import no.hig.strand.lars.todoity.helpers.Utilities.OnConfirmListener;
import no.hig.strand.lars.todoity.views.DynamicListView;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;


public class TodayFragment extends Fragment {
	
	private View mRootView;
	private DynamicListView mListView;
	private TodayListAdapter mAdapter;
	private ArrayList<Task> mTasks;
	
	public ProgressBar mProgress;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_today, container, false);
		
		mListView = (DynamicListView) mRootView.findViewById(R.id.today_list);
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		
		setupUI();
		
		return mRootView;
	}
	
	
	
	@Override
	public void onResume() {
		update();
		super.onResume();
	}



	private void setupUI() {
		Button button = (Button) mRootView.findViewById(
				R.id.today_delete_list_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show dialog to the user asking for confirmation of deletion.
				String dialogTitle = getString(R.string.delete) + " " + 
						mTasks.size() + " " + getString(R.string.tasks);
				String dialogMessage = getString(R.string.delete_list_message1) 
						+ " " + getString(R.string.today) + " " + 
						getString(R.string.delete_list_message2);
				Utilities.showConfirmDialog(getActivity(), dialogTitle, 
						dialogMessage, getString(R.string.delete), 
						new OnConfirmListener() {
					@Override
					public void onConfirm(DialogInterface dialog, int id) {
						ArrayList<Task> tasks = mTasks;
						updateList(new ArrayList<Task>());
						for (Task task : tasks) {
							new DeleteTask(getActivity(), task).execute();
						}
						((MainActivity) getActivity()).updateNeighborFragments();
					}
				});
			}
		});
		
		button = (Button) mRootView.findViewById(R.id.today_edit_list_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Check if there are tasks to edit
				/*if (! mTasks.isEmpty()) {
					Intent intent = new Intent(getActivity(),
							ListActivity.class);
					intent.putExtra(Constant.TASKS_EXTRA, mTasks);
					startActivity(intent);
				}*/
			}
		});
		
		button = (Button) mRootView.findViewById(R.id.today_new_list_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), ListActivity.class));
			}
		});
		
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.context, menu);
				return true;
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				default:
					return false;
				}
			}
			
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {}
		});
	}
	
	
	
	public void update() {
		new GetTasksByDateTask(getActivity(), 
				Utilities.getTodayDate()).execute();
	}
	
	
	
	public void updateList(ArrayList<Task> tasks) {
		Collections.sort(tasks, new TaskPriorityComparator());
		mTasks = tasks;
		mAdapter = new TodayListAdapter(getActivity(), mTasks);
		mListView.setTaskList(mTasks);
		mListView.setAdapter(mAdapter);
	}

}
