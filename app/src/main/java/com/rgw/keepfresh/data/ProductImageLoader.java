package com.rgw.keepfresh.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by baile on 1/22/2017.
 */

public class ProductImageLoader extends AsyncTaskLoader<Bitmap>

{

    private static final String LOG_TAG = ProductLoader.class.getName();

    private String mUrl;

    public ProductImageLoader(Context context) {
        super(context);

    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Bitmap loadInBackground() {
        return QueryUtils.getProductImage();
    }
}
