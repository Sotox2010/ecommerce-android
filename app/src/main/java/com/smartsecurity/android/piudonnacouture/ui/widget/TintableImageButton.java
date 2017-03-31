package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.smartsecurity.android.piudonnacouture.R;

public class TintableImageButton extends ImageButton {

    private ColorStateList mTintList;

    public TintableImageButton(Context context) {
        this(context, null, 0);
    }

    public TintableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TintableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
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
        super.drawableStateChanged();
        if (mTintList != null) {
            setColorFilter(mTintList.getColorForState(
                    getDrawableState(), mTintList.getDefaultColor()), PorterDuff.Mode.SRC_IN);
        }
    }
}
