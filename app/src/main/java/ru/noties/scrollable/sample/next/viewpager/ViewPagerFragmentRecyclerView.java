package ru.noties.scrollable.sample.next.viewpager;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.noties.scrollable.sample.R;
import ru.noties.vt.ViewTypesAdapter;

public class ViewPagerFragmentRecyclerView extends ViewPagerFragment {

    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {
        return inflater.inflate(R.layout.fragment_sample_view_pager_recycler_view, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle sis) {
        super.onViewCreated(view, sis);

        mRecyclerView = findView(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);

        final ViewTypesAdapter<String> adapter = ViewTypesAdapter.builder(String.class)
                .register(String.class, new ViewTypeItem())
                .setHasStableIds(false)
                .build(getContext());

        final List<String> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            list.add("Item #" + i);
        }
        adapter.setItems(list);

        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mRecyclerView != null && mRecyclerView.canScrollVertically(direction);
    }

    @Override
    public void onFlingOver(int y, long duration) {
        if (mRecyclerView != null) {
            mRecyclerView.smoothScrollBy(0, y);
        }
    }
}
