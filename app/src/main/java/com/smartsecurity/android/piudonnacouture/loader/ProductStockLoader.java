package com.smartsecurity.android.piudonnacouture.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import retrofit.RetrofitError;

public class ProductStockLoader extends AsyncTaskLoader<JsonObject> {

    private static final String LOG_TAG = "ProductStockLoader";

    private JsonObject mCache;
    private String mErrorText;
    private String mProductCode;
    private long mColorId;
    private long mSizeId;

    public ProductStockLoader(Context context, String productCode, long colorId, long sizeId) {
        super(context);
        this.mProductCode = productCode;
        this.mColorId = colorId;
        this.mSizeId = sizeId;
    }

    @Override
    protected void onStartLoading() {
        Log.d(LOG_TAG, "onStartLoading()");

        if (mCache != null) {
            // Deliver cached data.
            deliverResult(mCache);
        } else {
            // We have no data, so start loading it.
            forceLoad();
        }
    }

    @Override
    public JsonObject loadInBackground() {
        JsonObject stockData = null;

        try {
            stockData = SyncUtils.sWebService.getProductStock(mProductCode, mColorId, mSizeId);
        } catch (RetrofitError error) {
            error.printStackTrace();

            if (error.getKind() == RetrofitError.Kind.NETWORK) {
                mErrorText = "No connection";
            } else if (error.getKind() == RetrofitError.Kind.HTTP) {
                // TODO: parse the response body into an RequestError object.
                mErrorText = "Hello";
            } else {
                mErrorText = "Unknown error";
            }

        }

        return stockData;
    }

    @Override
    public void deliverResult(JsonObject data) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            mCache = null;
            return;
        }

        mCache = data;

        if (isStarted()) {
            if (!TextUtils.isEmpty(mErrorText)) {
                Toast.makeText(getContext(), mErrorText, Toast.LENGTH_LONG).show();
                mErrorText = null;
            }

            super.deliverResult(mCache);
        }
    }


    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
        mCache = null;
    }
}
