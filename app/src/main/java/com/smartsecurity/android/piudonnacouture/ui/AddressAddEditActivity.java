package com.smartsecurity.android.piudonnacouture.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.ui.worker.AddressBookWorkerFragment;
import com.smartsecurity.android.piudonnacouture.util.AccountUtils;

import java.util.Locale;

public class AddressAddEditActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, AddressBookWorkerFragment.OnAddNewAddressCallback {
    private static final String TAG = "AddressAddEditActivity";

    public static final String ACTION_ADD_ADDRESS = "action.ADD_ADDRESS";
    public static final String ACTION_EDIT_ADDRESS = "action.EDIT_ADDRESS";

    public static final String EXTRA_ADDRESS_ID = "extra_address_id";

    public static Intent buildAddAddressIntent(Context context) {
        Intent intent = new Intent(context, AddressAddEditActivity.class);
        intent.setAction(AddressAddEditActivity.ACTION_ADD_ADDRESS);
        return intent;
    }

    public static Intent buildEditAddressIntent(Context context, String addressId) {
        Intent intent = new Intent(context, AddressAddEditActivity.class);
        intent.setAction(AddressAddEditActivity.ACTION_EDIT_ADDRESS);
        intent.putExtra(EXTRA_ADDRESS_ID, addressId);
        return intent;
    }

    public static final String TAG_FRAGMENT_WORKER = "fragment_address_book_worker";

    private static int COLOR_ERR0R = 0xFFFF4444;
    private static ForegroundColorSpan sErrorColorSpan = new ForegroundColorSpan(COLOR_ERR0R);

    private Toolbar mToolbarActionBar;
    private ProgressBar mToolbarProgressBar;
    private MenuItem mSaveActionButton;

    private TextInputLayout mFullNameInputLayout;
    private TextInputLayout mAddressLine1InputLayout;
    private TextInputLayout mAddressLine2InputLayout;
    private TextInputLayout mCityInputLayout;
    private TextInputLayout mStateInputLayout;
    private TextInputLayout mZipCodeInputLayout;
    // TODO: Add country selector.
    //private TextInputLayout mCountryInputLayout;
    private TextInputLayout mPhoneNumberInputLayout;
    private View mCurrentFocusView = null;

    private AddressBookWorkerFragment mWorkerFragment;
    private PhoneNumberUtil mPhoneNumberUtil = PhoneNumberUtil.getInstance();

    private long mAddressId = -1;

    public interface AddressQuery {
        int LOADER_ID = 0x1;

        String[] PROJECTION = new String[] {
                PiuDonnaContract.AddressBookEntry._ID,
                PiuDonnaContract.AddressBookEntry.COLUMN_FULL_NAME,
                PiuDonnaContract.AddressBookEntry.COLUMN_ADDRESS_LINE_1,
                PiuDonnaContract.AddressBookEntry.COLUMN_ADDRESS_LINE_2,
                PiuDonnaContract.AddressBookEntry.COLUMN_CITY,
                PiuDonnaContract.AddressBookEntry.COLUMN_STATE,
                PiuDonnaContract.AddressBookEntry.COLUMN_ZIP_CODE,
                PiuDonnaContract.AddressBookEntry.COLUMN_COUNTRY,
                PiuDonnaContract.AddressBookEntry.COLUMN_PHONE_NUMBER
        };

        int COLUMN_FULL_NAME = 1;
        int COLUMN_ADDRESS_LINE_1 = 2;
        int COLUMN_ADDRESS_LINE_2 = 3;
        int COLUMN_CITY = 4;
        int COLUMN_STATE = 5;
        int COLUMN_ZIP_CODE = 6;
        int COLUMN_COUNTRY = 7;
        int COLUMN_PHONE_NUMBER = 8;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_address);

        mToolbarActionBar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarProgressBar = (ProgressBar) mToolbarActionBar.findViewById(R.id.toolbar_progress);

