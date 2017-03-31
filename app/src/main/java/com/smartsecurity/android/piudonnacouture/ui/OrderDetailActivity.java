package com.smartsecurity.android.piudonnacouture.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.loader.OrderDetailLoader;
import com.smartsecurity.android.piudonnacouture.ui.widget.EmptyView;
import com.smartsecurity.android.piudonnacouture.util.DateUtils;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Locale;

public class OrderDetailActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<JsonObject> {
    private static final String TAG = "OrderDetailsActivity";

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    public static Intent getStartIntent(Context context, long orderId) {
        Intent startIntent = new Intent(context, OrderDetailActivity.class);
        startIntent.putExtra(EXTRA_ORDER_ID, orderId);

        return startIntent;
    }

    private enum UiState {
        LOADING,
        DISPLAY_CONTENT,
        ERROR
    }

    private AppBarLayout mAppBar;
    private TextView mOrderNumberView;
    private TextView mOrderDateView;

    private ScrollView mScrollView;
    private LinearLayout mScrollViewChild;
    private ProgressBar mProgressBar;

    private ViewStub mEmptyViewStub;
    private EmptyView mEmptyView;

    private TextView mFullNameView;
    private TextView mAddressLinesView;
    private TextView mCityStateZipCodeView;
    private TextView mCountryView;
    private TextView mPhoneNumberView;

    private LinearLayout mOrderDetailsContainer;

    private TextView mSummaryLabelsView;
    private TextView mSummaryValuesView;
    private TextView mOrderTotalView;

    private Handler mHandler = new Handler();

    private long mOrderId;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        mAppBar = (AppBarLayout) findViewById(R.id.appbar);

        Toolbar toolbar = (Toolbar) mAppBar.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Tint navigation icon with the app's accent color.
        colorizeNavigationIcon(toolbar);

        mOrderNumberView = (TextView) toolbar.findViewById(R.id.order_number_view);
        mOrderDateView = (TextView) toolbar.findViewById(R.id.order_date_status_view);

        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mScrollViewChild = (LinearLayout) mScrollView.findViewById(R.id.scroll_view_child);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mEmptyViewStub = (ViewStub) findViewById(R.id.stub_empty_view);

        View shippingInfoSection = mScrollView.findViewById(R.id.shipping_info_section);
        mFullNameView = (TextView) shippingInfoSection.findViewById(R.id.address_full_name);
        mAddressLinesView = (TextView) shippingInfoSection.findViewById(R.id.address_lines);
        mCityStateZipCodeView = (TextView) shippingInfoSection.findViewById(R.id.address_city_state_zip);
        mCountryView = (TextView) shippingInfoSection.findViewById(R.id.address_country);
        mPhoneNumberView = (TextView) shippingInfoSection.findViewById(R.id.address_phone);

        mOrderDetailsContainer = (LinearLayout) mScrollView.findViewById(R.id.order_details_container);

        View orderSummarySection = mScrollView.findViewById(R.id.order_summary_section);
        mSummaryLabelsView = (TextView) orderSummarySection.findViewById(R.id.summary_labels);
        mSummaryValuesView = (TextView) orderSummarySection.findViewById(R.id.summary_values);
        mOrderTotalView = (TextView) orderSummarySection.findViewById(R.id.summary_order_total);

        // Get the incoming extras in the intent.
        mOrderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_order_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<JsonObject> onCreateLoader(int id, Bundle args) {
        setUiState(UiState.LOADING, true);
        return new OrderDetailLoader(this, mOrderId);
    }

    @Override
    public void onLoadFinished(Loader<JsonObject> loader, JsonObject data) {
        if (data != null) {
            processOrderDetailsData(data);
        }

        setUiState(data == null ? UiState.ERROR : UiState.DISPLAY_CONTENT, true);
    }

    @Override
    public void onLoaderReset(Loader<JsonObject> loader) {

    }

    private void reloadOrderDetailsData() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    private void processOrderDetailsData(JsonObject data) {
        JsonObject summary = data.get("summary").getAsJsonObject();
        JsonArray items = data.get("items").getAsJsonArray();

        mOrderNumberView.setText(getString(R.string.order_number,
                summary.get("reference").getAsString()));
        mOrderDateView.setText(getString(R.string.order_date,
                DateUtils.getOrderDetailDateFromMySqlDate(summary.get("date").getAsJsonObject())));

        setShippingInformation(summary);
        fillOrderDetails(items);
        fillOrderSummary(summary, items.size());
    }

