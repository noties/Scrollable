package ru.noties.scrollable.sample;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.colorful.ColorfulActivity;
import ru.noties.scrollable.sample.dialog.ScrollableDialog;
import ru.noties.scrollable.sample.manual.ManualControlActivity;
import ru.noties.scrollable.sample.overscroll.custompullrefresh.CustomOverScrollActivity;
import ru.noties.scrollable.sample.pager.fragment.FragmentPagerActivity;
import ru.noties.scrollable.sample.scrollheader.ScrollHeaderActivity;
import ru.noties.scrollable.sample.swiperefresh.SwipeRefreshActivity;
import ru.noties.vt.Holder;
import ru.noties.vt.OnItemClickListener;
import ru.noties.vt.ViewTypesAdapter;

public class SampleListActivity extends BaseActivity {

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
        scrollableLayout.setOverScrollListener(new ZoomInHeaderOverScrollListener(headerView, recyclerView));

        final ViewTypesAdapter<SampleListItem> adapter = ViewTypesAdapter.builder(SampleListItem.class)
                .register(SampleListItem.class, new SampleListItemViewType())
                .setHasStableIds(true)
                .registerOnClickListener(new OnItemClickListener<SampleListItem, Holder>() {
                    @Override
                    public void onItemClick(SampleListItem item, Holder holder) {
                        // yeah, conditions...
                        if (ScrollableDialog.class.equals(item.sampleActivityClass())) {
                            final ScrollableDialog dialog = new ScrollableDialog();
                            dialog.show(getSupportFragmentManager(), "tag.Dialog");
                        } else {
                            final Intent intent = new Intent(SampleListActivity.this, item.sampleActivityClass());
                            startActivity(intent);
                        }
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

        items.add(new SampleListItem(
                CustomOverScrollActivity.class,
                r.getString(R.string.sample_title_over_scroll_custom),
                r.getString(R.string.sample_description_over_scroll_custom)
        ));

        items.add(new SampleListItem(
                ScrollableDialog.class,
                r.getString(R.string.sample_title_dialog),
                r.getString(R.string.sample_description_dialog)
        ));

        items.add(new SampleListItem(
                SwipeRefreshActivity.class,
                r.getString(R.string.sample_title_swipe_refresh),
                r.getString(R.string.sample_description_swipe_refresh)
        ));

        items.add(new SampleListItem(
                ManualControlActivity.class,
                r.getString(R.string.sample_title_manual_control),
                r.getString(R.string.sample_description_manual_control)
        ));

        items.add(new SampleListItem(
                ScrollHeaderActivity.class,
                r.getString(R.string.sample_title_scrollable_header),
                r.getString(R.string.sample_description_scrollable_header)
        ));

        items.add(new SampleListItem(
                ColorfulActivity.class,
                r.getString(R.string.sample_title_colorful),
                r.getString(R.string.sample_description_colorful)
        ));

        return items;
    }
}
