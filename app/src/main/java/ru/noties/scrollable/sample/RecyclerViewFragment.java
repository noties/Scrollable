package ru.noties.scrollable.sample;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public class RecyclerViewFragment extends BaseFragment {

    static final String TAG = "tag.RecyclerViewFragment";

    public static RecyclerViewFragment newInstance(int color) {
        final Bundle bundle = new Bundle();
        bundle.putInt(ARG_COLOR, color);

        final RecyclerViewFragment fragment = new RecyclerViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private static RecyclerView mRecyclerView; // must be static

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {

        final View view = inflater.inflate(R.layout.fragment_recycler_view, parent, false);

        mRecyclerView = findView(view, R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final RecyclerAdapter adapter = new RecyclerAdapter(getActivity(), 150);
        mRecyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public CharSequence getTitle(Resources r) {
        return "RecyclerView";
    }

    @Override
    public String getSelfTag() {
        return TAG;
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
