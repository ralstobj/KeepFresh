package com.rgw.keepfresh;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.rgw.keepfresh.app.AppController;
import com.rgw.keepfresh.data.ProductContract.ProductEntry;
import com.rgw.keepfresh.data.QueryUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.fragment_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        TextView expirationDateView = (TextView)view.findViewById(R.id.expDate);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        NetworkImageView thumbNail = (NetworkImageView) view.findViewById(R.id.pImage);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int barcodeColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_BARCODE);
        int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGEURL);
        int dayColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_DATE_DAY);
        int monthColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_DATE_MONTH);
        int yearColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_DATE_YEAR);
        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        String productBarcode = cursor.getString(barcodeColumnIndex);
        String imageUrl = cursor.getString(imageColumnIndex);
        Calendar newDate = Calendar.getInstance();
        newDate.set(cursor.getInt(yearColumnIndex), cursor.getInt(monthColumnIndex), cursor.getInt(dayColumnIndex));
        DateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        expirationDateView.setText(dateFormatter.format(newDate.getTime()));
        thumbNail.setImageUrl(imageUrl, imageLoader);
        QueryUtils.setImageUrl(imageUrl);
        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        summaryTextView.setText(productBarcode);

    }
}
