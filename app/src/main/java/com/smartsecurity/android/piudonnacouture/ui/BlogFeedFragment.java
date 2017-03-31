package com.smartsecurity.android.piudonnacouture.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.loader.BlogFeedLoader;
import com.smartsecurity.android.piudonnacouture.model.BlogEntry;
import com.smartsecurity.android.piudonnacouture.ui.widget.EmptyView;
import com.smartsecurity.android.piudonnacouture.util.OnItemClickListener;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

public class BlogFeedFragment extends BaseNavigationFragment implements LoaderManager.LoaderCallbacks<List<BlogEntry>> {
    private static final String LOG_TAG = "BlogFeedFragment";

    /*enum UiState {
        LOADING,
        EMPTY,
        ERROR,
        DISPLAY_CONTENT
    }*/

    private RecyclerView mBlogRecyclerView;
    private BlogFeedAdapter mBlogFeedAdapter;
    private ProgressBar mProgressBar;
    private ViewStub mEmptyViewStub;
    private EmptyView mEmptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blog_feed, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        mEmptyViewStub = (ViewStub) rootView.findViewById(R.id.stub_empty_view);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBlogFeedAdapter = new BlogFeedAdapter(null);
        mBlogFeedAdapter.setOnItemClickListener((itemView, position) -> {
            Log.d(LOG_TAG, "onItemClick: " + position);
            BlogEntry entry = mBlogFeedAdapter.getBlogEntry(position);

            Intent intent = new Intent(getActivity(), BlogEntryReaderActivity.class);
            intent.putExtra(BlogEntryReaderActivity.EXTRA_ENTRY_TITLE, entry.getTitle());
            intent.putExtra(BlogEntryReaderActivity.EXTRA_ENTRY_CONTENT, entry.getContent());
            intent.putExtra(BlogEntryReaderActivity.EXTRA_ENTRY_IMAGE_URL, entry.getImageUrl());

            getActivity().startActivity(intent);
        });

        mBlogRecyclerView = (RecyclerView) view.findViewById(R.id.blog_feed_recycler_view);
        mBlogRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        mBlogRecyclerView.setAdapter(mBlogFeedAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEmptyView = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<BlogEntry>> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        mBlogRecyclerView.setVisibility(View.GONE);
        setEmptyViewVisibility(View.GONE);
        return new BlogFeedLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<BlogEntry>> loader, List<BlogEntry> entries) {
        mProgressBar.setVisibility(View.GONE);

        if (entries == null) {
            mBlogRecyclerView.setVisibility(View.GONE);
            showErrorEmptyView();
        } else if (entries.size() == 0) {
            mBlogRecyclerView.setVisibility(View.GONE);
            showEmptyFeedView();
        } else {
            mBlogRecyclerView.setVisibility(View.VISIBLE);
            mBlogFeedAdapter.setBlogEntries(entries);
            setEmptyViewVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<BlogEntry>> loader) {
        mBlogFeedAdapter.setBlogEntries(null);
    }

    private void reloadFeedData() {
        getLoaderManager().restartLoader(0, null, BlogFeedFragment.this);
    }

    private void setEmptyViewVisibility(int visibility) {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(visibility);
        }
    }

    private void showEmptyFeedView() {
        if (mEmptyView == null) {
            mEmptyView = (EmptyView) mEmptyViewStub.inflate();
        }

        // TODO: Change to string resources.
        mEmptyView.reset();
        mEmptyView.setImageResource(R.drawable.ic_empty_newsfeed);
        mEmptyView.setTitle("Nothing to see here");
        mEmptyView.setSubtitle("There are no news by the time, check later.");
        mEmptyView.setAction("Try Again", v -> reloadFeedData());
        mEmptyView.setVisibility(View.VISIBLE);

    }

    private void showErrorEmptyView() {
        if (mEmptyView == null) {
            mEmptyView = (EmptyView) mEmptyViewStub.inflate();
        }

        // TODO: Change to string resources.
        mEmptyView.reset();
        mEmptyView.setTitle("No connection");
        mEmptyView.setSubtitle("Check your network state and try again.");
        mEmptyView.setAction("Try Again", v -> reloadFeedData());
        mEmptyView.setVisibility(View.VISIBLE);
    }

    private class BlogFeedAdapter extends RecyclerView.Adapter<BlogFeedAdapter.ViewHolder> {

        private List<BlogEntry> mBlogEntries;
        private OnItemClickListener mItemClickListener;

        public BlogFeedAdapter(List<BlogEntry> entries) {
            mBlogEntries = entries;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_blog_entry, parent, false);

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
        public void onBindViewHolder(ViewHolder holder, int position) {
            BlogEntry entry = mBlogEntries.get(position);

            holder.mTitleView.setText(WordUtils.capitalizeFully(entry.getTitle()));
            //holder.mDateView.setText();
            holder.mExcerptView.setText(entry.getContent());

            Picasso.with(getActivity())
                    .load(entry.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mBlogEntries != null ? mBlogEntries.size() : 0;
        }

        public void setBlogEntries(List<BlogEntry> entries) {
            mBlogEntries = entries;
            notifyDataSetChanged();
        }

        public BlogEntry getBlogEntry(int position) {
            return mBlogEntries.get(position);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            mItemClickListener = listener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView mImageView;
            public TextView mTitleView;
            public TextView mDateView;
            public TextView mExcerptView;

            public ViewHolder(View itemView) {
                super(itemView);
                mImageView = (ImageView) itemView.findViewById(R.id.image);
                mTitleView = (TextView) itemView.findViewById(R.id.title);
                mDateView = (TextView) itemView.findViewById(R.id.date);
                mExcerptView = (TextView) itemView.findViewById(R.id.excerpt);
            }
        }

    }
}
