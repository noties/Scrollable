package ru.noties.scrollable.sample;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public class LinearListView extends LinearLayout {

    private ListAdapter mAdapter;

    public LinearListView(Context context) {
        super(context);
        init(context, null);
    }

    public LinearListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LinearListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        setOrientation(VERTICAL);
        setLayoutTransition(new LayoutTransition());
    }

    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            removeAllViews();
        }

        mAdapter = adapter;
        populateViews();
    }

    void populateViews() {
        final int count = mAdapter == null ? 0 : mAdapter.getCount();
        if (count == 0) {
            return;
        }

        View view;

        for (int i = 0; i < count; i++) {
            view = mAdapter.getView(i, null, this);
            addView(view, i);
        }
    }
}
