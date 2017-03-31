package com.smartsecurity.android.piudonnacouture.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.sync.AccountAuthenticator;
import com.smartsecurity.android.piudonnacouture.util.AccountUtils;
import com.smartsecurity.android.piudonnacouture.util.PrefUtils;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    private static final String TAG_FRAGMENT_STORE_HOME = "fragment_store_home";
    private static final String TAG_FRAGMENT_BLOG_FEED = "fragment_home";
    private static final String TAG_FRAGMENT_WISHLIST = "fragment_wishlist";
    private static final String TAG_FRAGMENT_ORDER_HISTORY = "fragment_order_history";
    private static final String TAG_FRAGMENT_ADDRESS_BOOK = "fragment_address_book";

    private static final String STATE_CURRENT_NAVDRAWER_ITEM = "state_current_navdrawer_item";
    private static final String STATE_CURRENT_DISPLAY_FRAGMENT = "state_current_display_fragment";

    /**
     * Symbols for navdrawer items (indices must correspond to array below). This is
     * not a list of items that are necessarily *present* in the Nav Drawer; rather,
     * it's a list of all possible items.
     */
    private static final int NAVDRAWER_ITEM_STORE_HOME = 0;
    private static final int NAVDRAWER_ITEM_BLOG_FEED = 1;
    private static final int NAVDRAWER_ITEM_WISHLIST = 2;
    private static final int NAVDRAWER_ITEM_ORDER_HISTORY = 3;
    private static final int NAVDRAWER_ITEM_ADDRESS_BOOK = 4;

    // Titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[] {
            R.string.title_fragment_store_home,
            R.string.title_fragment_blog_feed,
            R.string.title_fragment_wishlist,
            R.string.title_fragment_order_history,
            R.string.title_fragment_address_book
    };

    // Fragment classes for navdrawer items (indices must correspond to the above)
    private static final Class<?>[] NAVDRAWER_FRAGMENT_CLASS = new Class<?>[] {
            HomeFragment.class,
            BlogFeedFragment.class,
            null,
            OrderHistoryFragment.class,
            AddressBookFragment.class
    };

    // Tags for navdrawer fragments (indices must correspond to the above)
    private static final String[] NAVDRAWER_FRAGMENT_TAG = new String[] {
            TAG_FRAGMENT_STORE_HOME,
            TAG_FRAGMENT_BLOG_FEED,
            TAG_FRAGMENT_WISHLIST,
            TAG_FRAGMENT_ORDER_HISTORY,
            TAG_FRAGMENT_ADDRESS_BOOK
    };

    private Toolbar mToolbarActionBar;
    private ImageView mToolbarLogoView;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private TextView mShoppingCartBadge;
    private Menu mOptionsMenu;
    private View mContainer;
    private View mNavigationHeaderView;
    private View.OnClickListener mNavigationHeaderClickListener;

    private int mCurrentNavDrawerItem;
    private Fragment mCurrentFragment;

    private Object mSyncObserverHandle;
    private int mAppBarElevation;

    private Handler mHandler = new Handler();

    interface CartCountQuery {
        int LOADER_ID = 0x1;
        Uri URI = PiuDonnaContract.CartEntry.CONTENT_URI;
        String[] PROJECTION = {
                PiuDonnaContract.CartEntry._ID,
                PiuDonnaContract.CartEntry.COLUMN_QUANTITY
        };

        int COLUMN_ID = 0;
        int COLUMN_QUANTITY = 1;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In the first launch, the Authenticator Activity should be displayed
        // unless an account was added manually via system account settings.
        if (PrefUtils.isFirstLaunch(this)) {
            PrefUtils.markFirstLaunchDone(this);
            if (!AccountUtils.hasActiveAccount(this)) {
                Intent intent = new Intent(this, AuthenticatorActivity.class);
                intent.putExtra(AuthenticatorActivity.EXTRA_IS_FIRST_LAUNCH, true);
                startActivity(intent);
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_main);

        mToolbarActionBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbarActionBar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(
                new DrawerArrowDrawable(getSupportActionBar().getThemedContext()));

        mToolbarLogoView = (ImageView) mToolbarActionBar.findViewById(R.id.toolbar_logo_view);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mContainer = findViewById(R.id.container);

        mAppBarElevation = getResources().getDimensionPixelSize(R.dimen.appbar_elevation);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationHeaderView = mNavigationView.getHeaderView(0);

        setupNavigationView();
        setupNavigationViewHeader();
        updateNavigationView();

        // Restore the current display fragment or create the default HomeFragment to display it.
        if (savedInstanceState != null) {
            Log.i(TAG, "onCreate: restoring current fragment");
            FragmentManager fm = getSupportFragmentManager();
            mCurrentFragment = fm.getFragment(savedInstanceState, STATE_CURRENT_DISPLAY_FRAGMENT);

            fm.beginTransaction()
                    .attach(mCurrentFragment)
                    .commit();
        } else {
            Log.i(TAG, "onCreate: creating brand new HomeFragment.");
            mCurrentFragment = HomeFragment.newInstance();
            mCurrentNavDrawerItem = NAVDRAWER_ITEM_STORE_HOME;

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, mCurrentFragment, TAG_FRAGMENT_STORE_HOME)
                    .commit();
            ViewCompat.setElevation(mContainer, mAppBarElevation);
        }

        SyncUtils.performInitialSync(this);

        //String url = "content://com.smartsecurity.android.piudonnacouture.provider/cart/1?paramone=true&paramtwo=7";
        //Uri uri = Uri.parse(url);
        /*Log.i(TAG, "scheme: " + uri.getScheme());
        Log.i(TAG, "authority: " + uri.getAuthority());
        Log.i(TAG, "host: " + uri.getHost());
        Log.i(TAG, "segments 0: " + uri.getPathSegments());
        Log.i(TAG, "base: " + uri.getScheme() + "://" + uri.getAuthority());
        Log.i(TAG, "without id: " + uri.toString().substring(0, uri.toString().lastIndexOf('/')));*/

        //Log.i(TAG, "uri: " + uri.toString());
        //Log.i(TAG, "removeLastSegmentPath: " + PiuDonnaProvider.removeLastUriPathSegment(uri).toString());
    }

    private void setupNavigationView() {
        mNavigationView.setNavigationItemSelectedListener((MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.nav_item_store_home:
                    navigateToFragment(NAVDRAWER_ITEM_STORE_HOME);
                    break;
                case R.id.nav_item_blog_feed:
                    navigateToFragment(NAVDRAWER_ITEM_BLOG_FEED);
                    break;
                /*case R.id.nav_item_wishlist:
                    break;*/
                case R.id.nav_item_order_history:
                    navigateToFragment(NAVDRAWER_ITEM_ORDER_HISTORY);
                    break;
                case R.id.nav_item_address_book:
                    navigateToFragment(NAVDRAWER_ITEM_ADDRESS_BOOK);
                    break;
                case R.id.nav_action_about: {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.piudonnacouture.com/"));
                    startActivity(intent);
                    break;
                }
                case R.id.nav_action_help: {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.piudonnacouture.com/customerSupport/faq"));
                    startActivity(intent);
                    break;
                }
                /*case R.id.nav_action_settings:
                    break;*/
                case R.id.nav_action_sign_out:
                    showSignOutDialog();
                    break;
            }

            if (item.getGroupId() != R.id.actions) {
                item.setChecked(true);
            }

            mDrawerLayout.closeDrawers();

            return true;
        });
    }

    private void setupNavigationViewHeader() {
        final AccountManagerCallback<Bundle> callback = future -> {
            try {
                Log.d(TAG, "addAccount: " + future.getResult().toString());
                updateNavigationView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        mNavigationHeaderClickListener = (View view) -> {
            final AccountManager manager = AccountManager.get(MainActivity.this);
            manager.addAccount(AccountAuthenticator.ACCOUNT_TYPE,
                    AccountAuthenticator.AUTHTOKEN_TYPE, null, null, MainActivity.this,
                            callback, null);

            mDrawerLayout.closeDrawers();
        };
    }

    private void updateNavigationView() {
        boolean hasActiveAccount = AccountUtils.hasActiveAccount(this);
        int padding = hasActiveAccount ? 0 : getResources().getDimensionPixelSize(R.dimen.spacing_large);
        int compoundDrawableResId = hasActiveAccount ? 0 : R.drawable.ic_person_white;

        // Update navigation menu items.
        Menu navigationMenu = mNavigationView.getMenu();
        navigationMenu.findItem(R.id.nav_action_sign_out).setVisible(hasActiveAccount);
        navigationMenu.setGroupVisible(R.id.user_actions, hasActiveAccount);

        // FIXME: Use real user information for header view (we don't store user info since we use OAuth)

        // Update the header tile to show an icon or text instead.
        TextView tile = ((TextView) mNavigationHeaderView.findViewById(R.id.header_tile_view));
        tile.setText(hasActiveAccount ? "JD" : null);
        tile.setPadding(padding, padding, padding, padding);
        tile.setCompoundDrawablesWithIntrinsicBounds(0, compoundDrawableResId, 0, 0);

        // Update the 'name' and 'email' text views.
        ((TextView) mNavigationHeaderView.findViewById(R.id.header_full_name))
                .setText(hasActiveAccount ? "John Doe" : getString(R.string.guest_user));
        ((TextView) mNavigationHeaderView.findViewById(R.id.header_email)).setText(hasActiveAccount
                ? AccountUtils.getActiveAccountName(this) : getString(R.string.tap_to_sign_in));

        // Set the header clickable if no user is logged in so the guest could sign in.
        mNavigationHeaderView.setOnClickListener(
                hasActiveAccount ? null : mNavigationHeaderClickListener);
        mNavigationHeaderView.setClickable(!hasActiveAccount);
    }

    private void switchFragments(Class<?> fragmentClass, String tag) {
        try {
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentByTag(tag);

            if (fragment == null) {
                Log.i(TAG, "switchFragments: Instantiating new " + fragmentClass.getSimpleName());
                fragment = (Fragment) fragmentClass.newInstance();
                fm.beginTransaction()
                        .detach(mCurrentFragment)
                        .add(R.id.container, fragment, tag)
                        .commit();
            } else {
                Log.i(TAG, "switchFragments: " + fragmentClass.getSimpleName() + " found on FragmentManager, attaching...");
                fm.beginTransaction()
                        .detach(mCurrentFragment)
                        .attach(fragment)
                        .commit();
            }

            mCurrentFragment = fragment;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateAppBarTitle(int navDrawerItem) {
        // We only show the logo view if the selected item is the Store Home, in any other case it
        // should be gone.
        mToolbarLogoView.setVisibility(navDrawerItem == NAVDRAWER_ITEM_STORE_HOME
                ? View.VISIBLE : View.GONE);

        mHandler.post(() -> mToolbarActionBar.setTitle(NAVDRAWER_TITLE_RES_ID[navDrawerItem]));

        // Update fragments container elevation. When home is selected the container should match
        // the app bar elevation so the tabs in the home fragment 'merge' with the app bar. In any
        // other case, the container should not have elevation.
        ViewCompat.setElevation(mContainer, navDrawerItem == NAVDRAWER_ITEM_STORE_HOME
                ? mAppBarElevation : 0);
    }

    private void navigateToFragment(int navDrawerItem) {
        switchFragments(NAVDRAWER_FRAGMENT_CLASS[navDrawerItem], NAVDRAWER_FRAGMENT_TAG[navDrawerItem]);
        updateAppBarTitle(navDrawerItem);
        mCurrentNavDrawerItem = navDrawerItem;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState()");
        if (mCurrentFragment != null) {
            Log.i(TAG, "onSaveInstanceState: saving current fragment");
            getSupportFragmentManager().putFragment(
                    outState, STATE_CURRENT_DISPLAY_FRAGMENT, mCurrentFragment);
        }

        outState.putInt(STATE_CURRENT_NAVDRAWER_ITEM, mCurrentNavDrawerItem);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);

        mCurrentNavDrawerItem = savedInstanceState.getInt(STATE_CURRENT_NAVDRAWER_ITEM);
        updateAppBarTitle(mCurrentNavDrawerItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        mOptionsMenu = menu;

        // Get a reference to the "badge" view of the shopping cart action.
        final MenuItem cartItem = menu.findItem(R.id.action_shopping_cart);
        View cartActionView = cartItem.getActionView();
        cartActionView.setOnClickListener(v -> onOptionsItemSelected(cartItem));

        mShoppingCartBadge = (TextView) cartActionView.findViewById(R.id.shopping_cart_badge);

        // This query changes the counter of the shopping cart badge, which is not available until
        // the menu is inflated, so we must wait until onCreateOptionsMenu() to start the loader.
        getSupportLoaderManager().initLoader(CartCountQuery.LOADER_ID, null, this);

        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_refresh:
                SyncUtils.syncImmediately(this);
                return true;
            case R.id.action_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.action_shopping_cart:
                startActivity(new Intent(this, ShoppingCartActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Close the navigation drawer if open.
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CartCountQuery.LOADER_ID) {
            return new CursorLoader(this,
                    CartCountQuery.URI,
                    CartCountQuery.PROJECTION,
                    null, null, null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CartCountQuery.LOADER_ID) {
            int count = 0;
            if (data.moveToFirst()) {
                do {
                    count += data.getInt(CartCountQuery.COLUMN_QUANTITY);
                } while (data.moveToNext());
            }

            setShoppingCartBadgeCount(count);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == CartCountQuery.LOADER_ID) {
            setShoppingCartBadgeCount(0);
        }
    }

    private void setShoppingCartBadgeCount(int count) {
        boolean shouldHide = count <= 0;
        mShoppingCartBadge.setText(count > 0 && count < 10 ? Integer.toString(count) : "9+");
        mShoppingCartBadge.setVisibility(shouldHide ? View.GONE : View.VISIBLE);
    }

    private void showSignOutDialog() {
        DialogFragment dialog = new SignOutDialogFragment();
        dialog.show(getSupportFragmentManager(), "signOutDialog");
    }

    public void performSignOut() {
        if (!AccountUtils.hasActiveAccount(this)) {
            return;
        }

        // Remove the current active account from the account manager and the preferences.
        AccountUtils.removeAccount(this, AccountUtils.getActiveAccount(this));
        AccountUtils.setActiveAccountName(this, null);

        // Perform immediate sync with the default sync account.
        SyncUtils.performInitialSync(this);

        // Update que navigation view items and header according to the user account status.
        updateNavigationView();

        // Manually switch to the Home Fragment, to prevent the now non-logged user from viewing a
        // fragment only visible to logged users.
        navigateToFragment(NAVDRAWER_ITEM_STORE_HOME);
        mNavigationView.setCheckedItem(R.id.nav_item_store_home);

        // TODO: Completely remove logged user fragments from the FragmentManager.
    }

    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occurring, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRefreshActionButtonState(boolean refreshing) {
        if (mOptionsMenu == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.action_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.action_view_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    /**
     * Create a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    // FIXME: use the current active account on this SyncStatusObserver.
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(() -> {
                // Create a handle to the account that was created by
                // SyncService.CreateSyncAccount(). This will be used to query the system to
                // see how the sync status has changed.
                Account account = AccountUtils.hasActiveAccount(MainActivity.this)
                        ? AccountUtils.getActiveAccount(MainActivity.this)
                        : SyncUtils.getDefaultSyncAccount();

                if (account == null) {
                    // GetAccount() returned an invalid value. This shouldn't happen, but
                    // we'll set the status to "not refreshing".
                    setRefreshActionButtonState(false);
                    return;
                }

                // Test the ContentResolver to see if the sync adapter is active or pending.
                // Set the state of the refresh button accordingly.
                boolean syncActive = ContentResolver.isSyncActive(
                        account, PiuDonnaContract.CONTENT_AUTHORITY);
                boolean syncPending = ContentResolver.isSyncPending(
                        account, PiuDonnaContract.CONTENT_AUTHORITY);
                setRefreshActionButtonState(syncActive || syncPending);
            });
        }
    };

    private NavigationView.OnNavigationItemSelectedListener mNavigationItemSelectedListener = (MenuItem item) -> {
        switch (item.getItemId()) {
            case R.id.nav_item_store_home:
                navigateToFragment(NAVDRAWER_ITEM_STORE_HOME);
                break;
            case R.id.nav_item_blog_feed:
                navigateToFragment(NAVDRAWER_ITEM_BLOG_FEED);
                break;
                /*case R.id.nav_item_wishlist:
                    break;*/
            case R.id.nav_item_order_history:
                navigateToFragment(NAVDRAWER_ITEM_ORDER_HISTORY);
                break;
            case R.id.nav_item_address_book:
                navigateToFragment(NAVDRAWER_ITEM_ADDRESS_BOOK);
                break;
            case R.id.nav_action_about: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.piudonnacouture.com/"));
                startActivity(intent);
                break;
            }
            case R.id.nav_action_help: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.piudonnacouture.com/customerSupport/faq"));
                startActivity(intent);
                break;
            }
                /*case R.id.nav_action_settings:
                    break;*/
            case R.id.nav_action_sign_out:
                showSignOutDialog();
                break;
        }

        if (item.getGroupId() != R.id.actions) {
            item.setChecked(true);
        }

        mDrawerLayout.closeDrawers();

        return true;
    };

    public static class SignOutDialogFragment extends DialogFragment {

        private MainActivity mActivity;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mActivity = (MainActivity) context;
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mActivity = null;
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // TODO: Change this dialog hardcoded strings to string resources.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_PiuDonna_Dialog_Alert);
            builder.setTitle("Sign out?");
            builder.setMessage("Do you really want to sign out?");
            builder.setNegativeButton("Cancel", null);
            builder.setPositiveButton("Sign Out", (dialog, which) -> {
                if (mActivity != null) {
                    mActivity.performSignOut();
                }
            });

            return builder.create();
        }
    }
}
