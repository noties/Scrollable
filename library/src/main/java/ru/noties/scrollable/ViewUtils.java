package ru.noties.scrollable;

import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by Dimitry Ivanov on 13.04.2016.
 */
class ViewUtils {

    private ViewUtils() {}

    static void removeGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {

        final ViewTreeObserver observer = view.getViewTreeObserver();
        if (!observer.isAlive()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            observer.removeOnGlobalLayoutListener(onGlobalLayoutListener);
        } else {
            //noinspection deprecation
            observer.removeGlobalOnLayoutListener(onGlobalLayoutListener);
        }
    }
}
