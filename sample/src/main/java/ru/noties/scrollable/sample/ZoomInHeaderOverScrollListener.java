package ru.noties.scrollable.sample;

import android.view.View;

import ru.noties.scrollable.OverScrollListenerBase;
import ru.noties.scrollable.ScrollableLayout;

public class ZoomInHeaderOverScrollListener extends OverScrollListenerBase {

    private final View mHeader;
    private final View mContent;

    public ZoomInHeaderOverScrollListener(View header, View content) {
        mHeader = header;
        mContent = content;
    }

    @Override
    protected void onRatioChanged(ScrollableLayout layout, float ratio) {
        final float scale = 1.F + (.33F * ratio);
        mHeader.setScaleX(scale);
        mHeader.setScaleY(scale);

        final int headerHeight = mHeader.getHeight();
        mContent.setTranslationY(((headerHeight * scale) - headerHeight) / 2.F);
    }

    @Override
    protected int getMaxOverScrollY(ScrollableLayout layout) {
        return layout.getMaxScrollY() / 2;
    }
}
