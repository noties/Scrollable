package ru.noties.scrollable;

public interface OverScrollListener {

    void onOverScrolled(ScrollableLayout layout, int overScrollY);
    boolean hasOverScroll(ScrollableLayout layout, int overScrollY);
    void onCancelled(ScrollableLayout layout);
    void clear();

}
