package ru.noties.scrollable.sample;

import android.content.res.Resources;
import android.support.v4.app.FragmentManager;

import java.util.List;

/**
 * Created by Dimitry Ivanov on 21.08.2015.
 */
class ViewPagerAdapter extends FragmentPagerAdapterExt {

    private final Resources mResources;
    private final List<BaseFragment> mFragments;

    public ViewPagerAdapter(FragmentManager fm, Resources r, List<BaseFragment> fragments) {
        super(fm);
        this.mResources = r;
        this.mFragments = fragments;
    }

    @Override
    public BaseFragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments != null ? mFragments.size() : 0;
    }

    @Override
    public String makeFragmentTag(int position) {
        return mFragments.get(position).getSelfTag();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getTitle(mResources);
    }

    boolean canScrollVertically(int position, int direction) {
        return getItem(position).canScrollVertically(direction);
    }
}
