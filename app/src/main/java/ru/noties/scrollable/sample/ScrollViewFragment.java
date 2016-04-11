package ru.noties.scrollable.sample;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public class ScrollViewFragment extends BaseFragment {

    static final String TAG = "tag.ScrollViewFragment";

    public static ScrollViewFragment newInstance(int color) {
        final Bundle bundle = new Bundle();
        bundle.putInt(ARG_COLOR, color);

        final ScrollViewFragment fragment = new ScrollViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private static ScrollView mScrollView; // must be static

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {

        final View view = inflater.inflate(R.layout.fragment_scroll_view, parent, false);

        mScrollView = findView(view, R.id.scroll_view);
        final LinearListView linearListView = findView(view, R.id.linear_list_view);
        final BaseListAdapter adapter = new BaseListAdapter(getActivity(), 30);
        linearListView.setAdapter(adapter);

        return view;
    }

    @Override
    public CharSequence getTitle(Resources r) {
        return "ScrollView";
    }

    @Override
    public String getSelfTag() {
        return TAG;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mScrollView != null && mScrollView.canScrollVertically(direction);
    }
}
