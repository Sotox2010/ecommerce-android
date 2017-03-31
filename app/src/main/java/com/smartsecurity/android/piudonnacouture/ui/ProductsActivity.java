package com.smartsecurity.android.piudonnacouture.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.MetricAffectingSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.Category;
import com.smartsecurity.android.piudonnacouture.model.Product;
import com.smartsecurity.android.piudonnacouture.model.Subcategory;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.util.OnItemClickListener;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.text.WordUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ProductsActivity.class.getSimpleName();

    public static final String EXTRA_PARENT_CATEGORY = "extra_parent_category";
    public static final String EXTRA_PARENT_SUBCATEGORY = "extra_parent_subcategory";

    private static final MetricAffectingSpan sPriceSizeSpan = new AbsoluteSizeSpan(11, true);
    private static final DecimalFormat sPriceFormatter = new DecimalFormat("'$'#,###.00");

    private Category mParentCategory;
    private Subcategory mParentSubcategory;
    private ProductsAdapter mProductsAdapter;

    interface ProductsQuery {
        int LOADER_ID = 0x1;
        Uri URI = PiuDonnaContract.ProductEntry.CONTENT_URI;
        String[] PROJECTION = {
                PiuDonnaContract.ProductEntry._ID,
                PiuDonnaContract.ProductEntry.COLUMN_SERVER_ID,
                PiuDonnaContract.ProductEntry.COLUMN_NAME,
                PiuDonnaContract.ProductEntry.COLUMN_PRICE,
                PiuDonnaContract.ProductEntry.COLUMN_LEAD_IMAGE_URL,
                PiuDonnaContract.ProductEntry.COLUMN_CODE

        };

        String SELECTION = PiuDonnaContract.ProductEntry.COLUMN_SUBCATEGORY_ID + " = ?";
        String SORT_ORDER = PiuDonnaContract.ProductEntry.COLUMN_NAME + " ASC";

        int COLUMN_SERVER_ID = 1;
        int COLUMN_NAME = 2;
        int COLUMN_PRICE = 3;
        int COLUMN_LEAD_IMAGE_URL = 4;
        int COLUMN_CODE= 5;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        Intent intent = getIntent();
        if (getIntent().hasExtra(EXTRA_PARENT_CATEGORY) && getIntent().hasExtra(EXTRA_PARENT_SUBCATEGORY)){
            Bundle extras = intent.getExtras();
            mParentCategory = extras.getParcelable(EXTRA_PARENT_CATEGORY);
            mParentSubcategory = extras.getParcelable(EXTRA_PARENT_SUBCATEGORY);
        } else {
            throw new IllegalStateException(
                    "ProductsActivity needs a Category and Subcategory object as an extra.");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable navIcon = toolbar.getNavigationIcon();
        if (navIcon != null) {
            navIcon.setColorFilter(new PorterDuffColorFilter(
                    UiUtils.getThemeColor(this, R.attr.colorAccent), PorterDuff.Mode.SRC_IN));
        }

        TextView toolbarTitleView = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitleView.setText(WordUtils.capitalizeFully(mParentSubcategory.getName()));

        TextView toolbarSubtitleView = (TextView) toolbar.findViewById(R.id.toolbar_subtitle);
        toolbarSubtitleView.setText(mParentCategory.getName());

        mProductsAdapter = new ProductsAdapter(null);
        mProductsAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Log.d(LOG_TAG, "onItemClick(" + position + ")");
                Product product = mProductsAdapter.getProduct(position);

                Intent intent = ProductDetailActivity.getStartIntent(ProductsActivity.this,
                        product.getServerId(), product.getCode());

                startActivity(intent);

                /*Intent intent = new Intent(ProductsActivity.this, ProductDetailActivity.class);
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getServerId());
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_CODE, product.getCode());
                startActivity(intent);*/
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.products_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.column_span_products_grid)));
        recyclerView.setAdapter(mProductsAdapter);
        recyclerView.setItemAnimator(new ProductsItemAnimator());

        getSupportLoaderManager().initLoader(ProductsQuery.LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == ProductsQuery.LOADER_ID) {
            return new CursorLoader(this,
                    ProductsQuery.URI,
                    ProductsQuery.PROJECTION,
                    ProductsQuery.SELECTION,
                    new String[]{Long.toString(mParentSubcategory.getServerId())},
                    ProductsQuery.SORT_ORDER);
        }

        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == ProductsQuery.LOADER_ID) {

            List<Product> products = new ArrayList<>();
            if (data.moveToFirst()) {
                do {
                    Product product = new Product(
                            data.getLong(ProductsQuery.COLUMN_SERVER_ID),
                            data.getString(ProductsQuery.COLUMN_CODE),
                            data.getString(ProductsQuery.COLUMN_NAME),
                            data.getFloat(ProductsQuery.COLUMN_PRICE),
                            data.getString(ProductsQuery.COLUMN_LEAD_IMAGE_URL));

                    products.add(product);
                } while (data.moveToNext());
            }
            mProductsAdapter.setProducts(products);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

        private List<Product> mProducts;
        private OnItemClickListener mItemClickListener;

        public ProductsAdapter(List<Product> products) {
            mProducts = products;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.list_item_product, parent, false);
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

            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Product product = mProducts.get(position);
            Log.d(LOG_TAG, "Lead Image URL: " + product.getLeadImageUrl());

            holder.mTitleView.setText(WordUtils.capitalizeFully(product.getName()));
            holder.mPriceView.setText(UiUtils.formatPrice(product.getPrice()));
            Picasso.with(ProductsActivity.this)
                    .load(product.getLeadImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mProducts != null ? mProducts.size() : 0;
        }

        public Product getProduct(int position) {
            return mProducts != null ? mProducts.get(position) : null;
        }

        public void setProducts(@Nullable List<Product> products) {
            mProducts = products;
            notifyDataSetChanged();
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            mItemClickListener = listener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView mImageView;
            public TextView mTitleView;
            public TextView mPriceView;

            public ViewHolder(View itemView) {
                super(itemView);
                mImageView = (ImageView) itemView.findViewById(R.id.product_image);
                mTitleView = (TextView) itemView.findViewById(R.id.product_title);
                mPriceView = (TextView) itemView.findViewById(R.id.product_price);
            }
        }

    }

    private class ProductsItemAnimator extends DefaultItemAnimator {
        private final String LOG_TAG = ProductsItemAnimator.class.getSimpleName();

        private int mLastAddAnimatedItem = -2;

        @Override
        public boolean animateAdd(RecyclerView.ViewHolder holder) {
            Log.d(LOG_TAG, "animateAdd(" + holder.getLayoutPosition() + ")");
            if (holder.getLayoutPosition() > mLastAddAnimatedItem) {
                mLastAddAnimatedItem++;
                runEnterAnimation((ProductsAdapter.ViewHolder) holder);
                return false;
            }


            dispatchAddFinished(holder);
            return false;
        }

        private void runEnterAnimation(final ProductsAdapter.ViewHolder holder) {
            //final int screenHeight = Utils.getScreenHeight(holder.itemView.getContext());
            holder.itemView.setTranslationY(300);
            holder.itemView.setAlpha(0);
            holder.itemView.animate()
                    .translationY(0)
                    .alpha(1)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            dispatchAddFinished(holder);
                        }
                    })
                    .start();
        }
    }

    /*public static Spanned formatPrice(float price) {
        String formattedPrice = sPriceFormatter.format(price);

        SpannableString styledString = new SpannableString(formattedPrice);
        styledString.setSpan(sPriceSizeSpan,
                formattedPrice.lastIndexOf(".") + 1,  formattedPrice.length(), 0);

        return styledString;
    }*/
}
