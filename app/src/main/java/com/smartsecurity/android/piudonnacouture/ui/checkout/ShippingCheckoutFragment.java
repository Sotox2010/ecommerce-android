package com.smartsecurity.android.piudonnacouture.ui.checkout;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.Address;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.ui.CheckoutActivity;
import com.smartsecurity.android.piudonnacouture.ui.widget.EmptyView;

public class ShippingCheckoutFragment extends CheckoutFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ShippingFragment";

    private static final String STATE_PAYMENT_BUTTON = "state_payment_button";

    public static ShippingCheckoutFragment newInstance() {
        return new ShippingCheckoutFragment();
    }

    public interface AddressBookQuery {
        int LOADER_ID = 0x1;
        Uri URI = PiuDonnaContract.AddressBookEntry.CONTENT_URI;

        String[] PROJECTION = new String[] {
                PiuDonnaContract.AddressBookEntry._ID,
                PiuDonnaContract.AddressBookEntry.COLUMN_SERVER_ID,
                PiuDonnaContract.AddressBookEntry.COLUMN_FULL_NAME,
                PiuDonnaContract.AddressBookEntry.COLUMN_ADDRESS_LINE_1,
                PiuDonnaContract.AddressBookEntry.COLUMN_ADDRESS_LINE_2,
                PiuDonnaContract.AddressBookEntry.COLUMN_CITY,
                PiuDonnaContract.AddressBookEntry.COLUMN_STATE,
                PiuDonnaContract.AddressBookEntry.COLUMN_ZIP_CODE,
                PiuDonnaContract.AddressBookEntry.COLUMN_COUNTRY,
                PiuDonnaContract.AddressBookEntry.COLUMN_PHONE_NUMBER
        };

        String SORT_ORDER = PiuDonnaContract.AddressBookEntry.COLUMN_SERVER_ID + " DESC";

        int COLUMN_SERVER_ID = 1;
        int COLUMN_FULL_NAME = 2;
        int COLUMN_ADDRESS_LINE_1 = 3;
        int COLUMN_ADDRESS_LINE_2 = 4;
        int COLUMN_CITY = 5;
        int COLUMN_STATE = 6;
        int COLUMN_ZIP_CODE = 7;
        int COLUMN_COUNTRY = 8;
        int COLUMN_PHONE_NUMBER = 9;
    }

    private CheckoutActivity mActivity;

    private ListView mListView;
    private AddressBookAdapter mAdapter;
    private ViewStub mEmptyViewStub;
    private EmptyView mEmptyView;
    private Button mProceedToPaymentButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (CheckoutActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_checkout_shipping, container, false);

        mAdapter = new AddressBookAdapter(getActivity(), null, 0);

        mListView = (ListView) rootView.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: " + position + " " + id);
                mProceedToPaymentButton.setEnabled(id != -1);
            }
        });

        mEmptyViewStub = (ViewStub) rootView.findViewById(R.id.stub_empty_view);

        mProceedToPaymentButton = (Button) rootView.findViewById(R.id.proceed_to_payment_button);
        mProceedToPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.proceedToPayment(mAdapter.getItem(mListView.getCheckedItemPosition()));
            }
        });

        if (savedInstanceState != null) {
            mProceedToPaymentButton.setEnabled(savedInstanceState.getBoolean(STATE_PAYMENT_BUTTON));
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_PAYMENT_BUTTON, mProceedToPaymentButton.isEnabled());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(AddressBookQuery.LOADER_ID, null, this);
    }

    @Override
    public boolean isCompleted() {
        return mListView.getSelectedItemId() != -1;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == AddressBookQuery.LOADER_ID) {
            return new CursorLoader(getActivity(),
                    AddressBookQuery.URI,
                    AddressBookQuery.PROJECTION,
                    null,
                    null,
                    AddressBookQuery.SORT_ORDER);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() == 0) {
            setEmptyViewVisibility(View.VISIBLE);
        }

        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void setEmptyViewVisibility(int visibility) {
        if (mEmptyView == null && visibility == View.VISIBLE) {
            mEmptyView = (EmptyView) mEmptyViewStub.inflate();
            // TODO: Convert to string resources.
            mEmptyView.setTitle("Address book empty");
            mEmptyView.setSubtitle("There are no addresses in your book.");
            mEmptyView.setAction("Add address", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        if (mEmptyView != null) {
            mEmptyView.setVisibility(visibility);
        }
    }

    private class AddressBookAdapter extends CursorAdapter {

        public AddressBookAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View itemView = LayoutInflater.from(context).inflate(
                    R.layout.list_item_address, parent, false);

            ViewHolder holder = new ViewHolder(itemView);
            itemView.setTag(holder);

            return itemView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();

            holder.mFullNameView.setText(cursor.getString(AddressBookQuery.COLUMN_FULL_NAME));
            holder.mAddressLinesView.setText(String.format("%s %s",
                    cursor.getString(AddressBookQuery.COLUMN_ADDRESS_LINE_1),
                    cursor.getString(AddressBookQuery.COLUMN_ADDRESS_LINE_2)));
            holder.mCityStateZipView.setText(String.format("%s %s %s",
                    cursor.getString(AddressBookQuery.COLUMN_CITY),
                    cursor.getString(AddressBookQuery.COLUMN_STATE),
                    cursor.getString(AddressBookQuery.COLUMN_ZIP_CODE)));
            holder.mCountryView.setText(cursor.getString(AddressBookQuery.COLUMN_COUNTRY));
            holder.mPhoneView.setText(cursor.getString(AddressBookQuery.COLUMN_PHONE_NUMBER));

            /*CheckableFrameLayout checkableView = (CheckableFrameLayout) holder.itemView;
            checkableView.setOnCheckedChangeListener(new CheckableFrameLayout.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View checkableView, boolean isChecked) {
                    holder.mRadioButton.performClick();
                }
            });*/
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            Cursor cursor = getCursor();
            return cursor != null && cursor.moveToPosition(position)
                    ? cursor.getLong(AddressBookQuery.COLUMN_SERVER_ID) : -1;
        }

        @Override
        public Address getItem(int position) {
            Cursor cursor = getCursor();
            if (cursor != null && cursor.moveToPosition(position)) {
                return new Address(
                        cursor.getLong(AddressBookQuery.COLUMN_SERVER_ID),
                        cursor.getString(AddressBookQuery.COLUMN_FULL_NAME),
                        cursor.getString(AddressBookQuery.COLUMN_ADDRESS_LINE_1),
                        cursor.getString(AddressBookQuery.COLUMN_ADDRESS_LINE_2),
                        cursor.getString(AddressBookQuery.COLUMN_CITY),
                        cursor.getString(AddressBookQuery.COLUMN_STATE),
                        cursor.getString(AddressBookQuery.COLUMN_COUNTRY),
                        cursor.getString(AddressBookQuery.COLUMN_ZIP_CODE),
                        cursor.getString(AddressBookQuery.COLUMN_PHONE_NUMBER));
            } else {
                return null;
            }
        }

        private class ViewHolder {
            public View itemView;
            public RadioButton mRadioButton;
            public TextView mFullNameView;
            public TextView mAddressLinesView;
            public TextView mCityStateZipView;
            public TextView mCountryView;
            public TextView mPhoneView;

            public ViewHolder(View itemView) {
                this.itemView = itemView;
                mRadioButton = (RadioButton) itemView.findViewById(R.id.radio);
                mFullNameView = (TextView) itemView.findViewById(R.id.full_name);
                mAddressLinesView = (TextView) itemView.findViewById(R.id.address_lines);
                mCityStateZipView = (TextView) itemView.findViewById(R.id.city_state_zip);
                mCountryView = (TextView) itemView.findViewById(R.id.country);
                mPhoneView = (TextView) itemView.findViewById(R.id.phone_number);
            }
        }
    }
}
