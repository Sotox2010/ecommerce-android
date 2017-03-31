package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.smartsecurity.android.piudonnacouture.R;

import java.util.Arrays;


public class TintableImageView extends ImageView {
    private static final String TAG = "TintableImageView";

    private ColorStateList mTintList;

    public TintableImageView(Context context) {
        this(context, null, 0);
    }

    public TintableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(
                    attrs, R.styleable.TintableView, defStyleAttr, 0);

            mTintList = array.getColorStateList(R.styleable.TintableView_tint);

            array.recycle();
        }
    }

    @Override
    protected void drawableStateChanged() {
        Log.d(TAG, "drawableStateChanged: " + Arrays.toString(getDrawableState()));
        super.drawableStateChanged();
        if (mTintList != null) {
            setColorFilter(mTintList.getColorForState(
                    getDrawableState(), mTintList.getDefaultColor()), PorterDuff.Mode.SRC_IN);
        }
    }

}
