package ru.noties.scrollable.sample.colorful;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPagerUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import ru.noties.ccf.CCFAnimator;
import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.BaseActivity;
import ru.noties.scrollable.sample.ItemsGenerator;
import ru.noties.scrollable.sample.SampleHeaderView;
import ru.noties.scrollable.sample.TabsLayout;
import ru.noties.scrollable.sample.ViewTypeItem;
import ru.noties.scrollable.sample.ZoomInHeaderOverScrollListener;
import ru.noties.vt.ViewTypesAdapter;

public class ColorfulActivity extends BaseActivity {

    private interface WindowCompat {
        void setStatusBarColor(int color);
    }

    private int mPrimaryColor;
    private int mAccentColor;

    @Override
    public void onCreate(Bundle sis) {
        super.onCreate(sis);

        setContentView(R.layout.activity_colorful);

        final ScrollableLayout scrollableLayout = findView(R.id.scrollable_layout);
        final SampleHeaderView sampleHeaderView = findView(R.id.header);
        final TabsLayout tabsLayout = findView(R.id.tabs);
        final ViewPager viewPager = findView(R.id.view_pager);

        final List<Item> items = viewPagerItems(this);
        final Adapter adapter = new Adapter(items);

        // set initial colors
        {
            final Item item = items.get(0);
            mPrimaryColor = item.primaryColor;
            mAccentColor = item.accentColor;
        }

        viewPager.setAdapter(adapter);
        tabsLayout.setViewPager(viewPager);

        final ViewPagerUtils.CurrentView currentView = ViewPagerUtils.currentView(viewPager);
        final WindowCompat windowCompat = windowCompat(this);

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                final View view = currentView.currentView();
                return view != null && view.canScrollVertically(direction);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private ValueAnimator mPrimaryAnimator;
            private ValueAnimator mAccentAnimator;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (mPrimaryAnimator != null
                        && mPrimaryAnimator.isRunning()) {
                    mPrimaryAnimator.cancel();
                }

                if (mAccentAnimator != null
                        && mAccentAnimator.isRunning()) {
                    mAccentAnimator.cancel();
                }

                // right, if
                final Item item = adapter.getItem(position);

                final CCFAnimator primary = CCFAnimator.rgb(mPrimaryColor, item.primaryColor);
                mPrimaryAnimator = primary.asValueAnimator(new CCFAnimator.OnNewColorListener() {
                    @Override
                    public void onNewColor(@ColorInt int color) {
                        tabsLayout.setBackgroundColor(color);
                        windowCompat.setStatusBarColor(color);
                    }
                });
                mPrimaryAnimator.setDuration(250L);
                mPrimaryAnimator.start();

                // we need to evaluate colors (in case we are currently in scrolling transaction
                final CCFAnimator accent = CCFAnimator.rgb(
                        accentColor(scrollableLayout, mAccentColor, mPrimaryColor),
                        accentColor(scrollableLayout, item.accentColor, item.primaryColor)
                );
                mAccentAnimator = accent.asValueAnimator(new CCFAnimator.OnNewColorListener() {
                    @Override
                    public void onNewColor(@ColorInt int color) {
                        sampleHeaderView.setBackgroundColor(color);
                    }
                });
                mAccentAnimator.setDuration(250L);
                mAccentAnimator.start();

                mPrimaryColor = item.primaryColor;
                mAccentColor = item.accentColor;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        scrollableLayout.addOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {

                final float tabY;
                if (y < maxY) {
                    tabY = .0F;
                } else {
                    tabY = y - maxY;
                }
                tabsLayout.setTranslationY(tabY);

                final float ratio = (float) y / maxY;
                final CCFAnimator animator = CCFAnimator.rgb(mAccentColor, mPrimaryColor);
                sampleHeaderView.setBackgroundColor(animator.getColor(ratio));

                sampleHeaderView.getTextView().setTranslationY(y / 2.F);
                sampleHeaderView.getTextView().setAlpha(1.F - ratio);
            }
        });

        scrollableLayout.setOverScrollListener(new ColorfulOverScrollListener(sampleHeaderView, viewPager, tabsLayout));

        scrollableLayout.setOnFlingOverListener(new OnFlingOverListener() {
            @Override
            public void onFlingOver(int y, long duration) {
                final View view = currentView.currentView();
                if (view != null) {
                    // we are using RecyclerView only, so it's safe to cast
                    ((RecyclerView) view).smoothScrollBy(0, y);
                }
            }
        });
    }

    private static List<Item> viewPagerItems(final Context context) {

        final ViewProvider provider = new ViewProvider() {

            final LayoutInflater inflater = LayoutInflater.from(context);
            final ViewTypesAdapter<String> adapter = ViewTypesAdapter.builder(String.class)
                    .register(String.class, new ViewTypeItem())
                    .build(context);
            {
                adapter.setItems(ItemsGenerator.generate(100));
            }

            @Override
            public View provide(ViewGroup parent) {
                final RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.view_pager_recycler_view, parent, false);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(adapter);
                return recyclerView;
            }
        };

        final List<Item> items = new ArrayList<>();

        items.add(new Item(
                ContextCompat.getColor(context, R.color.md_teal_500),
                ContextCompat.getColor(context, R.color.md_teal_300),
                "First Teal",
                provider
        ));

        items.add(new Item(
                ContextCompat.getColor(context, R.color.md_red_500),
                ContextCompat.getColor(context, R.color.md_red_300),
                "Second Red",
                provider
        ));

        items.add(new Item(
                ContextCompat.getColor(context, R.color.md_purple_500),
                ContextCompat.getColor(context, R.color.md_purple_300),
                "Third Purple",
                provider
        ));

        items.add(new Item(
                ContextCompat.getColor(context, R.color.md_blue_500),
                ContextCompat.getColor(context, R.color.md_blue_300),
                "Forth Blue",
                provider
        ));

        return items;
    }

    private static int accentColor(ScrollableLayout layout, int accentColor, int primaryColor) {
        final int out;
        final int y = layout.getScrollY();
        final int max = layout.getMaxScrollY();
        if (y == 0
                || y == max) {
            // we are not in transition
            out = accentColor;
        } else {
            final CCFAnimator animator = CCFAnimator.rgb(accentColor, primaryColor);
            out = animator.getColor((float) y / max);
        }

        return out;
    }

    private interface ViewProvider {
        View provide(ViewGroup parent);
    }

    private static class Item {

        final int primaryColor;
        final int accentColor;
        final String title;
        final ViewProvider viewProvider;

        private Item(int primaryColor, int accentColor, String title, ViewProvider viewProvider) {
            this.primaryColor = primaryColor;
            this.accentColor = accentColor;
            this.title = title;
            this.viewProvider = viewProvider;
        }
    }

    private static class Adapter extends PagerAdapter {

        private final List<Item> mItems;

        private Adapter(List<Item> items) {
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems != null ? mItems.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final View view = mItems.get(position).viewProvider.provide(container);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof View) {
                container.removeView((View) object);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mItems.get(position).title;
        }

        Item getItem(int position) {
            return mItems.get(position);
        }
    }

    private static class ColorfulOverScrollListener extends ZoomInHeaderOverScrollListener {

        private final SampleHeaderView mHeader;
        private final View mContent;
        private final View mTabs;

        ColorfulOverScrollListener(SampleHeaderView header, View content, View tabs) {
            super(header, content);
            mHeader = header;
            mContent = content;
            mTabs = tabs;
        }

        @Override
        protected void onRatioChanged(ScrollableLayout layout, float ratio) {
            super.onRatioChanged(layout, ratio);
            mTabs.setTranslationY(mContent.getTranslationY());
            // additionally, we will apply a bit of translationY to header text view
            // to achieve nice visual effect
            mHeader.getTextView().setTranslationY(mContent.getTranslationY() / 2.F);
        }
    }

    private static WindowCompat windowCompat(Activity activity) {
        final WindowCompat compat;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            compat = new WindowCompat21(activity.getWindow());
        } else {
            compat = new WindowCompatImpl();
        }
        return compat;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class WindowCompat21 implements WindowCompat {

        private final Window mWindow;

        private WindowCompat21(Window window) {
            mWindow = window;
        }

        @Override
        public void setStatusBarColor(int color) {
            mWindow.setStatusBarColor(color);
        }
    }

    private static class WindowCompatImpl implements WindowCompat {

        @Override
        public void setStatusBarColor(int color) {
            // no op
        }
    }
}
