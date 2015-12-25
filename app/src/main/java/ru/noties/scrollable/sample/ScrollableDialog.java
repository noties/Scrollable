package ru.noties.scrollable.sample;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ListView;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.CloseUpAlgorithm;
import ru.noties.scrollable.DefaultCloseUpAlgorithm;
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

        final ScrollableLayout scrollableLayout = findView(view, R.id.scrollable_layout);
        scrollableLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int height = scrollableLayout.getChildAt(0).getHeight();
                if (height == 0) {
                    return;
                }
                ViewUtils.removeOnGlobalLayoutListener(scrollableLayout, this);
                scrollableLayout.setMaxScrollY(height);
                scrollableLayout.scrollTo(0, height / 2);
                scrollableLayout.setCloseUpAlgorithm(new DialogCloseUpAlgorithm());
            }
        });

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

        final ListView listView = findView(view, R.id.list_view);
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

                if (y < (maxY / 4)) {
                    final float alpha = (float) y / (maxY / 4);
                    scrollableLayout.getChildAt(2).setAlpha(alpha);
                    scrollableLayout.getChildAt(1).setAlpha(alpha);
                } else {
                    scrollableLayout.getChildAt(2).setAlpha(1.F);
                    scrollableLayout.getChildAt(1).setAlpha(1.F);
                }

                if (y == 0) {
                    dismiss();
                }
            }
        });

        return dialog;
    }

    private <V extends View> V findView(View view, @IdRes int id) {
        return ViewUtils.findView(view, id);
    }

    private static class DialogCloseUpAlgorithm extends DefaultCloseUpAlgorithm {
        @Override
        public int getFlingFinalY(ScrollableLayout layout, boolean isScrollingBottom, int nowY, int suggestedY, int maxY) {

            if (isScrollingBottom) {
                if (nowY < (maxY / 2)) {
                    return super.getFlingFinalY(layout, true, nowY, suggestedY, maxY);
                } else {
                    return maxY / 2;
                }
            }

            return super.getFlingFinalY(layout, false, nowY, suggestedY, maxY);
        }

        @Override
        public int getIdleFinalY(ScrollableLayout layout, int nowY, int maxY) {
            final int quad = maxY / 4;
            if (nowY > maxY - quad) {
                return maxY;
            } else if (nowY < quad) {
                return 0;
            } else {
                return maxY / 2;
            }
        }
    }
}
