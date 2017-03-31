package com.smartsecurity.android.piudonnacouture.ui.checkout;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.style.MetricAffectingSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.Address;
import com.smartsecurity.android.piudonnacouture.model.CreditCard;
import com.smartsecurity.android.piudonnacouture.ui.CheckoutActivity;
import com.smartsecurity.android.piudonnacouture.ui.ShoppingCartActivity;
import com.smartsecurity.android.piudonnacouture.ui.widget.CustomTypefaceSpan;
import com.smartsecurity.android.piudonnacouture.util.FontUtils;
import com.smartsecurity.android.piudonnacouture.util.UiUtils;

import org.apache.commons.lang3.text.WordUtils;

public class ReviewCheckoutFragment extends CheckoutFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ReviewCheckoutFragment";

    public static ReviewCheckoutFragment newInstance() {
        return new ReviewCheckoutFragment();
    }

    public static final String ARG_ADDRESS_ID = "arg_address_id";
    public static final String ARG_SELECTED_ADDRESS = "arg_selected_address";
    public static final String ARG_CARD_HOLDER = "arg_name_on_card";
    public static final String ARG_CARD_NUMBER = "arg_card_number";
    public static final String ARG_CARD_CVC = "arg_card_cvc";
    public static final String ARG_CARD_EXP_MONTH = "arg_card_exp_month";
    public static final String ARG_CARD_EXP_YEAR = "arg_card_exp_year";

    private MetricAffectingSpan mMediumTypefaceSpan;

    private CheckoutActivity mActivity;

    private View mPaymentInfoSection;
    private TextView mCardTypeView;
    private TextView mCardHolderView;
    private TextView mCardExpDateView;

    private View mShippingInfoSection;
    private TextView mFullNameView;
    private TextView mAddressLinesView;
    private TextView mCityStateZipCodeView;
    private TextView mCountryView;
    private TextView mPhoneNumberView;

    private View mOrderDetailsSection;
    private LinearLayout mOrderDetailsContainer;

    private View mOrderSummarySection;
    private TextView mSummaryLabelsView;
    private TextView mSummaryValuesView;
    private TextView mOrderTotalView;

    private Button mPlaceOrderButton;

    interface ReviewCartProductsQuery extends ShoppingCartActivity.CartProductsQuery {
        int LOADER_ID = 0x1;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_checkout_review, container, false);

        mPaymentInfoSection = rootView.findViewById(R.id.payment_info_section);
        mCardTypeView = (TextView) mPaymentInfoSection.findViewById(R.id.card_type_view);
        mCardHolderView = (TextView) mPaymentInfoSection.findViewById(R.id.card_holder_view);
        mCardExpDateView = (TextView) mPaymentInfoSection.findViewById(R.id.card_exp_date_view);

        mShippingInfoSection = rootView.findViewById(R.id.shipping_info_section);
        mFullNameView = (TextView) mShippingInfoSection.findViewById(R.id.address_full_name);
        mAddressLinesView = (TextView) mShippingInfoSection.findViewById(R.id.address_lines);
        mCityStateZipCodeView = (TextView) mShippingInfoSection.findViewById(R.id.address_city_state_zip);
        mCountryView = (TextView) mShippingInfoSection.findViewById(R.id.address_country);
        mPhoneNumberView = (TextView) mShippingInfoSection.findViewById(R.id.address_phone);

        mOrderDetailsSection = rootView.findViewById(R.id.order_details_section);
        mOrderDetailsContainer = (LinearLayout) mOrderDetailsSection.findViewById(R.id.order_details_container);

        mOrderSummarySection = rootView.findViewById(R.id.order_summary_section);
        mSummaryLabelsView = (TextView) mOrderSummarySection.findViewById(R.id.summary_labels);
        mSummaryValuesView = (TextView) mOrderSummarySection.findViewById(R.id.summary_values);
        mOrderTotalView = (TextView) mOrderSummarySection.findViewById(R.id.summary_order_total);

        mPlaceOrderButton = (Button) rootView.findViewById(R.id.place_order_button);
        mPlaceOrderButton.setOnClickListener(v -> mActivity.placeOrder());

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated()");
        getLoaderManager().initLoader(ReviewCartProductsQuery.LOADER_ID, null, this);

        if (mMediumTypefaceSpan == null) {
            mMediumTypefaceSpan = new CustomTypefaceSpan(
                    FontUtils.getTypeface(getActivity(), "Roboto-Medium.ttf"));
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i(TAG, "setUserVisibleHint: " + isVisibleToUser);
        if (isVisibleToUser) {
            setShippingAddress(mActivity.getSelectedAddress());
            setPaymentInformation(mActivity.getCreditCard());
        }
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    private void setShippingAddress(Address address) {
        if (address != null) {
            mFullNameView.setText(address.getFullName());

            mAddressLinesView.setText(String.format("%s%s", address.getAddressLine1(),
                    address.getPrintableAddressLine2()));

            mCityStateZipCodeView.setText(String.format("%s %s %s", address.getCity(),
                    address.getState(), address.getZipCode()));

            mCountryView.setText(address.getCountry());

            SpannableString phone = new SpannableString(getString(R.string.text_phone, address.getPhoneNumber()));
            phone.setSpan(mMediumTypefaceSpan, 0, phone.toString().indexOf(':') + 1, 0);
            mPhoneNumberView.setText(phone);
        }
    }

    private void setPaymentInformation(CreditCard card) {
        if (card != null) {
            mCardTypeView.setText(String.format("%s **** %s", card.getType().getName(), card.getLastFourNumbers()));
            mCardHolderView.setText(card.getCardHolder());
            mCardExpDateView.setText(String.format("Expires %s/20%s", card.getExpMonth(), card.getExpYear()));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == ReviewCartProductsQuery.LOADER_ID) {
            return new CursorLoader(getActivity(),
                    ReviewCartProductsQuery.URI,
                    ReviewCartProductsQuery.PROJECTION,
                    null, null, null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == ReviewCartProductsQuery.LOADER_ID) {
            reloadOrderDetails(cursor);
            reloadItemCountAndTotalPrice(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void reloadOrderDetails(Cursor cursor) {
        mOrderDetailsContainer.removeAllViews();

        if (cursor.moveToFirst()) {
            do {
                View item = LayoutInflater.from(getActivity()).inflate(
                        R.layout.list_item_shopping_cart_review, mOrderDetailsContainer, false);

                ((TextView) item.findViewById(R.id.product_title)).setText(
                        cursor.getString(ReviewCartProductsQuery.COLUMN_NAME));

                ((TextView) item.findViewById(R.id.product_specs)).setText(
                        getString(R.string.cart_product_details,
                                WordUtils.capitalizeFully(cursor.getString(ReviewCartProductsQuery.COLUMN_COLOR)),
                                cursor.getString(ReviewCartProductsQuery.COLUMN_SIZE),
                                cursor.getInt(ReviewCartProductsQuery.COLUMN_QUANTITY)));

                ((TextView) item.findViewById(R.id.product_price)).setText(
                        UiUtils.formatPrice(cursor.getFloat(ReviewCartProductsQuery.COLUMN_PRICE)));

                mOrderDetailsContainer.addView(item);

            } while(cursor.moveToNext());
        }
    }

    private void reloadItemCountAndTotalPrice(Cursor cursor) {
        int itemCount = 0;
        float totalPrice = 0f;

        if (cursor.moveToFirst()) {
            do {
                int quantity = cursor.getInt(ReviewCartProductsQuery.COLUMN_QUANTITY);
                itemCount += quantity;
                totalPrice += (quantity * cursor.getFloat(ReviewCartProductsQuery.COLUMN_PRICE));
            } while (cursor.moveToNext());
        }

        mSummaryLabelsView.setText(getString(R.string.order_summary_labels, itemCount));
        mSummaryValuesView.setText(String.format("$%.2f\n$%.2f\n$%.2f\n$%.2f", totalPrice, 0f, 0f, 0f));
        mOrderTotalView.setText(UiUtils.formatPrice(totalPrice));
    }
}
