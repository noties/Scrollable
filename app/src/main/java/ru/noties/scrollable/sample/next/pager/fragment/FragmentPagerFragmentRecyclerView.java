package ru.noties.scrollable.sample.next.pager.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.next.ViewTypeItem;
import ru.noties.scrollable.sample.next.ItemsGenerator;
import ru.noties.vt.ViewTypesAdapter;

public class FragmentPagerFragmentRecyclerView extends FragmentPagerFragment {

    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {
        return inflater.inflate(R.layout.fragment_pager_recycler_view, parent, false);
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

        final List<String> list = ItemsGenerator.generate(100);
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
