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
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class AddressBookWorkerFragment extends Fragment {
    private static final String TAG = "AddressBookWorkerFragment";

    public static AddressBookWorkerFragment newInstance() {
        return new AddressBookWorkerFragment();
    }

    public interface OnAddNewAddressCallback {
        void onAddNewAddressResult(String result);
    }

    public interface OnRemoveAddressCallback {
        void onRemoveAddressResult(String result);
    }

    public interface OnUpdateAddressCallback {
        void onUpdateAddressResult(String result);
    }

    private static boolean sAddressBookOperationInProgress = false;

    private OnAddNewAddressCallback mAddNewAddressCallback;
    private OnRemoveAddressCallback mRemoveAddressCallback;
    private OnUpdateAddressCallback mUpdateAddressCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mAddNewAddressCallback = (OnAddNewAddressCallback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        try {
            mRemoveAddressCallback = (OnRemoveAddressCallback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        try {
            mUpdateAddressCallback = (OnUpdateAddressCallback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAddNewAddressCallback = null;
        mRemoveAddressCallback = null;
        mUpdateAddressCallback = null;
    }

    public void addNewAddress(String accountName, String fullName, String addressLine1, String addressLine2, String city, String state, String zipCode, String country, String phone) {
        if (sAddressBookOperationInProgress) {
            return;
        }

        new AddNewAddressTask().execute(accountName, fullName, addressLine1, addressLine2, city,
                state, zipCode, country, phone);
    }

    public void removeAddress(String accountName, long addressServerId) {
        if (sAddressBookOperationInProgress) {
            return;
        }

        new RemoveAddressTask().execute(accountName, Long.toString(addressServerId));
    }

    public void updateAddress(String accountName, long addressServerId, String fullName, String addressLine1, String addressLine2, String city, String state, String zipCode, String country, String phone) {
        if (sAddressBookOperationInProgress) {
            return;
        }

        new UpdateCartItemTask().execute(accountName, Long.toString(addressServerId), fullName,
                addressLine1, addressLine2, city, state, zipCode, country, phone);
    }

    private class AddNewAddressTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sAddressBookOperationInProgress = true;
        }

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String fullName = params[1];
            String addressLine1 = params[2];
            String addressLine2 = params[3];
            String city = params[4];
            String state = params[5];
            String zipCode = params[6];
            String country = params[7];
            String phone = params[8];

            Account account = new Account(accountName, AccountAuthenticator.ACCOUNT_TYPE);
            AccountManager manager = AccountManager.get(getActivity());
            String authToken = manager.peekAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE);
            String result = null;

            try {
                JsonObject object = SyncUtils.sWebService.addNewAddress(authToken, accountName,
                        fullName, addressLine1, addressLine2, city, state, zipCode, country, phone);

                long newAddressId = object.get("id").getAsLong();

                saveNewAddress(newAddressId, fullName, addressLine1, addressLine2, city, state,
                        zipCode, country, phone);

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
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            sAddressBookOperationInProgress = false;

            if (mAddNewAddressCallback != null) {
                mAddNewAddressCallback.onAddNewAddressResult(result);
            }
        }

        private void saveNewAddress(long serverId, String fullName, String addressLine1, String addressLine2, String city, String state, String zipCode, String country, String phone) {
            ContentValues values = new ContentValues();
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_SERVER_ID, serverId);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_FULL_NAME, fullName);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_ADDRESS_LINE_1, addressLine1);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_ADDRESS_LINE_2, addressLine2);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_CITY, city);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_STATE, state);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_ZIP_CODE, zipCode);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_COUNTRY, country);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_PHONE_NUMBER, phone);

            getContext().getContentResolver().insert(
                    PiuDonnaContract.AddressBookEntry.CONTENT_URI, values);
        }
    }

    private class RemoveAddressTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sAddressBookOperationInProgress = true;
        }

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String addressServerId = params[1];

            Account account = new Account(accountName, AccountAuthenticator.ACCOUNT_TYPE);
            AccountManager manager = AccountManager.get(getActivity());
            String authToken = manager.peekAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE);
            String result = null;

            try {
                SyncUtils.sWebService.removeAddress(authToken, accountName, addressServerId);
                removeAddressFromDatabase(addressServerId);

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
            sAddressBookOperationInProgress = false;

            if (mRemoveAddressCallback != null) {
                mRemoveAddressCallback.onRemoveAddressResult(result);
            }
        }

        private void removeAddressFromDatabase(String addressServerId) {
            getContext().getContentResolver().delete(PiuDonnaContract.AddressBookEntry
                    .buildAddressUri(Long.parseLong(addressServerId)), null, null);
        }

    }

    private class UpdateCartItemTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sAddressBookOperationInProgress = true;
        }

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String addressServerId = params[1];
            String fullName = params[2];
            String addressLine1 = params[3];
            String addressLine2 = params[4];
            String city = params[5];
            String state = params[6];
            String zipCode = params[7];
            String country = params[8];
            String phone = params[9];

            Account account = new Account(accountName, AccountAuthenticator.ACCOUNT_TYPE);
            AccountManager manager = AccountManager.get(getActivity());
            String authToken = manager.peekAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE);
            String result = null;

            try {
                SyncUtils.sWebService.updateAddress(
                        authToken, accountName, addressServerId, fullName, addressLine1, addressLine2, city, state, zipCode, country, phone);

                saveUpdatedAddress(addressServerId, fullName, addressLine1, addressLine2, city,
                        state, zipCode, country, phone);

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
            sAddressBookOperationInProgress = false;

            if (mUpdateAddressCallback != null) {
                mUpdateAddressCallback.onUpdateAddressResult(result);
            }
        }

        private void saveUpdatedAddress(String serverId, String fullName, String addressLine1, String addressLine2, String city, String state, String zipCode, String country, String phone) {
            ContentValues values = new ContentValues();
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_FULL_NAME, fullName);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_ADDRESS_LINE_1, addressLine1);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_ADDRESS_LINE_2, addressLine2);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_CITY, city);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_STATE, state);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_ZIP_CODE, zipCode);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_COUNTRY, country);
            values.put(PiuDonnaContract.AddressBookEntry.COLUMN_PHONE_NUMBER, phone);

            getContext().getContentResolver().update(PiuDonnaContract.AddressBookEntry
                            .buildAddressUri(Long.parseLong(serverId)), values, null, null);
        }
    }
}
