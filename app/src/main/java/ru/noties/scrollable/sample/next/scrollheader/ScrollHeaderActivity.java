package ru.noties.scrollable.sample.next.scrollheader;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.next.BaseActivity;
import ru.noties.scrollable.sample.next.ItemsGenerator;
import ru.noties.scrollable.sample.next.ViewTypeItem;
import ru.noties.vt.ViewTypesAdapter;

public class ScrollHeaderActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle sis) {
        super.onCreate(sis);

        setContentView(R.layout.activity_scroll_header);

        final ScrollableLayout scrollableLayout = findView(R.id.scrollable_layout);
        final RecyclerView headerRecyclerView = findView(R.id.recycler_view_header);
        final RecyclerView contentRecyclerView = findView(R.id.recycler_view);
        final View header = findViewById(R.id.header);

        final ViewTypesAdapter<String> adapter = ViewTypesAdapter.builder(String.class)
                .register(String.class, new ViewTypeItem())
                .build(this);
        adapter.setItems(ItemsGenerator.generate(100));

        headerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        headerRecyclerView.setAdapter(adapter);
        contentRecyclerView.setAdapter(adapter);

        scrollableLayout.setFriction(.04F);
        scrollableLayout.setDraggableView(header);

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return contentRecyclerView.canScrollVertically(direction);
            }
        });
    }
}
