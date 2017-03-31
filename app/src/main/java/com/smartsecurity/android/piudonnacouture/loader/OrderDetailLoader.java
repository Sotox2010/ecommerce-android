package com.smartsecurity.android.piudonnacouture.loader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartsecurity.android.piudonnacouture.model.RequestError;
import com.smartsecurity.android.piudonnacouture.sync.AccountAuthenticator;
import com.smartsecurity.android.piudonnacouture.util.AccountUtils;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class OrderDetailLoader extends AsyncTaskLoader<JsonObject> {

    private static final String TAG = "OrderDetailLoader";

    private final Context mContext;
    private long mOrderId;
    private String mErrorText;
    private JsonObject mCache;

    public OrderDetailLoader(Context context, long orderId) {
        super(context);
        mContext = context;
        mOrderId = orderId;
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

        Account account = AccountUtils.getActiveAccount(mContext);
        if (account == null) {
            mErrorText = "No active account is present";
            return null;
        }

        AccountManager manager = AccountManager.get(mContext);
        String authToken = manager.peekAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE);

        try {
            jsonObject = SyncUtils.sWebService.getOrderDetails(authToken, account.name, Long.toString(mOrderId));

        } catch (RetrofitError error) {

            if (error.getKind() == RetrofitError.Kind.NETWORK) {
                mErrorText = "No connection.";
            } else if (error.getKind() == RetrofitError.Kind.HTTP) {
                Response response = error.getResponse();
                String json = new String(((TypedByteArray) response.getBody()).getBytes());
                RequestError reqError = new Gson().fromJson(json, RequestError.class);
                mErrorText = reqError.getDescription();
            } else {
                mErrorText = error.getLocalizedMessage();
            }
        }

        return jsonObject;
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
            if (!TextUtils.isEmpty(mErrorText)){
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
