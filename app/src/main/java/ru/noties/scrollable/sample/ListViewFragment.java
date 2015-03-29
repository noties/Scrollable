package ru.noties.scrollable.sample;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public class ListViewFragment extends BaseFragment {

    static final String TAG = "tag.ListViewFragment";

    public static ListViewFragment newInstance(int color) {
        final Bundle bundle = new Bundle();
        bundle.putInt(ARG_COLOR, color);

        final ListViewFragment fragment = new ListViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {

        final View view = inflater.inflate(R.layout.fragment_list_view, parent, false);

        mListView = findView(view, R.id.list_view);
        final BaseListAdapter adapter = new BaseListAdapter(getActivity(), 100);
        mListView.setAdapter(adapter);

        return view;
    }

    @Override
    public CharSequence getTitle(Resources r) {
        return "ListView";
    }

    @Override
    public String getSelfTag() {
        return TAG;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mListView != null && mListView.canScrollVertically(direction);
    }
}
