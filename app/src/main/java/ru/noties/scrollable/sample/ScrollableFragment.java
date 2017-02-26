package ru.noties.scrollable.sample;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.OverScrollListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.next.TabsLayout;

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
        final SwipeRefreshLayout swipeRefreshLayout = findView(view, R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }, 10000L);
            }
        });
        swipeRefreshLayout.setEnabled(false);
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
        scrollableLayout.setOnFlingOverListener(new OnFlingOverListener() {
            @Override
            public void onFlingOver(int y, long duration) {
                adapter.getItem(pager.getCurrentItem()).onFlingOver(y, duration);
            }
        });

//        scrollableLayout.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                final ValueAnimator animator = scrollableLayout.animateScroll(0);
//                animator.setDuration(5000L);
//                animator.setInterpolator(new AccelerateDecelerateInterpolator());
//                animator.start();
//            }
//        }, 2000L);

        final HeaderPagerAdapter headerPagerAdapter = new HeaderPagerAdapter(getChildFragmentManager(), 20);
        header.setAdapter(headerPagerAdapter);

        scrollableLayout.setOverScrollListener(new OverScrollImpl());

        return view;
    }

    private static class OverScrollImpl implements OverScrollListener {

        private float mDistanceY;
        private ValueAnimator mAnimator;

        @Override
        public void onOverScrolled(ScrollableLayout layout, int overScrollY) {

            // cancel animation
            if (mAnimator != null
                    && mAnimator.isRunning()) {
                mAnimator.cancel();
            }

            // make it positive
            mDistanceY += -overScrollY;
            if (Float.compare(mDistanceY, layout.getMaxScrollY()) > 0) {
                mDistanceY = layout.getMaxScrollY();
            }

            // now evaluate our logic to scale header

//            final float f = mInterpolator.getInterpolation((float) Math.cos(mDistanceY / layout.getMaxScrollY()));
//            final float scale = 1.F + (f / 3.F);

            final float ratio = mDistanceY / layout.getMaxScrollY();
            // okay, for example, max scale is 1.5
            final float scale = 1.F + (.5F * ratio);

            // ((currentScale - 1.F) / .5F) * max

            final View view = layout.getChildAt(0);
            view.setScaleX(scale);
            view.setScaleY(scale);

            final View content = layout.getChildAt(1);
            content.setTranslationY(((view.getHeight() * scale) - view.getHeight()) / 2.F);
        }

        @Override
        public boolean hasOverScroll(ScrollableLayout layout, int overScrollY) {
            return mDistanceY > 0;
        }

        @Override
        public void onCancelled(ScrollableLayout layout) {

            if (mAnimator != null
                    && mAnimator.isRunning()) {
                mAnimator.cancel();
            }

            // todo, here, with animation we must decrease our mDistance, so at the end it's just 0.0
            final View header = layout.getChildAt(0);
            final View content = layout.getChildAt(1);
            final float currentScale = header.getScaleY();
            if (Float.compare(currentScale, .0F) == 0) {
                return;
            }

            // we are interested in:
            // relative scale step & relative distance step

            final float scaleDiff = currentScale - 1.F;
//            final float distanceY = mDistanceY;
            final float max = layout.getMaxScrollY();

            mAnimator = ValueAnimator.ofFloat(.0F, 1.F);
            mAnimator.setEvaluator(new FloatEvaluator());
            mAnimator.setDuration(250L);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    header.setScaleX(currentScale - (fraction * scaleDiff));
                    header.setScaleY(currentScale - (fraction * scaleDiff));
                    mDistanceY = (header.getScaleX() - 1.F) / .5F * max;
                    content.setTranslationY(((header.getHeight() * header.getScaleX()) - header.getHeight()) / 2.F);
                }
            });
            mAnimator.start();
        }

        @Override
        public void clear() {
            mDistanceY = .0F;
        }
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
