package ru.noties.scrollable.sample;

import android.view.View;

import ru.noties.ccf.CCFAnimator;
import ru.noties.scrollable.OnScrollChangedListener;

public class SampleHeaderViewOnScrollChangedListener implements OnScrollChangedListener {

    private final SampleHeaderView mView;
    private final CCFAnimator mCCFAnimator;

    public SampleHeaderViewOnScrollChangedListener(SampleHeaderView view) {
        mView = view;
        mCCFAnimator = CCFAnimator.rgb(view.getExpandedColor(), view.getCollapsedColor());
    }

    @Override
    public void onScrollChanged(int y, int oldY, int maxY) {
        final View textView = mView.getTextView();
        final int top = textView.getTop();
        final float headerY;
        final boolean isSticky;
        if (Float.compare(y, top) < 0) {
            headerY = .0F;
            isSticky = false;
        } else {
            headerY = y - top;
            isSticky = true;
        }
        textView.setTranslationY(headerY);

        final int color = mCCFAnimator.getColor(headerY / top);
        mView.setBackgroundColor(color);

        // to skip overdraw we remove background if we are in normal state
        // and add background if we are in sticky state
        if (isSticky) {
            textView.setBackgroundColor(color);
        } else {
            textView.setBackgroundColor(0);
        }
    }
}
