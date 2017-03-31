package com.smartsecurity.android.piudonnacouture.ui;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.smartsecurity.android.piudonnacouture.R;

public class SearchActivity extends BaseActivity {

    private AutoCompleteTextView mSearchEditText;
    private ImageView mClearButton;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Tint the navigation icon with the App Accent Color.
        int accentColor = ContextCompat.getColor(this, R.color.theme_accent);
        Drawable navIcon = toolbar.getNavigationIcon();
        navIcon.setColorFilter(new PorterDuffColorFilter(accentColor, PorterDuff.Mode.SRC_IN));

        mSearchEditText = (AutoCompleteTextView) toolbar.findViewById(R.id.search_edit_text);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mClearButton.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
            }
        });


        mClearButton = (ImageView) toolbar.findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEditText.setText(null);
            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);

        // Get the underlying search view and set it up.
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        setupSearchView(searchView);

        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupSearchView(SearchView searchView) {
        //searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);

        //Drawable drawable = getSupportActionBar()
    }
}
