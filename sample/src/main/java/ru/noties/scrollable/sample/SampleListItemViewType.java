package ru.noties.scrollable.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.noties.vt.ViewType;

class SampleListItemViewType extends ViewType<SampleListItem, SampleListItemViewType.Holder> {

    @Override
    protected Holder createView(LayoutInflater inflater, ViewGroup parent) {
        return new Holder(inflater.inflate(R.layout.adapter_sample_list_item, parent, false));
    }

    @Override
    protected void bindView(Context context, Holder holder, SampleListItem item, List<Object> payloads) {
        holder.title.setText(item.title());
        holder.description.setText(item.description());
    }

    static class Holder extends ru.noties.vt.Holder {

        final TextView title;
        final TextView description;

        Holder(View itemView) {
            super(itemView);

            title = findView(R.id.sample_list_item_title);
            description = findView(R.id.sample_list_item_description);
        }
    }
}
