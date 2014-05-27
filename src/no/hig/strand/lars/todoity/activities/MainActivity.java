package no.hig.strand.lars.todoity.activities;

import java.util.ArrayList;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.adapters.TabsPagerAdapter;
import no.hig.strand.lars.todoity.data.Task;
import no.hig.strand.lars.todoity.fragments.AllTasksFragment;
import no.hig.strand.lars.todoity.fragments.TodayFragment;
import no.hig.strand.lars.todoity.fragments.WeekFragment;
import no.hig.strand.lars.todoity.helpers.DatabaseUtilities.OnTasksLoadedListener;
import no.hig.strand.lars.todoity.helpers.DatePickerFragment.OnDateSetListener;
import no.hig.strand.lars.todoity.helpers.Installation;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements 
		OnTasksLoadedListener, OnDateSetListener {
	
	private TabsPagerAdapter mTabsPagerAdapter;
	private ViewPager mViewPager;
	//private TasksDb mTasksDb;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // First launch.
        Installation.id(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsPagerAdapter);
        
        setupUI();
    }



	@Override
	protected void onResume() {
		super.onResume();
	}



	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new_list:
			startActivity(new Intent(this, ListActivity.class));
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}



	private void setupUI() {
		final ActionBar actionBar = getActionBar();
    	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    	
    	ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition());
			}
			
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {}
		};
		
		actionBar.addTab(actionBar.newTab().setText(R.string.today)
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText(R.string.week)
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText(R.string.all)
				.setTabListener(tabListener));
		
		mViewPager.setOnPageChangeListener(
				new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
			}
		});
	}
	
	
	
	/**
	 * Returns the fragment at the specified position in the view pager if
	 * it exists, null otherwise.
	 * @param position
	 * @return The Fragment at the specified position.
	 */
	public Fragment getFragmentAt(int position) {
		return mTabsPagerAdapter.getRegisteredFragment(position);
	}
	
	
	
	/**
	 * Calls update on the fragments that are neighbors to the currently
	 *  visible fragment in the view pager. This essentially rereads their
	 *  data from the database and redraws the lists.
	 */
	public void updateNeighborFragments() {
		Fragment fragment;
		Fragment currentFragment = mTabsPagerAdapter.getRegisteredFragment(
				mViewPager.getCurrentItem());
		for (int i = 0; i < mTabsPagerAdapter.getCount(); i++) {
			fragment = mTabsPagerAdapter.getRegisteredFragment(i);
			if (fragment != currentFragment) {
				if (fragment instanceof TodayFragment) {
					((TodayFragment) fragment).update();
				} else if (fragment instanceof WeekFragment) {
					((WeekFragment) fragment).update();
				} else  if (fragment instanceof AllTasksFragment){
					((AllTasksFragment) fragment).update();
				}
			}
		}
	}



	@Override
	public void onTasksLoaded(ArrayList<Task> tasks) {
		Fragment fragment = mTabsPagerAdapter.getRegisteredFragment(0);
		if (fragment instanceof TodayFragment) {
			((TodayFragment) fragment).updateList(tasks);
		}
	}



	@Override
	public void onDateSet(String date, Fragment target, Bundle args) {
		if (target instanceof TodayFragment) {
			((TodayFragment) target).onDateSet(date, null, args);
		} else if (target instanceof WeekFragment) {
			((WeekFragment) target).onDateSet(date, null, args);
		} else if (target instanceof AllTasksFragment) {
			((AllTasksFragment) target).onDateSet(date, null, args);
		}
	}
	
}
