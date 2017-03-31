package com.smartsecurity.android.piudonnacouture.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.OrderEntry;
import com.smartsecurity.android.piudonnacouture.util.SelectionBuilder;

import java.util.List;

import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.AddressBookEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.BrandEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.CONTENT_AUTHORITY;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.CartEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.CategoryEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.PATH_ADDRESS_BOOK;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.PATH_BRAND;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.PATH_CART;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.PATH_CATEGORY;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.PATH_ORDER;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.PATH_PRODUCT;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.PATH_SUBCATEGORY;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.ProductEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.SubcategoryEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaDatabase.Tables;

public class PiuDonnaProvider extends ContentProvider {
    private static final String TAG = PiuDonnaProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PiuDonnaDatabase mOpenHelper;

    private static final int CATEGORY = 100;
    private static final int CATEGORY_ID = 101;

    private static final int SUBCATEGORY = 200;
    private static final int SUBCATEGORY_ID = 201;

    private static final int BRAND = 300;
    private static final int BRAND_ID = 301;

    private static final int PRODUCT = 400;
    private static final int PRODUCT_ID = 401;

    /*private static final int MULTIMEDIA = 500;
    private static final int MULTIMEDIA_ID = 501;*/

    private static final int CART = 600;
    private static final int CART_ID = 601;
    private static final int CART_PRODUCTS = 602;

    private static final int ADDRESS_BOOK = 700;
    private static final int ADDRESS_BOOK_ID = 701;

    private static final int ORDER = 800;
    private static final int ORDER_ID = 801;

    private static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead? Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found. The code passed into the constructor represents the code to return for the root
        // URI. It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, PATH_CATEGORY, CATEGORY);
        matcher.addURI(authority, PATH_CATEGORY + "/#", CATEGORY_ID);

        matcher.addURI(authority, PATH_SUBCATEGORY, SUBCATEGORY);
        matcher.addURI(authority, PATH_SUBCATEGORY + "/#", SUBCATEGORY_ID);

        matcher.addURI(authority, PATH_BRAND, BRAND);
        matcher.addURI(authority, PATH_BRAND + "/#", BRAND_ID);

        matcher.addURI(authority, PATH_PRODUCT, PRODUCT);
        matcher.addURI(authority, PATH_PRODUCT + "/#", PRODUCT_ID);

        matcher.addURI(authority, PATH_CART, CART);
        matcher.addURI(authority, PATH_CART + "/#", CART_ID);
        matcher.addURI(authority, PATH_CART + "/product", CART_PRODUCTS);

        matcher.addURI(authority, PATH_ADDRESS_BOOK, ADDRESS_BOOK);
        matcher.addURI(authority, PATH_ADDRESS_BOOK + "/#", ADDRESS_BOOK_ID);

