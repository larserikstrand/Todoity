package no.hig.strand.lars.todoity.adapters;

import no.hig.strand.lars.todoity.fragments.AllTasksFragment;
import no.hig.strand.lars.todoity.fragments.TodayFragment;
import no.hig.strand.lars.todoity.fragments.WeekFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabsPagerAdapter extends FragmentStatePagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case 0: return new TodayFragment();
		case 1: return new WeekFragment();
		case 2: return new AllTasksFragment();
		default: return new TodayFragment();
		}
	}

	@Override
	public int getCount() {
		return 3;
	}
	
}
