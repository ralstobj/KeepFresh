package com.ralstwolfe.ralstonb.keepfresh.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;


/**
 * API Contract for the Keep Fresh App
 */
public final class ProductContract {

    private ProductContract() {}


    public static final String CONTENT_AUTHORITY = "com.ralstwolfe.ralstonb.keepfresh";


    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_PRODUCTS = "products";


    public static final class ProductEntry implements BaseColumns {


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);


        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;


        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;


        public final static String TABLE_NAME = "product";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * Product Barcode.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_BARCODE = "barcode";

        }
    }


