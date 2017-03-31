package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;


public class CircleViewPagerIndicator extends View implements ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;

    public CircleViewPagerIndicator(Context context) {
        this(context, null, 0);
    }

    public CircleViewPagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measureWidth(int widthMeasureSpec) {
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        int measuredWidth;

        if (specMode == MeasureSpec.EXACTLY) {
            measuredWidth = specSize;
        } else {
            measuredWidth = 0;
            measuredWidth = Math.min(specSize, measuredWidth);
        }

        return measuredWidth;
    }

    private int measureHeight(int heightMeasureSpec) {
        return 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }


    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setViewPager(@Nullable ViewPager pager) {
        if (mViewPager == pager) {
            return;
        }

        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(this);
        }

        if (pager != null) {
            if (pager.getAdapter() == null) {
                throw new IllegalStateException("The ViewPager has no valid PagerAdapter.");
            }
            pager.addOnPageChangeListener(this);
        }

        mViewPager = pager;
        invalidate();
    }
}
