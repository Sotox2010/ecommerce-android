package com.smartsecurity.android.piudonnacouture.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.smartsecurity.android.piudonnacouture.model.BlogEntry;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import java.util.List;

import retrofit.RetrofitError;

public class BlogFeedLoader extends AsyncTaskLoader<List<BlogEntry>> {

    private static final String LOG_TAG = "BlogFeedLoader";

    private List<BlogEntry> mBlogFeedCache;

    public BlogFeedLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        Log.d(LOG_TAG, "onStartLoading()");

        if (mBlogFeedCache != null) {
            // Deliver cached data.
            deliverResult(mBlogFeedCache);
        } else {
            // We have no data, so start loading it.
            forceLoad();
        }
    }

    @Override
    public List<BlogEntry> loadInBackground() {
        List<BlogEntry> feed = null;

        try {
            feed = SyncUtils.sWebService.getBlogFeed();
        } catch (RetrofitError error) {
            error.printStackTrace();
        }

        return feed;
    }

    @Override
    public void deliverResult(List<BlogEntry> feed) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            mBlogFeedCache = null;
            return;
        }

        mBlogFeedCache = feed;

        if (isStarted()) {
            super.deliverResult(mBlogFeedCache);
        }
    }


    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
        mBlogFeedCache = null;
    }

}
