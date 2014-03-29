package no.hig.strand.lars.todoity.adapters;

import no.hig.strand.lars.todoity.fragments.AllTasksFragment;
import no.hig.strand.lars.todoity.fragments.TodayFragment;
import no.hig.strand.lars.todoity.fragments.WeekFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

public class TabsPagerAdapter extends FragmentStatePagerAdapter {
	private SparseArray<Fragment> registeredFragments = 
			new SparseArray<Fragment>();
	
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

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		registeredFragments.remove(position);
		super.destroyItem(container, position, object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(
				container, position);
		registeredFragments.put(position, fragment);
		return fragment;
	}
	
	public Fragment getRegisteredFragment(int position) {
		return registeredFragments.get(position);
	}
	
}
