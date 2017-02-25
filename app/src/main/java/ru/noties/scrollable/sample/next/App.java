package ru.noties.scrollable.sample.next;

import android.app.Application;

import ru.noties.debug.AndroidLogDebugOutput;
import ru.noties.debug.Debug;
import ru.noties.scrollable.sample.BuildConfig;

public class App extends Application {

    // support for all scrolling and non-scrolling views, including ScrollView, ListView,
    // RecyclerView, WebView, etc and ViewPager containing all of the above

    @Override
    public void onCreate() {
        super.onCreate();

        Debug.init(new AndroidLogDebugOutput(BuildConfig.DEBUG));
    }
}
