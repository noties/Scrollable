package ru.noties.scrollable.sample.swiperefresh;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.BaseActivity;
import ru.noties.scrollable.sample.ItemsGenerator;
import ru.noties.scrollable.sample.SampleHeaderView;
import ru.noties.scrollable.sample.SampleHeaderViewOnScrollChangedListener;
import ru.noties.scrollable.sample.ViewTypeItem;
import ru.noties.vt.ViewTypesAdapter;

public class SwipeRefreshActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle sis) {
        super.onCreate(sis);

        setContentView(R.layout.activity_swipe_refresh);

        final SwipeRefreshLayout swipeRefreshLayout = findView(R.id.swipe_refresh_layout);
        final ScrollableLayout scrollableLayout = findView(R.id.scrollable_layout);
        final SampleHeaderView headerView = findView(R.id.header);
        final RecyclerView recyclerView = findView(R.id.recycler_view);

        swipeRefreshLayout.setColorSchemeResources(R.color.md_teal_500);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 5000L);
            }
        });

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return recyclerView.canScrollVertically(direction);
            }
        });

        scrollableLayout.setOnFlingOverListener(new OnFlingOverListener() {
            @Override
            public void onFlingOver(int y, long duration) {
                recyclerView.smoothScrollBy(0, y);
            }
        });

        scrollableLayout.addOnScrollChangedListener(new SampleHeaderViewOnScrollChangedListener(headerView));

        final ViewTypesAdapter<String> adapter = ViewTypesAdapter.builder(String.class)
                .register(String.class, new ViewTypeItem())
                .build(this);
        adapter.setItems(ItemsGenerator.generate(100));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }
}
