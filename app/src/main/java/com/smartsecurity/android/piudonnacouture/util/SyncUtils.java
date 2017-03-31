package com.smartsecurity.android.piudonnacouture.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.smartsecurity.android.piudonnacouture.Config;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.sync.AccountAuthenticator;
import com.smartsecurity.android.piudonnacouture.sync.WebService;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;

public class SyncUtils {
    private static final String TAG = "SyncUtils";

    // Interval at which to sync with the data with the server, in seconds.
    // We use 1 day in seconds as the sync frequency.
    public static final int SYNC_FREQUENCY = (int) TimeUnit.SECONDS.convert(1L, TimeUnit.DAYS);
    public static final int SYNC_FLEXTIME = SYNC_FREQUENCY / 12;

    public static final WebService sWebService = new RestAdapter.Builder()
            .setEndpoint(Config.ENDPOINT_URL)
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build()
            .create(WebService.class);

    //private static final long SYNC_FREQUENCY = 60 * 60;  // 1 hour (in seconds)
    //private static final String CONTENT_AUTHORITY = PiuDonnaContract.CONTENT_AUTHORITY;
    //private static final String PREF_SETUP_COMPLETE = "setup_complete";

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    /*public static Account createSyncAccount(Context context) {
        Log.d(TAG, "createSyncAccount()");
        boolean newAccount = false;
        //boolean setupComplete = PreferenceManager
        //        .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = getSyncAccount(context);
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            Log.d(TAG, "createSyncAccount() > New account created.");

            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, CONTENT_AUTHORITY, new Bundle(),SYNC_FREQUENCY);
            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount/* || !setupComplete* /) {
            syncImmediately(context);
            //PreferenceManager.getDefaultSharedPreferences(context).edit()
            //        .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }

        return account;
    }*/

    /**
     * Obtain a handle to the {@link android.accounts.Account} used for sync in this application.
     *
     * @return Handle to application's account (not guaranteed to resolve unless CreateSyncAccount()
     *         has been called)
     */
    public static Account getDefaultSyncAccount() {
        // Note: Normally the account name is set to the user's identity (username or email
        // address). However, since we aren't actually using any user accounts, it makes more sense
        // to use a generic string in this case.
        //
        // This string should *not* be localized. If the user switches locale, we would not be
        // able to locate the old account, and may erroneously register multiple accounts.
        final String accountName = AccountAuthenticator.ACCOUNT_NAME_SYNC;
        final String accountType = AccountAuthenticator.ACCOUNT_TYPE;
        return new Account(accountName, accountType);
    }


    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    /*public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = AccountManager.get(context);

        // Create the account type and default account
        Account newAccount = new Account(
                AccountAuthenticator.ACCOUNT_NAME_SYNC,
                AccountAuthenticator.ACCOUNT_TYPE);

        // If the password doesn't exist, the account doesn't exist
        if (accountManager.getPassword(newAccount) == null) {
            // Add the account and account type, no password or user data
            // If successful, return the Account object, otherwise report an error.
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            // If you don't set android:syncable="true" in
            // in your <provider> element in the manifest,
            // then call context.setIsSyncable(account, AUTHORITY, 1)
            // here.
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }*/

    /**
     * Helper method to create the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.
     *
     * @param context The context used to access the account service
     */
    public static void createDefaultSyncAccount(Context context) {
        Log.d(TAG, "createDefaultSyncAccount()");
        // Get an instance of the Android account manager
        AccountManager accountManager = AccountManager.get(context);

        // Create the account type and default account
        Account account = getDefaultSyncAccount();

        // Create account, if it's missing (either first run, or user has deleted account).
        // If successful, configure sync for this account.
        if (accountManager.addAccountExplicitly(account, null, null)) {
            
            // If you don't set android:syncable="true" in
            // in your <provider> element in the manifest,
            // then call context.setIsSyncable(account, AUTHORITY, 1)
            // here.
            onAccountCreated(account, context);
        }
    }

    public static void onAccountCreated(Account newAccount, Context context) {
        Log.d(TAG, "onAccountCreated(" + newAccount.toString() + ")");

        // Inform the system that this account is eligible for auto sync when the network is up.
        ContentResolver.setSyncAutomatically(newAccount, PiuDonnaContract.CONTENT_AUTHORITY, true);

        // Schedule the sync for periodic execution.
        configurePeriodicSync(newAccount, SYNC_FREQUENCY, SYNC_FLEXTIME);

        // Let's do a sync to get things started.
        syncImmediately(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    private static void configurePeriodicSync(Account account, int syncInterval, int flexTime) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // We can enable inexact timers in our periodic sync.
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, PiuDonnaContract.CONTENT_AUTHORITY)
                    .setExtras(Bundle.EMPTY)
                    .build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, PiuDonnaContract.CONTENT_AUTHORITY,
                    Bundle.EMPTY, syncInterval);
        }
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void syncImmediately(Context context) {
        Log.d(TAG, "syncImmediately()");

        Account activeAccount = AccountUtils.hasActiveAccount(context)
                ? AccountUtils.getActiveAccount(context)
                : getDefaultSyncAccount();

        // Disable sync backoff and ignore sync preferences. In other words... perform sync NOW!
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(activeAccount, PiuDonnaContract.CONTENT_AUTHORITY, extras);
    }

    public static void performInitialSync(Context context) {
        Log.d(TAG, "performInitialSync()");

        if (!AccountUtils.hasActiveAccount(context)) {
            createDefaultSyncAccount(context);
        }
    }

}
