package ru.noties.scrollable;

import android.animation.ValueAnimator;
import android.view.animation.Interpolator;

public class InterpolatorCloseUpAnimatorConfigurator implements CloseUpAnimatorConfigurator {

    private final Interpolator mInterpolator;

    public InterpolatorCloseUpAnimatorConfigurator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(ValueAnimator animator) {
        animator.setInterpolator(mInterpolator);
    }
}
