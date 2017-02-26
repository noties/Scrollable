package ru.noties.scrollable.sample.next;

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

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.OverScrollListenerBase;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.next.overscroll.custompullrefresh.CustomOverScrollActivity;
import ru.noties.scrollable.sample.next.pager.fragment.FragmentPagerActivity;
import ru.noties.scrollable.sample.next.swiperefresh.SwipeRefreshActivity;
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
        final SampleHeaderView headerView = findView(R.id.header);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

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
        scrollableLayout.setOverScrollListener(new SampleListOverScrollListener(
                headerView,
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
                r.getString(R.string.sample_title_fragment_pager),
                r.getString(R.string.sample_description_fragment_pager))
        );

//        items.add(new SampleListItem(
//                null,
//                r.getString(R.string.sample_title_over_scroll_scale),
//                r.getString(R.string.sample_description_over_scroll_scale)
//        ));

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
                SwipeRefreshActivity.class,
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

    private static class SampleListOverScrollListener extends OverScrollListenerBase {

        private final View mHeader;
        private final View mContent;

        SampleListOverScrollListener(View header, View content) {
            mHeader = header;
            mContent = content;
        }

        @Override
        protected void onRatioChanged(ScrollableLayout layout, float ratio) {
            final float scale = 1.F + (.33F * ratio);
            mHeader.setScaleX(scale);
            mHeader.setScaleY(scale);

            final int headerHeight = mHeader.getHeight();
            mContent.setTranslationY(((headerHeight * scale) - headerHeight) / 2.F);
        }

        @Override
        protected int getMaxOverScrollY(ScrollableLayout layout) {
            return layout.getMaxScrollY() / 2;
        }
    }
}
