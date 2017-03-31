package com.smartsecurity.android.piudonnacouture.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.Address;
import com.smartsecurity.android.piudonnacouture.model.CreditCard;
import com.smartsecurity.android.piudonnacouture.ui.checkout.PaymentCheckoutFragment;
import com.smartsecurity.android.piudonnacouture.ui.checkout.ReviewCheckoutFragment;
import com.smartsecurity.android.piudonnacouture.ui.checkout.ShippingCheckoutFragment;
import com.smartsecurity.android.piudonnacouture.ui.widget.Stepper;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends BaseActivity {
    private static final String TAG = "CheckoutActivity";

    private static final String STATE_SELECTED_ADDRESS = "state_selected_address";
    private static final String STATE_CREDIT_CARD = "state_credit_card";
    private static final String STATE_FRAGMENT_SHIPPING = "state_checkout_shipping";
    private static final String STATE_FRAGMENT_PAYMENT = "state_checkout_payment";
    private static final String STATE_FRAGMENT_REVIEW = "state_checkout_review";

    private ViewPager mViewPager;
    private CheckoutPagerAdapter mStepperAdapter;

    private ShippingCheckoutFragment mShippingFragment;
    private PaymentCheckoutFragment mPaymentFragment;
    private ReviewCheckoutFragment mReviewFragment;

    private Address mSelectedAddress;
    private CreditCard mCreditCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set this activity's window as "Secure".
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_checkout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            mShippingFragment = (ShippingCheckoutFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, STATE_FRAGMENT_SHIPPING);

            mPaymentFragment = (PaymentCheckoutFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, STATE_FRAGMENT_PAYMENT);

            mReviewFragment = (ReviewCheckoutFragment) getSupportFragmentManager()
                    .getFragment(savedInstanceState, STATE_FRAGMENT_REVIEW);
        } else {
            mShippingFragment = ShippingCheckoutFragment.newInstance();
            mPaymentFragment = PaymentCheckoutFragment.newInstance();
            mReviewFragment = ReviewCheckoutFragment.newInstance();
        }

        /*stepper.addStep(new Step.Builder(this).setTitle(getString(R.string.step_shipping)).build());
        stepper.addStep(new Step.Builder(this).setTitle(getString(R.string.step_payment)).build());
        stepper.addStep(new Step.Builder(this).setTitle(getString(R.string.step_review)).build());*/

        mStepperAdapter = new CheckoutPagerAdapter(getSupportFragmentManager());
        mStepperAdapter.addFragment(mShippingFragment);
        mStepperAdapter.addFragment(mPaymentFragment);
        mStepperAdapter.addFragment(mReviewFragment);

        mViewPager = (ViewPager) findViewById(R.id.steps_pager);
        mViewPager.setAdapter(mStepperAdapter);
        mViewPager.setOffscreenPageLimit(2);

        Stepper stepper = (Stepper) findViewById(R.id.stepper);
        stepper.setViewPager(mViewPager);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState()");
        outState.putParcelable(STATE_SELECTED_ADDRESS, mSelectedAddress);
        outState.putParcelable(STATE_CREDIT_CARD, mCreditCard);

        getSupportFragmentManager().putFragment(outState, STATE_FRAGMENT_SHIPPING, mShippingFragment);
        getSupportFragmentManager().putFragment(outState, STATE_FRAGMENT_PAYMENT, mPaymentFragment);
        getSupportFragmentManager().putFragment(outState, STATE_FRAGMENT_REVIEW, mReviewFragment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);

        mSelectedAddress = savedInstanceState.getParcelable(STATE_SELECTED_ADDRESS);
        mCreditCard = savedInstanceState.getParcelable(STATE_CREDIT_CARD);

        if (mSelectedAddress != null) {
            Log.d(TAG, "onRestoreInstanceState:\n" + mSelectedAddress.toString());
        }
        if (mCreditCard != null) {
            Log.d(TAG, "onRestoreInstanceState:\n" + mCreditCard.toString());
        }
    }

    public Address getSelectedAddress() {
        return mSelectedAddress;
    }

    public CreditCard getCreditCard() {
        return mCreditCard;
    }

    public void prevStep() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
    }

    public void nextStep() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    public void proceedToPayment(Address address) {
        Log.d(TAG, "proceedToPayment: " + address.toString());

        mSelectedAddress = address;
        nextStep();
    }

    public void proceedToReview(CreditCard card) {
        Log.d(TAG, "proceedToReview: " + card.toString());

        mCreditCard = card;
        nextStep();
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() > 0) {
            prevStep();
            return;
        }

        super.onBackPressed();
    }

    public void placeOrder() {
        Intent intent = new Intent(this, OrderConfirmationActivity.class);
        intent.putExtra(OrderConfirmationActivity.EXTRA_ADDRESS, mSelectedAddress);
        intent.putExtra(OrderConfirmationActivity.EXTRA_CREDIT_CARD, mCreditCard);

        startActivity(intent);
        finish();
    }

    private class CheckoutPagerAdapter extends Stepper.StepperPagerAdapter {

        private List<Fragment> mFragments = new ArrayList<>();

        private int[] mStepTitleStringRes = {
                R.string.step_shipping,
                R.string.step_payment,
                R.string.step_review
        };

        public CheckoutPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragments.get(position).getClass().getSimpleName();
        }

        @Override
        public CharSequence getStepTitle(int position) {
            return getString(mStepTitleStringRes[position]);
        }

        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
        }
    }
}
