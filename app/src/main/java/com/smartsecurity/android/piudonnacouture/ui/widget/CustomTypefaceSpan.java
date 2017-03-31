package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

public class CustomTypefaceSpan extends TypefaceSpan {

    private final Typeface mTypeface;

    public CustomTypefaceSpan(Typeface tf) {
        super("sans-serif");
        mTypeface = tf;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        apply(ds, mTypeface);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint, mTypeface);
    }

    private static void apply(Paint paint, Typeface tf) {
        int oldStyle;

        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = 0;
        } else {
            oldStyle = old.getStyle();
        }

        int fake = oldStyle & ~tf.getStyle();
        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }
}
