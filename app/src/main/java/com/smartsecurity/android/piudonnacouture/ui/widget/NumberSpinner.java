package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;

public class NumberSpinner extends LinearLayout {
    private static final String TAG = "NumberSpinner";

    public interface OnValueChangeListener {
        void onValueChanged(int newValue);
    }

    private TextView mValueView;
    private ImageView mDecrementButton;
    private ImageView  mIncrementButton;
    private ProgressBar mProgress;
    private OnValueChangeListener mValueChangeListener;

    private int mMinValue = 0;
    private int mMaxValue = 0;
    private int mCurrentValue = 0;

    public NumberSpinner(Context context) {
        this(context, null, 0);
    }

    public NumberSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "NumberSpinner: Constructor.");

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setShowDividers(SHOW_DIVIDER_MIDDLE);
        setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.divider_vertical_light));
        UiUtils.setBackgroundDrawable(this, ContextCompat.getDrawable(context,
                R.drawable.number_spinner_background));

        LayoutInflater.from(context).inflate(R.layout.number_spinner_merge, this, true);

        mValueView = (TextView) findViewById(R.id.spinner_value_view);
        mValueView.setText(Integer.toString(mCurrentValue));

        mProgress = (ProgressBar) findViewById(R.id.spinner_progress);

        mDecrementButton = (ImageView) findViewById(R.id.spinner_decrement_button);
        mDecrementButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementValue();
            }
        });

        mIncrementButton = (ImageView) findViewById(R.id.spinner_increment_button);
        mIncrementButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementValue();
            }
        });

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(
                    attrs, R.styleable.NumberSpinner, defStyleAttr, 0);

            mMaxValue = array.getInt(R.styleable.NumberSpinner_maxValue, mMaxValue);

            int minValue = array.getInt(R.styleable.NumberSpinner_minValue, mMinValue);
            setMinValue(minValue);

            int currentValue = array.getInt(R.styleable.NumberSpinner_value, mCurrentValue);
            setCurrentValue(currentValue);

            // FIXME: Fix setting textAppearance property.
            /*int textAppearanceResId = array.getResourceId(
                    R.styleable.NumberSpinner_android_textAppearance, -1);

            if (textAppearanceResId != -1) {
                setTextAppearance(textAppearanceResId);
            }*/

            setEnabled(array.getBoolean(R.styleable.NumberSpinner_android_enabled, true));

            array.recycle();
        }

    }

    public int getCurrentValue() {
        return mCurrentValue;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setCurrentValue(int value) {
        int newValue = (int) UiUtils.clamp(value, mMinValue, mMaxValue);
        updateValueViewIfNeeded(newValue);
    }

    public void setMaxValue(int max) {
        if (max < mMinValue) {
            throw new IllegalStateException("The max value should be greater or equal than the min value.");
        }

        mMaxValue = max;
        int newValue = (int) UiUtils.clamp(mCurrentValue, mMinValue, mMaxValue);
        updateValueViewIfNeeded(newValue);
    }

    public void setMinValue(int min) {
        if (min > mMaxValue) {
            throw new IllegalStateException("The min value should be less or equal than the max value.");
        }

        mMinValue = min;
        int newValue = (int) UiUtils.clamp(mCurrentValue, mMinValue, mMaxValue);
        updateValueViewIfNeeded(newValue);
    }

    public void setOnValueChangedListener(OnValueChangeListener listener) {
        mValueChangeListener = listener;
    }

    public void showProgress() {
        setProgressShown(true);
    }

    public void hideProgress() {
        setProgressShown(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mValueView.setEnabled(enabled);
        mDecrementButton.setEnabled(enabled);
        mIncrementButton.setEnabled(enabled);
    }

    public void setTextAppearance(@StyleRes int resId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mValueView.setTextAppearance(getContext(), resId);
        } else {
            mValueView.setTextAppearance(resId);
        }
    }

    private void setProgressShown(boolean shown) {
        mProgress.setVisibility(shown ? VISIBLE : GONE);
        mValueView.setVisibility(shown ? INVISIBLE : VISIBLE);
    }

    private void updateValueViewIfNeeded(int newValue) {
        if (mCurrentValue != newValue) {
            mCurrentValue = newValue;
            mValueView.setText(Integer.toString(mCurrentValue));
            if (mValueChangeListener != null) {
                mValueChangeListener.onValueChanged(mCurrentValue);
            }
        }
    }

    private void incrementValue() {
        int newValue = (int) UiUtils.clamp(mCurrentValue + 1, mMinValue, mMaxValue);
        updateValueViewIfNeeded(newValue);
    }

    private void decrementValue() {
        int newValue = (int) UiUtils.clamp(mCurrentValue - 1, mMinValue, mMaxValue);
        updateValueViewIfNeeded(newValue);
    }

    public static class SavedState extends BaseSavedState {
        private int mMinValue;
        private int mMaxValue;
        private int mCurrentValue;

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        public SavedState(Parcel source) {
            super(source);
            mMinValue = source.readInt();
            mMaxValue = source.readInt();
            mCurrentValue = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mMinValue);
            out.writeInt(mMaxValue);
            out.writeInt(mCurrentValue);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d(TAG, "onSaveInstanceState: ");
        Parcelable superState = super.onSaveInstanceState();
        if (isSaveEnabled()) {
            SavedState ss = new SavedState(superState);
            ss.mMinValue = mMinValue;
            ss.mMaxValue = mMaxValue;
            ss.mCurrentValue = mCurrentValue;
            return ss;
        }
        return superState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.d(TAG, "onRestoreInstanceState: ");
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mMinValue = ss.mMinValue;
        mMaxValue = ss.mMaxValue;
        updateValueViewIfNeeded(ss.mCurrentValue);
    }
}
