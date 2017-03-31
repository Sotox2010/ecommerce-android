package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.smartsecurity.android.piudonnacouture.R;

/**
 * Extension of ImageView that assumes a measured (non-zero) width and sets the
 * height according to the provided aspect ratio.
 */
public class AspectRatioImageView extends ImageView {
    private float mAspectRatio = 0f;

    public AspectRatioImageView(Context context) {
        this(context, null, 0);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(
                    attrs, R.styleable.AspectRatioView, defStyleAttr, 0);

            mAspectRatio = array.getFloat(R.styleable.AspectRatioView_aspectRatio, 0f);

            array.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAspectRatio == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width / mAspectRatio);
        setMeasuredDimension(width, height);
    }
}
