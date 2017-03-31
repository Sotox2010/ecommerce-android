package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.smartsecurity.android.piudonnacouture.R;

public class StepsViewPager extends ViewPager {
    private boolean mSwipeable = true;

    public StepsViewPager(Context context) {
        super(context);
    }

    public StepsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(
                    attrs, R.styleable.StepsViewPager, 0, 0);

            mSwipeable = array.getBoolean(R.styleable.StepsViewPager_swipeable, true);

            array.recycle();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mSwipeable && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSwipeable && super.onTouchEvent(ev);
    }

    public void setSwipeable(boolean swipeable) {
        mSwipeable = swipeable;
    }
}
