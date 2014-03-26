package no.hig.strand.lars.todoity.fragments;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.activities.ListActivity;
import no.hig.strand.lars.todoity.adapters.TodayListAdapter;
import no.hig.strand.lars.todoity.data.Constant;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.helpers.Utilities;
import no.hig.strand.lars.todoity.views.DraggableListView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;


public class TodayFragment extends Fragment {
	
	private View mRootView;
	private DraggableListView mList;
	private TodayListAdapter mAdapter;
	private ArrayList<Task> mTasks;
	//private TodayListAdapter mAdapter;
	private int mSelectedTask;
	
	public ProgressBar mProgress;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_today, container, false);
		
		mTasks = new ArrayList<Task>();
		mAdapter = new TodayListAdapter(getActivity(), mTasks);
		mList = (DraggableListView) mRootView.findViewById(R.id.today_list);
		mList.setAdapter(mAdapter);
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progressBar);
		
		setupUI();
		
		return mRootView;
	}
	
	
	
	private void setupUI() {
		Button button = (Button) mRootView.findViewById(
				R.id.today_delete_list_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Show dialog to the user asking for confirmation of deletion.
				/*Utilities.showConfirmDialog(getActivity(), 
						getString(R.string.confirm), 
						getString(R.string.delete_list_message), 
						new Utilities.ConfirmDialogListener() {
					// The user confirms deletion.
					@Override
					public void PositiveClick(DialogInterface dialog, int id) {
						String date = Utilities.getTodayDate();
						new DeleteList(getActivity(), new OnDeletionCallback() {
							// Task has been deleted. Update UI.
							@Override
							public void onDeletionDone() {
								mTasks.clear();
								DraggableListView listView = (DraggableListView)
										mRootView.findViewById(R.id.tasks_list);
								listView.setAdapter(new TodayListAdapter(
										getActivity(), mTasks));
								Button edit = (Button) mRootView
										.findViewById(R.id.edit_button);
								Button delete = (Button) mRootView
										.findViewById(R.id.delete_button);
								edit.setEnabled(false);
								delete.setEnabled(false);
								((MainActivity)getActivity()).updateGeofences();
							}
						}).execute(date);
					}
				});*/
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
				Intent intent = new Intent(getActivity(), ListActivity.class);
				if (mTasks.size() > 0) {
					intent.putExtra(Constant.TASKS_EXTRA, mTasks);
				}
				startActivityForResult(intent, Constant.NEW_LIST_REQUEST);
			}
		});
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constant.NEW_LIST_REQUEST:
			if (resultCode == Activity.RESULT_OK) {
				ArrayList<Task> tasks = data.getParcelableArrayListExtra(
						Constant.TASKS_EXTRA);
				mTasks.clear();
				mTasks.addAll(tasks);
				mAdapter.notifyDataSetChanged();
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	
	
	

}
