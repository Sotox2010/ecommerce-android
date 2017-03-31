package com.smartsecurity.android.piudonnacouture.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartsecurity.android.piudonnacouture.Config;
import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaDatabase;
import com.smartsecurity.android.piudonnacouture.ui.widget.EmptyView;
import com.smartsecurity.android.piudonnacouture.ui.worker.ShoppingCartWorkerFragment;
import com.smartsecurity.android.piudonnacouture.util.AccountUtils;
import com.smartsecurity.android.piudonnacouture.util.DateUtils;
import com.smartsecurity.android.piudonnacouture.util.OnItemClickListener;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.text.WordUtils;

public class ShoppingCartActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, ShoppingCartWorkerFragment.OnRemoveFromCartCallback {
    private static final String TAG = "ShoppingCartActivity";

    private static final String STATE_REMOVE_DIALOG_SHOWN = "state_remove_dialog_shown";

    private static final String TAG_FRAGMENT_CART_WORKER = "fragment_cart_worker";

    interface OnCartItemActionClickListener {
        void onCartItemRemoveClick(String productName, String productCode, long cartItemServerId);
        void onCartItemEditClick(String productName, String productCode, float productPrice, long cartItemServerId, long colorServerId, String color, long sizeServerId, String size);
    }

    private Toolbar mToolbarActionBar;
    private ViewStub mEmptyViewStub;
    private EmptyView mEmptyView;
    private Button mCheckoutButton;
    private ProgressDialog mRemoveProgressDialog;

    private ShoppingCartAdapter mCartAdapter;
    private ShoppingCartWorkerFragment mWorkerFragment;

    private Handler mHandler = new Handler();

    public interface CartProductsQuery {
        int LOADER_ID = 0x1;

        Uri URI = PiuDonnaContract.CartEntry.buildCartProductsUri();
        String[] PROJECTION = {
                PiuDonnaDatabase.Tables.CART + "." + PiuDonnaContract.CartEntry._ID,
                PiuDonnaDatabase.Tables.CART + "." + PiuDonnaContract.CartEntry.COLUMN_SERVER_ID,
                PiuDonnaDatabase.Tables.PRODUCT + "." + PiuDonnaContract.ProductEntry.COLUMN_SERVER_ID,
                PiuDonnaDatabase.Tables.PRODUCT + "." + PiuDonnaContract.ProductEntry.COLUMN_CODE,
                PiuDonnaDatabase.Tables.CART + "." + PiuDonnaContract.CartEntry.COLUMN_QUANTITY,
                PiuDonnaContract.CartEntry.COLUMN_COLOR_SERVER_ID,
                PiuDonnaContract.CartEntry.COLUMN_COLOR,
                PiuDonnaContract.CartEntry.COLUMN_SIZE_SERVER_ID,
                PiuDonnaContract.CartEntry.COLUMN_SIZE,
                PiuDonnaContract.ProductEntry.COLUMN_NAME,
                PiuDonnaContract.ProductEntry.COLUMN_PRICE,
                PiuDonnaContract.CartEntry.COLUMN_DATE_ADDED
        };

        int COLUMN_CART_ID = 0;
        int COLUMN_CART_SERVER_ID = 1;
        int COLUMN_PRODUCT_SERVER_ID = 2;
        int COLUMN_PRODUCT_CODE = 3;
        int COLUMN_QUANTITY = 4;
        int COLUMN_COLOR_SERVER_ID = 5;
        int COLUMN_COLOR = 6;
        int COLUMN_SIZE_SERVER_ID = 7;
        int COLUMN_SIZE = 8;
        int COLUMN_NAME = 9;
        int COLUMN_PRICE = 10;
        int COLUMN_DATE_ADDED = 11;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        mToolbarActionBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbarActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable navIcon = mToolbarActionBar.getNavigationIcon();
        if (navIcon != null) {
            navIcon.setColorFilter(new PorterDuffColorFilter(
                    UiUtils.getThemeColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_IN));
        }

