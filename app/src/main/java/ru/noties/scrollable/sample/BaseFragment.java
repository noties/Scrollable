package ru.noties.scrollable.sample;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import ru.noties.scrollable.CanScrollVerticallyDelegate;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 28.03.2015.
 */
public abstract class BaseFragment extends Fragment implements CanScrollVerticallyDelegate {

    static final String ARG_COLOR = "arg.Color";

    protected <V> V findView(View view, int id) {
        //noinspection unchecked
        return (V) view.findViewById(id);
    }

    public abstract CharSequence getTitle(Resources r);
    public abstract String getSelfTag();

    protected int getColor() {
        return getArguments().getInt(ARG_COLOR);
    }

    @Override
    public void onViewCreated(View view, Bundle sis) {
        super.onViewCreated(view, sis);

        view.setBackgroundColor(getColor());
    }
}
