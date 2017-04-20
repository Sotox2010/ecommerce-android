package com.smartsecurity.android.piudonnacouture.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.loader.ProductStockLoader;
import com.smartsecurity.android.piudonnacouture.ui.widget.NumberSpinner;
import com.smartsecurity.android.piudonnacouture.ui.worker.ShoppingCartWorkerFragment;
import com.smartsecurity.android.piudonnacouture.util.AccountUtils;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;

import org.apache.commons.lang3.text.WordUtils;

public class UpdateCartItemActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<JsonObject>, ShoppingCartWorkerFragment.OnUpdateCartItemCallback, NumberSpinner.OnValueChangeListener {
    private static final String TAG = "UpdateCartItemActivity";

    public static final String EXTRA_PRODUCT_NAME = "extra_product_name";
    public static final String EXTRA_PRODUCT_PRICE = "extra_product_price";
    public static final String EXTRA_PRODUCT_CODE = "extra_product_code";
    public static final String EXTRA_CART_ITEM_SERVER_ID= "extra_cart_item_server_id";
    public static final String EXTRA_COLOR_SERVER_ID = "extra_color_server_id";
    public static final String EXTRA_COLOR = "extra_color";
    public static final String EXTRA_SIZE_SERVER_ID = "extra_size_server_id";
    public static final String EXTRA_SIZE = "extra_size";

    private static final String TAG_FRAGMENT_WORKER = "fragment_shopping_cart_worker";

    public static Intent getStartIntent(Context context, String productName, String productCode,
                                        float productPrice, long cartItemServerId,
                                        long colorServerId, String color, long sizeServerId,
                                        String size) {

        Intent startIntent = new Intent(context, UpdateCartItemActivity.class);
        startIntent.putExtra(EXTRA_PRODUCT_NAME, productName);
        startIntent.putExtra(EXTRA_PRODUCT_CODE, productCode);
        startIntent.putExtra(EXTRA_PRODUCT_PRICE, productPrice);
        startIntent.putExtra(EXTRA_CART_ITEM_SERVER_ID, cartItemServerId);
        startIntent.putExtra(EXTRA_COLOR_SERVER_ID, colorServerId);
        startIntent.putExtra(EXTRA_COLOR, color);
        startIntent.putExtra(EXTRA_SIZE_SERVER_ID, sizeServerId);
        startIntent.putExtra(EXTRA_SIZE, size);

        return startIntent;
    }

    private View mUpdateQuantityButton;
    private TextView mStockAvailabilityView;
    private ProgressBar mButtonProgress;
    private NumberSpinner mQuantitySpinner;
    private ShoppingCartWorkerFragment mWorkerFragment;

    private String mProductCode;
    private long mCartItemServerId;
    private long mColorServerId;
    private long mSizeServerId;
    private int mMaxQuantity;

