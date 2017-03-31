package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.smartsecurity.android.piudonnacouture.R;

public class Stepper extends LinearLayout {
    private static final String TAG = "Stepper";

    private static final int STEPPER_HEIGHT_DP = 72;
    private static final int LINE_COLOR = 0x1e000000;

    private float mScreenDensity;
    private float[] mLinePoints;
    private final Paint mLinesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    public Stepper(Context context) {
        this(context, null, 0);
    }

    public Stepper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Stepper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(HORIZONTAL);

        mScreenDensity = context.getResources().getDisplayMetrics().density;

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (STEPPER_HEIGHT_DP * mScreenDensity));

        mLinesPaint.setColor(LINE_COLOR);
        mLinesPaint.setStyle(Paint.Style.STROKE);
        mLinesPaint.setStrokeWidth(context.getResources().getDimensionPixelSize(R.dimen.stepper_line_height));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //ArrayList<Float> points = new ArrayList<>();
        //mLinePoints = new float[(getChildCount() - 1) * 4];
        //for int i = mLinePoints.

        //points.toArray(mLinePoints);

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (getChildCount() == 0) {
            return;
        }

        int stepWidth = getMeasuredWidth() / getChildCount();
        int currentX = stepWidth / 2;
        int y = (int) (mScreenDensity * 24);
        int padding = (int) (mScreenDensity * 20);

        for (int i = 1; i < getChildCount(); ++i, currentX += stepWidth) {
            canvas.drawLine(currentX + padding, y, currentX + stepWidth - padding, y, mLinesPaint);

        }

        //canvas.drawLines(mLinePoints, mLinesPaint);
    }

    public void addStep(Step newStep) {
        newStep.setNumber(getChildCount() + 1);
        addView(newStep);
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    private void populateTabStrip() {
        final StepperPagerAdapter adapter = (StepperPagerAdapter) mViewPager.getAdapter();
        final View.OnClickListener stepClickListener = new StepClickListener();

        for (int i = 0; i < adapter.getCount(); i++) {
            Step newStep = new Step.Builder(getContext())
                    .setNumber(i + 1)
                    .setTitle(adapter.getStepTitle(i))
                    .build();

            //newStep.setOnClickListener(stepClickListener);
            addView(newStep);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            /*int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);*/

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            /*if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }*/

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }

    private class StepClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v instanceof Step) {
                Step step = (Step) v;
                mViewPager.setCurrentItem(step.getStepNumber() - 1);
            } else {
                Log.e(TAG, "onClick: this view is not an instance of the Step class.");
            }
        }
    }

    public static abstract class StepperPagerAdapter extends FragmentPagerAdapter {

        public StepperPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return the step title associated with a specified position.
         */
        public abstract CharSequence getStepTitle(int position);
    }
}
