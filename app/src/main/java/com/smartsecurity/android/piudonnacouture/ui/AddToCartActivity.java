package com.smartsecurity.android.piudonnacouture.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.loader.ProductStockLoader;
import com.smartsecurity.android.piudonnacouture.model.ProductColor;
import com.smartsecurity.android.piudonnacouture.model.ProductSize;
import com.smartsecurity.android.piudonnacouture.ui.worker.ShoppingCartWorkerFragment;
import com.smartsecurity.android.piudonnacouture.util.AccountUtils;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;

import java.lang.reflect.Type;
import java.util.List;

public class AddToCartActivity extends BaseActivity implements ShoppingCartWorkerFragment.OnAddToCartCallback, LoaderManager.LoaderCallbacks<JsonObject> {
    private static final String TAG = "AddToCartActivity";

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_PRODUCT_CODE = "extra_product_code";
    public static final String EXTRA_PRODUCT_NAME = "extra_product_name";
    public static final String EXTRA_PRODUCT_PRICE = "extra_product_price";
    public static final String EXTRA_COLOR_SIZE_DATA= "extra_color_size_data";
    public static final String EXTRA_COLOR_SERVER_ID= "extra_color_server_id";
    public static final String EXTRA_SIZE_SERVER_ID= "extra_size_server_id";

    private static final String STATE_SIZE_CHECKED_POSITION = "state_size_checked_position";
    private static final String STATE_COLOR_CHECKED_POSITION = "state_color_checked_position";

    private static final String TAG_FRAGMENT_WORKER = "fragment_shopping_cart_worker";

    private RadioGroup mSizePickerRadioGroup;
    private RadioGroup mColorPickerRadioGroup;
    private ImageView mSubtractButton;
    private ImageView mAddButton;
    private View mAddToCartButton;
    private TextView mQuantityView;
    private TextView mStockAvailabilityView;
    private ProgressBar mQuantityProgress;
    private ProgressBar mButtonProgress;
    private ShoppingCartWorkerFragment mWorkerFragment;

    private List<ProductColor> mColors;
    private List<ProductSize> mSizes;

    private Long mProductId;
    private String mProductCode;

    private int mSizeCheckedPosition = -1;
    private int mColorCheckedPosition = -1;
    private int mMaxQuantity = 1;

    private enum UiState {
        AWAITING_COLOR_SIZE_SELECTION,
        LOADING_STOCK,
        STOCK_LOADED,
        ADDING_TO_CART,
        ERROR_LOADING_STOCK,
        ERROR_ADDING_TO_CART
    }

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

            Toast cheatSheet = Toast.makeText(AddToCartActivity.this, text, Toast.LENGTH_SHORT);

