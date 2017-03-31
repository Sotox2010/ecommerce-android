package com.smartsecurity.android.piudonnacouture.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.smartsecurity.android.piudonnacouture.R;

public abstract class BaseNavigationFragment extends Fragment {
    private View mDropShadowView;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDropShadowView = view.findViewById(R.id.drop_shadow);
        if (mDropShadowView != null) {
            mDropShadowView.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    ? View.GONE : View.VISIBLE);
        }
    }
}