    private View.OnClickListener mRetryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getSupportLoaderManager().restartLoader(0, null, UpdateCartItemActivity.this);
        }
    };

    private enum UiState {
        LOADING_STOCK,
        STOCK_LOADED,
        UPDATING_CART,
        ERROR_LOADING_STOCK,
        ERROR_UPDATING_CART
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setupFloatingWindow();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_cart_item);

        processIncomingIntentData(getIntent());

        mQuantitySpinner = (NumberSpinner) findViewById(R.id.quantity_spinner);
        mQuantitySpinner.setOnValueChangedListener(this);

        mStockAvailabilityView = (TextView) findViewById(R.id.stock_availability_view);

        mUpdateQuantityButton = findViewById(R.id.add_to_cart_button);
        mUpdateQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCartItem();
            }
        });
        mButtonProgress = (ProgressBar) mUpdateQuantityButton.findViewById(R.id.button_progress);

        mWorkerFragment = (ShoppingCartWorkerFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_FRAGMENT_WORKER);

        if (mWorkerFragment == null) {
            mWorkerFragment = ShoppingCartWorkerFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(mWorkerFragment, TAG_FRAGMENT_WORKER)
                    .commit();
        }

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void setupFloatingWindow() {
        int horizontalMargin = getResources().getDimensionPixelSize(R.dimen.spacing_xlarge);
        int width = getResources().getDisplayMetrics().widthPixels - (horizontalMargin * 2);

        setupFloatingWindow(width, WindowManager.LayoutParams.WRAP_CONTENT, 1, 0.5f);
    }

    private void processIncomingIntentData(Intent intent) {
        if (intent.getExtras() == null) {
            throw new IllegalStateException("Required extras no provided for this activity.");
        }

        mProductCode = intent.getStringExtra(EXTRA_PRODUCT_CODE);
        mCartItemServerId = intent.getLongExtra(EXTRA_CART_ITEM_SERVER_ID, -1);
        mColorServerId = intent.getLongExtra(EXTRA_COLOR_SERVER_ID, -1);
        mSizeServerId = intent.getLongExtra(EXTRA_SIZE_SERVER_ID, -1);

        String productName = intent.getStringExtra(EXTRA_PRODUCT_NAME);
        float price = intent.getFloatExtra(EXTRA_PRODUCT_PRICE, 0f);
        String color = intent.getStringExtra(EXTRA_COLOR);
        String size = intent.getStringExtra(EXTRA_SIZE);

        TextView productNameView = (TextView) findViewById(R.id.product_title);
        productNameView.setText(productName);

        TextView productPriceView = (TextView) findViewById(R.id.product_price);
        productPriceView.setText(UiUtils.formatPrice(price));

        // TODO: Improve this by using spans instead of two text views.
        TextView detailsView = (TextView) findViewById(R.id.details);
        detailsView.setText(String.format("%s\n%s", WordUtils.capitalizeFully(color), size));
    }

    @Override
    public Loader<JsonObject> onCreateLoader(int id, Bundle args) {
        setUiState(UiState.LOADING_STOCK);
        return new ProductStockLoader(this, mProductCode, mColorServerId, mSizeServerId);
    }

    @Override
    public void onLoadFinished(Loader<JsonObject> loader, JsonObject data) {
        if (data != null) {
            mMaxQuantity = data.get("quantity").getAsInt();
            mStockAvailabilityView.setTextColor(ContextCompat.getColor(this, mMaxQuantity > 0
                    ? R.color.text_disable
                    : android.R.color.holo_red_light));

            setUiState(UiState.STOCK_LOADED);

            // Call this here so the OnValueChangeListener will trigger after update the UI and the
            // "update" button will change state properly.
            mQuantitySpinner.setMaxValue(mMaxQuantity);
            mQuantitySpinner.setCurrentValue(1);
            updateStockView();
        } else {
            mStockAvailabilityView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            setUiState(UiState.ERROR_LOADING_STOCK);
        }
    }

    @Override
    public void onLoaderReset(Loader<JsonObject> loader) {

    }

    private void updateCartItem() {
        setUiState(UiState.UPDATING_CART);

        mWorkerFragment.updateCartItem(
                AccountUtils.getActiveAccountName(UpdateCartItemActivity.this),
                mProductCode,
                mCartItemServerId,
                mQuantitySpinner.getCurrentValue());
    }

    @Override
    public void onUpdateCartItemResult(String result) {
        // A 'null' or empty result means 'success' for us.
        if (TextUtils.isEmpty(result)) {
            Toast.makeText(this, "Item updated successfully.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            setUiState(UiState.ERROR_UPDATING_CART);
        }
    }

    @Override
    public void onValueChanged(int newValue) {
        setUpdateQuantityButtonEnabledNoProgress(newValue != 0);
    }

    private void setUiState(UiState state) {
        Log.d(TAG, "setUiState: " + state.toString());

        switch (state) {
            case LOADING_STOCK:
                mQuantitySpinner.setEnabled(false);
                mQuantitySpinner.showProgress();
                setUpdateQuantityButtonEnabledNoProgress(false);
                setStockViewVisibility(View.INVISIBLE);
                break;
            case STOCK_LOADED:
                mQuantitySpinner.setEnabled(true);
                mQuantitySpinner.hideProgress();
                setUpdateQuantityButtonEnabledNoProgress(true);
                setStockViewVisibility(View.VISIBLE);
                break;
            case UPDATING_CART:
                setBackEnabled(false);
                setFinishOnTouchOutside(false);
                mQuantitySpinner.setEnabled(false);
                mQuantitySpinner.hideProgress();
                setUpdateQuantityButtonEnabledWithProgress(false);
                setStockViewVisibility(View.VISIBLE);
                break;
            case ERROR_LOADING_STOCK:
                mQuantitySpinner.setEnabled(false);
                mQuantitySpinner.hideProgress();
                setUpdateQuantityButtonEnabledNoProgress(false);
                setStockViewVisibility(View.VISIBLE, true);
                break;
            case ERROR_UPDATING_CART:
                setBackEnabled(true);
                setFinishOnTouchOutside(true);
                mQuantitySpinner.setEnabled(true);
                mQuantitySpinner.hideProgress();
                setUpdateQuantityButtonEnabledNoProgress(true);
                setStockViewVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Update stock view according to the retrieved stock quantity.
     */
    private void updateStockView() {

        mStockAvailabilityView.setText(mMaxQuantity > 0
                ? getString(R.string.stock_availability, mMaxQuantity)
                : "Out of stock, consider remove it from cart.");
    }

    private void setStockViewVisibility(int visibility) {
        setStockViewVisibility(visibility, false);
    }

    private void setStockViewVisibility(int visibility, boolean error) {
        mStockAvailabilityView.setVisibility(visibility);
        mStockAvailabilityView.setOnClickListener(error ? mRetryClickListener : null);

        if (error) {
            mStockAvailabilityView.setText("Error loading stock. Tap to retry.");
        }
    }

    private void setUpdateQuantityButtonEnabledNoProgress(boolean enabled) {
        setUpdateQuantityButtonEnabled(enabled, false);
    }

    private void setUpdateQuantityButtonEnabledWithProgress(boolean enabled) {
        setUpdateQuantityButtonEnabled(enabled, true);
    }

    private void setUpdateQuantityButtonEnabled(boolean enabled, boolean showProgress) {
        mUpdateQuantityButton.setEnabled(enabled);
        mButtonProgress.setVisibility(!enabled && showProgress ? View.VISIBLE : View.GONE);
    }
}
