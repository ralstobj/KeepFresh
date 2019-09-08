package com.rgw.keepfresh.data;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by baile on 1/19/2017.
 */

public class ProductLoader extends AsyncTaskLoader<String> {

    private static final String LOG_TAG = ProductLoader.class.getName();

    private String mUrl;

    public ProductLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        return QueryUtils.getProductName(mUrl);
    }
}
