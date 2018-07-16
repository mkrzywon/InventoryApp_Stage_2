package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the InventoryApp.
 */
public final class InventoryContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_BOOKS = "inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Giving an empty constructor to set this class as a unique one.
    private InventoryContract() {
    }

    /**
     * Inner class that defines constant values for the inventory database table.
     * Each entry in the table represents a single book.
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the book data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of books.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single book.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * Resource path used for defining the image location
         */
        public static final Uri RES_URI = Uri.parse("android.resource://" + CONTENT_AUTHORITY + "/");

        /**
         * Name of database table for books
         */
        public static final String TABLE_NAME = "books";

        /**
         * Book title.
         *
         * Type: TEXT
         */
        public static final String COLUMN_BOOK_TITLE = "BookTitle";

        /**
         * Price of the book.
         *
         * Type: REAL
         */
        public static final String COLUMN_BOOK_PRICE = "BookPrice";

        /**
         * Quantity.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_QUANTITY = "Quantity";

        /**
         * Supplier name.
         *
         * The only possible values are {@link #SUPPLIER_ONE}, {@link #SUPPLIER_TWO},
         * {@link #SUPPLIER_THREE}, {@link #SUPPLIER_FOUR}, {@link #SUPPLIER_FIVE},
         * {@link #SUPPLIER_SIX}, {@link #SUPPLIER_SEVEN}.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_SUPPLIER_NAME = "SupplierName";

        /**
         * Supplier phone number.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PHONE_NUMBER = "SupplierPhoneNumber";

        /**
         * Image of the book.
         *
         * Type: TEXT
         */
        public static final String COLUMN_IMAGE = "image";

        /**
         * Possible values for the supplier's name:
         */
        public static final String SUPPLIER_ONE = "Supplier One";
        public static final String SUPPLIER_TWO = "Supplier Two";
        public static final String SUPPLIER_THREE = "Supplier Three";
        public static final String SUPPLIER_FOUR = "Supplier Four";
        public static final String SUPPLIER_FIVE = "Supplier Five";
        public static final String SUPPLIER_SIX = "Supplier Six";
        public static final String SUPPLIER_SEVEN = "Supplier Seven";

        /**
         * Returns whether or not the given supplier is {@link #SUPPLIER_ONE}, {@link #SUPPLIER_TWO},
         * {@link #SUPPLIER_THREE}, {@link #SUPPLIER_FOUR}, {@link #SUPPLIER_FIVE},
         * {@link #SUPPLIER_SIX}, {@link #SUPPLIER_SEVEN}.
         */
        public static boolean isValidSupplier(String supplier) {

            return (supplier.equals(SUPPLIER_ONE) || supplier.equals(SUPPLIER_TWO) || supplier.equals(SUPPLIER_THREE)
                    || supplier.equals(SUPPLIER_FOUR) || supplier.equals(SUPPLIER_FIVE)
                    || supplier.equals(SUPPLIER_SIX) || supplier.equals(SUPPLIER_SEVEN));
        }
    }
}


