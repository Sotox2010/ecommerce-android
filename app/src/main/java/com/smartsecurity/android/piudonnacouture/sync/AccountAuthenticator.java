package com.smartsecurity.android.piudonnacouture.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.smartsecurity.android.piudonnacouture.Config;
import com.smartsecurity.android.piudonnacouture.model.AuthTokenResponse;
import com.smartsecurity.android.piudonnacouture.ui.AuthenticatorActivity;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import java.util.Arrays;

import retrofit.RetrofitError;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private static final String TAG = "AccountAuthenticator";

    public static final String ACCOUNT_TYPE = "com.jesussoto.ecommerce";
    public static final String ACCOUNT_NAME_SYNC = "Sync Account";
    public static final String AUTHTOKEN_TYPE = "com.jesussoto.ecommerce.access_token";

    private final Context mContext;

    public AccountAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "addAccount(response, accountType, authTokenType, requiredFeatures, options)");
        Log.d(TAG, "response: " + response.toString());
        Log.d(TAG, "accountType: " + accountType);
        Log.d(TAG, "authTokenType: " + authTokenType);
        Log.d(TAG, "requiredFeatures: " + (requiredFeatures != null ? Arrays.toString(requiredFeatures) : "(null)"));
        Log.d(TAG, "options: " + (options != null ? options.toString() : "(null)"));

        Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthenticatorActivity.EXTRA_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.EXTRA_IS_ADDING_NEW_ACCOUNT, true);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.v(TAG, "getAuthToken(response, account, authTokenType, options)");
        Log.v(TAG, "response: " + response.toString());
        Log.v(TAG, "account: " + account.toString());
        Log.v(TAG, "authTokenType: " + authTokenType);
        Log.v(TAG, "options: " + (options != null ? options.toString() : "(null)"));

        // If the caller requested an authToken type we don't support, then return an error
        if (!authTokenType.equals(AUTHTOKEN_TYPE)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        // Extract the username and password from the AccountManager, and ask
        // the server for an appropriate auth token.
        AccountManager manager = AccountManager.get(mContext);
        String authToken = manager.peekAuthToken(account, authTokenType);

        // If no AuthToken is present, obtain a new token using the refresh token.
        if (TextUtils.isEmpty(authToken)) {
            // We use the account password field to store the refresh token.
            String refreshToken = manager.getPassword(account);
            if (refreshToken != null) {
                try {
                    AuthTokenResponse tokenResponse = SyncUtils.sWebService.refreshAccessToken(
                            refreshToken, Config.OAUTH_CLIENT_ID, Config.OAUTH_CLIENT_SECRET,
                                    Config.OAUTH_GRANT_TYPE_REFRESH_TOKEN);

                    authToken = tokenResponse.getAccessToken();
                    refreshToken = tokenResponse.getRefreshToken();
                    manager.setPassword(account, refreshToken); // Update refresh token.
                } catch (RetrofitError error) {
                    error.printStackTrace();
                }
            }
        }

        // If we get an auth token, we return it.
        // This result automatically sets the auth token for this account.
        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.EXTRA_ACCOUNT_NAME, account.name);
        intent.putExtra(AuthenticatorActivity.EXTRA_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // null means we don't support multiple authToken types
        Log.v(TAG, "getAuthTokenLabel()");
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        Bundle bundle = new Bundle();
        bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        return bundle;
    }
}
