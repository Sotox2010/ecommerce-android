package com.smartsecurity.android.piudonnacouture.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.smartsecurity.android.piudonnacouture.R;

public class CreateAccountActivity extends BaseActivity /*implements SignUpFragment.Callbacks*/ {

    /*private static final String TAG = "CreateAccountActivity";

    public static final String TAG_FRAGMENT_CREATE_ACCOUNT = "fragment_sign_up";

    private ProgressBar mToolbarProgress;
    private boolean mIsBackEnabled = true;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_or_create_account);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbarProgress = (ProgressBar) toolbar.findViewById(R.id.toolbar_progress);

        FragmentManager fm = getSupportFragmentManager();
        Fragment createAccountFragment = fm.findFragmentByTag(TAG_FRAGMENT_CREATE_ACCOUNT);

        if (createAccountFragment == null) {
            createAccountFragment = SignUpFragment.newInstance();
            fm.beginTransaction()
                    .add(R.id.container, createAccountFragment, TAG_FRAGMENT_CREATE_ACCOUNT)
                    .commit();
        } else {
            fm.beginTransaction()
                    .attach(createAccountFragment)
                    .commit();
        }
    }

    @Override
    public void setToolbarProgressEnabled(boolean enabled) {
        mToolbarProgress.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setDisplayHomeAsUpEnabled(boolean enabled) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
        mIsBackEnabled = enabled;
    }

    @Override
    public void onBackPressed() {
        if (mIsBackEnabled) {
            super.onBackPressed();
        }
    }*/
}
