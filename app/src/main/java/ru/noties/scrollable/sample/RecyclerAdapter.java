package ru.noties.scrollable.sample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

    private final LayoutInflater mInflater;
    private final int mCount;

    public RecyclerAdapter(Context context, int count) {
        this.mInflater = LayoutInflater.from(context);
        this.mCount = count;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup group, int i) {
        final View view = mInflater.inflate(android.R.layout.simple_list_item_1, group, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int i) {
        ((TextView) holder.itemView).setText(String.valueOf(i));
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    static class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }
    }
}
