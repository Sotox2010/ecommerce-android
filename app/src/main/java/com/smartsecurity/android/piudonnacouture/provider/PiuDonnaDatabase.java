package com.smartsecurity.android.piudonnacouture.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.smartsecurity.android.piudonnacouture.BuildConfig;

import java.io.File;

import static com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract.*;

public class PiuDonnaDatabase extends SQLiteOpenHelper {

    private static final String LOG_TAG = PiuDonnaDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "piudonna_couture.db";

    // NOTE: carefully update onUpgrade() when bumping database versions to make
    // sure user data is saved.

    private static final int DATABASE_VERSION = 1; // app version 0.1
    //private static final int CUR_DATABASE_VERSION = VER_2015_RELEASE_B;

    //private final Context mContext;

    public interface Tables {
        String CATEGORY = "category";
        String SUBCATEGORY = "subcategory";
        String BRAND = "brand";
        String PRODUCT = "product";
        String CART = "cart";
        String ADDRESS_BOOK = "address_book";
        String ORDER = "order_history";

        String CART_JOIN_PRODUCT = CART + " LEFT OUTER JOIN " + PRODUCT
                + " ON " + CART + "." + CartEntry.COLUMN_PRODUCT_ID
                + " = " + PRODUCT + "." + ProductEntry.COLUMN_SERVER_ID;
    }

    public PiuDonnaDatabase(Context context) {
        // TODO: Change this for production.
        super(context, BuildConfig.DEBUG
                    ? context.getExternalFilesDir(null).getAbsolutePath() + File.separator + DATABASE_NAME
                    : DATABASE_NAME,
                null, DATABASE_VERSION);

        //Log.w(LOG_TAG, context.getFilesDir().getAbsolutePath());
        Log.w(LOG_TAG, context.getExternalFilesDir(null).getAbsolutePath());

        //mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate()");

        // TODO: Check 'ON CONFLICT REPLACE'.

        final String SQL_CREATE_TABLE_CATEGORY = "CREATE TABLE " + Tables.CATEGORY + " ("
                + CategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CategoryEntry.COLUMN_SERVER_ID + " INTEGER NOT NULL,"
                + CategoryEntry.COLUMN_NAME + " TEXT NOT NULL,"
                + CategoryEntry.COLUMN_IMAGE_URL + " TEXT,"
                + "UNIQUE (" + CategoryEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE)";

