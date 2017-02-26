package ru.noties.scrollable;

import android.animation.ValueAnimator;

public abstract class OverScrollListenerBase implements OverScrollListener {

    private int mDistanceY;
    private float mRatio = -1.F;
    private ValueAnimator mValueAnimator;


    protected abstract void onRatioChanged(ScrollableLayout layout, float ratio);


    @Override
    public void onOverScrolled(ScrollableLayout layout, int overScrollY) {

        cancelAnimation();

        final int max = getMaxOverScrollY(layout);

        mDistanceY += -overScrollY;
        if (mDistanceY > max) {
            mDistanceY = max;
        }

        final float ratio = getRatio(layout);

        if (Float.compare(mRatio, ratio) != 0) {
            onRatioChanged(layout, ratio);
            mRatio = ratio;
        }
    }

    @Override
    public boolean hasOverScroll(ScrollableLayout layout, int overScrollY) {
        return mDistanceY > 0;
    }

    @Override
    public void onCancelled(final ScrollableLayout layout) {

        cancelAnimation();

        final float ratio = getRatio(layout);

        mValueAnimator = ValueAnimator.ofFloat(.0F, 1.F);
        configureAnimator(mValueAnimator);

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float fraction = animation.getAnimatedFraction();
                mDistanceY = mDistanceY - ((int) (mDistanceY * fraction));
                onRatioChanged(layout, ratio - (ratio * fraction));
            }
        });
        mValueAnimator.start();
    }

    @Override
    public void clear() {
        mDistanceY = 0;
    }

    public void cancelAnimation() {
        if (mValueAnimator != null
                && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
    }

    protected float getRatio(ScrollableLayout layout) {
        final float ratio;
        if (mDistanceY < 0) {
            ratio = .0F;
        } else {
            ratio = (float) mDistanceY / getMaxOverScrollY(layout);
        }
        return ratio;
    }

    protected int getMaxOverScrollY(ScrollableLayout layout) {
        return layout.getMaxScrollY();
    }

    protected void configureAnimator(ValueAnimator animator) {
        animator.setDuration(250L);
    }
}
