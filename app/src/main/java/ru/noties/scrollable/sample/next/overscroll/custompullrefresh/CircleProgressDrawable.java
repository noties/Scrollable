package ru.noties.scrollable.sample.next.overscroll.custompullrefresh;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

class CircleProgressDrawable extends Drawable {

    private final Paint mPaint;
    private final RectF mRectF;

    private float mProgress;

    CircleProgressDrawable(int color) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        mRectF = new RectF();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRectF.set(bounds);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!mRectF.isEmpty()) {
            final int save = canvas.save();
            try {
                canvas.drawArc(mRectF, 270.F, (360.F * mProgress), true, mPaint);
            } finally {
                canvas.restoreToCount(save);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    void setProgress(float progress) {
        mProgress = progress;
        invalidateSelf();
    }

    float getProgress() {
        return mProgress;
    }
}
