package com.smartsecurity.android.piudonnacouture.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.Category;
import com.smartsecurity.android.piudonnacouture.model.Subcategory;
import com.smartsecurity.android.piudonnacouture.provider.PiuDonnaContract;
import com.smartsecurity.android.piudonnacouture.util.OnItemClickListener;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

public class SubcategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SubcategoryFragment.class.getSimpleName();

    public static final String ARG_PARENT_CATEGORY = "arg_parent_category";

    private ActionBar mActionBar;
    private RecyclerView mRecyclerView;
    private SubcategoriesAdapter mSubcategoriesAdapter;
    private View mEmptyView;

    private Category mParentCategory;

    interface SubcategoriesQuery {
        int LOADER_ID = 0x2;
        Uri URI = PiuDonnaContract.SubcategoryEntry.CONTENT_URI;
        String[] PROJECTION = {
                PiuDonnaContract.SubcategoryEntry._ID,
                PiuDonnaContract.SubcategoryEntry.COLUMN_NAME,
                PiuDonnaContract.SubcategoryEntry.COLUMN_SERVER_ID,
                PiuDonnaContract.SubcategoryEntry.COLUMN_IMAGE_URL,
                PiuDonnaContract.SubcategoryEntry.COLUMN_CATEGORY_ID
        };
        String SELECTION = PiuDonnaContract.SubcategoryEntry.COLUMN_CATEGORY_ID + " = ?";
        String SORT_ORDER = PiuDonnaContract.SubcategoryEntry.COLUMN_NAME + " ASC";

        int COLUMN_NAME = 1;
        int COLUMN_SERVER_ID = 2;
        int COLUMN_IMAGE_URL = 3;
        int COLUMN_CATEGORY_ID = 4;
    }

    private OnItemClickListener mSubcategoryItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(View itemView, int position) {
            Intent intent = new Intent(getActivity(), ProductsActivity.class);
            intent.putExtra(ProductsActivity.EXTRA_PARENT_CATEGORY, mParentCategory);
            intent.putExtra(ProductsActivity.EXTRA_PARENT_SUBCATEGORY,
                    mSubcategoriesAdapter.getSubcategory(position));
            getActivity().startActivity(intent);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(LOG_TAG, "onCreate()");

        mParentCategory = getArguments().getParcelable(ARG_PARENT_CATEGORY);
        //getLoaderManager().restartLoader(SubcategoriesQuery.LOADER_ID, null, this);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w(LOG_TAG, "onCreateView()");

        View rootView = inflater.inflate(R.layout.fragment_subcategory, container, false);

        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        mSubcategoriesAdapter = new SubcategoriesAdapter(null);
        mSubcategoriesAdapter.setOnItemClickListener(mSubcategoryItemClickListener);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.subcategories_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.column_span_subcategories_grid)));
        mRecyclerView.setAdapter(mSubcategoriesAdapter);

        mEmptyView = rootView.findViewById(R.id.empty_view);
        //((TextView) mEmptyView).setText(mParentCategory.getName());

        /*final int tabsHeight = getResources().getDimensionPixelSize(R.dimen.tabs_height);
        final int spacingSmall = getResources().getDimensionPixelSize(R.dimen.spacing_small);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int toolbarHeight = mActionBar.getHeight();
                mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(),
                        spacingSmall + toolbarHeight + tabsHeight,
                        mRecyclerView.getPaddingRight(),
                        mRecyclerView.getPaddingBottom());
            }
        });*/

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.w(LOG_TAG, "onActivityCreated()");
        getLoaderManager().initLoader(SubcategoriesQuery.LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.w(LOG_TAG, "onCreateLoader(" + id + ")");

        if (id == SubcategoriesQuery.LOADER_ID) {
            return new CursorLoader(getActivity(),
                    SubcategoriesQuery.URI,
                    SubcategoriesQuery.PROJECTION,
                    SubcategoriesQuery.SELECTION,
                    new String[]{Long.toString(mParentCategory.getServerId())},
                    SubcategoriesQuery.SORT_ORDER);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.w(LOG_TAG, "onLoadFinished(" + loader.getId() + ")");

        if (loader.getId() == SubcategoriesQuery.LOADER_ID) {
            reloadSubcategoriesData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void reloadSubcategoriesData(Cursor cursor) {
        mEmptyView.setVisibility(cursor.getCount() == 0 ? View.VISIBLE : View.GONE);

        List<Subcategory> subcategories = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Subcategory subcategory = new Subcategory(
                        cursor.getLong(SubcategoriesQuery.COLUMN_SERVER_ID),
                        cursor.getString(SubcategoriesQuery.COLUMN_NAME),
                        cursor.getString(SubcategoriesQuery.COLUMN_IMAGE_URL),
                        cursor.getLong(SubcategoriesQuery.COLUMN_CATEGORY_ID));

                subcategories.add(subcategory);
                Log.d(LOG_TAG, subcategory.getName());
            } while (cursor.moveToNext());
        }

        mSubcategoriesAdapter.setSubcategories(subcategories);
    }

    private class SubcategoriesAdapter extends RecyclerView.Adapter<SubcategoriesAdapter.ViewHolder> {

        private List<Subcategory> mSubcategories;
        private OnItemClickListener mItemClickListener;

        public SubcategoriesAdapter(@Nullable List<Subcategory> subcategories) {
            mSubcategories = subcategories;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getActivity().getLayoutInflater().inflate(
                    R.layout.list_item_subcategory, parent, false);

            final ViewHolder holder = new ViewHolder(itemView);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (mItemClickListener != null && position != RecyclerView.NO_POSITION) {
                        mItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Subcategory subcategory = mSubcategories.get(position);

            holder.mSubcategoryNameView.setText(WordUtils.capitalizeFully(subcategory.getName()));
            Picasso.with(getActivity())
                    .load(subcategory.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mSubcategories != null ? mSubcategories.size() : 0;
        }

        public Subcategory getSubcategory(int position) {
            return mSubcategories != null ? mSubcategories.get(position) : null;
        }

        public void setSubcategories(@Nullable List<Subcategory> subcategories) {
            mSubcategories = subcategories;
            notifyDataSetChanged();
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            mItemClickListener = listener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mImageView;
            TextView mSubcategoryNameView;

            public ViewHolder(View itemView) {
                super(itemView);

                mImageView = (ImageView) itemView.findViewById(R.id.image);
                mSubcategoryNameView = (TextView) itemView.findViewById(R.id.subcategory_name);
            }
        }
    }
}
