package ru.noties.scrollable.sample;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;

public class MainActivity extends BaseActivity implements ConfigurationFragmentCallbacks {

    private static final String ARG_LAST_SCROLL_Y = "arg.LastScrollY";

    private ScrollableLayout mScrollableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View header = findViewById(R.id.header);
        final TabsLayout tabs = findView(this, R.id.tabs);

        mScrollableLayout = findView(this, R.id.scrollable_layout);
        mScrollableLayout.setDraggableView(tabs);

        final ViewPager viewPager = findView(this, R.id.view_pager);
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getResources(), getFragments());
        viewPager.setAdapter(adapter);

        tabs.setViewPager(viewPager);

        mScrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return adapter.canScrollVertically(viewPager.getCurrentItem(), direction);
            }
        });

        mScrollableLayout.setOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {

                final float tabsTranslationY;
                if (y < maxY) {
                    tabsTranslationY = .0F;
                } else {
                    tabsTranslationY = y - maxY;
                }

                tabs.setTranslationY(tabsTranslationY);

                header.setTranslationY(y / 2);
            }
        });

        if (savedInstanceState != null) {
            final int y = savedInstanceState.getInt(ARG_LAST_SCROLL_Y);
            mScrollableLayout.post(new Runnable() {
                @Override
                public void run() {
                    mScrollableLayout.scrollTo(0, y);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_LAST_SCROLL_Y, mScrollableLayout.getScrollY());
        super.onSaveInstanceState(outState);
    }

    private List<BaseFragment> getFragments() {

        final FragmentManager manager = getSupportFragmentManager();
        final ColorRandomizer colorRandomizer = new ColorRandomizer(getResources().getIntArray(R.array.fragment_colors));
        final List<BaseFragment> list = new ArrayList<>();

        ConfigurationFragment configurationFragment
                = (ConfigurationFragment) manager.findFragmentByTag(ConfigurationFragment.TAG);
        if (configurationFragment == null) {
            configurationFragment = ConfigurationFragment.newInstance(colorRandomizer.next());
        }

        ListViewFragment listViewFragment
                = (ListViewFragment) manager.findFragmentByTag(ListViewFragment.TAG);
        if (listViewFragment == null) {
            listViewFragment = ListViewFragment.newInstance(colorRandomizer.next());
        }

        ScrollViewFragment scrollViewFragment
                = (ScrollViewFragment) manager.findFragmentByTag(ScrollViewFragment.TAG);
        if (scrollViewFragment == null) {
            scrollViewFragment = ScrollViewFragment.newInstance(colorRandomizer.next());
        }

        RecyclerViewFragment recyclerViewFragment
                = (RecyclerViewFragment) manager.findFragmentByTag(RecyclerViewFragment.TAG);
        if (recyclerViewFragment == null) {
            recyclerViewFragment = RecyclerViewFragment.newInstance(colorRandomizer.next());
        }

        Collections.addAll(list, configurationFragment, listViewFragment, scrollViewFragment, recyclerViewFragment);

        return list;
    }

    @Override
    public void onFrictionChanged(float friction) {
        mScrollableLayout.setFriction(friction);
    }

    @Override
    public void openDialog(float friction) {
        final ScrollableDialog dialog = ScrollableDialog.newInstance(friction);
        dialog.show(getSupportFragmentManager(), null);
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapterExt {

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


}
