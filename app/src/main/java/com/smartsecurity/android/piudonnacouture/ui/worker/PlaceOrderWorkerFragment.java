package com.smartsecurity.android.piudonnacouture.ui.worker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartsecurity.android.piudonnacouture.model.Address;
import com.smartsecurity.android.piudonnacouture.model.CreditCard;
import com.smartsecurity.android.piudonnacouture.model.RequestError;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.sync.AccountAuthenticator;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class PlaceOrderWorkerFragment extends Fragment {

    public static PlaceOrderWorkerFragment newInstance() {

        Bundle args = new Bundle();

        PlaceOrderWorkerFragment fragment = new PlaceOrderWorkerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnPlaceOrderResultCallback {
        void onPlaceOrderResult(String result);
    }

    private OnPlaceOrderResultCallback mPlaceOrderResultCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPlaceOrderResultCallback = (OnPlaceOrderResultCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPlaceOrderResultCallback = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    public void placeOrder(String accountName, Address address, CreditCard card) {
        new PlaceOrderTask().execute(accountName,
                address.getServerId().toString(),
                card.getCardHolder(),
                card.getNumber(),
                card.getCvv(),
                card.getExpYear(),
                card.getExpMonth());
    }

    private class PlaceOrderTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String addressId = params[1];
            String nameOnCard = params[2];
            String cardNumber = params[3];
            String cvc = params[4];
            String expMonth = params[5];
            String expYear = params[6];

            Account account = new Account(accountName, AccountAuthenticator.ACCOUNT_TYPE);
            AccountManager manager = AccountManager.get(getActivity());
            String authToken = manager.peekAuthToken(account, AccountAuthenticator.AUTHTOKEN_TYPE);
            String result = null;

            try {
                JsonObject object = SyncUtils.sWebService.placeOrder(authToken, accountName,
                        addressId, nameOnCard, cardNumber, cvc, expMonth, expYear);

                getContext().getContentResolver()
                        .delete(PiuDonnaContract.CartEntry.CONTENT_URI, null, null);

                //long newAddressId = object.get("id").getAsLong();

                //saveNewAddress(newAddressId, fullName, addressLine1, addressLine2, city, state,
                        //zipCode, country, phone);

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
            if (mPlaceOrderResultCallback != null) {
                mPlaceOrderResultCallback.onPlaceOrderResult(result);
            }
        }
    }

}