        final String SQL_CREATE_TABLE_SUBCATEGORY = "CREATE TABLE " + Tables.SUBCATEGORY + " ("
                + SubcategoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SubcategoryEntry.COLUMN_SERVER_ID + " INTEGER NOT NULL,"
                + SubcategoryEntry.COLUMN_NAME + " TEXT NOT NULL,"
                + SubcategoryEntry.COLUMN_IMAGE_URL + " TEXT,"
                + SubcategoryEntry.COLUMN_CATEGORY_ID + " INTEGER NOT NULL,"
                + "UNIQUE (" + SubcategoryEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE)";
                //+ "UNIQUE (" + SubcategoryEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE,"
                //+ "FOREIGN KEY (" + SubcategoryEntry.COLUMN_CATEGORY_ID + ") REFERENCES "
                //    + Tables.CATEGORY + "(" + CategoryEntry.COLUMN_SERVER_ID + ") ON DELETE CASCADE)";

        final String SQL_CREATE_TABLE_BRAND = "CREATE TABLE " + Tables.BRAND + " ("
                + BrandEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BrandEntry.COLUMN_SERVER_ID + " INTEGER NOT NULL,"
                + BrandEntry.COLUMN_NAME + " TEXT NOT NULL,"
                + "UNIQUE (" + BrandEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE)";

        // TODO: Check if 'brand_id' could be NULL.
        final String SQL_CREATE_TABLE_PRODUCT = "CREATE TABLE " + Tables.PRODUCT + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ProductEntry.COLUMN_SERVER_ID + " INTEGER NOT NULL,"
                + ProductEntry.COLUMN_CODE + " TEXT NOT NULL,"
                + ProductEntry.COLUMN_NAME + " TEXT NOT NULL,"
                + ProductEntry.COLUMN_DESCRIPTION + " TEXT,"
                + ProductEntry.COLUMN_PRICE + " REAL NOT NULL,"
                //+ ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL,"
                //+ ProductEntry.COLUMN_MAX_QUANTITY + " INTEGER,"
                //+ ProductEntry.COLUMN_MIN_QUANTITY + " INTEGER,"
                + ProductEntry.COLUMN_LEAD_IMAGE_URL + " TEXT,"
                + ProductEntry.COLUMN_SUBCATEGORY_ID + " INTEGER NOT NULL,"
                + ProductEntry.COLUMN_BRAND_ID + " INTEGER,"
                + "UNIQUE (" + ProductEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE)";
                //+ "UNIQUE (" + ProductEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE,"
                //+ "FOREIGN KEY (" + ProductEntry.COLUMN_SUBCATEGORY_ID + ") REFERENCES "
                //    + Tables.SUBCATEGORY + "(" + SubcategoryEntry.COLUMN_SERVER_ID + ") ON DELETE CASCADE,"
                //+ "FOREIGN KEY (" + ProductEntry.COLUMN_BRAND_ID + ") REFERENCES "
                //    + Tables.BRAND + "(" + BrandEntry.COLUMN_SERVER_ID + ") ON DELETE SET NULL)";

        /*final String SQL_CREATE_TABLE_MULTIMEDIA = "CREATE TABLE " + Tables.MULTIMEDIA + " ("
                + MultimediaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MultimediaEntry.COLUMN_SERVER_ID + " INTEGER NOT NULL,"
                + MultimediaEntry.COLUMN_IMAGE_URL + " TEXT NOT NULL,"
                + MultimediaEntry.COLUMN_DISPLAY_ORDER + " INTEGER NOT NULL,"
                + MultimediaEntry.COLUMN_PRODUCT_ID + " INTEGER NOT NULL,"
                + "UNIQUE (" + MultimediaEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE)";*/

        final String SQL_CREATE_TABLE_CART = "CREATE TABLE " + Tables.CART + " ("
                + CartEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CartEntry.COLUMN_SERVER_ID + " INTEGER NOT NULL,"
                + CartEntry.COLUMN_PRODUCT_ID + " INTEGER NOT NULL,"
                + CartEntry.COLUMN_COLOR_SERVER_ID + " INTEGER NOT NULL,"
                + CartEntry.COLUMN_COLOR + " TEXT NOT NULL,"
                + CartEntry.COLUMN_SIZE_SERVER_ID + " INTEGER NOT NULL,"
                + CartEntry.COLUMN_SIZE + " TEXT NOT NULL,"
                + CartEntry.COLUMN_QUANTITY + " INTEGER NOT NULL,"
                + CartEntry.COLUMN_DATE_ADDED + " TEXT NOT NULL,"
                //+ CartEntry.COLUMN_LEAD_IMAGE_URL + " TEXT,"
                + "UNIQUE (" + CartEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE)";

        final String SQL_CREATE_TABLE_ADDRESS_BOOK = "CREATE TABLE " + Tables.ADDRESS_BOOK + " ("
                + AddressBookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AddressBookEntry.COLUMN_SERVER_ID + " INTEGER NOT NULL,"
                + AddressBookEntry.COLUMN_FULL_NAME + " TEXT NOT NULL,"
                + AddressBookEntry.COLUMN_ADDRESS_LINE_1 + " TEXT NOT NULL,"
                + AddressBookEntry.COLUMN_ADDRESS_LINE_2 + " TEXT,"
                + AddressBookEntry.COLUMN_CITY + " TEXT NOT NULL,"
                + AddressBookEntry.COLUMN_STATE + " TEXT NOT NULL,"
                + AddressBookEntry.COLUMN_ZIP_CODE + " TEXT NOT NULL,"
                + AddressBookEntry.COLUMN_COUNTRY + " TEXT NOT NULL,"
                + AddressBookEntry.COLUMN_PHONE_NUMBER + " TEXT NOT NULL,"
                + "UNIQUE (" + AddressBookEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE)";

        final String SQL_CREATE_TABLE_ORDER = "CREATE TABLE " + Tables.ORDER + " ("
                + OrderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + OrderEntry.COLUMN_SERVER_ID + " INTEGER NOT NULL,"
                + OrderEntry.COLUMN_REFERENCE + " TEXT NOT NULL,"
                + OrderEntry.COLUMN_QUANTITY + " INTEGER NOT NULL,"
                + OrderEntry.COLUMN_DATE + " TEXT NOT NULL,"
                + OrderEntry.COLUMN_SUBTOTAL + " REAL NOT NULL,"
                + OrderEntry.COLUMN_SHIPPING_PRICE + " REAL NOT NULL,"
                + OrderEntry.COLUMN_TAX + " REAL NOT NULL,"
                + OrderEntry.COLUMN_STATUS + " TEXT NOT NULL,"
                + "UNIQUE (" + OrderEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE)";

        db.execSQL(SQL_CREATE_TABLE_CATEGORY);
        db.execSQL(SQL_CREATE_TABLE_SUBCATEGORY);
        db.execSQL(SQL_CREATE_TABLE_BRAND);
        db.execSQL(SQL_CREATE_TABLE_PRODUCT);
        db.execSQL(SQL_CREATE_TABLE_CART);
        db.execSQL(SQL_CREATE_TABLE_ADDRESS_BOOK);
        db.execSQL(SQL_CREATE_TABLE_ORDER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.SUBCATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.BRAND);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.PRODUCT);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CART);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ADDRESS_BOOK);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ORDER);

        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Uncomment this to enable foreign key constraints.
        /*if (!db.isReadOnly()) {
            // Enable foreign key constraints.
            db.execSQL("PRAGMA foreign_keys = ON;");
        }*/
    }
}
