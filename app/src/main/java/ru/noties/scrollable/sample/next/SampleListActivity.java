package ru.noties.scrollable.sample.next;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import ru.noties.ccf.CCFAnimator;
import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.OverScrollListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.next.overscroll.custompullrefresh.CustomOverScrollActivity;
import ru.noties.scrollable.sample.next.viewpager.fragment.FragmentPagerActivity;
import ru.noties.vt.Holder;
import ru.noties.vt.OnItemClickListener;
import ru.noties.vt.ViewTypesAdapter;

public class SampleListActivity extends BaseActivity {

    // okay, basic ViewPager with draggable tabs
    // ViewPager with views/fragments
    // inside SwipeRefreshLayout
    // custom animation (animateScroll())
    // overscroll (multiple)
    // let's do this main list also inside ScrollableLayout with overscroll demo
    // dialog
    // scrollable header
    // cancelling dealing with scroll events handling
    // manual control sample (scroll up/down/half)
    // inside fragment (with childFragmentManager)

    @Override
    public void onCreate(Bundle sis) {
        super.onCreate(sis);

        final Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_sample_list);

        final ScrollableLayout scrollableLayout = findView(R.id.scrollable_layout);
        final RecyclerView recyclerView = findView(R.id.recycler_view);
        final View headerContainer = findViewById(R.id.header_container);
        final View header = findViewById(R.id.header);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final Resources r = getResources();
        scrollableLayout.setMaxScrollY(
                r.getDimensionPixelSize(R.dimen.header_height) - r.getDimensionPixelSize(R.dimen.tabs_height)
        );

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return recyclerView.canScrollVertically(direction);
            }
        });
        scrollableLayout.setOnScrollChangedListener(new OnScrollChangedListener() {

            // todo, change to ContextCompat
            final CCFAnimator mCCFAnimator = CCFAnimator.rgb(
                    r.getColor(R.color.md_teal_300),
                    r.getColor(R.color.md_teal_500)
            );

            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {
                final float headerY;
                final float maxHeaderY = header.getTop();
                if (y < maxHeaderY) {
                    headerY = .0F;
                } else {
                    headerY = y - maxHeaderY;
                }
                header.setTranslationY(headerY);

                final int color = mCCFAnimator.getColor(headerY / maxHeaderY);
                headerContainer.setBackgroundColor(color);
                header.setBackgroundColor(color);
            }
        });
        scrollableLayout.setOnFlingOverListener(new OnFlingOverListener() {
            @Override
            public void onFlingOver(int y, long duration) {
                recyclerView.smoothScrollBy(0, y);
            }
        });
        scrollableLayout.setOverScrollListener(new SampleListOverScrollListener(
                (View) header.getParent(),
                recyclerView
        ));

        final ViewTypesAdapter<SampleListItem> adapter = ViewTypesAdapter.builder(SampleListItem.class)
                .register(SampleListItem.class, new SampleListItemViewType())
                .setHasStableIds(true)
                .registerOnClickListener(new OnItemClickListener<SampleListItem, Holder>() {
                    @Override
                    public void onItemClick(SampleListItem item, Holder holder) {
                        final Intent intent = new Intent(SampleListActivity.this, item.sampleActivityClass());
                        startActivity(intent);
                    }
                })
                .build(this);
        adapter.setItems(sampleItems(getApplicationContext()));
        recyclerView.setAdapter(adapter);
    }

    private static List<SampleListItem> sampleItems(Context context) {

        final Resources r = context.getResources();
        final List<SampleListItem> items = new ArrayList<>();

        items.add(new SampleListItem(
                FragmentPagerActivity.class,
                r.getString(R.string.sample_title_view_pager),
                r.getString(R.string.sample_description_view_pager))
        );

        items.add(new SampleListItem(
                null,
                r.getString(R.string.sample_title_over_scroll_scale),
                r.getString(R.string.sample_description_over_scroll_scale)
        ));

        items.add(new SampleListItem(
                CustomOverScrollActivity.class,
                r.getString(R.string.sample_title_over_scroll_custom),
                r.getString(R.string.sample_description_over_scroll_custom)
        ));

        items.add(new SampleListItem(
                null,
                r.getString(R.string.sample_title_dialog),
                r.getString(R.string.sample_description_dialog)
        ));

        items.add(new SampleListItem(
                null,
                r.getString(R.string.sample_title_swipe_refresh),
                r.getString(R.string.sample_description_swipe_refresh)
        ));

        items.add(new SampleListItem(
                null,
                r.getString(R.string.sample_title_manual_control),
                r.getString(R.string.sample_description_manual_control)
        ));

        items.add(new SampleListItem(
                null,
                r.getString(R.string.sample_title_inside_fragment),
                r.getString(R.string.sample_description_inside_fragment)
        ));

        items.add(new SampleListItem(
                null,
                r.getString(R.string.sample_title_header_view_pager),
                r.getString(R.string.sample_description_header_view_pager)
        ));

        return items;
    }

    private static class SampleListOverScrollListener implements OverScrollListener {

        private final View mHeader;
        private final View mContent;

        private float mDistanceY;
        private ValueAnimator mAnimator;

        SampleListOverScrollListener(View header, View content) {
            mHeader = header;
            mContent = content;
        }

        @Override
        public void onOverScrolled(ScrollableLayout layout, float overScrollY) {

            if (mAnimator != null
                    && mAnimator.isRunning()) {
                mAnimator.cancel();
            }

            // to abstract:
            //  * maximum over scroll distance (getMaxScrollY() / 2)
            //  * ratio coefficient (.33F), default .33, meaning bounds are 0.0-0.33
            //      coefficient 1.0 means bounds 0.0-1.0

            // we will use half of the maxScrollY
            final float max = layout.getMaxScrollY() / 2.F;

            mDistanceY += -overScrollY;
            if (Float.compare(mDistanceY, max) > 0) {
                mDistanceY = max;
            }

            // important one
            if (mDistanceY < 0) {
                return;
            }

            final float ratio = mDistanceY / max;
            final float scale = 1.F + (.33F * ratio);

            mHeader.setScaleX(scale);
            mHeader.setScaleY(scale);

            final int headerHeight = mHeader.getHeight();

            // as we scale the whole view, we are interested only in half of the
            // scaling height (the one that goes to the bottom)
            mContent.setTranslationY(((headerHeight * scale) - headerHeight) / 2.F);
        }

        @Override
        public boolean hasOverScroll(ScrollableLayout layout, float overScrollY) {
            return Float.compare(mDistanceY, 0) > 0;
        }

        @Override
        public void onCancelled(ScrollableLayout layout) {

            if (mAnimator != null
                    && mAnimator.isRunning()) {
                mAnimator.cancel();
            }

            final float currentScale = mHeader.getScaleY();

            // we can also check if it's > 0

            final float scaleDiff = currentScale - 1.F;
            final float max = layout.getMaxScrollY() / 2.F;
            final float headerHeight = mHeader.getHeight();

            mAnimator = ValueAnimator.ofFloat(.0F, 1.F);
            mAnimator.setDuration(250L);
            mAnimator.setEvaluator(new FloatEvaluator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    final float fraction = animation.getAnimatedFraction();

                    final float scale = currentScale - (fraction * scaleDiff);
                    mHeader.setScaleX(scale);
                    mHeader.setScaleY(scale);

                    mDistanceY = (scale - 1.F) / .33F * max;

                    mContent.setTranslationY(((headerHeight * scale) - headerHeight) / 2.F);
                }
            });
            mAnimator.start();
        }

        @Override
        public void clear() {
            mDistanceY = .0F;
        }
    }
}
