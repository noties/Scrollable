package ru.noties.scrollable.sample;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Dimitry Ivanov (mail@dimitryivanov.ru) on 29.03.2015.
 */
public class TabsLayout extends HorizontalScrollView {

    private ViewGroup mContainer;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private LayoutInflater mInflater;

    private final Rect mRect;
    {
        mRect = new Rect();
    }

    public TabsLayout(Context context) {
        super(context);
        init(context, null);
    }

    public TabsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TabsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {

        mInflater = LayoutInflater.from(context);

        mContainer = new LinearLayout(context);
        ((LinearLayout) mContainer).setOrientation(LinearLayout.HORIZONTAL);
        mContainer.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        mContainer.setLayoutTransition(new LayoutTransition());

        addView(mContainer);
    }

    public void setViewPager(ViewPager pager) {
        if (mPagerAdapter != null) {
            mContainer.removeAllViews();
        }

        mPager = pager;
        mPagerAdapter = pager.getAdapter();
        populateViews();
        setItemSelected(0);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setItemSelected(position);
                mContainer.getChildAt(position).getHitRect(mRect);
                post(new Runnable() {
                    @Override
                    public void run() {
                        smoothScrollTo(mRect.left, 0);
                    }
                });
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void populateViews() {
        final int count = mPagerAdapter != null ? mPagerAdapter.getCount() : 0;
        if (count < 0) {
            return;
        }

        TextView view;
        for (int i = 0; i < count; i++) {
            view = createTabView();
            view.setText(mPagerAdapter.getPageTitle(i));
            final int position = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPager.setCurrentItem(position);
                }
            });
            mContainer.addView(view, i);
        }
    }

    private TextView createTabView() {
        return (TextView) mInflater.inflate(R.layout.view_tab_item, mContainer, false);
    }

    public void setItemSelected(int position) {
        final int count = mContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            mContainer.getChildAt(i).setSelected(i == position);
        }
    }
}
