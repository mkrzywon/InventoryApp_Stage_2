package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * {@link ContentProvider} for InventoryApp.
 */
public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the books table
     */
    private static final int BOOKS = 100;

    /**
     * URI matcher code for the content URI for a single book in the books table
     */
    private static final int BOOK_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.inventoryapp/inventoryapp" will map to the
        // integer code {@link #BOOKS}. This URI is used to provide access to MULTIPLE rows
        // of the books table.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKS, BOOKS);

        // The content URI of the form "content://com.example.android.inventoryapp/inventoryapp/#" will map to the
        // integer code {@link #BOOK_ID}. This URI is used to provide access to ONE single row
        // of the books table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.inventoryapp/inventoryapp/1" matches, but
        // "content://com.example.android.inventoryapp/inventoryapp" (without a number at the end) doesn't match.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    /**
     * Database helper object
     */
    private ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {

            case BOOKS:
                // For the BOOKS code, query the books table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the books table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventoryapp/inventoryapp/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the books table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:

                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }

        if (cursor != null && getContext() != null) {

            // Set notification URI on the Cursor,
            // so we know what content URI the Cursor was created for.
            // If the data at this URI changes, then we know we need to update the Cursor.
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        }

        return cursor;
    }

    /**
     * This method returns type of the Content.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);

        if (match == BOOKS) {

            return insertBook(uri, values);

        } else {

            throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a book into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBook(Uri uri, ContentValues values) {

        if (getContext() != null) {

            setTitleCondition(values, getContext().getResources().getString(R.string.insert_title));
            setSupplierCondition(values, getContext().getResources().getString(R.string.insert_supplier));
            setQuantityCondition(values, getContext().getResources().getString(R.string.insert_quantity));
            setPriceCondition(values, getContext().getResources().getString(R.string.insert_price));

        }

        // There's no need to check the supplier's phone number and image as any value is valid

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new book with the given values
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {

            if (getContext() != null) {

                Log.e(LOG_TAG, getContext().getResources().getString(R.string.throw_insertion) + uri);
            }

            return null;
        }

        if (getContext() != null) {

            // Notify all listeners that the data has changed for the book content URI
            getContext().getContentResolver().notifyChange(uri, null);

        }

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Deletes a book from the database with the given selection and selectionArgs.
     * Return the number of rows deleted.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[]
            selectionArgs) {

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {

            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (getContext() != null && rowsDeleted != 0) {

            getContext().getContentResolver().notifyChange(uri, null);

        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String
            selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);

        switch (match) {

            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOK_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update books in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more books).
     * Return the number of rows that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {

        if (getContext() != null) {

            setTitleCondition(values, getContext().getResources().getString(R.string.update_title));
            setSupplierCondition(values, getContext().getResources().getString(R.string.update_supplier));
            setQuantityCondition(values, getContext().getResources().getString(R.string.update_quantity));
            setPriceCondition(values, getContext().getResources().getString(R.string.update_price));

        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (getContext() != null && rowsUpdated != 0) {

            getContext().getContentResolver().notifyChange(uri, null);

        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Helper method for setting of the book's title conditions.
     */
    private void setTitleCondition(ContentValues values, String throwTitle) {

        String title;

        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_BOOK_TITLE)) {
            title = values.getAsString(InventoryEntry.COLUMN_BOOK_TITLE);
            if (title == null || title.equals("")) {
                throw new IllegalArgumentException(throwTitle);
            }
        }
    }

    /**
     * Helper method for setting of the supplier's name conditions.
     */
    private void setSupplierCondition(ContentValues values, String throwSupplier) {

        String supplier;

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_SUPPLIER_NAME)) {
            supplier = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
            if (supplier == null || !InventoryEntry.isValidSupplier(supplier)) {
                throw new IllegalArgumentException(throwSupplier);
            }
        }
    }

    /**
     * Helper method for setting of the quantity conditions.
     */
    private void setQuantityCondition(ContentValues values, String throwQuantity) {

        Integer quantity;

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            quantity = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException(throwQuantity);
            }
        }
    }

    /**
     * Helper method for setting of the price conditions.
     */
    private void setPriceCondition(ContentValues values, String throwPrice) {

        Double price;

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_BOOK_PRICE)) {
            // Check that the weight is greater than or equal to 0 kg
            price = values.getAsDouble(InventoryEntry.COLUMN_BOOK_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException(throwPrice);
            }
        }
    }
}
