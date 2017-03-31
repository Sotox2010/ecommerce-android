package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.util.FontUtils;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;

public class Step extends LinearLayout {
    private static final String TAG = "Step";

    public enum State {
        ACTIVE,
        VISITED,
        UNDONE,
        DONE
    }

    private TextView mStepNumberView;
    private TextView mStepTitleView;
    private int mStepNumber;

    private Step(Context context) {
        this(context, null, 0);
    }

    private Step(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private Step(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        setLayoutParams(lp);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setClickable(true);

        Drawable selectableItemBackground = UiUtils.getThemeDrawable(context, R.attr.selectableItemBackground);
        UiUtils.setBackgroundDrawable(this, selectableItemBackground);

        LayoutInflater.from(context).inflate(R.layout.step_view_merge, this, true);

        mStepNumberView = (TextView) findViewById(R.id.step_number);
        mStepTitleView = (TextView) findViewById(R.id.step_title);
    }

    public void setState(State state) {
        FontUtils.setTypeface(mStepTitleView, "Roboto-Medium.ttf");
    }

    public void setTitle(CharSequence title) {
        mStepTitleView.setText(title);
    }

    public void setNumber(int number) {
        mStepNumber = number;
        mStepNumberView.setText(String.valueOf(number));
    }

    public int getStepNumber() {
        return mStepNumber;
    }

    public static class Builder {
        private CharSequence stepTitle;
        private int stepNumber;
        private final Context context;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(CharSequence title) {
            stepTitle = title;
            return this;
        }

        public Builder setNumber(int number) {
            stepNumber = number;
            return this;
        }

        public Step build() {
            Step step = new Step(context);
            step.setNumber(stepNumber);
            step.setTitle(stepTitle);
            return step;
        }
    }

}
