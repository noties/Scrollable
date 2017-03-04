package ru.noties.scrollable.sample;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.IdRes;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by Dimitry Ivanov on 25.12.2015.
 */
public class ViewUtils {

    private ViewUtils() {}

    public static <V extends View> V findView(Activity activity, @IdRes int id) {
        //noinspection unchecked
        return (V) activity.findViewById(id);
    }

    public static <V extends View> V findView(View view, @IdRes int id) {
        //noinspection unchecked
        return (V) view.findViewById(id);
    }

    public static void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        if (Build.VERSION.SDK_INT >= 16) {

        } else {
            //noinspection deprecation
            observer.removeGlobalOnLayoutListener(listener);
        }
    }
}