        mCartAdapter = new ShoppingCartAdapter(null);
        mCartAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                startProductDetailActivity(position);
            }
        });
        mCartAdapter.setOnCartItemActionClickListener(new OnCartItemActionClickListener() {
            @Override
            public void onCartItemRemoveClick(String productName, String productCode, long cartItemServerId) {
                displayConfirmationDialog(productName, productCode, cartItemServerId);
            }

            @Override
            public void onCartItemEditClick(String productName, String productCode, float productPrice, long cartItemServerId, long colorServerId, String color, long sizeServerId, String size) {
                startActivity(UpdateCartItemActivity.getStartIntent(ShoppingCartActivity.this,
                        productName, productCode, productPrice, cartItemServerId, colorServerId,
                                color, sizeServerId, size));
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.cart_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mCartAdapter);

        mEmptyViewStub = (ViewStub) findViewById(R.id.stub_empty_view);
        mCheckoutButton = (Button) findViewById(R.id.checkout_button);
        mCheckoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ShoppingCartActivity.this, R.style.Theme_PiuDonna_Dialog_Alert)
                        .setTitle("Checkout temporarily disabled")
                        .setMessage("The checkout process is temporarily disabled due to app maintenance. It will be back soon, thanks for your patience.")
                        .setPositiveButton("OK", null)
                        .show();

                //Temporarily disabled.
                //startActivity(new Intent(ShoppingCartActivity.this, CheckoutActivity.class));
            }
        });

        // Add the worker fragment (id needed) and keep a reference to it.
        mWorkerFragment = (ShoppingCartWorkerFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_FRAGMENT_CART_WORKER);

        if (mWorkerFragment == null) {
            mWorkerFragment = ShoppingCartWorkerFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(mWorkerFragment, TAG_FRAGMENT_CART_WORKER)
                    .commit();
        }

        boolean loggedIn = AccountUtils.hasActiveAccount(this);
        if (loggedIn) {
            getSupportLoaderManager().initLoader(CartProductsQuery.LOADER_ID, null, this);
        } else {
            setEmptyViewVisibility(View.VISIBLE, loggedIn);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState ) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_REMOVE_DIALOG_SHOWN, mRemoveProgressDialog != null);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getBoolean(STATE_REMOVE_DIALOG_SHOWN, false)) {
            showProgressDialog();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CartProductsQuery.LOADER_ID) {
            return new CursorLoader(this,
                    CartProductsQuery.URI,
                    CartProductsQuery.PROJECTION,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == CartProductsQuery.LOADER_ID) {
            setEmptyViewVisibility(cursor.getCount() > 0 ? View.GONE : View.VISIBLE, true);
            mCheckoutButton.setVisibility(cursor.getCount() > 0 ? View.VISIBLE : View.GONE);

            mCartAdapter.swapCursor(cursor);
            reloadItemCountAndTotalPrice(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CartProductsQuery.LOADER_ID) {
            mCartAdapter.swapCursor(null);
        }
    }

    private void reloadItemCountAndTotalPrice(Cursor cursor) {
        int count = 0;
        float price = 0f;

        if (cursor.moveToFirst()) {
            do {
                int quantity = cursor.getInt(CartProductsQuery.COLUMN_QUANTITY);
                count += quantity;
                price += (quantity * cursor.getFloat(CartProductsQuery.COLUMN_PRICE));
            } while (cursor.moveToNext());
        }

        final int itemCount = count;
        mHandler.post(() -> {
            String countStr = itemCount > 0 ? String.format(" (%d)", itemCount) : "";
            mToolbarActionBar.setTitle(getString(R.string.title_activity_shopping_cart) + countStr);
        });

        mCheckoutButton.setText(getString(R.string.button_text_checkout, price));
    }

    private void startProductDetailActivity(int position) {
        long productServerId = mCartAdapter.getProductId(position);
        String productCode = mCartAdapter.getProductCode(position);

        Intent intent = ProductDetailActivity.getStartIntent(this, productServerId, productCode);
        startActivity(intent);
    }

    @Override
    public void onRemoveFromCartResult(String result) {
        hideProgressDialog();

        // A 'null' result means 'success' for us.
        if (result == null) {
            Toast.makeText(this, "Item removed successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFromCart(String productCode, long cartItemServerId) {
        Log.i(TAG, String.format("removeFromCart()\nproductCode: %s\ncartItemServerId: %d", productCode, cartItemServerId));
        showProgressDialog();

        mWorkerFragment.removeFromCart(
                AccountUtils.getActiveAccountName(ShoppingCartActivity.this), productCode, cartItemServerId);
    }

    private void displayConfirmationDialog(String productName, String productCode, long cartItemServerId) {
        DialogFragment dialog = RemoveConfirmationDialogFragment
                .newInstance(productName, productCode, cartItemServerId);

        dialog.show(getSupportFragmentManager(), "dialog_remove_confirmation");
    }

    private void showProgressDialog() {
        if (mRemoveProgressDialog == null) {
            mRemoveProgressDialog = new ProgressDialog(this);
            mRemoveProgressDialog.setTitle("Removing item...");
            mRemoveProgressDialog.setMessage("Please wait.");
            mRemoveProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mRemoveProgressDialog.setCancelable(false);
            mRemoveProgressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (mRemoveProgressDialog != null) {
            mRemoveProgressDialog.dismiss();
            mRemoveProgressDialog = null;
        }
    }

    private void setEmptyViewVisibility(@Visibility int visibility, boolean loggedIn) {
        if (mEmptyView == null) {
            mEmptyView = (EmptyView) mEmptyViewStub.inflate();
        }

        if (visibility == View.VISIBLE) {
            mEmptyView.reset();

            // TODO: change to string resources.
            if (loggedIn) {
                mEmptyView.setTitle("Your cart is empty");
                mEmptyView.setSubtitle("When you add an item to your cart, you'll see it here.");
            } else {
                mEmptyView.setTitle("Your cart is empty");
                mEmptyView.setSubtitle("You should sign in to see your cart items.");
            }
        }

        mEmptyView.setVisibility(visibility);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {

        private OnItemClickListener mItemClickListener;
        private OnCartItemActionClickListener mCartItemActionClickListener;
        private Cursor mCursor;

        public ShoppingCartAdapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater()
                    .inflate(R.layout.list_item_shopping_cart, parent, false);

            final ViewHolder holder = new ViewHolder(itemView);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (mItemClickListener != null && position != RecyclerView.NO_POSITION) {
                        mItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            });

            holder.mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (mCartItemActionClickListener != null && position != RecyclerView.NO_POSITION) {
                        if (mCursor != null && mCursor.moveToPosition(position)) {
                            String productName = mCursor.getString(CartProductsQuery.COLUMN_NAME);
                            String productCode = mCursor.getString(CartProductsQuery.COLUMN_PRODUCT_CODE);
                            float productPrice = mCursor.getFloat(CartProductsQuery.COLUMN_PRICE);
                            long cartItemServerId = mCursor.getLong(CartProductsQuery.COLUMN_CART_SERVER_ID);
                            long colorServerId = mCursor.getLong(CartProductsQuery.COLUMN_COLOR_SERVER_ID);
                            String color = mCursor.getString(CartProductsQuery.COLUMN_COLOR);
                            long sizeServerId = mCursor.getLong(CartProductsQuery.COLUMN_SIZE_SERVER_ID);
                            String size = mCursor.getString(CartProductsQuery.COLUMN_SIZE);

                            mCartItemActionClickListener.onCartItemEditClick(productName,
                                    productCode, productPrice, cartItemServerId, colorServerId,
                                            color, sizeServerId, size);
                        }
                    }
                }
            });

            holder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (mCartItemActionClickListener != null && position != RecyclerView.NO_POSITION) {
                        if (mCursor != null && mCursor.moveToPosition(position)) {
                            String productName = mCursor.getString(CartProductsQuery.COLUMN_NAME);
                            String productCode = mCursor.getString(CartProductsQuery.COLUMN_PRODUCT_CODE);
                            long cartItemServerId = mCursor.getLong(CartProductsQuery.COLUMN_CART_SERVER_ID);
                            mCartItemActionClickListener.onCartItemRemoveClick(productName, productCode, cartItemServerId);
                        }
                    }
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);

            holder.mProductTitleView.setText(mCursor.getString(CartProductsQuery.COLUMN_NAME));
            holder.mAddedDateView.setText(getString(R.string.cart_added_date, DateUtils.getCartItemDateFromRfc3339(mCursor.getString(CartProductsQuery.COLUMN_DATE_ADDED))));
            holder.mProductSpecsView.setText(getString(R.string.cart_product_details,
                    WordUtils.capitalizeFully(mCursor.getString(CartProductsQuery.COLUMN_COLOR)),
                    mCursor.getString(CartProductsQuery.COLUMN_SIZE),
                    mCursor.getInt(CartProductsQuery.COLUMN_QUANTITY)));
            holder.mPriceView.setText(UiUtils.formatPrice(
                    mCursor.getFloat(CartProductsQuery.COLUMN_PRICE)));

            Uri imageUri = Config.buildProductImageUrlByCodeAndColor(
                    mCursor.getString(CartProductsQuery.COLUMN_PRODUCT_CODE),
                    mCursor.getLong(CartProductsQuery.COLUMN_COLOR_SERVER_ID));

            Picasso.with(ShoppingCartActivity.this)
                    .load(imageUri)
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        public void swapCursor(Cursor cursor) {
            if (mCursor != cursor) {
                mCursor = cursor;
                notifyDataSetChanged();
            }
        }

        public long getProductId(int position) {
            if (mCursor == null) {
                return -1;
            }

            mCursor.moveToPosition(position);
            return mCursor.getLong(CartProductsQuery.COLUMN_PRODUCT_SERVER_ID);
        }

        public String getProductCode(int position) {
            if (mCursor == null) {
                return null;
            }

            mCursor.moveToPosition(position);
            return mCursor.getString(CartProductsQuery.COLUMN_PRODUCT_CODE);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            if (mItemClickListener != listener) {
                mItemClickListener = listener;
            }
        }

        public void setOnCartItemActionClickListener(OnCartItemActionClickListener listener) {
            if (mCartItemActionClickListener != listener) {
                mCartItemActionClickListener = listener;
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mImageView;
            TextView mProductTitleView;
            TextView mPriceView;
            TextView mAddedDateView;
            TextView mProductSpecsView;
            Button mEditButton;
            Button mRemoveButton;

            public ViewHolder(View itemView) {
                super(itemView);
                mImageView = (ImageView) itemView.findViewById(R.id.product_image);
                mProductTitleView = (TextView) itemView.findViewById(R.id.product_title);
                mPriceView = (TextView) itemView.findViewById(R.id.product_price);
                mAddedDateView = (TextView) itemView.findViewById(R.id.added_on);
                mProductSpecsView = (TextView) itemView.findViewById(R.id.product_specs);
                mEditButton = (Button) itemView.findViewById(R.id.edit_button);
                mRemoveButton = (Button) itemView.findViewById(R.id.remove_button);

            }
        }
    }

    public static class RemoveConfirmationDialogFragment extends DialogFragment {

        public static String ARG_PRODUCT_NAME = "arg_product_name";
        public static String ARG_PRODUCT_CODE = "arg_product_code";
        public static String ARG_CART_ITEM_SERVER_ID = "arg_cart_item_server_id";

        public static RemoveConfirmationDialogFragment newInstance(String productName, String productCode, long cartItemServerId) {
            Bundle args = new Bundle();
            args.putString(ARG_PRODUCT_NAME, productName);
            args.putString(ARG_PRODUCT_CODE, productCode);
            args.putLong(ARG_CART_ITEM_SERVER_ID, cartItemServerId);

            RemoveConfirmationDialogFragment fragment = new RemoveConfirmationDialogFragment();
            fragment.setArguments(args);
            return fragment;
        }

        private ShoppingCartActivity mActivity;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mActivity = (ShoppingCartActivity) context;
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mActivity = null;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // TODO: Change this dialog hardcoded strings to string resources.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_PiuDonna_Dialog_Alert);
            builder.setTitle("Remove item?");
            builder.setMessage(String.format("Do you really want to remove \"%s\"?", getArguments().getString(ARG_PRODUCT_NAME)));
            builder.setNegativeButton("Cancel", null);
            builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mActivity != null) {
                        mActivity.removeFromCart(
                                getArguments().getString(ARG_PRODUCT_CODE),
                                getArguments().getLong(ARG_CART_ITEM_SERVER_ID));
                    }
                }
            });

            return builder.create();
        }
    }
}
