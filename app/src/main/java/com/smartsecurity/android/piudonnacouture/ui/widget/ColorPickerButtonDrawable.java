package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

public class ColorPickerButtonDrawable extends Drawable {

    private final static int STROKE_COLOR = 0x1f000000; // 12% black.
    private final static int STROKE_WIDTH_DP = 2;
    private final static int STROKE_TO_FILL_SPACE_DP = 4;

    private final Paint mPaint;
    private final float mDensity;
    private final int mColor;
    private boolean mChecked;

    public ColorPickerButtonDrawable(Context context, @ColorInt int color) {
        this(context, color, false);
    }

    public ColorPickerButtonDrawable(Context context, @ColorInt int color, boolean checked) {
        mColor = color;
        mChecked = checked;
        mDensity = context.getResources().getDisplayMetrics().density;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(STROKE_WIDTH_DP * mDensity);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        float radius = Math.min(bounds.width(), bounds.height()) / 2;
        int padding = (int) (STROKE_TO_FILL_SPACE_DP * mDensity);

        // Draw the circle fill.
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(bounds.width() / 2, bounds.height() / 2, radius - padding, mPaint);

        // Draw the inner circle stroke for better visibility.
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(STROKE_COLOR);
        canvas.drawCircle(bounds.width() / 2, bounds.height() / 2, radius - padding, mPaint);

        // Draw the checked state stroke.
        if (mChecked) {
            mPaint.setColor(mColor);
            canvas.drawCircle(
                    bounds.width() / 2,
                    bounds.height() / 2,
                    radius - (mPaint.getStrokeWidth() / 2),
                    mPaint);
        }

    }

    @Override
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("setAlpha(int) is not supported");
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        throw new UnsupportedOperationException("setColorFilter(ColorFilter) is not supported");
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        invalidateSelf();
    }
}
