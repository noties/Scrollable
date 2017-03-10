package ru.noties.scrollable.sample;

import android.app.Application;

import ru.noties.debug.AndroidLogDebugOutput;
import ru.noties.debug.Debug;
import ru.noties.scrollable.sample.BuildConfig;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Debug.init(new AndroidLogDebugOutput(BuildConfig.DEBUG));
    }
}
