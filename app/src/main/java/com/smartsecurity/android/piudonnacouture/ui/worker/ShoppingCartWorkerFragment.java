package com.smartsecurity.android.piudonnacouture.ui.worker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartsecurity.android.piudonnacouture.model.RequestError;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.sync.AccountAuthenticator;
import com.smartsecurity.android.piudonnacouture.util.DateUtils;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class ShoppingCartWorkerFragment extends Fragment {
    private static final String TAG = "ShoppingCartWorkerFragment";

    public static ShoppingCartWorkerFragment newInstance() {
        return new ShoppingCartWorkerFragment();
    }

    public interface OnAddToCartCallback {
        void onAddToCartResult(String result);
    }

    public interface OnRemoveFromCartCallback {
        void onRemoveFromCartResult(String result);
    }

    public interface OnUpdateCartItemCallback {
        void onUpdateCartItemResult(String result);
    }

    private static boolean sCartOperationInProgress = false;

    private OnAddToCartCallback mAddToCartCallback;
    private OnRemoveFromCartCallback mRemoveFromCartCallback;
    private OnUpdateCartItemCallback mUpdateCartItemCallback;
    private AsyncTask mCartOperationTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mAddToCartCallback = (OnAddToCartCallback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        try {
            mRemoveFromCartCallback = (OnRemoveFromCartCallback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        try {
            mUpdateCartItemCallback = (OnUpdateCartItemCallback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAddToCartCallback = null;
        mRemoveFromCartCallback = null;
        mUpdateCartItemCallback = null;
    }

    public void addToCart(String accountName, Long productId, String productCode, long colorId, String colorName, long sizeId, String sizeName, int quantity) {
        if (sCartOperationInProgress) {
            return;
        }

        new AddToCartTask().execute(
                accountName,
                Long.toString(productId),
                productCode,
                Long.toString(colorId),
                colorName,
                Long.toString(sizeId),
                sizeName,
                Integer.toString(quantity)
        );
    }

    @SuppressWarnings("unchecked")
    public void removeFromCart(String accountName, String productCode, long cartItemIServerId) {
        if (sCartOperationInProgress) {
            return;
        }

        new RemoveFromCartTask().execute(
                accountName,
                productCode,
                Long.toString(cartItemIServerId));
    }

    public void updateCartItem(String accountName, String productCode, long cartItemServerId, int newQuantity) {
        if (sCartOperationInProgress) {
            return;
        }

        new UpdateCartItemTask().execute(
                accountName,
                productCode,
                Long.toString(cartItemServerId),
                Integer.toString(newQuantity));
    }

    /*private abstract class ShoppingCartTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        protected abstract String executeServerOperation() throws RetrofitError;
    }

    private class AddToCartTask extends ShoppingCartTask {

        @Override
        protected String executeServerOperation() throws RetrofitError {
            return null;
        }
    }*/

    private class AddToCartTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sCartOperationInProgress = true;
        }

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String productId = params[1];
            String productCode = params[2];
            String colorServerId = params[3];
            String color = params[4];
            String sizeServerId = params[5];
            String size = params[6];
            String quantity = params[7];

            Account account = new Account(accountName, AccountAuthenticator.ACCOUNT_TYPE);
            AccountManager manager = AccountManager.get(getActivity());
            String authToken = manager.peekAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE);
            String result = null;

            try {
                JsonObject object = SyncUtils.sWebService.addToCart(
                        authToken, accountName, productCode, colorServerId, sizeServerId, quantity);

                long cartItemServerId = object.get("id").getAsLong();
                saveAddedItemToDatabase(cartItemServerId, productId, colorServerId, color, sizeServerId, size, quantity);

            } catch (RetrofitError error) {

                if (error.getKind() == RetrofitError.Kind.NETWORK) {
                    result = "No connection.";
                } else if (error.getKind() == RetrofitError.Kind.HTTP) {
                    Response response = error.getResponse();
                    String json = new String(((TypedByteArray) response.getBody()).getBytes());
                    RequestError reqError = new Gson().fromJson(json, RequestError.class);
                    result = reqError.getDescription();
                } else {
                    result = error.getLocalizedMessage();
                }
            }

            /*try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            sCartOperationInProgress = false;

            if (mAddToCartCallback != null) {
                mAddToCartCallback.onAddToCartResult(result);
            }
        }

        private void saveAddedItemToDatabase(long serverId, String productId, String colorServerId, String color, String sizeServerId, String size, String quantity) {
            ContentValues values = new ContentValues();
            values.put(PiuDonnaContract.CartEntry.COLUMN_SERVER_ID, serverId);
            values.put(PiuDonnaContract.CartEntry.COLUMN_PRODUCT_ID, productId);
            values.put(PiuDonnaContract.CartEntry.COLUMN_COLOR_SERVER_ID, colorServerId);
            values.put(PiuDonnaContract.CartEntry.COLUMN_COLOR, color);
            values.put(PiuDonnaContract.CartEntry.COLUMN_SIZE_SERVER_ID, sizeServerId);
            values.put(PiuDonnaContract.CartEntry.COLUMN_SIZE, size);
            values.put(PiuDonnaContract.CartEntry.COLUMN_QUANTITY, quantity);
            values.put(PiuDonnaContract.CartEntry.COLUMN_DATE_ADDED, DateUtils.getCurrentRfc3339Date());

            getContext().getContentResolver().insert(PiuDonnaContract.CartEntry.CONTENT_URI, values);
        }
    }

    private class RemoveFromCartTask extends AsyncTask<String, Void, String> {

        public RemoveFromCartTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sCartOperationInProgress = true;
        }

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String productCode = params[1];
            String cartItemId = params[2];

            Account account = new Account(accountName, AccountAuthenticator.ACCOUNT_TYPE);
            AccountManager manager = AccountManager.get(getActivity());
            String authToken = manager.peekAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE);
            String result = null;

            try {
                SyncUtils.sWebService.removeFromCart(authToken, accountName, productCode, cartItemId);
                removeItemFromDatabase(cartItemId);

            } catch (RetrofitError error) {

                if (error.getKind() == RetrofitError.Kind.NETWORK) {
                    result = "No connection.";
                } else if (error.getKind() == RetrofitError.Kind.HTTP) {
                    Response response = error.getResponse();
                    String json = new String(((TypedByteArray) response.getBody()).getBytes());
                    RequestError reqError = new Gson().fromJson(json, RequestError.class);
                    result = reqError.getDescription();
                } else {
                    result = error.getLocalizedMessage();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            sCartOperationInProgress = false;

            if (mRemoveFromCartCallback != null) {
                mRemoveFromCartCallback.onRemoveFromCartResult(result);
            }
        }

        private void removeItemFromDatabase(String cartItemId) {
            getContext().getContentResolver().delete(
                    PiuDonnaContract.CartEntry.buildCartItemUri(Long.parseLong(cartItemId)), null, null);
        }

    }

    private class UpdateCartItemTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sCartOperationInProgress = true;
        }

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String productCode = params[1];
            String cartItemId = params[2];
            String newQuantity = params[3];

            Account account = new Account(accountName, AccountAuthenticator.ACCOUNT_TYPE);
            AccountManager manager = AccountManager.get(getActivity());
            String authToken = manager.peekAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE);
            String result = null;

            try {
                SyncUtils.sWebService.updateCartItem(
                        authToken, accountName, cartItemId, newQuantity);
                updateItemQuantityLocally(cartItemId, newQuantity);

            } catch (RetrofitError error) {

                if (error.getKind() == RetrofitError.Kind.NETWORK) {
                    result = "No connection.";
                } else if (error.getKind() == RetrofitError.Kind.HTTP) {
                    Response response = error.getResponse();
                    String json = new String(((TypedByteArray) response.getBody()).getBytes());
                    RequestError reqError = new Gson().fromJson(json, RequestError.class);
                    result = reqError.getDescription();
                } else {
                    result = error.getLocalizedMessage();
                }
            }

            /*try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            sCartOperationInProgress = false;

            if (mUpdateCartItemCallback != null) {
                mUpdateCartItemCallback.onUpdateCartItemResult(result);
            }
        }

        private void updateItemQuantityLocally(String cartItemId, String newQuantity) {
            ContentValues values = new ContentValues();
            values.put(PiuDonnaContract.CartEntry.COLUMN_QUANTITY, newQuantity);

            getContext().getContentResolver().update(
                    PiuDonnaContract.CartEntry.buildCartItemUri(Long.parseLong(cartItemId)),
                            values, null, null);
        }
    }
}
