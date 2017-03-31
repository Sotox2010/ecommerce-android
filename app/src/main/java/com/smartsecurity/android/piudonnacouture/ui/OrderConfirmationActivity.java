package com.smartsecurity.android.piudonnacouture.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.Address;
import com.smartsecurity.android.piudonnacouture.model.CreditCard;
import com.smartsecurity.android.piudonnacouture.ui.widget.EmptyView;
import com.smartsecurity.android.piudonnacouture.ui.worker.PlaceOrderWorkerFragment;
import com.smartsecurity.android.piudonnacouture.util.AccountUtils;

public class OrderConfirmationActivity extends BaseActivity implements PlaceOrderWorkerFragment.OnPlaceOrderResultCallback {

    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_CREDIT_CARD = "extra+credit_card";

    public static final String TAG_FRAGMENT_WORKER = "fragment_place_order_worker";

    private View mProgressContainer;
    private EmptyView mEmptyView;

    private Address mAddress;
    private CreditCard mCreditCard;
    private PlaceOrderWorkerFragment mWorkerFragment;

    private final Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        mAddress = getIntent().getParcelableExtra(EXTRA_ADDRESS);
        mCreditCard = getIntent().getParcelableExtra(EXTRA_CREDIT_CARD);

        mProgressContainer = findViewById(R.id.progress_container);
        mEmptyView = (EmptyView) findViewById(R.id.empty_view);

        mWorkerFragment = (PlaceOrderWorkerFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_FRAGMENT_WORKER);

        if (mWorkerFragment == null) {
            mWorkerFragment = PlaceOrderWorkerFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(mWorkerFragment, TAG_FRAGMENT_WORKER)
                    .commit();
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                placeOrder();
            }
        });
    }

    private void setProgress(boolean progress) {
        mProgressContainer.setVisibility(progress ? View.VISIBLE : View.GONE);
        mEmptyView.setVisibility(progress ? View.GONE : View.VISIBLE);
    }

    private void placeOrder() {
        setProgress(true);
        mWorkerFragment.placeOrder(AccountUtils.getActiveAccountName(this), mAddress, mCreditCard);
    }

    @Override
    public void onPlaceOrderResult(String result) {
        if (result == null) {
            mEmptyView.setTitle("Payment success!");
            mEmptyView.setSubtitle("You order has been processed successfully.");
            mEmptyView.setAction("Close", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            mEmptyView.setTitle("Payment error");
            mEmptyView.setSubtitle("An error has occurred while processing yor payment, check you credit card and try again.");
            mEmptyView.setAction("Try again", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    placeOrder();
                }
            });
        }

        setProgress(false);
    }
}
