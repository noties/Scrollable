package ru.noties.scrollable.sample.next;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.noties.scrollable.sample.R;
import ru.noties.vt.HolderSingle;
import ru.noties.vt.ViewType;

public class ViewTypeItem extends ViewType<String, ViewTypeItem.ItemHolder> {

    @Override
    public ItemHolder createView(LayoutInflater inflater, ViewGroup parent) {
        return new ItemHolder(inflater.inflate(R.layout.vt_sample_view_pager_item, parent, false));
    }

    @Override
    public void bindView(Context context, ItemHolder holder, String item, List<Object> payloads) {
        holder.view.setText(item);
    }

    public static class ItemHolder extends HolderSingle<TextView> {
        ItemHolder(View itemView) {
            super(itemView);
        }
    }
}
