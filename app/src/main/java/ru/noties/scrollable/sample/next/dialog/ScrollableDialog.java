package ru.noties.scrollable.sample.next.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.DefaultCloseUpAlgorithm;
import ru.noties.scrollable.OnFlingOverListener;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;
import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.ViewUtils;
import ru.noties.scrollable.sample.next.ItemsGenerator;
import ru.noties.scrollable.sample.next.ViewTypeItem;
import ru.noties.vt.ViewTypesAdapter;

public class ScrollableDialog extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle sis) {

        final Context context = getContext();
        final Dialog dialog = new Dialog(context, R.style.ScrollableDialogStyle);
        final LayoutInflater inflater = LayoutInflater.from(context);
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

        final RecyclerView recyclerView = findView(view, R.id.recycler_view);
        final ViewTypesAdapter<String> adapter = ViewTypesAdapter.builder(String.class)
                .register(String.class, new ViewTypeItem())
                .build(context);
        adapter.setItems(ItemsGenerator.generate(100));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return recyclerView.canScrollVertically(direction);
            }
        });
        scrollableLayout.setOnFlingOverListener(new OnFlingOverListener() {
            @Override
            public void onFlingOver(int y, long duration) {
                recyclerView.smoothScrollBy(0, y);
            }
        });

        scrollableLayout.addOnScrollChangedListener(new OnScrollChangedListener() {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = dialog.getWindow();
            if (window != null) {
                window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.scrollable_dialog_status_bar_color));
            }
        }

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
