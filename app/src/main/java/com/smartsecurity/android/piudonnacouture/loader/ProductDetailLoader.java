package com.smartsecurity.android.piudonnacouture.loader;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.smartsecurity.android.piudonnacouture.model.ProductColor;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

public class ProductDetailLoader extends AsyncTaskLoader<JsonObject> {

    private static final String TAG = "ProductDetailLoader";

    private String mProductCode;
    private JsonObject mCache;

    public ProductDetailLoader(Context context, String code) {
        super(context);
        mProductCode = code;
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading()");

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
        JsonObject jsonObject = null;

        try {
            jsonObject = SyncUtils.sWebService.getProductDetails(mProductCode);

            Gson gson = new Gson();
            JsonArray colorsArray  = jsonObject.get("colors").getAsJsonArray();
            JsonArray multimediaArray  = jsonObject.get("multimedia").getAsJsonArray();
            jsonObject.remove("colors");
            jsonObject.remove("multimedia");

            Type colorListType = new TypeToken<List<ProductColor>>(){}.getType();
            List<ProductColor> colors = gson.fromJson(colorsArray, colorListType);

            for (ProductColor color : colors) {
                List<String> images = filterByColorId(multimediaArray, color.getServerId());
                color.setImageUrls(images);
            }

            JsonElement colorElements = gson.toJsonTree(colors);
            jsonObject.add("colors", colorElements);

            Log.w(TAG, "loadInBackground: json: " + jsonObject.toString());

        } catch (RetrofitError error) {
            error.printStackTrace();
        }

        return jsonObject;
    }

    private List<String> filterByColorId(JsonArray multimedia, Long colorId) {
        List<String> match = new ArrayList<>();

        for (JsonElement element : multimedia) {
            JsonObject object = element.getAsJsonObject();
            if (object.get("colorId").getAsLong() == colorId) {
                match.add(object.get("imageURL").getAsString());
            }
        }

        return match;
    }

    @Override
    public void deliverResult(JsonObject jsonObject) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            mCache = null;
            return;
        }

        mCache = jsonObject;

        if (isStarted()) {
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
