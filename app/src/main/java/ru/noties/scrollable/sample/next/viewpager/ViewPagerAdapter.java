package ru.noties.scrollable.sample.next.viewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

class ViewPagerAdapter extends FragmentPagerAdapter {

    interface Provider {
        Fragment provide();
    }

    static class Item {

        final String name;
        final Provider provider;

        public Item(String name, Provider provider) {
            this.name = name;
            this.provider = provider;
        }
    }

    private final List<Item> mItems;

    ViewPagerAdapter(FragmentManager fm, List<Item> items) {
        super(fm);
        mItems = items;
    }

    @Override
    public Fragment getItem(int position) {
        return mItems.get(position).provider.provide();
    }

    @Override
    public int getCount() {
        return mItems != null
                ? mItems.size()
                : 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mItems.get(position).name;
    }
}
