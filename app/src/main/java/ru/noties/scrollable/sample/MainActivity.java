package ru.noties.scrollable.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.next.TabsLayout;
import ru.noties.scrollable.sample.next.dialog.ScrollableDialog;

public class MainActivity extends BaseActivity implements ConfigurationFragmentCallbacks {

    private static final String ARG_LAST_SCROLL_Y = "arg.LastScrollY";

    private ScrollableLayout mScrollableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View header = findViewById(R.id.header);
        final TabsLayout tabs = findView(R.id.tabs);

        mScrollableLayout = findView(R.id.scrollable_layout);
        mScrollableLayout.setDraggableView(tabs);

        final ViewPager viewPager = findView(R.id.view_pager);
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getResources(), getFragments());
        viewPager.setAdapter(adapter);

        tabs.setViewPager(viewPager);

        mScrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return adapter.canScrollVertically(viewPager.getCurrentItem(), direction);
            }
        });
        mScrollableLayout.setOnFlingOverListener(new OnFlingOverListener() {
            @Override
            public void onFlingOver(int y, long duration) {
                adapter.getItem(viewPager.getCurrentItem()).onFlingOver(y, duration);
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

                // parallax effect for collapse/expand
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
        final ScrollableDialog dialog = new ScrollableDialog();
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void openActivity(Intent intent) {
        startActivity(intent);
    }


}
