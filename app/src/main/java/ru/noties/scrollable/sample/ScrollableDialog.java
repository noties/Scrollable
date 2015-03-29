package ru.noties.scrollable.sample;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public class ScrollableDialog extends DialogFragment {

    private static final String ARG_FRICTION = "arg.Friction";

    public static ScrollableDialog newInstance(float friction) {
        final Bundle bundle = new Bundle();
        bundle.putFloat(ARG_FRICTION, friction);

        final ScrollableDialog fragment = new ScrollableDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle sis) {

        final Dialog dialog = new Dialog(getActivity(), R.style.ScrollableDialogStyle);
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.fragment_dialog, null);

        dialog.setContentView(view);

        final ScrollableLayout scrollableLayout = (ScrollableLayout) view.findViewById(R.id.scrollable_layout);
        final View placeholder = view.findViewById(R.id.dialog_placeholder);
        final View title = view.findViewById(R.id.dialog_title);

        placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        scrollableLayout.setDraggableView(title);
        scrollableLayout.setFriction(getArguments().getFloat(ARG_FRICTION, BuildConfig.START_FRICTION));

        final ListView listView = (ListView) view.findViewById(R.id.list_view);
        final BaseListAdapter adapter = new BaseListAdapter(getActivity(), 50);
        listView.setAdapter(adapter);

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return listView.canScrollVertically(direction);
            }
        });

        scrollableLayout.setOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int y, int oldY, int maxY) {

                final float tY;
                if (y < maxY) {
                    tY = .0F;
                } else {
                    tY = y - maxY;
                }

                title.setTranslationY(tY);

                final float alpha = 1.F - ((float) y / maxY);
                placeholder.setAlpha(alpha);
            }
        });

        return dialog;
    }
}
