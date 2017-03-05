package ru.noties.scrollable;

import android.animation.ValueAnimator;

/**
 * This interface might be used to customize {@link android.animation.ValueAnimator} behavior during close-up animation
 * @see android.animation.ValueAnimator
 * @see InterpolatorCloseUpAnimatorConfigurator
 * Created by Dimitry Ivanov on 22.05.2015.
 */
public interface CloseUpAnimatorConfigurator {

    /**
     * Note that {@link android.animation.ValueAnimator#setDuration(long)} would erase current value set by {@link CloseUpIdleAnimationTime} if any present
     * @param animator current {@link android.animation.ValueAnimator} object to animate close-up animation of a {@link ScrollableLayout}
     */
    void configure(ValueAnimator animator);
}
