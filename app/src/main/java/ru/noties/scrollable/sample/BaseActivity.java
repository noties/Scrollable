package ru.noties.scrollable.sample;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public abstract class BaseActivity extends ActionBarActivity {

    protected <V> V findView(Activity activity, int id) {
        //noinspection unchecked
        return (V) activity.findViewById(id);
    }
}
