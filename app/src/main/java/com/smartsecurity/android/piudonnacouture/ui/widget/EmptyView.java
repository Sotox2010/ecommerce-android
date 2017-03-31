package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;

public class EmptyView extends LinearLayout {
    private static final String TAG = "EmptyView";

    private final Context mContext;
    private ImageView mImageView;
    private TextView mTitleView;
    private TextView mSubtitleView;
    private Button mActionButton;

    private @DrawableRes int mImageResId;

    public EmptyView(Context context) {
        this(context, null, 0);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.emptyViewStyle);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        LayoutInflater.from(context).inflate(R.layout.empty_view_merge, this, true);

        mImageView = (ImageView) findViewById(R.id.empty_image);
        mTitleView = (TextView) findViewById(R.id.empty_title);
        mSubtitleView = (TextView) findViewById(R.id.empty_subtitle);
        mActionButton = (Button) findViewById(R.id.empty_action);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(
                    attrs, R.styleable.EmptyView, defStyleAttr, 0);


            int resId = array.getResourceId(R.styleable.EmptyView_android_src, -1);
            if (resId != -1) {
                setImageResource(resId);
            }

            setTitle(array.getString(R.styleable.EmptyView_android_title));
            setSubtitle(array.getString(R.styleable.EmptyView_android_subtitle));
            setAction(array.getString(R.styleable.EmptyView_action), null);

            array.recycle();
        }

    }

    public void setImageResource(@DrawableRes int resId) {
        mImageResId = resId;
        setImageDrawable(ContextCompat.getDrawable(mContext, resId));
    }

    private void setImageDrawable(@Nullable Drawable drawable) {
        mImageView.setImageDrawable(drawable);
        mImageView.setVisibility(drawable == null ? GONE : VISIBLE);
    }

    public void setTitle(@StringRes int resId) {
        setTitle(mContext.getString(resId));
    }

    public void setTitle(@Nullable CharSequence text) {
        mTitleView.setText(text);
        mTitleView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setSubtitle(@StringRes int resId) {
        setSubtitle(mContext.getString(resId));
    }

    public void setSubtitle(@Nullable CharSequence text) {
        mSubtitleView.setText(text);
        mSubtitleView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setAction(@Nullable CharSequence text, @Nullable OnClickListener listener) {
        boolean unset = TextUtils.isEmpty(text);
        mActionButton.setText(unset ? null : text);
        mActionButton.setOnClickListener(unset ? null : listener);
        mActionButton.setVisibility(unset ? GONE : VISIBLE);
    }

    public void reset() {
        mImageView.setVisibility(GONE);
        mTitleView.setVisibility(GONE);
        mSubtitleView.setVisibility(GONE);
        mActionButton.setVisibility(GONE);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isSaveEnabled()) {
            SavedState ss = new SavedState(superState);
            ss.mTitle = mTitleView.getText().toString();
            ss.mSubtitle = mSubtitleView.getText().toString();
            ss.mAction = mActionButton.getText().toString();
            ss.mImageResId = mImageResId;
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

        mTitleView.setText(ss.mTitle);
        mSubtitleView.setText(ss.mSubtitle);
        mActionButton.setText(ss.mAction);
        setImageResource(ss.mImageResId);
    }

    public static class SavedState extends BaseSavedState {
        private String mTitle;
        private String mSubtitle;
        private String mAction;
        private int mImageResId;

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
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(mTitle);
            out.writeString(mSubtitle);
            out.writeString(mAction);
            out.writeInt(mImageResId);
        }
    }
}
