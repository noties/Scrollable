package ru.noties.scrollable.sample.pager.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import ru.noties.ccf.CCFAnimator;
import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.BaseActivity;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.SampleHeaderView;
import ru.noties.scrollable.sample.TabsLayout;

public class FragmentPagerActivity extends BaseActivity {

    private interface CurrentFragment {
        @Nullable
        FragmentPagerFragment currentFragment();
    }

    @Override
    public void onCreate(Bundle sis) {
        super.onCreate(sis);



        setContentView(R.layout.activity_fragment_pager);

        final ScrollableLayout scrollableLayout = findView(R.id.scrollable_layout);
        final SampleHeaderView header = findView(R.id.header);
        final ViewPager viewPager = findView(R.id.view_pager);
        final TabsLayout tabsLayout = findView(R.id.tabs);

        final FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager(), items(getApplicationContext()));
        viewPager.setAdapter(adapter);
        tabsLayout.setViewPager(viewPager);

        scrollableLayout.setDraggableView(tabsLayout);

        final CurrentFragment currentFragment = new CurrentFragmentImpl(viewPager, getSupportFragmentManager());

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                final FragmentPagerFragment fragment = currentFragment.currentFragment();
                return fragment != null && fragment.canScrollVertically(direction);
            }
        });

        scrollableLayout.setOnFlingOverListener(new OnFlingOverListener() {
            @Override
            public void onFlingOver(int y, long duration) {
                final FragmentPagerFragment fragment = currentFragment.currentFragment();
                if (fragment != null) {
                    fragment.onFlingOver(y, duration);
                }
            }
        });

        scrollableLayout.addOnScrollChangedListener(new OnScrollChangedListener() {

            final CCFAnimator mAnimator = CCFAnimator.rgb(header.getExpandedColor(), header.getCollapsedColor());

            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {

//                Debug.i("y: %s, oldY: %s, maxY: %s", y, oldY, maxY);

                final float tabsTranslationY;
                if (y < maxY) {
                    tabsTranslationY = .0F;
                } else {
                    tabsTranslationY = y - maxY;
                }

                tabsLayout.setTranslationY(tabsTranslationY);

                // parallax effect for collapse/expand
                final float ratio = (float) y / maxY;
                header.setBackgroundColor(mAnimator.getColor(ratio));
                header.getTextView().setAlpha(1.F - ratio);
                header.getTextView().setTranslationY(y / 2);
            }
        });
    }

    private static List<FragmentPagerAdapter.Item> items(Context context) {
        final Resources r = context.getResources();
        final List<FragmentPagerAdapter.Item> items = new ArrayList<>(4);

        // RecyclerView
        items.add(new FragmentPagerAdapter.Item(
                r.getString(R.string.fragment_pager_tab_recycler_view),
                new FragmentPagerAdapter.Provider() {
                    @Override
                    public Fragment provide() {
                        return new FragmentPagerFragmentRecyclerView();
                    }
                }
        ));

        // ListView
        items.add(new FragmentPagerAdapter.Item(
                r.getString(R.string.fragment_pager_tab_list_view),
                new FragmentPagerAdapter.Provider() {
                    @Override
                    public Fragment provide() {
                        return new FragmentPagerFragmentListView();
                    }
                }
        ));

        // ScrollView
        items.add(new FragmentPagerAdapter.Item(
                r.getString(R.string.fragment_pager_tab_scroll_view),
                new FragmentPagerAdapter.Provider() {
                    @Override
                    public Fragment provide() {
                        return new FragmentPagerFragmentScrollView();
                    }
                }
        ));

        // WebView
        items.add(new FragmentPagerAdapter.Item(
                r.getString(R.string.fragment_pager_tab_web_view),
                new FragmentPagerAdapter.Provider() {
                    @Override
                    public Fragment provide() {
                        return new FragmentPagerFragmentWebView();
                    }
                }
        ));

        return items;
    }

    private static class CurrentFragmentImpl implements CurrentFragment {

        private final ViewPager mViewPager;
        private final FragmentManager mFragmentManager;
        private final FragmentPagerAdapter mAdapter;

        CurrentFragmentImpl(ViewPager pager, FragmentManager manager) {
            mViewPager = pager;
            mFragmentManager = manager;
            mAdapter = (FragmentPagerAdapter) pager.getAdapter();
        }

        @Override
        @Nullable
        public FragmentPagerFragment currentFragment() {
            final FragmentPagerFragment out;
            final int position = mViewPager.getCurrentItem();
            if (position < 0
                    || position >= mAdapter.getCount()) {
                out = null;
            } else {
                final String tag = makeFragmentName(mViewPager.getId(), mAdapter.getItemId(position));
                final Fragment fragment = mFragmentManager.findFragmentByTag(tag);
                if (fragment != null) {
                    out = (FragmentPagerFragment) fragment;
                } else {
                    // fragment is still not attached
                    out = null;
                }
            }
            return out;
        }

        // this is really a bad thing from Google. One cannot possible obtain normally
        // an instance of a fragment that is attached. Bad, really bad
        private static String makeFragmentName(int viewId, long id) {
            return "android:switcher:" + viewId + ":" + id;
        }
    }
}
