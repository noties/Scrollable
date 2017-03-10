package android.support.v4.view;

import android.view.View;
import android.view.ViewGroup;

public class ViewPagerUtils {

    public interface CurrentView {
        View currentView();
    }

    // please note that it registers OnHierarchyChangedListener, so if any has already been registered
    // it will be removed
    public static CurrentView currentView(final ViewPager pager) {
        return CurrentViewImpl.create(pager);
    }

    private static class CurrentViewImpl implements CurrentView, ViewGroup.OnHierarchyChangeListener {

        static CurrentView create(ViewPager pager) {
            final CurrentViewImpl impl = new CurrentViewImpl(pager);
            pager.setOnHierarchyChangeListener(impl);
            return impl;
        }

        private final ViewPager mViewPager;
        private View mView;

        private CurrentViewImpl(ViewPager viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public View currentView() {
            final View view;
            if (mView != null) {
                view = mView;
            } else {
                final int position = mViewPager.getCurrentItem();
                final int count = mViewPager.getChildCount();
                if (count == 0 || position < 0) {
                    view = null;
                } else {
                    View child = null;
                    ViewPager.LayoutParams params;
                    for (int i = 0; i < count; i++) {
                        child = mViewPager.getChildAt(i);
                        params = (ViewPager.LayoutParams) child.getLayoutParams();
                        if (params != null
                                && params.position == position) {
                            break;
                        }
                        child = null;
                    }
                    mView = view = child;
                }
            }
            return view;
        }

        @Override
        public void onChildViewAdded(View parent, View child) {
            mView = null;
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            mView = null;
        }
    }
}
