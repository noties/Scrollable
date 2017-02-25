package ru.noties.scrollable.sample.next.viewpager.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import ru.noties.scrollable.sample.R;

public class FragmentPagerFragmentWebView extends FragmentPagerFragment {

    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {
        return inflater.inflate(R.layout.fragment_pager_web_view, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle sis) {
        super.onViewCreated(view, sis);

        mWebView = findView(R.id.web_view);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("https://github.com/noties/Scrollable");
            }
        });
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mWebView != null && mWebView.canScrollVertically(direction);
    }

    @Override
    public void onFlingOver(int y, long duration) {
        if (mWebView != null) {
            mWebView.flingScroll(0, y);
        }
    }

    @Override
    public void onDestroyView() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        super.onDestroyView();
    }
}
