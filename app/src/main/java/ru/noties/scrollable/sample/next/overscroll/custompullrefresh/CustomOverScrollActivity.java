package ru.noties.scrollable.sample.next.overscroll.custompullrefresh;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ru.noties.debug.Debug;
import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.OverScrollListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.next.BaseActivity;
import ru.noties.scrollable.sample.next.ItemsGenerator;
import ru.noties.scrollable.sample.next.ViewTypeItem;
import ru.noties.vt.ViewTypesAdapter;

public class CustomOverScrollActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle sis) {
        super.onCreate(sis);

        setContentView(R.layout.activity_custom_pull_refresh);

        final ScrollableLayout scrollableLayout = findView(R.id.scrollable_layout);
        final View overScrollHeader = findView(R.id.over_scroll_header);
        final View overScrollBg = findViewById(R.id.custom_over_scroll_bg);

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

        scrollableLayout.setOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {
                // we are showing background on the top for custom overScroll
                // we need to hide it accordingly
                Debug.i("y: %s, oldY: %s, maxY: %s", y, oldY, maxY);
                overScrollBg.setTranslationY(-y); // just hide it
            }
        });

        final int overScrollMax = getResources().getDimensionPixelSize(R.dimen.custom_over_scroll_height);

        scrollableLayout.setOverScrollListener(
                new ProgressOverScrollListener(
                        overScrollHeader,
                        overScrollMax,
                        0xFFffffff
                )
        );
    }

    private static class ProgressOverScrollListener implements OverScrollListener {

        private final View mOverScrollHeader;
        private final CircleProgressDrawable mCircleProgressDrawable;
        private final int mOverScrollMax;
        private final float mOverScrollHeaderInitialY;

        private float mDistanceY;
        private ValueAnimator mAnimator;

        ProgressOverScrollListener(View overScrollHeader, int overScrollMax, int circleProgressColor) {
            mOverScrollHeader = overScrollHeader;
            mOverScrollMax = overScrollMax;
            mOverScrollHeaderInitialY = mOverScrollHeader.getTranslationY();
            mCircleProgressDrawable = new CircleProgressDrawable(circleProgressColor);
            mOverScrollHeader.setBackground(mCircleProgressDrawable);
        }

        @Override
        public void onOverScrolled(ScrollableLayout layout, float overScrollY) {

            if (mAnimator != null
                    && mAnimator.isRunning()) {
                mAnimator.cancel();
            }

            final float max = mOverScrollMax;

            mDistanceY += -overScrollY;
            if (Float.compare(mDistanceY, max) > 0) {
                mDistanceY = max;
            }

            // important one
            if (mDistanceY < 0) {
                return;
            }

            final float ratio = mDistanceY / max;

            mOverScrollHeader.setAlpha(ratio);
            mCircleProgressDrawable.setProgress(ratio);

            final float translationY;
            if (mDistanceY < 0) {
                translationY = .0F;
            } else {
                translationY = max * ratio;
            }

            final View header = layout.getChildAt(0);
            final View content = layout.getChildAt(1);

            final float y = max * ratio;

            header.setTranslationY(y);
            content.setTranslationY(y);
            mOverScrollHeader.setTranslationY(mOverScrollHeaderInitialY + translationY);
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

            final View header = layout.getChildAt(0);
            final View content = layout.getChildAt(1);

            final float y = header.getTranslationY();
            final float progress = mCircleProgressDrawable.getProgress();
            final float distance = mDistanceY;

            mAnimator = ValueAnimator.ofFloat(.0F, 1.F);
            mAnimator.setDuration(250L);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    mDistanceY = distance - (distance * fraction);
                    header.setTranslationY(y - (y * fraction));
                    content.setTranslationY(y - (y * fraction));
                    mOverScrollHeader.setTranslationY(mOverScrollHeaderInitialY + (y - (y * fraction)));
                    mCircleProgressDrawable.setProgress(progress - (progress * fraction));
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
