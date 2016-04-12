package ru.noties.scrollable.sample;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;

/**
 * Created by Dimitry Ivanov on 21.08.2015.
 */
public class ScrollableFragment extends Fragment {

    public static ScrollableFragment newInstance() {
        final Bundle bundle = new Bundle();

        final ScrollableFragment fragment = new ScrollableFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {
        final View view = inflater.inflate(R.layout.fragment_scrollable, parent, false);
        final ScrollableLayout scrollableLayout = findView(view, R.id.scrollable_layout);
        final ViewPager header = findView(view, R.id.header_view_pager);
        final ViewPager pager = findView(view, R.id.view_pager);
        final TabsLayout tabsLayout = findView(view, R.id.tabs);

        final ViewPagerAdapter adapter = new ViewPagerAdapter(
                getChildFragmentManager(),
                getResources(),
                fragments()
        );
        pager.setAdapter(adapter);
        tabsLayout.setViewPager(pager);
        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return adapter.canScrollVertically(pager.getCurrentItem(), direction);
            }
        });
        scrollableLayout.setOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {
                final float tabsTranslationY;
                if (y < maxY) {
                    tabsTranslationY = .0F;
                } else {
                    tabsTranslationY = y - maxY;
                }

                tabsLayout.setTranslationY(tabsTranslationY);

                header.setTranslationY(y / 2);
            }
        });

        scrollableLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                final ValueAnimator animator = scrollableLayout.animateScroll(0);
                animator.setDuration(5000L);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.start();
            }
        }, 2000L);

        final HeaderPagerAdapter headerPagerAdapter = new HeaderPagerAdapter(getChildFragmentManager(), 20);
        header.setAdapter(headerPagerAdapter);

        return view;
    }

    private List<BaseFragment> fragments() {

        final FragmentManager manager = getChildFragmentManager();

        final BaseFragment list;
        {
            final Fragment fragment = manager.findFragmentByTag(ListViewFragment.TAG);
            if (fragment == null) {
                list = ListViewFragment.newInstance(0x80FF0000);
            } else {
                list = (ListViewFragment) fragment;
            }
        }

        final BaseFragment recycler;
        {
            final Fragment fragment = manager.findFragmentByTag(RecyclerViewFragment.TAG);
            if (fragment == null) {
                recycler = RecyclerViewFragment.newInstance(0x8000FF00);
            } else {
                recycler = (RecyclerViewFragment) fragment;
            }
        }

        return Arrays.asList(list, recycler);
    }

    protected <V> V findView(View view, @IdRes int id) {
        //noinspection unchecked
        return (V) view.findViewById(id);
    }

    private static class HeaderPagerAdapter extends FragmentPagerAdapterExt {

        private final int mCount;
        private final ColorRandomizer mColorRandomizer;

        public HeaderPagerAdapter(FragmentManager fm, int count) {
            super(fm);
            this.mCount = count;
            this.mColorRandomizer = new ColorRandomizer(new int[] {0xFFf44336, 0xFF9c27b0, 0xFF3f51b5, 0xFF03a9f4, 0xFF8bc34a});
        }

        @Override
        public Fragment getItem(int position) {
            return HeaderFragment.newInstance(mColorRandomizer.next());
        }

        @Override
        public int getCount() {
            return mCount;
        }
    }

    public static class HeaderFragment extends Fragment {

        private static final String ARG_COLOR = "arg.Color";

        public static HeaderFragment newInstance(int color) {
            final Bundle bundle = new Bundle();
            bundle.putInt(ARG_COLOR, color);

            final HeaderFragment fragment = new HeaderFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {
            final TextView textView = new TextView(getActivity());
            textView.setText("HEADER");
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            textView.setBackgroundColor(getArguments().getInt(ARG_COLOR));
            return textView;
        }
    }
}