        setSupportActionBar(mToolbarActionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Drawable closeDrawable = ContextCompat.getDrawable(this, R.drawable.ic_close);
        closeDrawable.setColorFilter(new PorterDuffColorFilter(
                ContextCompat.getColor(this, R.color.icon_system_default), PorterDuff.Mode.SRC_IN));

        mToolbarActionBar.setNavigationIcon(closeDrawable);
        mToolbarActionBar.setNavigationOnClickListener(mNavigationClickListener);

        mFullNameInputLayout = (TextInputLayout) findViewById(R.id.full_name_input_layout);
        mFullNameInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mFullNameInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mAddressLine1InputLayout = (TextInputLayout) findViewById(R.id.address_line_one_input_layout);
        mAddressLine1InputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mAddressLine1InputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mAddressLine2InputLayout = (TextInputLayout) findViewById(R.id.address_line_two_input_layout);
        mAddressLine2InputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mAddressLine2InputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mCityInputLayout = (TextInputLayout) findViewById(R.id.city_input_layout);
        mCityInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mCityInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mStateInputLayout = (TextInputLayout) findViewById(R.id.state_input_layout);
        mStateInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mStateInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mZipCodeInputLayout = (TextInputLayout) findViewById(R.id.zip_code_input_layout);
        mZipCodeInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mZipCodeInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mPhoneNumberInputLayout = (TextInputLayout) findViewById(R.id.phone_input_layout);
        mPhoneNumberInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mPhoneNumberInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mWorkerFragment = (AddressBookWorkerFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_FRAGMENT_WORKER);

        if (mWorkerFragment == null) {
            mWorkerFragment = AddressBookWorkerFragment.newInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(mWorkerFragment, TAG_FRAGMENT_WORKER)
                    .commit();
        }

        /*Intent intent = getIntent();
        String action = intent.getAction();

        if (action != null) {
            if (intent.getAction().equals(ACTION_ADD_ADDRESS)) {

            } else {

            }
        } else {
            Toast.makeText(this, "Action is missing.", Toast.LENGTH_SHORT).show();
            finish();
        }*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_new_address, menu);
        mSaveActionButton = menu.findItem(R.id.action_save_address);
        mSaveActionButton.setEnabled(validateFields());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_address) {
            addNewAddress();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("ConstantConditions")
    private void addNewAddress() {
        setFormEditable(false);
        setBackEnabled(false);

        mWorkerFragment.addNewAddress(AccountUtils.getActiveAccountName(this),
                mFullNameInputLayout.getEditText().getText().toString(),
                mAddressLine1InputLayout.getEditText().getText().toString(),
                mAddressLine2InputLayout.getEditText().getText().toString(),
                mCityInputLayout.getEditText().getText().toString(),
                mStateInputLayout.getEditText().getText().toString(),
                mZipCodeInputLayout.getEditText().getText().toString(),
                "United States",
                mPhoneNumberInputLayout.getEditText().getText().toString());
    }

    @Override
    public void onAddNewAddressResult(String result) {

        // A 'null' or empty result means success for us.
        if (TextUtils.isEmpty(result)) {
            Toast.makeText(this, "Successfully added new address.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            setFormEditable(true);
            setBackEnabled(true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private boolean validateFields() {
        return !TextUtils.isEmpty(mFullNameInputLayout.getEditText().getText())
                && !TextUtils.isEmpty(mAddressLine1InputLayout.getEditText().getText())
                && !TextUtils.isEmpty(mCityInputLayout.getEditText().getText())
                && !TextUtils.isEmpty(mStateInputLayout.getEditText().getText())
                && !TextUtils.isEmpty(mZipCodeInputLayout.getEditText().getText())
                && isValidPhoneNumber(mPhoneNumberInputLayout.getEditText().getText().toString());
    }

    private boolean isValidPhoneNumber(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        try {
            Phonenumber.PhoneNumber number = mPhoneNumberUtil.parse(text, Locale.US.getCountry());
            return mPhoneNumberUtil.isValidNumber(number);
        } catch (NumberParseException e) {
            //e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setFormEditable(boolean editable) {
        // Clear the focus of the current focused text field.
        if (mCurrentFocusView != null) {
            mCurrentFocusView.clearFocus();
        }

        setNavigationButtonEnabled(editable);
        mToolbarProgressBar.setVisibility(editable ? View.GONE : View.VISIBLE);

        mFullNameInputLayout.getEditText().setEnabled(editable);
        mAddressLine1InputLayout.getEditText().setEnabled(editable);
        mAddressLine2InputLayout.getEditText().setEnabled(editable);
        mCityInputLayout.getEditText().setEnabled(editable);
        mStateInputLayout.getEditText().setEnabled(editable);
        mZipCodeInputLayout.getEditText().setEnabled(editable);
        mPhoneNumberInputLayout.getEditText().setEnabled(editable);

        mSaveActionButton.setEnabled(editable);
    }

    private void setNavigationButtonEnabled(boolean enabled) {
        mToolbarActionBar.setNavigationOnClickListener(enabled ? mNavigationClickListener : null);
    }

    private String formatNumber(AsYouTypeFormatter formatter, String input) {
        String result = "";

        formatter.clear();
        for (int i = 0; i < input.length(); i++) {
            result = formatter.inputDigit(input.charAt(i));
        }

        return result;
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        private boolean mManualTrigger = false;
        private AsYouTypeFormatter mPhoneNumberFormatter =
                mPhoneNumberUtil.getAsYouTypeFormatter(Locale.US.getCountry());

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void afterTextChanged(Editable s) {
            if (mManualTrigger) {
                mManualTrigger = false;
                return;
            }

            // Mask the phone number field.
            if (mPhoneNumberInputLayout.getEditText().getText().toString().equals(s.toString())) {
                Log.d(TAG, "afterTextChanged: entered.");
                    String formatted = formatNumber(mPhoneNumberFormatter, s.toString().replaceAll("[^\\d\\+]", ""));
                    mManualTrigger = true;
                    s.replace(0, s.length(), formatted);
            }

            if (mSaveActionButton != null) {
                mSaveActionButton.setEnabled(validateFields());
            }
        }
    };

    @SuppressWarnings("ConstantConditions")
    private View.OnFocusChangeListener mFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            mCurrentFocusView = v;
        }

        // Change the phone field text color depending on whether the phone number is valid or not.
        if (v == mPhoneNumberInputLayout.getEditText()) {
            if (!hasFocus) {
                Editable editable = mPhoneNumberInputLayout.getEditText().getText();
                if (!isValidPhoneNumber(editable.toString())) {
                    editable.setSpan(sErrorColorSpan, 0, editable.length(), 0);
                }
            } else {
                mPhoneNumberInputLayout.getEditText().getText().removeSpan(sErrorColorSpan);
            }
        }
    };

    private View.OnClickListener mNavigationClickListener = v -> finish();

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                PiuDonnaContract.AddressBookEntry.buildAddressUri(mAddressId),
                AddressQuery.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AddressQuery.LOADER_ID) {
            fillForm(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @SuppressWarnings("ConstantConditions")
    private void fillForm(Cursor cursor) {
        if (cursor.moveToFirst()) {
            mFullNameInputLayout.getEditText().setText(cursor.getString(AddressQuery.COLUMN_FULL_NAME));
            mAddressLine1InputLayout.getEditText().setText(cursor.getString(AddressQuery.COLUMN_ADDRESS_LINE_1));
            mAddressLine2InputLayout.getEditText().setText(cursor.getString(AddressQuery.COLUMN_ADDRESS_LINE_2));
            mCityInputLayout.getEditText().setText(cursor.getString(AddressQuery.COLUMN_CITY));
            mStateInputLayout.getEditText().setText(cursor.getString(AddressQuery.COLUMN_STATE));
            mZipCodeInputLayout.getEditText().setText(cursor.getString(AddressQuery.COLUMN_ZIP_CODE));
            mPhoneNumberInputLayout.getEditText().setText(cursor.getString(AddressQuery.COLUMN_PHONE_NUMBER));
        } else {
            Toast.makeText(this, "Could not find the specified address.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