        matcher.addURI(authority, PATH_ORDER, ORDER);
        matcher.addURI(authority, PATH_ORDER + "/#", ORDER_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate()");
        mOpenHelper = new PiuDonnaDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CATEGORY:
                return CategoryEntry.CONTENT_TYPE;
            case CATEGORY_ID:
                return CategoryEntry.CONTENT_ITEM_TYPE;
            case SUBCATEGORY:
                return SubcategoryEntry.CONTENT_TYPE;
            case SUBCATEGORY_ID:
                return SubcategoryEntry.CONTENT_ITEM_TYPE;
            case BRAND:
                return BrandEntry.CONTENT_TYPE;
            case BRAND_ID:
                return BrandEntry.CONTENT_ITEM_TYPE;
            case PRODUCT:
                return ProductEntry.CONTENT_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            case CART:
                return CartEntry.CONTENT_TYPE;
            case ADDRESS_BOOK:
                return AddressBookEntry.CONTENT_TYPE;
            case ADDRESS_BOOK_ID:
                return AddressBookEntry.CONTENT_ITEM_TYPE;
            case ORDER:
                return OrderEntry.CONTENT_TYPE;
            case ORDER_ID:
                return OrderEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CATEGORY: {
                long _id = db.insert(Tables.CATEGORY, null, values);
                if (_id > 0) {
                    returnUri = CategoryEntry.buildCategoryUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case SUBCATEGORY: {
                long _id = db.insertOrThrow(Tables.SUBCATEGORY, null, values);
                returnUri = SubcategoryEntry.buildSubcategoryUri(_id);
                break;
            }
            case BRAND: {
                long _id = db.insert(Tables.BRAND, null, values);
                if (_id > 0) {
                    returnUri = BrandEntry.buildBrandUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case PRODUCT: {
                long _id = db.insert(Tables.PRODUCT, null, values);
                if (_id > 0) {
                    returnUri = ProductEntry.buildProductUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case CART: {
                long _id = db.insert(Tables.CART, null, values);
                if (_id > 0) {
                    returnUri = CartEntry.buildCartItemUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case ADDRESS_BOOK: {
                long _id = db.insert(Tables.ADDRESS_BOOK, null, values);
                if (_id > 0) {
                    returnUri = AddressBookEntry.buildAddressUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case ORDER: {
                long _id = db.insert(Tables.ORDER, null, values);
                if (_id > 0) {
                    returnUri = OrderEntry.buildOrderUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        notifyChange(uri);
        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CATEGORY:
                return insertValues(uri, values, db, Tables.CATEGORY);
            case SUBCATEGORY:
                return insertValues(uri, values, db, Tables.SUBCATEGORY);
            case BRAND:
                return insertValues(uri, values, db, Tables.BRAND);
            case PRODUCT:
                return insertValues(uri, values, db, Tables.PRODUCT);
            case CART:
                return insertValues(uri, values, db, Tables.CART);
            case ADDRESS_BOOK:
                return insertValues(uri, values, db, Tables.ADDRESS_BOOK);
            case ORDER:
                return insertValues(uri, values, db, Tables.ORDER);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private int insertValues(@NonNull Uri uri, @NonNull ContentValues[] values, SQLiteDatabase db, String table) {
        int numValues = 0;
        db.beginTransaction();

        try {
            for (ContentValues value : values) {
                long _id = db.insertOrThrow(table, null, value);
                numValues = (_id > 0) ? numValues + 1 : numValues;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        notifyChange(uri);
        return numValues;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            // "/category"
            case CATEGORY: {
                cursor = builder.table(Tables.CATEGORY)
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/category/#"
            case CATEGORY_ID: {
                long categoryId = CategoryEntry.getCategoryId(uri);
                cursor = builder.table(Tables.CATEGORY)
                        .where(CategoryEntry.COLUMN_SERVER_ID + " = ?", Long.toString(categoryId))
                        //.where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/subcategory"
            case SUBCATEGORY: {
                cursor = builder.table(Tables.SUBCATEGORY)
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/subcategory/#"
            case SUBCATEGORY_ID: {
                long subcategoryId = SubcategoryEntry.getSubcategoryId(uri);
                cursor = builder.table(Tables.CATEGORY)
                        .where(SubcategoryEntry.COLUMN_SERVER_ID + " = ?", Long.toString(subcategoryId))
                        //.where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/brand"
            case BRAND: {
                cursor = builder.table(Tables.BRAND)
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/brand/#"
            case BRAND_ID: {
                long brandId = BrandEntry.getBrandId(uri);
                cursor = builder.table(Tables.BRAND)
                        .where(BrandEntry.COLUMN_SERVER_ID + " = ?", Long.toString(brandId))
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/product"
            case PRODUCT: {
                cursor = builder.table(Tables.PRODUCT)
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/product/#"
            case PRODUCT_ID: {
                long productId = ProductEntry.getProductId(uri);
                cursor = builder.table(Tables.PRODUCT)
                        .where(ProductEntry.COLUMN_SERVER_ID + " = ?", Long.toString(productId))
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/cart"
            case CART: {
                cursor = builder.table(Tables.CART)
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/cart/product"
            case CART_PRODUCTS: {
                cursor = builder.table(Tables.CART_JOIN_PRODUCT)
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/address_book"
            case ADDRESS_BOOK: {
                cursor = builder.table(Tables.ADDRESS_BOOK)
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/address_book/#"
            case ADDRESS_BOOK_ID: {
                long addressId = AddressBookEntry.getAddressId(uri);
                cursor = builder.table(Tables.ADDRESS_BOOK)
                        .where(AddressBookEntry.COLUMN_SERVER_ID + " = ?", Long.toString(addressId))
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/order"
            case ORDER: {
                cursor = builder.table(Tables.ORDER)
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            // "/order/#"
            case ORDER_ID: {
                long orderId = OrderEntry.getOrderId(uri);
                cursor = builder.table(Tables.ORDER)
                        .where(OrderEntry.COLUMN_SERVER_ID + " = ?", Long.toString(orderId))
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        int deletedRows = 0;

        switch (match) {
            // "/category"
            case CATEGORY:
                deletedRows = db.delete(Tables.CATEGORY, "1", null);
                break;
            // "/subcategory"
            case SUBCATEGORY:
                deletedRows = db.delete(Tables.SUBCATEGORY, "1", null);
                break;
            // "/product"
            case PRODUCT:
                deletedRows = db.delete(Tables.PRODUCT, "1", null);
                break;
            // "/brand"
            case BRAND:
                deletedRows = db.delete(Tables.BRAND, "1", null);
                break;
            // "/cart"
            case CART:
                deletedRows = db.delete(Tables.CART, "1", null);
                break;
            // "/cart/#"
            case CART_ID:
                long cartItemId = CartEntry.getCartItemId(uri);
                deletedRows = builder.table(Tables.CART)
                        .where(CartEntry.COLUMN_SERVER_ID + " = ?", Long.toString(cartItemId))
                        .where(selection, selectionArgs)
                        .delete(db);

                // Convert the URI to its base form (without the 'id' path segment) in order to
                // notify the content observers of this base 'delete' URI.
                uri = removeLastUriPathSegment(uri);
                break;
            // "/address_book"
            case ADDRESS_BOOK:
                deletedRows = db.delete(Tables.ADDRESS_BOOK, "1", null);
                break;
            // "/address_book/#"
            case ADDRESS_BOOK_ID:
                long addressId = AddressBookEntry.getAddressId(uri);
                deletedRows = builder.table(Tables.ADDRESS_BOOK)
                        .where(AddressBookEntry.COLUMN_SERVER_ID + " = ?", Long.toString(addressId))
                        .where(selection, selectionArgs)
                        .delete(db);

                // Convert the URI to its base form (without the 'id' path segment) in order to
                // notify the content observers of this base 'delete' URI.
                uri = removeLastUriPathSegment(uri);
                break;
            // "/order"
            case ORDER:
                deletedRows = db.delete(Tables.ORDER, "1", null);
                break;
            // "/order/#"
            case ORDER_ID:
                long orderId = OrderEntry.getOrderId(uri);
                deletedRows = builder.table(Tables.ORDER)
                        .where(OrderEntry.COLUMN_SERVER_ID + " = ?", Long.toString(orderId))
                        .where(selection, selectionArgs)
                        .delete(db);

                // Convert the URI to its base form (without the 'id' path segment) in order to
                // notify the content observers of this base 'delete' URI.
                uri = removeLastUriPathSegment(uri);
                break;
            default:
                //if (BuildConfig.DEBUG) {
                    Log.w(TAG, "match (" + match + ") is not handled in 'delete'");
                //}
        }

        notifyChange(uri);
        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        int updatedRows = 0;

        switch (match) {
            // "/cart/#"
            case CART_ID:
                long cartItemServerId = CartEntry.getCartItemId(uri);
                updatedRows = builder.table(Tables.CART)
                        .where(CartEntry.COLUMN_SERVER_ID + " = ?", Long.toString(cartItemServerId))
                        .where(selection, selectionArgs)
                        .update(db, values);

                // Convert the URI to its base form (without the 'id' path segment) in order to
                // notify the content observers of this base 'update' URI.
                uri = removeLastUriPathSegment(uri);
                break;
            default:
                //if (BuildConfig.DEBUG) {
                    Log.w(TAG, "match (" + match + ") is not handled in 'update'");
                //}
        }

        notifyChange(uri);
        return updatedRows;
    }

    private void notifyChange(@NonNull Uri uri) {
        if (!PiuDonnaContract.isCallerSyncAdapter(uri)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    public static Uri removeLastUriPathSegment(Uri uri) {
        Uri.Builder builder = new Uri.Builder()
                .scheme(uri.getScheme())
                .authority(uri.getAuthority());

        // Append all the existing path segments except for the last one.
        List<String> segments = uri.getPathSegments();
        for (int i = 0; i < segments.size() - 1; i++) {
            builder.appendPath(segments.get(i));
        }

        for (String key : uri.getQueryParameterNames()) {
            builder.appendQueryParameter(key, uri.getQueryParameter(key));
        }

        return builder.build();
    }
}