    private void setShippingInformation(JsonObject summary) {
        mFullNameView.setText(summary.get("fullName").getAsString());

        String line1 = summary.get("lineOne").getAsString();
        String line2 = summary.get("lineTwo").getAsString();
        mAddressLinesView.setText(String.format("%s%s", line1, (line2 == null ? "" : (" " + line2))));

        mCityStateZipCodeView.setText(String.format("%s %s %s",
                summary.get("city").getAsString(),
                summary.get("state").getAsString(),
                summary.get("zip").getAsString()));

        mCountryView.setText(summary.get("country").getAsString());

        // TODO: Give format to the phone number.
        mPhoneNumberView.setText(getString(R.string.text_phone, summary.get("phoneNumber").getAsString()));
    }

    private void fillOrderDetails(JsonArray items) {
        mOrderDetailsContainer.removeAllViews();

        for (JsonElement itemElement : items) {
            JsonObject item = itemElement.getAsJsonObject();

            View itemView = getLayoutInflater().inflate(
                    R.layout.list_item_shopping_cart_review, mOrderDetailsContainer, false);

            ((TextView) itemView.findViewById(R.id.product_title)).setText(
                    item.get("nameEn").getAsString());

            ((TextView) itemView.findViewById(R.id.product_specs)).setText(
                    getString(R.string.cart_product_details,
                            WordUtils.capitalizeFully(item.get("color").getAsString()),
                            item.get("size").getAsString(),
                            item.get("quantity").getAsInt()));

            ((TextView) itemView.findViewById(R.id.product_price)).setText(
                    UiUtils.formatPrice(item.get("price").getAsFloat()));

            mOrderDetailsContainer.addView(itemView);
        }
    }

    private void fillOrderSummary(JsonObject summary, int itemCount) {
        float subtotal = summary.get("subtotal").getAsFloat();
        float shipping = summary.get("shippingPrice").getAsFloat();
        float tax = summary.get("tax").getAsFloat();

        mSummaryLabelsView.setText(getString(R.string.order_summary_labels, itemCount));
        mSummaryValuesView.setText(String.format(Locale.US, "$%.2f\n$%.2f\n$%.2f\n$%.2f",
                subtotal, shipping, subtotal + shipping, tax));
        mOrderTotalView.setText(UiUtils.formatPrice(subtotal + shipping + tax));
    }

    private void setUiState(UiState state, boolean animate) {
        switch (state) {
            case LOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                setContentVisibility(View.GONE, animate);
                setEmptyViewVisibility(View.GONE);
                break;
            case DISPLAY_CONTENT:
                mProgressBar.setVisibility(View.GONE);
                setContentVisibility(View.VISIBLE, animate);
                setEmptyViewVisibility(View.GONE);
                break;
            case ERROR:
                mProgressBar.setVisibility(View.GONE);
                setContentVisibility(View.GONE, animate);
                setEmptyViewVisibility(View.VISIBLE);
                break;
        }
    }

    private void setContentVisibility(int visibility, boolean animate) {
        mHandler.post(() -> mAppBar.setVisibility(visibility));
        mScrollView.setVisibility(visibility);

        if (visibility == View.VISIBLE && animate) {
            float density = getResources().getDisplayMetrics().density;

            for (int i = 0; i < mScrollViewChild.getChildCount(); i++) {
                View child = mScrollViewChild.getChildAt(i);
                child.setTranslationY(50 * density + .5f);
                child.setAlpha(0);
            }

            TimeInterpolator interpolator = new DecelerateInterpolator(2.0f);

            for (int i = 0; i < mScrollViewChild.getChildCount(); i++) {
                final View child = mScrollViewChild.getChildAt(i);
                child.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                child.animate()
                        .translationY(0)
                        .alpha(1)
                        .setDuration(333L)
                        .setInterpolator(interpolator)
                        .setStartDelay(i * 100)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                child.setLayerType(View.LAYER_TYPE_NONE, null);
                            }
                        })
                        .start();
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && getDropShadowView() != null) {
            getDropShadowView().setVisibility(visibility);
        }
    }

    private void setEmptyViewVisibility(int visibility) {
        if (mEmptyView == null && visibility == View.VISIBLE) {
            mEmptyView = (EmptyView) mEmptyViewStub.inflate();
            mEmptyView.setTitle("No connection");
            mEmptyView.setSubtitle("Check your network and try again");
            mEmptyView.setAction("Try again", v -> reloadOrderDetailsData());
        }

        if (mEmptyView != null) {
            mEmptyView.setVisibility(visibility);
        }
    }
}
