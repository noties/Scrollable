package ru.noties.scrollable.sample.next;

import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {

    protected <V> V findView(int id) {
        //noinspection unchecked
        return (V) findViewById(id);
    }
}
