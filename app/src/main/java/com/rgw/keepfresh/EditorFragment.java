package com.rgw.keepfresh;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rgw.keepfresh.data.ProductContract.ProductEntry;
import com.rgw.keepfresh.data.ProductImageLoader;
import com.rgw.keepfresh.data.ProductLoader;
import com.rgw.keepfresh.data.QueryUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditorFragment extends Fragment {

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int EXISTING_PRODUCT_NAME_LOADER = 1;
    private static final int EXISTING_IMAGE_LOADER = 2;
    private static final String UPC_REQUEST_URL = "https://api.upcitemdb.com/prod/trial/lookup";
    /**
     * View for the fragment
     */
    View view;
    private DatePickerDialog expirationdateDialog;
    private SimpleDateFormat dateFormatter;
    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;
    private String barcodeText;
    private Button mScanButton;
    private Button dateButton;
    /**
     * EditText field to enter the product name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the product barcode
     */
    private EditText mBarcodeEditText;
    private ImageView mImageView;
    private LoaderManager.LoaderCallbacks<Bitmap> picLoaderListener = new LoaderManager.LoaderCallbacks<Bitmap>() {
        @Override
        public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
            return new ProductImageLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
            if (data != null) {
                mImageView.setImageBitmap(data);
            } else {
                Toast.makeText(getActivity(), "Unable to find Photo",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<Bitmap> loader) {

        }
    };
    private LoaderManager.LoaderCallbacks<String> nameLoaderListener = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            Uri baseUri = Uri.parse(UPC_REQUEST_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendQueryParameter("upc", barcodeText);

            return new ProductLoader(getContext(), uriBuilder.toString());
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            if (data != null) {
                mNameEditText.setText(data);
                getLoaderManager().initLoader(EXISTING_IMAGE_LOADER, null, picLoaderListener);
            } else {
                Toast.makeText(getActivity(), "Please Manually Input Name",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    };
    private TextView dateTextView;
    private LoaderManager.LoaderCallbacks<Cursor> databaseLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Since the editor shows all product attributes, define a projection that contains
            // all columns from the product table
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
                    mCurrentProductUri,         // Query the content URI for the current product
                    projection,             // Columns to include in the resulting Cursor
                    null,                   // No selection clause
                    null,                   // No selection arguments
                    null);                  // Default sort order
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            // Bail early if the cursor is null or there is less than 1 row in the cursor
            if (cursor == null || cursor.getCount() < 1) {
                return;
            }

            // Proceed with moving to the first row of the cursor and reading data from it
            // (This should be the only row in the cursor)
            if (cursor.moveToFirst()) {
                // Find the columns of product attributes that we're interested in
                int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
                int barcodeColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_BARCODE);
                int urlColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGEURL);
                int dayColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_DATE_DAY);
                int monthColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_DATE_MONTH);
                int yearColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_DATE_YEAR);
                // Extract out the value from the Cursor for the given column index
                String name = cursor.getString(nameColumnIndex);
                String barcode = cursor.getString(barcodeColumnIndex);
                String imageUrl = cursor.getString(urlColumnIndex);
                Calendar newDate = Calendar.getInstance();
                newDate.set(cursor.getInt(yearColumnIndex), cursor.getInt(monthColumnIndex), cursor.getInt(dayColumnIndex));
                DateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
                dateTextView = (TextView) getActivity().findViewById(R.id.dateText);
                dateTextView.setText(dateFormatter.format(newDate.getTime()));
                // Update the views on the screen with the values from the database
                QueryUtils.setImageUrl(imageUrl);
                getLoaderManager().initLoader(EXISTING_IMAGE_LOADER, null, picLoaderListener);
                mNameEditText = (EditText) getActivity().findViewById(R.id.edit_product_name);
                mBarcodeEditText = (EditText) getActivity().findViewById(R.id.edit_product_barcode);
                mNameEditText.setText(name);
                mBarcodeEditText.setText(barcode);


            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // If the loader is invalidated, clear out all the data from the input fields.
            mNameEditText.setText("");
            mBarcodeEditText.setText("");
            dateTextView.setText("");
        }
    };
    private Calendar newDate = Calendar.getInstance();
    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    public EditorFragment() {
        // Required empty public constructor
    }

    public void setmCurrentProductUri(Uri uri) {
        mCurrentProductUri = uri;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.editor_fragment, container, false);
        return view;
    }

    public void setBarcodeText(String s) {
        barcodeText = s;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey("uri")) {
            mCurrentProductUri = Uri.parse(args.getString("uri"));
        }

        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            getActivity().setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            getActivity().invalidateOptionsMenu();
            mScanButton = (Button) getActivity().findViewById(R.id.scanButton);
            mScanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragmentManager().beginTransaction().replace(R.id.content, new BarcodeUI(), "UI").commit();
                }
            });
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            getActivity().setTitle(getString(R.string.editor_activity_title_edit_product));
            mScanButton = (Button) getActivity().findViewById(R.id.scanButton);
            mScanButton.setVisibility(View.GONE);
            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, databaseLoaderListener);
        }

        dateTextView = (TextView) getActivity().findViewById(R.id.dateText);
        dateFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        expirationdateDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                newDate.set(year, monthOfYear, dayOfMonth);
                dateTextView.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        dateButton = (Button) getActivity().findViewById(R.id.dateButton);
        expirationdateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expirationdateDialog.show();
            }
        });
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) getActivity().findViewById(R.id.edit_product_name);
        mBarcodeEditText = (EditText) getActivity().findViewById(R.id.edit_product_barcode);
        mImageView = (ImageView) getActivity().findViewById(R.id.productImage);
        mNameEditText.setOnTouchListener(mTouchListener);
        dateButton.setOnTouchListener(mTouchListener);
        mBarcodeEditText.setOnTouchListener(mTouchListener);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Get user input from editor and save product into database.
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String barcodeString = mBarcodeEditText.getText().toString().trim();


        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(barcodeString)) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_BARCODE, barcodeString);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGEURL, QueryUtils.imageUrl);
        values.put(ProductEntry.COLUMN_DATE_DAY, newDate.get(Calendar.DAY_OF_MONTH));
        values.put(ProductEntry.COLUMN_DATE_MONTH, newDate.get(Calendar.MONTH));
        values.put(ProductEntry.COLUMN_DATE_YEAR, newDate.get(Calendar.YEAR));


        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getActivity().getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(getActivity(), getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(getActivity(), getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getActivity().getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(getActivity(), getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(getActivity(), getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.editor_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeText != null) {
            mBarcodeEditText.setText(barcodeText);
            getLoaderManager().initLoader(EXISTING_PRODUCT_NAME_LOADER, null, nameLoaderListener);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                getActivity().setTitle(getString(R.string.timer_fragment_title));
                MainActivity.hideKeyboard(getActivity());
                getFragmentManager().popBackStackImmediate();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    getActivity().setTitle(getString(R.string.timer_fragment_title));
                    MainActivity.hideKeyboard(getActivity());
                    getFragmentManager().popBackStackImmediate();
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.hideKeyboard(getActivity());
                                getFragmentManager().popBackStackImmediate();
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getActivity().getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(getActivity(), getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(getActivity(), getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        getActivity().setTitle(getString(R.string.timer_fragment_title));
        getFragmentManager().popBackStackImmediate();
    }

}
