package ru.noties.scrollable;

public interface OverScrollListener {
    void onOverScrolled(ScrollableLayout layout, float overScrollY);
    void onCancelled(ScrollableLayout layout);
}
