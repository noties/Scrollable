package ru.noties.scrollable.sample.overscroll.custompullrefresh;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.OverScrollListenerBase;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.BaseActivity;
import ru.noties.scrollable.sample.ItemsGenerator;
import ru.noties.scrollable.sample.SampleHeaderView;
import ru.noties.scrollable.sample.SampleHeaderViewOnScrollChangedListener;
import ru.noties.scrollable.sample.ViewTypeItem;
import ru.noties.vt.ViewTypesAdapter;

public class CustomOverScrollActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle sis) {
        super.onCreate(sis);

        setContentView(R.layout.activity_custom_over_scroll);

        final ScrollableLayout scrollableLayout = findView(R.id.scrollable_layout);
        final View overScrollHeader = findView(R.id.over_scroll_header);
        final View overScrollBg = findViewById(R.id.custom_over_scroll_bg);
        final SampleHeaderView headerView = findView(R.id.header);

        final RecyclerView recyclerView = findView(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final ViewTypesAdapter<String> adapter = ViewTypesAdapter.builder(String.class)
                .register(String.class, new ViewTypeItem())
                .build(this);
        adapter.setItems(ItemsGenerator.generate(100));
        recyclerView.setAdapter(adapter);

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return recyclerView.canScrollVertically(direction);
            }
        });

        scrollableLayout.addOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {
                // we are showing background on the top for custom overScroll
                // we need to hide it accordingly
                overScrollBg.setTranslationY(-y);
            }
        });
        scrollableLayout.addOnScrollChangedListener(new SampleHeaderViewOnScrollChangedListener(headerView));

        final int overScrollMax = getResources().getDimensionPixelSize(R.dimen.custom_over_scroll_height);

        scrollableLayout.setOverScrollListener(
                new ProgressOverScrollListener(
                        overScrollHeader,
                        overScrollMax,
                        ContextCompat.getColor(this, R.color.white_disabled)
                )
        );

        scrollableLayout.setOnFlingOverListener(new OnFlingOverListener() {
            @Override
            public void onFlingOver(int y, long duration) {
                recyclerView.smoothScrollBy(0, y);
            }
        });
    }

    private static class ProgressOverScrollListener extends OverScrollListenerBase {

        private final View mOverScrollHeader;
        private final CircleProgressDrawable mCircleProgressDrawable;
        private final int mOverScrollMax;
        private final float mOverScrollHeaderInitialY;

        ProgressOverScrollListener(View overScrollHeader, int overScrollMax, int circleProgressColor) {
            mOverScrollHeader = overScrollHeader;
            mOverScrollMax = overScrollMax;
            mOverScrollHeaderInitialY = mOverScrollHeader.getTranslationY();
            mCircleProgressDrawable = new CircleProgressDrawable(circleProgressColor);
            mOverScrollHeader.setBackground(mCircleProgressDrawable);
        }

        @Override
        protected void onRatioChanged(ScrollableLayout layout, float ratio) {

            mOverScrollHeader.setAlpha(ratio);
            mCircleProgressDrawable.setProgress(ratio);

            final float y = ratio * mOverScrollMax;

            final View header = layout.getChildAt(0);
            final View content = layout.getChildAt(1);

            header.setTranslationY(y);
            content.setTranslationY(y);
            mOverScrollHeader.setTranslationY(mOverScrollHeaderInitialY + y);
        }

        @Override
        protected int getMaxOverScrollY(ScrollableLayout layout) {
            return mOverScrollMax;
        }
    }
}