            cheatSheet.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    (screenPos[0] + width / 2) - screenWidth / 2,
                    (screenPos[1] + height)/* - mStatusBarHeightPixels*/);

            Log.d(TAG, "screenPos: " + screenPos[0] + " " + screenPos[1]);

            cheatSheet.show();
            return true;
        }
    };

    /*@Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        //if (getResources().getBoolean(R.bool.is_tablet) && mOpenAsSmallWindow) {
            final View view = getWindow().getDecorView();
            final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();

            lp.gravity = Gravity.CENTER;

            lp.width = mActivityWindowWidth;
            lp.height = mActivityWindowHeight;
            getWindowManager().updateViewLayout(view, lp);
        //}
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setupFloatingWindow();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_cart);

        processIncomingIntentData(getIntent());

        mColorPickerRadioGroup = (RadioGroup) findViewById(R.id.color_radio_group);
        mSizePickerRadioGroup = (RadioGroup) findViewById(R.id.size_radio_group);
        mQuantityView = (TextView) findViewById(R.id.quantity_text_view);
        mStockAvailabilityView = (TextView) findViewById(R.id.stock_availability_view);
        mQuantityProgress = (ProgressBar) findViewById(R.id.stock_progress);

        mSubtractButton = (ImageView) findViewById(R.id.subtract_button);
        mSubtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subtractQuantity();
            }
        });

        mAddButton = (ImageView) findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuantity();
            }
        });

        mAddToCartButton = findViewById(R.id.add_to_cart_button);
        mAddToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductColor selectedColor = mColors.get(mColorCheckedPosition);
                ProductSize selectedSize = mSizes.get(mSizeCheckedPosition);

                mWorkerFragment.addToCart(
                        AccountUtils.getActiveAccountName(AddToCartActivity.this),
                        mProductId,
                        mProductCode,
                        selectedColor.getServerId(),
                        selectedColor.getName(),
                        selectedSize.getServerId(),
                        selectedSize.getName(),
                        Integer.parseInt(mQuantityView.getText().toString()));

                setUiState(UiState.ADDING_TO_CART);
            }
        });

        mButtonProgress = (ProgressBar) mAddToCartButton.findViewById(R.id.button_progress);

        mWorkerFragment = (ShoppingCartWorkerFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_FRAGMENT_WORKER);

        if (mWorkerFragment == null) {
            mWorkerFragment = ShoppingCartWorkerFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(mWorkerFragment, TAG_FRAGMENT_WORKER)
                    .commit();
        }

        if (savedInstanceState != null) {
            mSizeCheckedPosition = savedInstanceState.getInt(STATE_SIZE_CHECKED_POSITION);
            mColorCheckedPosition = savedInstanceState.getInt(STATE_COLOR_CHECKED_POSITION);
        }

        setupPickers();
        setUiState(UiState.AWAITING_COLOR_SIZE_SELECTION);
    }

    private void setupFloatingWindow() {
        int horizontalMargin = getResources().getDimensionPixelSize(R.dimen.add_to_cart_activity_horizontal_margin);
        int verticalMargin = getResources().getDimensionPixelSize(R.dimen.add_to_cart_activity_vertical_margin);

        int width = getResources().getDisplayMetrics().widthPixels - (horizontalMargin * 2);
        int height = getResources().getDisplayMetrics().heightPixels - (verticalMargin * 2);

        setupFloatingWindow(width, height, 1, 0.5f);
    }

    private void processIncomingIntentData(Intent intent) {
        if (!intent.hasExtra(EXTRA_PRODUCT_ID)
                || !intent.hasExtra(EXTRA_PRODUCT_CODE)
                || !intent.hasExtra(EXTRA_PRODUCT_NAME)
                || !intent.hasExtra(EXTRA_PRODUCT_PRICE)
                || !intent.hasExtra(EXTRA_COLOR_SIZE_DATA)) {
            throw new IllegalStateException("A name, price and color/size data should be provided" +
                    " as extras to this activity.");
        }

        mProductId = intent.getLongExtra(EXTRA_PRODUCT_ID, 0L);
        mProductCode = intent.getStringExtra(EXTRA_PRODUCT_CODE);

        TextView productNameView = (TextView) findViewById(R.id.product_title);
        productNameView.setText(intent.getStringExtra(EXTRA_PRODUCT_NAME));

        TextView productPriceView = (TextView) findViewById(R.id.product_price);
        productPriceView.setText(UiUtils.formatPrice(
                intent.getFloatExtra(EXTRA_PRODUCT_PRICE, 0f)));

        Gson gson = new Gson();
        JsonElement parsed = new JsonParser().parse(intent.getStringExtra(EXTRA_COLOR_SIZE_DATA));
        JsonObject data = parsed.getAsJsonObject();

        JsonArray colorsArray  = data.getAsJsonArray("colors");
        Type colorsListType = new TypeToken<List<ProductColor>>(){}.getType();
        mColors = gson.fromJson(colorsArray, colorsListType);

        JsonArray sizesArray  = data.getAsJsonArray("sizes");
        Type sizeListType = new TypeToken<List<ProductSize>>(){}.getType();
        mSizes = gson.fromJson(sizesArray, sizeListType);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SIZE_CHECKED_POSITION, mSizeCheckedPosition);
        outState.putInt(STATE_COLOR_CHECKED_POSITION, mColorCheckedPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setupPickers() {

        // Populate color picker.
        for (int i = 0; i < mColors.size(); i++) {
            ProductColor color = mColors.get(i);
            RadioButton button = UiUtils.makeColorCompoundButton(
                    this, Color.parseColor(color.getHexa()), i, mCheatSheetClickListener);
            mColorPickerRadioGroup.addView(button);

            // Once added to a RadioGroup, the RadioButton has an assigned ViewID, so we can
            // retrieve it and set it as checked using that ViewID.
            if (i == mColorCheckedPosition) {
                mColorPickerRadioGroup.check(button.getId());
            }
        }

        // Populate size picker.
        for (int i = 0; i < mSizes.size(); i++) {
            RadioButton button = makeSizeCompoundButton(mSizes.get(i).getName(), i);
            mSizePickerRadioGroup.addView(button);

            // Once added to a RadioGroup, the RadioButton has an assigned ViewID, so we can
            // retrieve it and set it as checked using that ViewID.
            if (i == mSizeCheckedPosition) {
                mSizePickerRadioGroup.check(button.getId());
            }
        }

        RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "onCheckedChanged: " + checkedId);
                RadioButton button = (RadioButton) group.findViewById(checkedId);
                if (group == mColorPickerRadioGroup) {
                    mColorCheckedPosition = (int) button.getTag();
                    Log.d(TAG, "mColorPickerRadioGroup.onCheckedChanged > button.tag: " + button.getTag());
                } else if (group == mSizePickerRadioGroup) {
                    mSizeCheckedPosition = (int) button.getTag();
                    Log.d(TAG, "mSizePickerRadioGroup.onCheckedChanged > button.tag: " + button.getTag());
                }

                if (mColorPickerRadioGroup.getCheckedRadioButtonId() != View.NO_ID &&
                        mSizePickerRadioGroup.getCheckedRadioButtonId() != View.NO_ID) {
                    getSupportLoaderManager().restartLoader(0, null, AddToCartActivity.this);
                }
            }
        };

        mSizePickerRadioGroup.setOnCheckedChangeListener(listener);
        mColorPickerRadioGroup.setOnCheckedChangeListener(listener);
    }

    @Override
    public Loader<JsonObject> onCreateLoader(int id, Bundle args) {
        setUiState(UiState.LOADING_STOCK);
        return new ProductStockLoader(this, mProductCode,
                mColors.get(mColorCheckedPosition).getServerId(),
                mSizes.get(mSizeCheckedPosition).getServerId());
    }

    @Override
    public void onLoadFinished(Loader<JsonObject> loader, JsonObject data) {
        if (data != null) {
            mMaxQuantity = data.get("quantity").getAsInt();
            mStockAvailabilityView.setTextColor(ContextCompat.getColor(this,
                    mMaxQuantity > 0 ? R.color.text_disable : android.R.color.holo_red_light));

            int newValue = Math.min(mMaxQuantity, 1);
            mQuantityView.setText(Integer.toString(newValue));
            setUiState(UiState.STOCK_LOADED);
            onQuantityValueChanged(newValue);
        } else {
            setUiState(UiState.ERROR_LOADING_STOCK);
        }
    }

    @Override
    public void onLoaderReset(Loader<JsonObject> loader) {

    }

    private RadioButton makeSizeCompoundButton(CharSequence text, Object tag) {
        // TODO: Change the button size to dimen resource.
        int size = (int) (40 * getResources().getDisplayMetrics().density);
        Drawable background = ContextCompat.getDrawable(this, R.drawable.button_size);
        ColorStateList textColors = ContextCompat.getColorStateList(this, R.color.text_button_size);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, size);

        RadioButton button = new RadioButton(this);
        button.setLayoutParams(params);
        button.setMinimumWidth(size);
        button.setGravity(Gravity.CENTER);
        button.setButtonDrawable(null);
        button.setTextColor(textColors);
        button.setText(text);
        button.setTag(tag);
        UiUtils.setBackgroundDrawable(button, background);

        return button;
    }

    @Override
    public void onAddToCartResult(String result) {
        // A 'null' or empty result means 'success' for us.
        if (TextUtils.isEmpty(result)) {
            Toast.makeText(this, "Successfully added to cart.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            setUiState(UiState.STOCK_LOADED);
        }
    }

    private void addQuantity() {
        int currentQuantity = Integer.parseInt(mQuantityView.getText().toString());
        int newValue = (int) UiUtils.clamp(currentQuantity + 1, 0, mMaxQuantity);
        mQuantityView.setText(Integer.toString(newValue));
        onQuantityValueChanged(newValue);
    }

    private void subtractQuantity() {
        int currentQuantity = Integer.parseInt(mQuantityView.getText().toString());
        int newValue = (int) UiUtils.clamp(currentQuantity - 1, 0, mMaxQuantity);
        mQuantityView.setText(Integer.toString(newValue));
        onQuantityValueChanged(newValue);
    }

    private void onQuantityValueChanged(int newValue) {
        setAddToCartButtonEnabledNoProgress(newValue != 0);
    }

    private void setUiState(UiState state) {
        Log.d(TAG, "setUiState: " + state.toString());

        switch (state) {
            case AWAITING_COLOR_SIZE_SELECTION:
            case ERROR_LOADING_STOCK:
                setBackEnabled(true);
                setPickersEnabled(true);
                setQuantityPickerEnabled(false, false);
                mStockAvailabilityView.setVisibility(View.INVISIBLE);
                setAddToCartButtonEnabledNoProgress(false);
                break;
            case LOADING_STOCK:
                setBackEnabled(true);
                setPickersEnabled(false);
                setQuantityPickerEnabled(false, true);
                mStockAvailabilityView.setVisibility(View.INVISIBLE);
                setAddToCartButtonEnabledNoProgress(false);
                break;
            case STOCK_LOADED:
            case ERROR_ADDING_TO_CART:
                setBackEnabled(true);
                setFinishOnTouchOutside(true);
                setPickersEnabled(true);
                setQuantityPickerEnabled(true, false);
                mStockAvailabilityView.setVisibility(View.VISIBLE);
                setAddToCartButtonEnabledNoProgress(true);
                break;
            case ADDING_TO_CART:
                setBackEnabled(false);
                setFinishOnTouchOutside(false);
                setPickersEnabled(false);
                setQuantityPickerEnabled(false, false);
                mStockAvailabilityView.setVisibility(View.VISIBLE);
                setAddToCartButtonEnabledWithProgress(false);
        }
    }

    private void setPickersEnabled(boolean enabled) {
        setRadioGroupEnabled(mColorPickerRadioGroup, enabled);
        setRadioGroupEnabled(mSizePickerRadioGroup, enabled);
    }

    private void setRadioGroupEnabled(RadioGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); ++i) {
            View child = group.getChildAt(i);
            if (child instanceof RadioButton) {
                child.setEnabled(enabled);
            }
        }
    }

    private void setQuantityPickerEnabled(boolean enabled, boolean showProgress) {
        mQuantityView.setEnabled(enabled);
        mSubtractButton.setEnabled(enabled);
        mAddButton.setEnabled(enabled);
        mStockAvailabilityView.setText(mMaxQuantity > 0
                ? getString(R.string.stock_availability, mMaxQuantity)
                : "Out of stock");

        //mStockAvailabilityView.setVisibility(showProgress ? View.INVISIBLE : View.VISIBLE);
        mQuantityProgress.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        mQuantityView.setVisibility(showProgress ? View.INVISIBLE : View.VISIBLE);
    }

    private void setAddToCartButtonEnabledNoProgress(boolean enabled) {
        setAddToCartButtonEnabled(enabled, false);
    }

    private void setAddToCartButtonEnabledWithProgress(boolean enabled) {
        setAddToCartButtonEnabled(enabled, true);
    }

    private void setAddToCartButtonEnabled(boolean enabled, boolean showProgress) {
        mAddToCartButton.setEnabled(enabled);
        mButtonProgress.setVisibility(!enabled && showProgress ? View.VISIBLE : View.GONE);
    }
}
