package ru.noties.scrollable.sample.next.viewpager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import ru.noties.scrollable.sample.R;

public class ViewPagerFragmentListView extends ViewPagerFragment {

    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {
        return inflater.inflate(R.layout.fragment_sample_view_pager_list_view, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle sis) {
        super.onViewCreated(view, sis);

        mListView = findView(R.id.list_view);

        final String[] items = new String[100];
        for (int i = 0; i < 100; i++) {
            items[i] = "Item #" + i;
        }

        // Normally I would never ever use `ArrayAdapter`, but for the brevity of this sample it's ok
        final BaseAdapter adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        mListView.setAdapter(adapter);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mListView != null && mListView.canScrollVertically(direction);
    }

    @Override
    public void onFlingOver(int y, long duration) {
        if (mListView != null) {
            mListView.smoothScrollBy(y, (int) duration);
        }
    }
}
