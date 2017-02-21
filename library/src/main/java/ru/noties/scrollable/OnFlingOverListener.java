package ru.noties.scrollable;

/**
 * Class to listen for `fling` events and translate possible vertical scroll
 * to child view. This functionality would be useful (and actually used only for that)
 * to create effect of scroll continuation when ScrollableLayout completely collapses.
 * Created by Dimitry Ivanov on 13.04.2016.
 */
public interface OnFlingOverListener {
    /**
     * This method will be called, when ScrollableLayout completely collapses,
     * but initial fling event had big velocity. So this method will be called with
     * theoretical vertical scroll value to scroll the underlining child
     * @param y the final scroll y (theoretical) for underlining scrolling child
     * @param duration theoretical duration of the scroll event based on velocity value
     *                 of touch event
     */
    void onFlingOver(int y, long duration);
}
