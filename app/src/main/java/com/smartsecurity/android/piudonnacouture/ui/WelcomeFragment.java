package com.smartsecurity.android.piudonnacouture.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.ui.widget.EmptyView;

public class WelcomeFragment extends Fragment {

    private static final String TAG = "WelcomeFragment";

    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }

    private AuthenticatorActivity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AuthenticatorActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);

        EmptyView welcomeEmptyView = (EmptyView) rootView.findViewById(R.id.welcome_empty_view);
        welcomeEmptyView.setAction("Skip to home", (View view) -> {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });

        rootView.findViewById(R.id.create_account_button).setOnClickListener(v -> mActivity.switchToSignUpFragment());
        rootView.findViewById(R.id.sign_in_button).setOnClickListener(v -> mActivity.switchToSignInFragment());

        return rootView;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onStart() {
        super.onStart();
        if (mActivity != null) {
            //mActivity.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.theme_accent_dark));
            mActivity.setActionBarShown(false);
            mActivity.getSupportActionBar().setTitle(null);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
