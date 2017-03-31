package com.smartsecurity.android.piudonnacouture.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.MetricAffectingSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.ui.widget.CustomTypefaceSpan;
import com.smartsecurity.android.piudonnacouture.ui.widget.EmptyView;
import com.smartsecurity.android.piudonnacouture.util.FontUtils;
import com.smartsecurity.android.piudonnacouture.util.OnItemClickListener;

public class AddressBookFragment extends BaseNavigationFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "AddressBookFragment";

    public static AddressBookFragment newInstance() {

        Bundle args = new Bundle();

        AddressBookFragment fragment = new AddressBookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private AddressBookAdapter mAddressBookAdapter;
    private FloatingActionButton mNewAddressFab;
    private ViewStub mEmptyViewStub;
    private EmptyView mEmptyView;

    private MetricAffectingSpan mMediumTypefaceSpan;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_address_book, container, false);

        mAddressBookAdapter = new AddressBookAdapter(null);
        mAddressBookAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Log.d(TAG, "onItemClick: ");
            }
        });

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.address_book_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAddressBookAdapter);

        mNewAddressFab = (FloatingActionButton) rootView.findViewById(R.id.new_address_fab);
        mNewAddressFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddressAddEditActivity.class));
            }
        });

        mEmptyViewStub = (ViewStub) rootView.findViewById(R.id.stub_empty_view);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEmptyView = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(AddressBookQuery.LOADER_ID, null, this);

        if (mMediumTypefaceSpan == null) {
            mMediumTypefaceSpan = new CustomTypefaceSpan(
                    FontUtils.getTypeface(getActivity(), "Roboto-Medium.ttf"));
        }
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
        setEmptyViewVisibility(data.getCount() == 0 ? View.VISIBLE : View.GONE);
        mNewAddressFab.setVisibility(data.getCount() == 0 ? View.GONE : View.VISIBLE);

        mAddressBookAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAddressBookAdapter.swapCursor(null);
    }

    private void setEmptyViewVisibility(int visibility) {
        if (mEmptyView == null && visibility == View.VISIBLE) {
            mEmptyView = (EmptyView) mEmptyViewStub.inflate();
            // TODO: Convert to string resources.
            mEmptyView.setImageResource(R.drawable.ic_empty_address_book);
            mEmptyView.setTitle("Address book empty");
            mEmptyView.setSubtitle("There are no addresses in your book.");
            mEmptyView.setAction("Add address", v -> startActivity(new Intent(getActivity(), AddressAddEditActivity.class)));
        }

        if (mEmptyView != null) {
            mEmptyView.setVisibility(visibility);
        }
    }

    private class AddressBookAdapter extends RecyclerView.Adapter<AddressBookAdapter.ViewHolder> {

        private Cursor mCursor;
        private OnItemClickListener mItemClickListener;

        public AddressBookAdapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getActivity())
                    .inflate(R.layout.list_item_address_book, parent, false);

            final ViewHolder holder = new ViewHolder(itemView);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && mItemClickListener != null) {
                        mItemClickListener.onItemClick(v, position);
                    }
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mCursor.moveToPosition(position)) {
                holder.bindAddress(mCursor);
                holder.showDivider(position != mCursor.getCount() - 1);
                holder.showTopDivider(position == 0);
                holder.showBottomDivider(position == mCursor.getCount() - 1);
            }
        }

        @Override
        public int getItemCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        public void swapCursor(Cursor newCursor) {
            if (mCursor != newCursor) {
                mCursor = newCursor;
                notifyDataSetChanged();
            }
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.mItemClickListener = listener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mFullNameView;
            public TextView mAddressLinesView;
            public TextView mCityStateZipView;
            public TextView mCountryView;
            public TextView mPhoneView;
            public View mDivider;
            public View mTopDivider;
            public View mBottomDivider;


            public ViewHolder(View itemView) {
                super(itemView);
                mFullNameView = (TextView) itemView.findViewById(R.id.full_name);
                mAddressLinesView = (TextView) itemView.findViewById(R.id.address_lines);
                mCityStateZipView = (TextView) itemView.findViewById(R.id.city_state_zip);
                mCountryView = (TextView) itemView.findViewById(R.id.country);
                mPhoneView = (TextView) itemView.findViewById(R.id.phone_number);
                mDivider = itemView.findViewById(R.id.divider);
                mTopDivider = itemView.findViewById(R.id.divider_top);
                mBottomDivider = itemView.findViewById(R.id.divider_bottom);
            }

            public void bindAddress(Cursor cursor) {
                mFullNameView.setText(cursor.getString(AddressBookQuery.COLUMN_FULL_NAME));

                String line2 = cursor.getString(AddressBookQuery.COLUMN_ADDRESS_LINE_2);
                mAddressLinesView.setText(String.format("%s%s",
                        cursor.getString(AddressBookQuery.COLUMN_ADDRESS_LINE_1),
                        TextUtils.isEmpty(line2) ? "" : (" " + line2)));

                mCityStateZipView.setText(String.format("%s %s %s",
                        cursor.getString(AddressBookQuery.COLUMN_CITY),
                        cursor.getString(AddressBookQuery.COLUMN_STATE),
                        cursor.getString(AddressBookQuery.COLUMN_ZIP_CODE)));

                mCountryView.setText(cursor.getString(AddressBookQuery.COLUMN_COUNTRY));

                SpannableString phone = new SpannableString(getString(R.string.text_phone, cursor.getString(AddressBookQuery.COLUMN_PHONE_NUMBER)));
                phone.setSpan(mMediumTypefaceSpan, 0, phone.toString().indexOf(':') + 1, 0);
                mPhoneView.setText(phone);
            }

            public void showDivider(boolean show) {
                mDivider.setVisibility(show ? View.VISIBLE : View.GONE);
            }

            public void showTopDivider(boolean show) {
                if (mTopDivider != null) {
                    mTopDivider.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            }

            public void showBottomDivider(boolean show) {
                if (mBottomDivider != null) {
                    mBottomDivider.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            }
        }
    }
}
