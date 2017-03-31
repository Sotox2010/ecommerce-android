package com.smartsecurity.android.piudonnacouture.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.smartsecurity.android.piudonnacouture.util.LazyListManager;

import java.util.List;

public class SynchronizedScrollView extends ScrollView {

    public interface OnScrollListener {
        void onScrollChanged(int scrollX, int scrollY, int deltaX, int deltaY);
    }

    private List<OnScrollListener> mCallbacks;
    private boolean mIsOverScrollEnabled = true;

    public SynchronizedScrollView(Context context) {
        super(context);
    }

    public SynchronizedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SynchronizedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mCallbacks != null) {
            for (OnScrollListener listener : mCallbacks) {
                listener.onScrollChanged(l, t, l - oldl, t - oldt);
            }
        }
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(
                deltaX,
                deltaY,
                scrollX,
                scrollY,
                scrollRangeX,
                scrollRangeY,
                mIsOverScrollEnabled ? maxOverScrollX : 0,
                mIsOverScrollEnabled ? maxOverScrollY : 0,
                isTouchEvent);
    }

    public void addOnScrollListener(OnScrollListener listener) {
        mCallbacks = LazyListManager.add(mCallbacks, listener);
    }

    public void removeOnScrollListener(OnScrollListener listener) {
        mCallbacks = LazyListManager.remove(mCallbacks, listener);
    }

    public void setOverScrollEnabled(boolean enabled) {
        mIsOverScrollEnabled = enabled;
    }

    public boolean isOverScrollEnabled() {
        return mIsOverScrollEnabled;
    }
}
