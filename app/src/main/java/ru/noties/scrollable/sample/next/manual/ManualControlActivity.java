package ru.noties.scrollable.sample.next.manual;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import ru.noties.debug.Debug;
import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.next.BaseActivity;
import ru.noties.scrollable.sample.next.ItemsGenerator;
import ru.noties.scrollable.sample.next.SampleHeaderView;
import ru.noties.scrollable.sample.next.SampleHeaderViewOnScrollChangedListener;
import ru.noties.scrollable.sample.next.ViewTypeItem;
import ru.noties.vt.Holder;
import ru.noties.vt.OnItemClickListener;
import ru.noties.vt.ViewTypesAdapter;

public class ManualControlActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle sis) {
        super.onCreate(sis);

        setContentView(R.layout.activity_manual_control);

        final ScrollableLayout scrollableLayout = findView(R.id.scrollable_layout);
        final SampleHeaderView headerView = findView(R.id.header);
        final RecyclerView recyclerView = findView(R.id.recycler_view);

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return recyclerView.canScrollVertically(direction);
            }
        });

        scrollableLayout.addOnScrollChangedListener(new SampleHeaderViewOnScrollChangedListener(headerView));

        scrollableLayout.setOnFlingOverListener(new OnFlingOverListener() {
            @Override
            public void onFlingOver(int y, long duration) {
                recyclerView.smoothScrollBy(0, y);
            }
        });

        scrollableLayout.setSelfUpdateScroll(true);

        // important to set initial value based on ScrollableLayout state
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                final boolean result;
                if (recyclerView.getHeight() > 0) {
                    final ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                    params.height = recyclerView.getHeight() - scrollableLayout.getMaxScrollY();
                    recyclerView.requestLayout();
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    result = true;
                } else {
                    result = false;
                }
                return result;
            }
        });

        final ViewTypesAdapter<String> adapter = ViewTypesAdapter.builder(String.class)
                .register(String.class, new ViewTypeItem())
                .registerOnClickListener(new OnItemClickListener<String, Holder>() {
                    @Override
                    public void onItemClick(String item, Holder holder) {
                        // just for the sake of brevity, do not repeat
                        // even number scrolls to 0
                        // odd number scrolls to maxScrollY
                        final int index = item.lastIndexOf('#');
                        if (index > -1) {
                            try {
                                final int value = Integer.parseInt(item.substring(index + 1));
                                if ((value & 1) == 0) {
                                    final ValueAnimator animator = scrollableLayout.animateScroll(0);
                                    animator.setDuration(250L);
                                    animator.addUpdateListener(new RecyclerHeightChangeListener(recyclerView, -scrollableLayout.getMaxScrollY()));
                                    animator.start();
                                } else {
                                    final ValueAnimator animator = scrollableLayout.animateScroll(scrollableLayout.getMaxScrollY());
                                    animator.setDuration(250L);
                                    animator.addUpdateListener(new RecyclerHeightChangeListener(recyclerView, scrollableLayout.getMaxScrollY()));
                                    animator.start();
                                }
                            } catch (NumberFormatException e) {
                                Debug.e(e);
                            }
                        }
                    }
                })
                .build(this);
        adapter.setItems(ItemsGenerator.generate(100));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private static class RecyclerHeightChangeListener implements ValueAnimator.AnimatorUpdateListener {

        private final RecyclerView mRecyclerView;
        private final int mRecyclerHeight;
        private final int mHeightDelta;

        RecyclerHeightChangeListener(RecyclerView recyclerView, int delta) {
            mRecyclerView = recyclerView;
            mRecyclerHeight = recyclerView.getHeight();
            mHeightDelta = delta;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final float fraction = animation.getAnimatedFraction();
            final int height = mRecyclerHeight + (int)(mHeightDelta * fraction + .5F);
            final ViewGroup.LayoutParams params = mRecyclerView.getLayoutParams();
            params.height = height;
            mRecyclerView.requestLayout();
        }
    }
}
