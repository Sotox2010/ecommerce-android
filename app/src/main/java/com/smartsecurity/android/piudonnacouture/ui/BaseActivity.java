package com.smartsecurity.android.piudonnacouture.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class BaseActivity extends AppCompatActivity {

    private boolean mIsBackEnabled = true;

    //private Toolbar mToolbarActionBar;
    private View mDropShadowView;

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    protected  @interface Visibility {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isSessionRequired()) {
            
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        //mToolbarActionBar = (Toolbar) findViewById(R.id.toolbar);

        mDropShadowView = findViewById(R.id.drop_shadow);
        if (mDropShadowView != null) {
            mDropShadowView.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsBackEnabled) {
            super.onBackPressed();
        }
    }

    protected boolean isSessionRequired() {
        return false;
    }

    protected View getDropShadowView() {
        return mDropShadowView;
    }

    protected void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }

    /**
     * Configure this Activity as a floating window, with the given {@code width}, {@code height}
     * and {@code alpha}, and dimming the background with the given {@code dim} value.
     */
    protected void setupFloatingWindow(int width, int height, int alpha, float dim) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        //params.width = getResources().getDimensionPixelSize(width);
        //params.height = getResources().getDimensionPixelSize(height);
        params.width = width;
        params.height = height;
        params.alpha = alpha;
        params.dimAmount = dim;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    /**
     * Returns true if the theme sets the {@code R.attr.isFloatingWindow} flag to true.
     */
    /*protected boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();

        // Check isFloatingWindow flag is defined in theme.
        if (theme == null || !theme
                .resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag, true)) {
            return false;
        }

        return (floatingWindowFlag.data != 0);
    }*/

    protected boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();

        // Check isFloatingWindow flag is defined in theme.
        if (theme == null || !theme
                .resolveAttribute(android.R.attr.windowIsFloating, floatingWindowFlag, true)) {
            return false;
        }

        return (floatingWindowFlag.data != 0);
    }

    protected void setBackEnabled(boolean enabled) {
        mIsBackEnabled = enabled;
    }

    /**
     * Tints the toolbar navigation icon with the ?colorAccent.
     */
    protected void colorizeNavigationIcon(Toolbar toolbar) {
        if (toolbar != null) {
            Drawable navIcon = toolbar.getNavigationIcon();
            if (navIcon != null) {
                navIcon.setColorFilter(new PorterDuffColorFilter(
                        UiUtils.getThemeColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        Intent parentIntent = super.getParentActivityIntent();
        if (parentIntent != null) {
            parentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        return parentIntent;
    }
}
