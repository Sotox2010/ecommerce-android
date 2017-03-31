package com.smartsecurity.android.piudonnacouture.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.smartsecurity.android.piudonnacouture.Config;
import com.smartsecurity.android.piudonnacouture.model.Address;
import com.smartsecurity.android.piudonnacouture.model.Brand;
import com.smartsecurity.android.piudonnacouture.model.CartItem;
import com.smartsecurity.android.piudonnacouture.model.Category;
import com.smartsecurity.android.piudonnacouture.model.Order;
import com.smartsecurity.android.piudonnacouture.model.Product;
import com.smartsecurity.android.piudonnacouture.model.Subcategory;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.AddressBookEntry;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.CartEntry;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Vector;

import retrofit.RetrofitError;

import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.BASE_CONTENT_URI;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.BrandEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.CategoryEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.OrderEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.ProductEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.STORE_DATA_RELATED_PATHS;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.SubcategoryEntry;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.USER_DATA_RELATED_PATHS;
import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.addCallerIsSyncAdapterParameter;


public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    private final AccountManager mAccountManager;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync(account, extras, authority, provider, syncResult)");
        Log.d(TAG, "account: " + account.toString());
        Log.d(TAG, "extras: " + extras.toString());
        Log.d(TAG, "authority: " + authority);
        Log.d(TAG, "provider: " + provider.getLocalContentProvider().getClass().getName());
        //Log.d(TAG, "syncResult: " + syncResult.toString());

        long opStart = System.currentTimeMillis();
        long syncDuration;

        // Even if 'account' is the fake default sync account, delete possibly remaining user data.
        syncResult.stats.numDeletes += deleteUserData();

        try {
            JsonObject storeDataJson = SyncUtils.sWebService.getStoreData("0");

            syncResult.stats.numDeletes += deleteStoreData();

            Gson gson = new Gson();

            JsonArray categoriesJsonArray  = storeDataJson.getAsJsonArray("categories");
            Type categoryListType = new TypeToken<List<Category>>(){}.getType();
            List<Category> categories = gson.fromJson(categoriesJsonArray, categoryListType);
            syncResult.stats.numInserts += saveCategories(categories);

            JsonArray subcategoriesJsonArray  = storeDataJson.getAsJsonArray("subcategories");
            Type subcategoryListType = new TypeToken<List<Subcategory>>(){}.getType();
            List<Subcategory> subcategories = gson.fromJson(subcategoriesJsonArray, subcategoryListType);
            syncResult.stats.numInserts += saveSubcategories(subcategories);

            JsonArray brandsJsonArray  = storeDataJson.getAsJsonArray("brands");
            Type brandListType = new TypeToken<List<Brand>>(){}.getType();
            List<Brand> brands = gson.fromJson(brandsJsonArray, brandListType);
            syncResult.stats.numInserts += saveBrands(brands);

            JsonArray productsJsonArray  = storeDataJson.getAsJsonArray("products");
            Type productsListType = new TypeToken<List<Product>>(){}.getType();
            List<Product> products = gson.fromJson(productsJsonArray, productsListType);
            syncResult.stats.numInserts += saveProducts(products);

            // When all the data is updated, notify the changes to the observers.
            ContentResolver resolver = getContext().getContentResolver();
            for (String path : STORE_DATA_RELATED_PATHS) {
                Uri uri = BASE_CONTENT_URI.buildUpon().appendPath(path).build();
                resolver.notifyChange(uri, null);
            }

        } catch (RetrofitError error) {
            error.printStackTrace();
            if (error.getKind() == RetrofitError.Kind.NETWORK) {
                syncResult.stats.numIoExceptions++;
            }
        }

        // If there is a valid user accounts different than the default sync account,
        // then fetch this user data
        if (!account.name.equals(AccountAuthenticator.ACCOUNT_NAME_SYNC)) {
            Log.d(TAG, "onPerformSync: fetching user data for account " + account.name);
            try {
                String authToken = mAccountManager.blockingGetAuthToken(
                        account, AccountAuthenticator.AUTHTOKEN_TYPE, true);

                try {
                    List<CartItem> cart = SyncUtils.sWebService.getShoppingCart(account.name, authToken);
                    syncResult.stats.numInserts += saveCart(cart);
                } catch (RetrofitError error) {
                    error.printStackTrace();
                    if (error.getKind() == RetrofitError.Kind.NETWORK) {
                        syncResult.stats.numIoExceptions++;
                    } else if (error.getKind() == RetrofitError.Kind.HTTP && error.getResponse().getStatus() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        Log.d(TAG, "onPerformSync: getShoppingCart returned 304, the data has not been modified.");
                    }
                }

                try {
                    List<Address> book = SyncUtils.sWebService.getAddressBook(authToken, account.name);
                    syncResult.stats.numInserts += saveAddressBook(book);
                } catch (RetrofitError error) {
                    error.printStackTrace();
                    if (error.getKind() == RetrofitError.Kind.NETWORK) {
                        syncResult.stats.numIoExceptions++;
                    } else if (error.getKind() == RetrofitError.Kind.HTTP && error.getResponse().getStatus() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        Log.d(TAG, "onPerformSync: getAddressBook returned 304, the data has not been modified.");
                    }
                }

                try {
                    List<Order> orders = SyncUtils.sWebService.getOrders(authToken, account.name);
                    syncResult.stats.numInserts += saveOrders(orders);
                } catch (RetrofitError error) {
                    error.printStackTrace();
                    if (error.getKind() == RetrofitError.Kind.NETWORK) {
                        syncResult.stats.numIoExceptions++;
                    } else if (error.getKind() == RetrofitError.Kind.HTTP && error.getResponse().getStatus() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        Log.d(TAG, "onPerformSync: getOrders returned 304, the data has not been modified.");
                    }
                }

                /*getContext().getContentResolver().notifyChange(CartEntry.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(AddressBookEntry.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(OrderEntry.CONTENT_URI, null);*/

                // When all the data is updated, notify the changes to the observers.
                ContentResolver resolver = getContext().getContentResolver();
                for (String path : USER_DATA_RELATED_PATHS) {
                    Uri uri = BASE_CONTENT_URI.buildUpon().appendPath(path).build();
                    resolver.notifyChange(uri, null);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        syncDuration = System.currentTimeMillis() - opStart;

        //if (dataChanged) {
            //long totalDuration = choresDuration + syncDuration;
            Log.d(TAG, "SYNC STATS:\n" +
                    " *  Account synced: " + account.name + "\n" +
                    //" *  Content provider operations: " + operations + "\n" +
                    " *  Sync took: " + syncDuration + "ms\n" +
                    " *  " + syncResult.toString());

                    //" *  Post-sync chores took: " + choresDuration + "ms\n" +
                    //" *  Total time: " + totalDuration + "ms\n" +
                    //" *  Total data read from cache: \n" +
                    //(mRemoteDataFetcher.getTotalBytesReadFromCache() / 1024) + "kB\n" +
                    //" *  Total data downloaded: \n" +
                    //(mRemoteDataFetcher.getTotalBytesDownloaded() / 1024) + "kB");
        //}

    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
    }

    private int saveCategories(List<Category> categories) {
        Vector<ContentValues> cvVector = new Vector<>(categories.size());

        for (Category category : categories) {
            ContentValues categoryValues = new ContentValues();
            categoryValues.put(CategoryEntry.COLUMN_NAME, category.getName());
            categoryValues.put(CategoryEntry.COLUMN_SERVER_ID, category.getServerId());

            cvVector.add(categoryValues);
        }

        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);

        return getContext().getContentResolver().bulkInsert(
                addCallerIsSyncAdapterParameter(CategoryEntry.CONTENT_URI), cvArray);
    }

    private int saveSubcategories(List<Subcategory> subcategories) {
        Vector<ContentValues> cvVector = new Vector<>(subcategories.size());

        for (Subcategory subcategory : subcategories) {
            ContentValues subcategoryValues = new ContentValues();
            subcategoryValues.put(SubcategoryEntry.COLUMN_SERVER_ID, subcategory.getServerId());
            subcategoryValues.put(SubcategoryEntry.COLUMN_NAME, subcategory.getName());
            subcategoryValues.put(SubcategoryEntry.COLUMN_IMAGE_URL,
                    Config.buildSubcategoryImageUrl(subcategory.getImageUrl()));
            subcategoryValues.put(SubcategoryEntry.COLUMN_CATEGORY_ID, subcategory.getCategoryId());

            cvVector.add(subcategoryValues);
        }

        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);

        return getContext().getContentResolver().bulkInsert(
                addCallerIsSyncAdapterParameter(SubcategoryEntry.CONTENT_URI), cvArray);
    }

    private int saveBrands(List<Brand> brands) {
        Vector<ContentValues> cvVector = new Vector<>(brands.size());

        for (Brand brand : brands) {
            ContentValues brandValues = new ContentValues();
            brandValues.put(BrandEntry.COLUMN_SERVER_ID, brand.getServerId());
            brandValues.put(BrandEntry.COLUMN_NAME, brand.getName());

            cvVector.add(brandValues);
        }

        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);

        return getContext().getContentResolver().bulkInsert(
                addCallerIsSyncAdapterParameter(BrandEntry.CONTENT_URI), cvArray);
    }

    private int saveProducts(List<Product> products) {
        Vector<ContentValues> cvVector = new Vector<>(products.size());

        for (Product product : products) {
            ContentValues productValues = new ContentValues();
            productValues.put(ProductEntry.COLUMN_SERVER_ID, product.getServerId());
            productValues.put(ProductEntry.COLUMN_CODE, product.getCode());
            productValues.put(ProductEntry.COLUMN_NAME, product.getName());
            productValues.put(ProductEntry.COLUMN_DESCRIPTION, product.getDescription());
            productValues.put(ProductEntry.COLUMN_PRICE, product.getPrice());
            productValues.put(ProductEntry.COLUMN_LEAD_IMAGE_URL,
                    Config.buildProductImageUrl(product.getLeadImageUrl()));
            productValues.put(ProductEntry.COLUMN_BRAND_ID, product.getBrandId());
            productValues.put(ProductEntry.COLUMN_SUBCATEGORY_ID, product.getSubcategoryId());

            cvVector.add(productValues);
        }

        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);

        return getContext().getContentResolver().bulkInsert(
                addCallerIsSyncAdapterParameter(ProductEntry.CONTENT_URI), cvArray);
    }

    private int saveCart(List<CartItem> cart) {
        Vector<ContentValues> cvVector = new Vector<>(cart.size());

        for (CartItem item : cart) {
            ContentValues cartItemValues = new ContentValues();
            cartItemValues.put(CartEntry.COLUMN_SERVER_ID, item.getServerId());
            cartItemValues.put(CartEntry.COLUMN_QUANTITY, item.getQuantity());
            cartItemValues.put(CartEntry.COLUMN_DATE_ADDED, item.getRfc3339Date());
            cartItemValues.put(CartEntry.COLUMN_PRODUCT_ID, item.getProductServerId());
            cartItemValues.put(CartEntry.COLUMN_COLOR_SERVER_ID, item.getColorServerId());
            cartItemValues.put(CartEntry.COLUMN_COLOR, item.getColor());
            cartItemValues.put(CartEntry.COLUMN_SIZE_SERVER_ID, item.getSizeServerId());
            cartItemValues.put(CartEntry.COLUMN_SIZE, item.getSize());

            cvVector.add(cartItemValues);
        }

        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);

        return getContext().getContentResolver().bulkInsert(
                addCallerIsSyncAdapterParameter(CartEntry.CONTENT_URI), cvArray);
    }

    private int saveAddressBook(List<Address> book) {
        Vector<ContentValues> cvVector = new Vector<>(book.size());

        for (Address address : book) {
            ContentValues addressValues = new ContentValues();
            addressValues.put(AddressBookEntry.COLUMN_SERVER_ID, address.getServerId());
            addressValues.put(AddressBookEntry.COLUMN_FULL_NAME, address.getFullName());
            addressValues.put(AddressBookEntry.COLUMN_ADDRESS_LINE_1, address.getAddressLine1());
            addressValues.put(AddressBookEntry.COLUMN_ADDRESS_LINE_2, address.getAddressLine2());
            addressValues.put(AddressBookEntry.COLUMN_CITY, address.getCity());
            addressValues.put(AddressBookEntry.COLUMN_STATE, address.getState());
            addressValues.put(AddressBookEntry.COLUMN_ZIP_CODE, address.getZipCode());
            addressValues.put(AddressBookEntry.COLUMN_COUNTRY, address.getCountry());
            addressValues.put(AddressBookEntry.COLUMN_PHONE_NUMBER, address.getPhoneNumber());

            cvVector.add(addressValues);
        }

        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);

        return getContext().getContentResolver().bulkInsert(
                addCallerIsSyncAdapterParameter(AddressBookEntry.CONTENT_URI), cvArray);
    }

    private int saveOrders(List<Order> orders) {
        Vector<ContentValues> cvVector = new Vector<>(orders.size());

        for (Order order : orders) {
            ContentValues orderValues = new ContentValues();
            orderValues.put(OrderEntry.COLUMN_SERVER_ID, order.getServerId());
            orderValues.put(OrderEntry.COLUMN_REFERENCE, order.getReference());
            orderValues.put(OrderEntry.COLUMN_DATE, order.getDate());
            orderValues.put(OrderEntry.COLUMN_QUANTITY, order.getQuantity());
            orderValues.put(OrderEntry.COLUMN_SUBTOTAL, order.getSubtotal());
            orderValues.put(OrderEntry.COLUMN_SHIPPING_PRICE, order.getShippingPrice());
            orderValues.put(OrderEntry.COLUMN_TAX, order.getTax());
            orderValues.put(OrderEntry.COLUMN_STATUS, order.getRawStatus());

            cvVector.add(orderValues);
        }

        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);

        return getContext().getContentResolver().bulkInsert(
                addCallerIsSyncAdapterParameter(OrderEntry.CONTENT_URI), cvArray);
    }

    private int deleteStoreData() {
        int deletedRows = 0;
        ContentResolver resolver = getContext().getContentResolver();

        for (String path : STORE_DATA_RELATED_PATHS) {
            Uri uri = BASE_CONTENT_URI.buildUpon().appendPath(path).build();
            deletedRows += resolver.delete(uri, null, null);
        }

        return deletedRows;
    }

    private int deleteUserData() {
        int deletedRows = 0;
        ContentResolver resolver = getContext().getContentResolver();

        for (String path : USER_DATA_RELATED_PATHS) {
            Uri uri = BASE_CONTENT_URI.buildUpon().appendPath(path).build();
            deletedRows += resolver.delete(uri, null, null);
        }

        return deletedRows;
    }
}
