package no.hig.strand.lars.todoity.activities;

import no.hig.strand.lars.todoity.R;
import no.hig.strand.lars.todoity.adapters.TabsPagerAdapter;
import no.hig.strand.lars.todoity.helpers.Installation;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class MainActivity extends FragmentActivity {
	
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}



	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
		actionBar.addTab(actionBar.newTab().setText(R.string.all_tasks)
				.setTabListener(tabListener));
		
		mViewPager.setOnPageChangeListener(
				new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
			}
		});
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
    
	
	
}
