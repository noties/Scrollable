package ru.noties.scrollable.sample.next;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import ru.noties.scrollable.sample.R;
import ru.noties.scrollable.sample.ViewUtils;

public class SampleHeaderView extends FrameLayout {

    private TextView mTextView;

    private int mCollapsedColor;
    private int mExpandedColor;

    public SampleHeaderView(Context context) {
        this(context, null);
    }

    public SampleHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SampleHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {

        inflate(context, R.layout.view_sample_header, this);

        mTextView = ViewUtils.findView(this, R.id.text_view);

        if (attributeSet != null) {
            final TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.SampleHeaderView);
            try {
                mCollapsedColor = array.getColor(R.styleable.SampleHeaderView_shv_collapsedColor, 0);
                mExpandedColor = array.getColor(R.styleable.SampleHeaderView_shv_expandedColor, 0);
                mTextView.setText(array.getString(R.styleable.SampleHeaderView_shv_title));
            } finally {
                array.recycle();
            }
        }
    }

    public TextView getTextView() {
        return mTextView;
    }

    public int getCollapsedColor() {
        return mCollapsedColor;
    }

    public int getExpandedColor() {
        return mExpandedColor;
    }
}
