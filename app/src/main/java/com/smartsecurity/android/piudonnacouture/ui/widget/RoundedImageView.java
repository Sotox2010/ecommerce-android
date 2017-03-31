package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class RoundedImageView extends TintableImageView {

    private static final String TAG = "RoundedImageView";

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
