package ru.noties.scrollable.sample.next.viewpager;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import ru.noties.scrollable.sample.R;

public class ViewPagerFragmentScrollView extends ViewPagerFragment {

    private ScrollView mScrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {
        return inflater.inflate(R.layout.fragment_sample_view_pager_scroll_view, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle sis) {
        super.onViewCreated(view, sis);

        mScrollView = findView(R.id.scroll_view);

        // generate mock items
        final Context context = getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final LinearLayout linearLayout = findView(mScrollView, R.id.linear_layout);
        final ViewTypeItem viewTypeItem = new ViewTypeItem();
        ViewTypeItem.ItemHolder holder;
        for (int i = 0; i < 100; i++) {
            holder = viewTypeItem.createView(inflater, linearLayout);
            viewTypeItem.bindView(context, holder, "Item #" + i, null);
            linearLayout.addView(holder.itemView);
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mScrollView != null && mScrollView.canScrollVertically(direction);
    }

    @Override
    public void onFlingOver(int y, long duration) {
        if (mScrollView != null) {
            mScrollView.smoothScrollBy(0, y);
        }
    }
}
