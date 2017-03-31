package com.smartsecurity.android.piudonnacouture.ui;

import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.smartsecurity.android.piudonnacouture.Config;
import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.loader.ProductDetailLoader;
import com.smartsecurity.android.piudonnacouture.model.ProductColor;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.ui.widget.EmptyView;
import com.smartsecurity.android.piudonnacouture.ui.widget.SynchronizedScrollView;
import com.smartsecurity.android.piudonnacouture.util.AccountUtils;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends BaseActivity implements SynchronizedScrollView.OnScrollListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ProductDetailActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, long productServerId, String productCode) {
        Intent startIntent = new Intent(context, ProductDetailActivity.class);
        startIntent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, productServerId);
        startIntent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_CODE, productCode);
        return startIntent;
    }

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_PRODUCT_CODE = "extra_product_code";

    private static final String STATE_COLOR_CHECKED_POSITION = "state_color_checked_position";

    private static final long ANIMATION_DURATION = 333L;
    private static final TimeInterpolator ANIMATION_INTERPOLATOR = new DecelerateInterpolator(2f);

    private static final float SLIDER_ASPECT_RATIO = 1.0f;

    private Toolbar mToolbarActionBar;
    private List<Drawable> mToolbarIcons = new ArrayList<>();

    private ViewPager mSliderViewPager;
    private TextView mTitleView;
    private TextView mPriceView;
    private TextView mDescriptionView;
    private View mHeaderView;
    private View mHeaderContentBox;
    private View mHeaderBackgroundView;
    private View mPlaceholder;
    private View mPhotoContainer;
    private FloatingActionButton mAddToCartFab;
    private SliderPagerAdapter mSliderPagerAdapter;
    private RadioGroup mColorPickerRadioGroup;
    private View mContentView;
    private ProgressBar mProgressBar;
    private ViewStub mEmptyViewStub;
    private EmptyView mEmptyView;

    private Long mProductId;
    private String mProductCode;
    private String mProductName;
    private float mProductPrice;

    private int mActionBarHeightPixels;
    private int mHeaderHeightPixels;
    private int mHeaderPaddingTop;
    private int mHeaderMaxElevation;
    private int mAddToCartFabHeightPixels;
    private int mAddToCartFabMaxElevation;

    private boolean mHeaderExpanded = false;
    private int mColorCheckedPosition = 0;

    private LoaderManager.LoaderCallbacks<JsonObject> mDetailLoaderCallbacks;
    private List<ProductColor> mColors;
    private String mProductDetailsJson;

    private View.OnLongClickListener mCheatSheetClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final int[] screenPos = new int[2];
            v.getLocationOnScreen(screenPos);

            final int width = v.getWidth();
            final int height = v.getHeight();
            final int screenWidth = getResources().getDisplayMetrics().widthPixels;

            final int position = (int) v.getTag();
            final String text = mColors.get(position).getName();

            Toast cheatSheet = Toast.makeText(ProductDetailActivity.this, text, Toast.LENGTH_SHORT);

            // Show under the headline
            cheatSheet.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    (screenPos[0] + width / 2) - screenWidth / 2,
                    (screenPos[1] + height)/* - mStatusBarHeightPixels*/);

            Log.d(TAG, "screenPos: " + screenPos[0] + " " + screenPos[1]);

            cheatSheet.show();
            return true;
        }
    };

    interface ProductQuery {
        int LOADER_ID = 0x1;
        String[] PROJECTION = new String[] {
                PiuDonnaContract.ProductEntry._ID,
                PiuDonnaContract.ProductEntry.COLUMN_SERVER_ID,
                PiuDonnaContract.ProductEntry.COLUMN_CODE,
                PiuDonnaContract.ProductEntry.COLUMN_NAME,
                PiuDonnaContract.ProductEntry.COLUMN_DESCRIPTION,
                PiuDonnaContract.ProductEntry.COLUMN_PRICE,
        };

        int COLUMN_SERVER_ID = 1;
        int COLUMN_CODE = 2;
        int COLUMN_NAME = 3;
        int COLUMN_DESCRIPTION = 4;
        int COLUMN_PRICE = 5;
    }

    private enum UiState {
        PROGRESS,
        EMPTY,
        DISPLAY_CONTENT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_PRODUCT_ID) && intent.hasExtra(EXTRA_PRODUCT_CODE)) {
            mProductId = intent.getExtras().getLong(EXTRA_PRODUCT_ID);
            mProductCode = intent.getExtras().getString(EXTRA_PRODUCT_CODE);
            Log.d(TAG, "mProductId: " + mProductId);
        } else {
            throw new IllegalStateException(
                    "You must provide a non-null product id and code for this activity.");
        }

        mToolbarActionBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbarActionBar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable navIcon = ContextCompat.getDrawable(this, R.drawable.ic_close);
        mToolbarActionBar.setNavigationIcon(navIcon);
        mToolbarIcons.add(navIcon);

        View rootView = findViewById(R.id.root);
        final SynchronizedScrollView scrollView = (SynchronizedScrollView)
                rootView.findViewById(R.id.scroll_view);

        mHeaderView = rootView.findViewById(R.id.header);
        mHeaderContentBox = mHeaderView.findViewById(R.id.header_content);
        mHeaderBackgroundView = mHeaderView.findViewById(R.id.header_background);
        mPhotoContainer = rootView.findViewById(R.id.image_container);
        mSliderViewPager = (ViewPager) mPhotoContainer.findViewById(R.id.image_slider_pager);
        mPlaceholder = rootView.findViewById(R.id.appbar_placeholder);
        mTitleView = (TextView) mHeaderContentBox.findViewById(R.id.product_title);
        mPriceView = (TextView) mHeaderContentBox.findViewById(R.id.product_price);
        mDescriptionView = (TextView) rootView.findViewById(R.id.description_text_view);
        mColorPickerRadioGroup = (RadioGroup) findViewById(R.id.color_radio_group);

        mContentView = rootView.findViewById(R.id.scroll_view_child);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        mEmptyViewStub = (ViewStub) rootView.findViewById(R.id.stub_empty_view);

        mAddToCartFab = (FloatingActionButton) rootView.findViewById(R.id.add_to_cart_fab);
        mAddToCartFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccountUtils.hasActiveAccount(ProductDetailActivity.this)) {
                    Intent intent = new Intent(ProductDetailActivity.this, AddToCartActivity.class);
                    intent.putExtra(AddToCartActivity.EXTRA_PRODUCT_ID, mProductId);
                    intent.putExtra(AddToCartActivity.EXTRA_PRODUCT_CODE, mProductCode);
                    intent.putExtra(AddToCartActivity.EXTRA_PRODUCT_NAME, mProductName);
                    intent.putExtra(AddToCartActivity.EXTRA_PRODUCT_PRICE, mProductPrice);
                    intent.putExtra(AddToCartActivity.EXTRA_COLOR_SIZE_DATA, mProductDetailsJson);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProductDetailActivity.this,
                            "You should sign in to add items to your cart.", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*mHeaderContentBox.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.i(TAG, "onLayoutChange: " + v.toString());
                if (mContentView.getVisibility() == View.VISIBLE) {
                    mPlaceholder.getLayoutParams().height = v.getHeight();
                    mHeaderHeightPixels = v.getHeight();
                }
            }
        });*/

        mHeaderMaxElevation = getResources().getDimensionPixelSize(R.dimen.appbar_elevation);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mActionBarHeightPixels = mToolbarActionBar.getHeight();
                mHeaderHeightPixels = mHeaderView.getHeight();
                mHeaderPaddingTop = mHeaderContentBox.getPaddingTop();

                ViewGroup.LayoutParams lp = mPlaceholder.getLayoutParams();
                if (lp.height != mHeaderHeightPixels) {
                    lp.height = mHeaderHeightPixels;
                    mPlaceholder.setLayoutParams(lp);
                }

                /*Log.d(TAG, "onGlobalLayout()" +
                        "\n > mActionBarHeightPixels: " + mActionBarHeightPixels +
                        "\n > mHeaderHeightPixels: " + mHeaderHeightPixels +
                        "\n > mHeaderPaddingTop: " + mHeaderPaddingTop +
                        "\n > mPlaceholder.getWidth(): " + mPlaceholder.getWidth() +
                        "\n > mPlaceholder.getHeight(): " + mPlaceholder.getHeight());*/

                // TODO: Comment this check.
                if (mContentView.getVisibility() == View.VISIBLE) {
                    onScrollChanged(scrollView.getScrollX(), scrollView.getScrollY(), 0, 0);
                }
            }
        });

        ViewCompat.setElevation(mHeaderBackgroundView, mHeaderMaxElevation);
        ViewCompat.setElevation(mHeaderContentBox, mHeaderMaxElevation + 0.1f);

        scrollView.addOnScrollListener(this);
        scrollView.setOverScrollEnabled(false);

        if (savedInstanceState != null) {
            mColorCheckedPosition = savedInstanceState.getInt(STATE_COLOR_CHECKED_POSITION);
        }

        initLoaders();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_COLOR_CHECKED_POSITION, mColorCheckedPosition);
    }

    private void initLoaders() {
        mDetailLoaderCallbacks = new LoaderManager.LoaderCallbacks<JsonObject>() {
            @Override
            public Loader<JsonObject> onCreateLoader(int id, Bundle args) {
                Log.d(TAG, "onCreateLoader: ProductDetails");
                setUiState(UiState.PROGRESS);
                return new ProductDetailLoader(ProductDetailActivity.this, mProductCode);
            }

            @Override
            public void onLoadFinished(Loader<JsonObject> loader, JsonObject data) {
                Log.d(TAG, "onLoadFinished: ProductDetails");
                if (data == null) {
                    setUiState(UiState.EMPTY);
                    return;
                }

                mProductDetailsJson = data.toString();
                reloadColorsData(data);
                setUiState(UiState.DISPLAY_CONTENT);
            }

            @Override
            public void onLoaderReset(Loader<JsonObject> loader) {

            }
        };

        getSupportLoaderManager().initLoader(ProductQuery.LOADER_ID, null, this);
        //getSupportLoaderManager().initLoader(ProductMultimediaQuery.LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(0x3, null, mDetailLoaderCallbacks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_product_detail, menu);

        for (int i = 0; i < menu.size(); i++) {
            mToolbarIcons.add(menu.getItem(i).getIcon());
        }

        if (!mHeaderExpanded) {
            colorizeToolbarIcons(false);
        }

        return true;
    }

    // TODO: Animate the icons' color transition.
    private void colorizeToolbarIcons(boolean isHeaderExpanded) {
        int color = ContextCompat.getColor(this,
                isHeaderExpanded ? R.color.icon_system_default : R.color.white);

        PorterDuffColorFilter filter
                = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);

        for (Drawable icon : mToolbarIcons) {
            icon.setColorFilter(filter);
        }
    }

    @Override
    public void onScrollChanged(int scrollX, int scrollY, int deltaX, int deltaY) {
        int newTop = Math.max(mPlaceholder.getTop(), scrollY + (mActionBarHeightPixels - (int) (mHeaderPaddingTop * 1.5)));
        mHeaderView.setTranslationY(newTop);

        boolean shouldExpandHeader = newTop < (scrollY + mActionBarHeightPixels);

        if (mHeaderExpanded != shouldExpandHeader) {
            float desiredScaleY = shouldExpandHeader ?
                    1f + (mActionBarHeightPixels / (float) mHeaderHeightPixels) : 1f;

            mHeaderBackgroundView.setPivotY(mHeaderHeightPixels);
            mHeaderBackgroundView.animate()
                    .scaleY(desiredScaleY)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(ANIMATION_INTERPOLATOR)
                    .start();

            colorizeToolbarIcons(shouldExpandHeader);
        }
        mHeaderExpanded = shouldExpandHeader;

        mPhotoContainer.setTranslationY(scrollY / 2);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == ProductQuery.LOADER_ID) {
            return new CursorLoader(this, PiuDonnaContract.ProductEntry.buildProductUri(mProductId),
                    ProductQuery.PROJECTION, null, null, null);
        } /*else if (id == ProductMultimediaQuery.LOADER_ID) {
            return new CursorLoader(this,
                    ProductMultimediaQuery.URI,
                    ProductMultimediaQuery.PROJECTION,
                    ProductMultimediaQuery.SELECTION,
                    new String[] {Long.toString(mProductId)},
                    ProductMultimediaQuery.SORT_ORDER);
        }*/

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == ProductQuery.LOADER_ID) {
            data.moveToFirst();
            mProductName = data.getString(ProductQuery.COLUMN_NAME);
            mProductPrice = data.getFloat(ProductQuery.COLUMN_PRICE);

            mTitleView.setText(mProductName);
            mPriceView.setText(UiUtils.formatPrice(mProductPrice));
            mDescriptionView.setText(Html.fromHtml(data.getString(ProductQuery.COLUMN_DESCRIPTION)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private void reloadColorsData(JsonObject data) {
        Gson gson = new Gson();

        JsonArray colorsArray  = data.getAsJsonArray("colors");
        Type colorsListType = new TypeToken<List<ProductColor>>(){}.getType();
        mColors = gson.fromJson(colorsArray, colorsListType);

        populateColorPicker(mColors);

        if (mSliderPagerAdapter == null) {
            mSliderPagerAdapter = new SliderPagerAdapter(mColors.get(mColorCheckedPosition).getImageUrls());
            mSliderViewPager.setAdapter(mSliderPagerAdapter);
        } else {
            mSliderPagerAdapter.setImages(mColors.get(mColorCheckedPosition).getImageUrls());
        }
    }

    private void populateColorPicker(final List<ProductColor> colors) {
        for (int i = 0; i < colors.size(); i++) {
            ProductColor color = colors.get(i);
            RadioButton button = UiUtils.makeColorCompoundButton(this, Color.parseColor(color.getHexa()), i, mCheatSheetClickListener);
            mColorPickerRadioGroup.addView(button);

            // Once added to a RadioGroup, the RadioButton has an assigned ViewID, so we can
            // retrieve it and set it as checked using that ViewID.
            if (i == mColorCheckedPosition) {
                mColorPickerRadioGroup.check(button.getId());
            }
        }

        mColorPickerRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton button = (RadioButton) group.findViewById(checkedId);
                if (button != null) {
                    // Reload slider images according to the selected color.
                    mColorCheckedPosition = (int) button.getTag();
                    mSliderPagerAdapter.setImages(colors.get(mColorCheckedPosition).getImageUrls());
                }
            }
        });
    }

    private void setUiState(UiState state) {
        Log.d(TAG, "setUiState: " + state.toString());

        switch (state) {
            case PROGRESS:
                mProgressBar.setVisibility(View.VISIBLE);
                setContentViewVisibility(View.GONE);
                setEmptyViewVisibility(View.GONE);
                break;
            case EMPTY:
                mProgressBar.setVisibility(View.GONE);
                setContentViewVisibility(View.GONE);
                setEmptyViewVisibility(View.VISIBLE);
                break;
            case DISPLAY_CONTENT:
                mProgressBar.setVisibility(View.GONE);
                setContentViewVisibility(View.VISIBLE);
                setEmptyViewVisibility(View.GONE);
                break;
        }
    }

    private void setContentViewVisibility(@Visibility int visibility) {
        mToolbarActionBar.setVisibility(visibility);
        mContentView.setVisibility(visibility);
        mAddToCartFab.setVisibility(visibility);

        // Animate the FAB only if the new visibility is View.VISIBLE, otherwise set its scale
        // values to zero for a possible further animation when visible.
        if (visibility == View.VISIBLE) {
            mAddToCartFab.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(ANIMATION_DURATION)
                    .start();
        } else {
            mAddToCartFab.setScaleX(0f);
            mAddToCartFab.setScaleY(0f);
        }
    }

    private void setEmptyViewVisibility(@Visibility int visibility) {
        if (mEmptyView == null && visibility == View.VISIBLE) {
            mEmptyView = (EmptyView) mEmptyViewStub.inflate();
            mEmptyView.setTitle("No connection");
            mEmptyView.setSubtitle("Check your network and try again");
            mEmptyView.setAction("Try again", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSupportLoaderManager().restartLoader(0x3, null, mDetailLoaderCallbacks);
                }
            });
        }

        if (mEmptyView != null) {
            mEmptyView.setVisibility(visibility);
        }
    }

    private class SliderPagerAdapter extends PagerAdapter {

        private final ViewGroup.LayoutParams IMAGE_LAYOUT_PARAMS = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        List<String> mImages;

        public SliderPagerAdapter(List<String> images) {
            this.mImages = images;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return mImages != null ? mImages.size() : 0;
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}.
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(ProductDetailActivity.this);
            imageView.setLayoutParams(IMAGE_LAYOUT_PARAMS);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            container.addView(imageView);

            Picasso.with(ProductDetailActivity.this)
                    .load(Config.buildProductImageUrl(mImages.get(position)))
                    .placeholder(R.drawable.image_placeholder)
                    .into(imageView);

            Log.i(TAG, "instantiateItem() [position: " + position + "]");

            return imageView;
        }

        /**
         * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
         * {@link View}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            Log.i(TAG, "destroyItem() [position: " + position + "]");
        }

        /**
         * This is a hack to force the PagerAdapter to re-instantiate the views whenever
         * notifyDataSetChanged() is called.
         */
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void setImages(List<String> images) {
            this.mImages = images;
            notifyDataSetChanged();


            // Perform a subtle 'reload' animation by fading in the SliderViewPager.
            mSliderViewPager.setCurrentItem(0, false);
            mSliderViewPager.setAlpha(0);
            mSliderViewPager.animate()
                    .alpha(1)
                    .setDuration(ANIMATION_DURATION)
                    .start();
        }

    }

}
