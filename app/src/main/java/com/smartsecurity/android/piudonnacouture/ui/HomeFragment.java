package com.smartsecurity.android.piudonnacouture.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.Category;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseNavigationFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = HomeFragment.class.getSimpleName();

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    private View mToolbarShadowView;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mPagerAdapter;
    private ProgressBar mProgressBar;

    interface CategoriesQuery {
        int LOADER_ID = 0x1;
        Uri URI = PiuDonnaContract.CategoryEntry.CONTENT_URI;
        String[] PROJECTION = {
                PiuDonnaContract.CategoryEntry._ID,
                PiuDonnaContract.CategoryEntry.COLUMN_NAME,
                PiuDonnaContract.CategoryEntry.COLUMN_SERVER_ID,
        };

        int COLUMN_ID = 0;
        int COLUMN_NAME = 1;
        int COLUMN_SERVER_ID = 2;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate()");
        setHasOptionsMenu(true);
        //setRetainInstance(true);

        getLoaderManager().restartLoader(CategoriesQuery.LOADER_ID, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView()");

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        //mPagerAdapter = new Adapter(getChildFragmentManager());

        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        //mViewPager.setOffscreenPageLimit(8);
        /*Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addDummyFragment("Deals");
        adapter.addDummyFragment("Category 1");
        adapter.addDummyFragment("Category 2");
        adapter.addDummyFragment("Category 3");
        adapter.addDummyFragment("Category 4");
        adapter.addDummyFragment("Category 5");
        mViewPager.setAdapter(adapter);*/

        mTabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        //tabLayout.setupWithViewPager(pager);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);

        //mToolbarShadowView = rootView.findViewById(R.id.drop_shadow);
        //mToolbarShadowView.setVisibility(Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2
                //? View.VISIBLE : View.GONE);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated()");

        //getLoaderManager().initLoader(CategoriesQuery.LOADER_ID, null, this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader(" + id + ")");

        if (id == CategoriesQuery.LOADER_ID) {
            //mProgressBar.setVisibility(View.VISIBLE);
            //mTabLayout.setVisibility(View.GONE);
            //mViewPager.setVisibility(View.GONE);

            return new CursorLoader(getActivity(),
                    CategoriesQuery.URI,
                    CategoriesQuery.PROJECTION,
                    null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished(" + loader.getId() + ")");

        if (loader.getId() == CategoriesQuery.LOADER_ID) {
            mProgressBar.setVisibility(View.GONE);
            //mTabLayout.setVisibility(View.VISIBLE);
            //mViewPager.setVisibility(View.VISIBLE);

            Adapter adapter = new Adapter(getChildFragmentManager());

            if (data.moveToFirst()) {
                do {
                    Category category = new Category(
                            data.getLong(CategoriesQuery.COLUMN_SERVER_ID),
                            data.getString(CategoriesQuery.COLUMN_NAME));

                    Log.d(LOG_TAG, "name: " + category.getName());
                    adapter.addFragment(category);
                } while (data.moveToNext());
            }

            mViewPager.setAdapter(adapter);
            mTabLayout.setupWithViewPager(mViewPager);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        /*public void addDummyFragment(String title) {
            Bundle args = new Bundle();
            args.putString(DummyFragment.ARG_TITLE, title);

            DummyFragment fragment = new DummyFragment();
            fragment.setArguments(args);

            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }*/

        public void addFragment(Category category) {
            Bundle args = new Bundle();
            args.putParcelable(SubcategoryFragment.ARG_PARENT_CATEGORY, category);

            SubcategoryFragment fragment = new SubcategoryFragment();
            fragment.setArguments(args);

            mFragments.add(fragment);
            mFragmentTitles.add(category.getName());
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
            return mFragmentTitles.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            Log.w(LOG_TAG, "destroyItem(" + position + ")");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_home, menu);
    }

    /*public static class DummyFragment extends Fragment {
        public static final String ARG_TITLE = "arg_title";

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView view = (TextView) inflater.inflate(R.layout.fragment_dummy, container, false);
            view.setText(getArguments().getString(ARG_TITLE));
            return view;
        }
    }*/
}
