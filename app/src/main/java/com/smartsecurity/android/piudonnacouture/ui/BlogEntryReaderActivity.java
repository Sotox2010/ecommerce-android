package com.smartsecurity.android.piudonnacouture.ui;


import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Property;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.ui.widget.SynchronizedScrollView;
import com.smartsecurity.android.piudonnacouture.ui.widget.ViewBackgroundColorProperty;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.text.WordUtils;

public class BlogEntryReaderActivity extends BaseActivity implements SynchronizedScrollView.OnScrollListener{

    private static final String TAG = "BlogEntryReaderActivity";

    public static final String EXTRA_ENTRY_TITLE = "extra_entry_title";
    public static final String EXTRA_ENTRY_CONTENT = "extra_entry_content";
    public static final String EXTRA_ENTRY_IMAGE_URL = "extra_entry_image_url";

    public static final String STATE_ACTION_BAR_TRANSPARENT = "state_action_bar_transparent";

    /**
     * The default interpolator for animations.
     */
        private static final long ANIMATION_DURATION = 250L;

    /**
     * The default interpolator for animations.
     */
    private static final DecelerateInterpolator ANIMATION_INTERPOLATOR = new DecelerateInterpolator();

    /**
     * ARGB evaluator for color transition animations.
     */
    private static final ArgbEvaluator COLOR_EVALUATOR = new ArgbEvaluator();

    /**
     * Custom property for background color transitions on View's.
     */
    private static final Property<View, Integer> BACKGROUND_COLOR_PROPERTY =
            new ViewBackgroundColorProperty(Integer.TYPE, "backgroundColor");

    private Toolbar mToolbarActionBar;
    private ImageView mImageView;

    private ObjectAnimator mToolbarBackgroundColorAnimator;

    private int mMaxToolbarElevation;
    private boolean mToolbarActionBarTransparent = true;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_entry_reader);

        mToolbarActionBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbarActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        String title;
        String content;
        String imageUrl;

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ENTRY_CONTENT) && intent.hasExtra(EXTRA_ENTRY_IMAGE_URL) && intent.hasExtra(EXTRA_ENTRY_TITLE)) {
            Bundle extras = intent.getExtras();
            title = extras.getString(EXTRA_ENTRY_TITLE);
            content = extras.getString(EXTRA_ENTRY_CONTENT);
            imageUrl = extras.getString(EXTRA_ENTRY_IMAGE_URL);
        } else {
            throw new IllegalStateException("Extras needed!!");
        }

        if (savedInstanceState != null) {
            mToolbarActionBarTransparent =
                    savedInstanceState.getBoolean(STATE_ACTION_BAR_TRANSPARENT);
        }

        final SynchronizedScrollView scrollView = (SynchronizedScrollView) findViewById(R.id.scroll_view);
        scrollView.addOnScrollListener(this);

        findViewById(R.id.root).getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                onScrollChanged(0, scrollView.getScrollY(), 0, 0);
            }
        });

        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(WordUtils.capitalizeFully(title));

        //TextView dateView = (TextView) findViewById(R.id.date);

        TextView contentView = (TextView) findViewById(R.id.content);
        contentView.setText(content);

        mImageView = (ImageView) findViewById(R.id.image);
        Picasso.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .into(mImageView);

        mMaxToolbarElevation = getResources().getDimensionPixelSize(R.dimen.appbar_elevation);

        mToolbarBackgroundColorAnimator = ObjectAnimator.ofInt(
                mToolbarActionBar, BACKGROUND_COLOR_PROPERTY, 0);
        mToolbarBackgroundColorAnimator.setInterpolator(ANIMATION_INTERPOLATOR);
        mToolbarBackgroundColorAnimator.setEvaluator(COLOR_EVALUATOR);
        mToolbarBackgroundColorAnimator.setDuration(ANIMATION_DURATION);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_ACTION_BAR_TRANSPARENT, mToolbarActionBarTransparent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollChanged(int scrollX, int scrollY, int deltaX, int deltaY) {
        boolean shouldGoTransparent = scrollY < 20;

        if (shouldGoTransparent != mToolbarActionBarTransparent) {
            //int startColor = shouldGoTransparent ? Color.WHITE : Color.TRANSPARENT;
            int endColor = shouldGoTransparent ? Color.TRANSPARENT : Color.WHITE;
            int desiredElevation = shouldGoTransparent ? 0 : mMaxToolbarElevation;

            mToolbarBackgroundColorAnimator.setIntValues(endColor);
            mToolbarBackgroundColorAnimator.start();

            /*ObjectAnimator animator = ObjectAnimator.ofInt(mToolbarActionBar,
                    "backgroundColor", startColor, endColor);
            animator.setEvaluator(new ArgbEvaluator());
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(ANIMATION_DURATION);
            animator.start();*/

            ViewCompat.animate(mToolbarActionBar).translationZ(desiredElevation)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(ANIMATION_INTERPOLATOR)
                    .start();
        }

        mToolbarActionBarTransparent = shouldGoTransparent;
    }
}
