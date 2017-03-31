package com.smartsecurity.android.piudonnacouture.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.ui.widget.EmptyView;
import com.smartsecurity.android.piudonnacouture.util.DateUtils;

public class OrderHistoryFragment extends BaseNavigationFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "OrderHistoryFragment";

    public static OrderHistoryFragment newInstance() {
        return new OrderHistoryFragment();
    }

    interface OnOrderClickListener {
        void onOrderClick(long orderServerId);
    }

    private interface OrderHistoryQuery {
        int LOADER_ID = 0x1;
        Uri URI = PiuDonnaContract.OrderEntry.CONTENT_URI;
        String[] PROJECTION = {
                PiuDonnaContract.OrderEntry._ID,
                PiuDonnaContract.OrderEntry.COLUMN_SERVER_ID,
                PiuDonnaContract.OrderEntry.COLUMN_REFERENCE,
                PiuDonnaContract.OrderEntry.COLUMN_DATE,
                PiuDonnaContract.OrderEntry.COLUMN_SUBTOTAL,
                PiuDonnaContract.OrderEntry.COLUMN_SHIPPING_PRICE,
                PiuDonnaContract.OrderEntry.COLUMN_TAX,
                PiuDonnaContract.OrderEntry.COLUMN_STATUS,
                PiuDonnaContract.OrderEntry.COLUMN_QUANTITY
        };

        String SORT_ORDER = PiuDonnaContract.OrderEntry.COLUMN_SERVER_ID + " DESC";

        int COLUMN_SERVER_ID = 1;
        int COLUMN_REFERENCE = 2;
        int COLUMN_DATE = 3;
        int COLUMN_SUBTOTAL = 4;
        int COLUMN_SHIPPING_PRICE = 5;
        int COLUMN_TAX = 6;
        int COLUMN_STATUS = 7;
        int COLUMN_QUANTITY = 8;
    }

    private OrdersAdapter mOrdersAdapter;
    private ViewStub mEmptyViewStub;
    private EmptyView mEmptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order_history, container, false);

        mOrdersAdapter = new OrdersAdapter(null);
        mOrdersAdapter.setOnOrderClickListener(orderServerId -> {
            Intent intent = OrderDetailActivity.getStartIntent(getActivity(), orderServerId);
            startActivity(intent);
        });

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.order_history_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mOrdersAdapter);

        mEmptyViewStub = (ViewStub) rootView.findViewById(R.id.stub_empty_view);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEmptyView = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(OrderHistoryQuery.LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");
        return new CursorLoader(getActivity(),
                OrderHistoryQuery.URI,
                OrderHistoryQuery.PROJECTION,
                null, null,
                OrderHistoryQuery.SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished()");
        if (loader.getId() == OrderHistoryQuery.LOADER_ID) {
            setEmptyViewVisibility(cursor.getCount() == 0 ? View.VISIBLE : View.GONE);
            mOrdersAdapter.swapCursor(cursor);
        }
    }

    private void setEmptyViewVisibility(int visibility) {
        if (mEmptyView == null && visibility == View.VISIBLE) {
            // TODO: Change this strings for resources.
            mEmptyView = (EmptyView) mEmptyViewStub.inflate();
            mEmptyView.setImageResource(R.drawable.ic_empty_orders);
            mEmptyView.setTitle("No orders");
            mEmptyView.setSubtitle("When you place an order, you'll see it here.");
        }

        if (mEmptyView != null) {
            mEmptyView.setVisibility(visibility);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == OrderHistoryQuery.LOADER_ID) {
            mOrdersAdapter.swapCursor(null);
        }
    }

    private class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

        private Cursor mCursor;
        private OnOrderClickListener mOrderClickListener;

        public OrdersAdapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getActivity())
                    .inflate(R.layout.list_item_order, parent, false);

            ViewHolder holder = new ViewHolder(itemView);
            holder.itemView.setOnClickListener(v -> {
                int position = holder.getAdapterPosition();
                if (mOrderClickListener != null && position != RecyclerView.NO_POSITION) {
                    if (mCursor != null && mCursor.moveToPosition(position)) {
                        long orderServerId = mCursor.getLong(OrderHistoryQuery.COLUMN_SERVER_ID);
                        mOrderClickListener.onOrderClick(orderServerId);
                    }
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mCursor.moveToPosition(position)) {
                holder.bindOrder(mCursor);
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

        public void setOnOrderClickListener(OnOrderClickListener listener) {
            mOrderClickListener = listener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView mDateView;
            public TextView mOrderReferenceView;
            public TextView mOrderItemCountView;
            public TextView mOrderTotalView;
            public TextView mOrderStatusView;
            public View mDivider;
            public View mTopDivider;
            public View mBottomDivider;


            public long mOrderServerId;

            public ViewHolder(View itemView) {
                super(itemView);

                mDateView = (TextView) itemView.findViewById(R.id.order_date_view);
                mOrderReferenceView = (TextView) itemView.findViewById(R.id.order_reference_view);
                mOrderItemCountView = (TextView) itemView.findViewById(R.id.order_item_count_view);
                mOrderTotalView = (TextView) itemView.findViewById(R.id.order_total_view);
                mOrderStatusView = (TextView) itemView.findViewById(R.id.order_status_view);
                mDivider = itemView.findViewById(R.id.divider);
                mTopDivider = itemView.findViewById(R.id.divider_top);
                mBottomDivider = itemView.findViewById(R.id.divider_bottom);
            }

            public void bindOrder(Cursor cursor) {
                mDateView.setText(DateUtils.getOrderDateFromRfc3339(cursor.getString(OrderHistoryQuery.COLUMN_DATE)));
                mOrderReferenceView.setText(getString(R.string.order_number, cursor.getString(OrderHistoryQuery.COLUMN_REFERENCE)));
                mOrderItemCountView.setText(getString(R.string.order_item_count, cursor.getInt(OrderHistoryQuery.COLUMN_QUANTITY)));
                mOrderTotalView.setText(getString(R.string.order_total, cursor.getFloat(OrderHistoryQuery.COLUMN_SUBTOTAL)));
                mOrderStatusView.setText(getString(R.string.order_status, cursor.getString(OrderHistoryQuery.COLUMN_STATUS)));

                mOrderServerId = cursor.getLong(OrderHistoryQuery.COLUMN_SERVER_ID);
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
