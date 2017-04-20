package com.smartsecurity.android.piudonnacouture.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class PiuDonnaContract {

    public static final String CONTENT_AUTHORITY =
            "com.jesussoto.android.ecommerce.provider";

    public static final String CONTENT_TYPE_BASE =
            "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/";

    public static final String CONTENT_ITEM_TYPE_BASE =
            "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CATEGORY = "category";

    public static final String PATH_SUBCATEGORY = "subcategory";

    public static final String PATH_PRODUCT = "product";

    public static final String PATH_BRAND = "brand";

    public static final String PATH_MULTIMEDIA = "multimedia";

    public static final String PATH_CART = "cart";

    public static final String PATH_ADDRESS_BOOK = "address_book";

    public static final String PATH_ORDER = "orders";


    interface SyncExtras {
        String IS_CALLER_SYNC_ADAPTER = "is_caller_sync_adapter";
        //String NEED_SYNC_TO_NETWORK = "need_sync_to_network";
    }

    public static final String[] STORE_DATA_RELATED_PATHS = {
            PATH_CATEGORY,
            PATH_SUBCATEGORY,
            PATH_BRAND,
            PATH_PRODUCT,
            PATH_MULTIMEDIA
    };

    public static final String[] USER_DATA_RELATED_PATHS = {
            PATH_CART,
            PATH_ADDRESS_BOOK,
            PATH_ORDER
    };

    public static class CategoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String CONTENT_TYPE = makeContentType(PATH_CATEGORY);

        public static final String CONTENT_ITEM_TYPE = makeContentItemType(PATH_CATEGORY);

        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE_URL = "image_url";

        public static Uri buildCategoryUri(long categoryId) {
            return ContentUris.withAppendedId(CONTENT_URI, categoryId);
        }

        public static long getCategoryId(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    public static class SubcategoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBCATEGORY).build();

        public static final String CONTENT_TYPE = makeContentType(PATH_SUBCATEGORY);

        public static final String CONTENT_ITEM_TYPE = makeContentItemType(PATH_SUBCATEGORY);

        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_CATEGORY_ID = "category_id";

        public static Uri buildSubcategoryUri(long subcategoryId) {
            return ContentUris.withAppendedId(CONTENT_URI, subcategoryId);
        }

        public static long getSubcategoryId(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    public static class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT).build();

        public static final String CONTENT_TYPE = makeContentType(PATH_PRODUCT);

        public static final String CONTENT_ITEM_TYPE = makeContentItemType(PATH_PRODUCT);

        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_LEAD_IMAGE_URL = "lead_image_url";
        public static final String COLUMN_BRAND_ID = "brand_id";
        public static final String COLUMN_SUBCATEGORY_ID = "subcategory_id";

        public static Uri buildProductUri(long productId) {
            return ContentUris.withAppendedId(CONTENT_URI, productId);
        }

        public static long getProductId(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    public static class BrandEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BRAND).build();

        public static final String CONTENT_TYPE = makeContentType(PATH_BRAND);

        public static final String CONTENT_ITEM_TYPE = makeContentItemType(PATH_BRAND);

        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_NAME = "name";

        public static Uri buildBrandUri(long brandId) {
            return ContentUris.withAppendedId(CONTENT_URI, brandId);
        }

        public static long getBrandId(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

/*    public static class MultimediaEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MULTIMEDIA).build();

        public static final String CONTENT_TYPE = makeContentType(PATH_MULTIMEDIA);

        public static final String CONTENT_ITEM_TYPE = makeContentItemType(PATH_MULTIMEDIA);

        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_DISPLAY_ORDER = "display_order";
        public static final String COLUMN_PRODUCT_ID = "product_id";

        public static Uri buildMultimediaUri(long multimediaId) {
            return ContentUris.withAppendedId(CONTENT_URI, multimediaId);
        }

        public static String getMultimediaId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }*/

    public static class CartEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CART).build();

        public static final String CONTENT_TYPE = makeContentType(PATH_CART);

        public static final String CONTENT_ITEM_TYPE = makeContentItemType(PATH_CART);

        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_DATE_ADDED = "date_added";
        public static final String COLUMN_PRODUCT_ID = "product_id";
        public static final String COLUMN_COLOR_SERVER_ID = "color_server_id";
        public static final String COLUMN_COLOR = "color";
        public static final String COLUMN_SIZE_SERVER_ID = "size_server_id";
        public static final String COLUMN_SIZE = "size";

        public static Uri buildCartItemUri(long cartItemId) {
            return ContentUris.withAppendedId(CONTENT_URI, cartItemId);
        }

        public static Uri buildCartProductsUri() {
            return CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT).build();
        }

        public static long getCartItemId(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    public static class AddressBookEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ADDRESS_BOOK).build();

        public static final String CONTENT_TYPE = makeContentType(PATH_ADDRESS_BOOK);

        public static final String CONTENT_ITEM_TYPE = makeContentItemType(PATH_ADDRESS_BOOK);

        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_ADDRESS_LINE_1 = "address_line_one";
        public static final String COLUMN_ADDRESS_LINE_2 = "address_line_two";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_ZIP_CODE = "zip_code";
        public static final String COLUMN_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_COUNTRY = "country";

        public static Uri buildAddressUri(long addressId) {
            return ContentUris.withAppendedId(CONTENT_URI, addressId);
        }

        public static long getAddressId(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    public static class OrderEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ORDER).build();

        public static final String CONTENT_TYPE = makeContentType(PATH_ORDER);

        public static final String CONTENT_ITEM_TYPE = makeContentItemType(PATH_ORDER);

        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_REFERENCE = "reference";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUBTOTAL = "subtotal";
        public static final String COLUMN_SHIPPING_PRICE = "shipping_price";
        public static final String COLUMN_TAX = "tax";
        public static final String COLUMN_STATUS = "status";

        public static Uri buildOrderUri(long orderId) {
            return ContentUris.withAppendedId(CONTENT_URI, orderId);
        }

        public static long getOrderId(Uri uri) {
            return ContentUris.parseId(uri);
        }
    }

    private static String makeContentType(String path) {
        if (path != null) {
            return CONTENT_TYPE_BASE + path;
        } else {
            return null;
        }
    }

    private static String makeContentItemType(String path) {
        if (path != null) {
            return CONTENT_ITEM_TYPE_BASE + path;
        } else {
            return null;
        }
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(SyncExtras.IS_CALLER_SYNC_ADAPTER,
                Boolean.toString(true)).build();
    }

    public static boolean isCallerSyncAdapter(Uri uri) {
        return uri.getBooleanQueryParameter(SyncExtras.IS_CALLER_SYNC_ADAPTER, false);
    }

    /*public static Uri addNeedSyncToNetworkParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(SyncExtras.NEED_SYNC_TO_NETWORK,
                Boolean.toString(true)).build();
    }
    public static boolean hasNeedSyncToNetworkParameter(Uri uri) {
        final String parameter = uri.getQueryParameter(SyncExtras.NEED_SYNC_TO_NETWORK);
        return parameter != null && Boolean.parseBoolean(parameter);
    }*/

}
