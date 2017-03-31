package com.smartsecurity.android.piudonnacouture.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.smartsecurity.android.piudonnacouture.sync.AccountAuthenticator;

import java.io.IOException;

public class AccountUtils {
    private static final String TAG = "AccountUtils";

    public static final String PREF_ACTIVE_ACCOUNT_NAME = "pref_active_account_name";

    public static void setActiveAccountName(Context context, String accountName) {
        Log.d(TAG, "setActiveAccountName: " + accountName);
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREF_ACTIVE_ACCOUNT_NAME, accountName)
                .apply();
    }

    public static boolean hasActiveAccount(Context context) {
        return getActiveAccountName(context) != null;
    }

    public static String getActiveAccountName(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_ACTIVE_ACCOUNT_NAME, null);
    }

    public static Account getActiveAccount(Context context) {
        String accountName = getActiveAccountName(context);
        if (accountName != null) {
            return new Account(accountName, AccountAuthenticator.ACCOUNT_TYPE);
        } else {
            return null;
        }
    }

    public static void removeAccount(Context context, final Account account) {
        Log.d(TAG, "removeAccount(" + account.toString() + ")");
        AccountManager manager = AccountManager.get(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            manager.removeAccount(account, (AccountManagerFuture<Boolean> future) -> {
                try {
                    boolean removed = future.getResult();
                    Log.d(TAG, "Remove account: " +
                            (removed ? "true" : "false, the account was not present"));
                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    e.printStackTrace();
                }
            }, null);
        } else {
            boolean removed = manager.removeAccountExplicitly(account);
            Log.d(TAG, "Remove account: " +
                    (removed ? "true" : "false, the account was not present"));
        }
    }

    public static void removeExistingAccounts(Context context) {
        Log.d(TAG, "removeExistingAccounts() ");

        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
        for (Account account : accounts) {
            // In case the default Sync Account is added to the Account Manager, we skip its deletion.
            if (account.name.equals(AccountAuthenticator.ACCOUNT_NAME_SYNC)) {
                continue;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                manager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                    @Override
                    public void run(AccountManagerFuture<Boolean> future) {
                        try {
                            Log.d(TAG, "Removing account " + future.getResult());
                        } catch (OperationCanceledException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (AuthenticatorException e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
            } else {
                boolean result = manager.removeAccountExplicitly(account);
                Log.d(TAG, "Removing account " + account.name + ": " + result);
            }
        }
    }
}
