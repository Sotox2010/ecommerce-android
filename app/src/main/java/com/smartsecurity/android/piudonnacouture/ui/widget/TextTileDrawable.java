package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class TextTileDrawable extends Drawable {

    private static final int DEFAULT_TEXT_COLOR = 0xffffffff;
    private static final int DEFAULT_TILE_COLOR = 0x66000000;
    private static final int DEFAULT_TEXT_SIZE_DP = 22;

    private static TypedArray sColors;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect mTextBounds = new Rect();

    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mTextSizePx;
    private CharSequence mFirstName;
    private CharSequence mLastName;
    private String mDisplayText;


    public TextTileDrawable(final Resources res) {
        mTextSizePx = (int) (DEFAULT_TEXT_SIZE_DP * res.getDisplayMetrics().density + .5f);
        mPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        /*if (sColors == null) {
            sColors = res.obtainTypedArray(R.array.tile_colors);
        }*/

        configDisplayText();
    }

    @Override
    public void draw(Canvas canvas) {
        final RectF bounds = new RectF(getBounds());
        mPaint.setColor(pickColor());

        // Draw the background circle.
        //canvas.drawOval(bounds, mPaint);
        canvas.drawRect(bounds, mPaint);

        if (!TextUtils.isEmpty(mDisplayText)) {
            mPaint.setColor(mTextColor);
            mPaint.setTextSize(mTextSizePx);

            mPaint.getTextBounds(mDisplayText, 0, mDisplayText.length(), mTextBounds);
            int textWidth = (int) mPaint.measureText(mDisplayText);
            int textHeight = mTextBounds.height();

            // Draw the formatted text.
            canvas.drawText(mDisplayText,
                    bounds.centerX() - (textWidth / 2f),
                    bounds.centerY() + (textHeight / 2f),
                    mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("TextTileDrawable doesn't support setAlpha(int)");
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        throw new UnsupportedOperationException("TextTileDrawable doesn't support setColorFilter(ColorFilter)");
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    private void configDisplayText() {
        mDisplayText = "";

        if (!TextUtils.isEmpty(mFirstName)) {
            mDisplayText += getFormattedFirstChar(mFirstName);
        }
        if (!TextUtils.isEmpty(mLastName)) {
            mDisplayText += getFormattedFirstChar(mLastName);
        }
    }

    private char getFormattedFirstChar(CharSequence text) {
        return Character.toUpperCase(text.charAt(0));
    }

    private int pickColor() {
        if (TextUtils.isEmpty(mFirstName) || TextUtils.isEmpty(mLastName)) {
            return DEFAULT_TILE_COLOR;
        }

        CharSequence combined = TextUtils.concat(mFirstName, mLastName);
        int color = Math.abs(combined.hashCode()) % sColors.length();
        return sColors.getColor(color, DEFAULT_TEXT_COLOR);
    }

    public void setTextColor(int color) {
        mTextColor = color;
    }

    public void setTextSizePx(int size, DisplayMetrics dm) {
        mTextSizePx = (int) (size * dm.density + .5f);
    }

    public void setFirstNameAndLastName(CharSequence firstName, CharSequence lastName) {
        mFirstName = firstName;
        mLastName = lastName;
        configDisplayText();
    }

}

