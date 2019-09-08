package com.rgw.keepfresh;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.rgw.keepfresh.data.ProductContract.ProductEntry;
import com.rgw.keepfresh.data.ProductImageLoader;


/**
 * Created by Bailey on 1/9/2017.
 */

public class TimerFragment extends ListFragment {

    /**
     * Identifier for the product data loader.
     */
    private static final int PRODUCT_LOADER = 0;
    /**
     * The View for fragment.
     */
    View view;

    intentData it;
    /**
     * The Adapter for ListView
     */
    ProductCursorAdapter mCursorAdapter;

    /**
     * Cursor Loader to get data from SQLite.
     */
    private LoaderManager.LoaderCallbacks<Cursor> dataListener = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Define a projection that specifies the columns from the table we care about.
            String[] projection = {
                    ProductEntry._ID,
                    ProductEntry.COLUMN_PRODUCT_NAME,
                    ProductEntry.COLUMN_PRODUCT_BARCODE,
                    ProductEntry.COLUMN_PRODUCT_IMAGEURL,
                    ProductEntry.COLUMN_DATE_DAY,
                    ProductEntry.COLUMN_DATE_MONTH,
                    ProductEntry.COLUMN_DATE_YEAR};

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(getActivity(),   // Parent activity context
                    ProductEntry.CONTENT_URI,   // Provider content URI to query
                    projection,             // Columns to include in the resulting Cursor
                    null,                   // No selection clause
                    null,                   // No selection arguments
                    null);                  // Default sort order
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mCursorAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mCursorAdapter.swapCursor(null);
        }
    };

    /**
     * Inflates the list layout XML to show the Timer Fragment UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.list, container, false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    /**
     * When the fragment is created the option menu is enabled as well.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * When fragment is created the adapter is attached to the list and a FAB is created.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCursorAdapter = new ProductCursorAdapter(getActivity(), null);

        // Setup FAB to open EditorFragment.
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                it.recieveUri(null);

            }
        });

        setListAdapter(mCursorAdapter);
        //Data begins being pulled from Database.
        getActivity().getLoaderManager().initLoader(PRODUCT_LOADER, null, dataListener);
    }

    /**
     * What happens when an item in the list is pressed,
     * sends an "intent" to the activity which passes it to an editor fragment.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
        it.recieveUri(currentProductUri);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            it = (intentData) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IntentData");
        }
    }

    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getActivity().getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("TimerFragment", rowsDeleted + " rows deleted from product database");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_timer, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Interface to allow for data communication between TimerFragment and Editor Fragment.
     */
    public interface intentData {
        public void recieveUri(Uri uri);
    }
}
