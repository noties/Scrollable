package ru.noties.scrollable;

public interface OverScrollListener {

    void onOverScrolled(ScrollableLayout layout, float overScrollY);
    boolean hasOverScroll(ScrollableLayout layout, float overScrollY);
    void onCancelled(ScrollableLayout layout);
    void clear();

}
