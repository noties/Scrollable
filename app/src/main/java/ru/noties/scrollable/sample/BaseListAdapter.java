package ru.noties.scrollable.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public class BaseListAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final int mCount;

    public BaseListAdapter(Context context, int count) {
        this.mInflater = LayoutInflater.from(context);
        this.mCount    = count;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0L;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        } else {
            view = convertView;
        }

        ((TextView) view).setText(String.valueOf(position));
        return view;
    }
}
