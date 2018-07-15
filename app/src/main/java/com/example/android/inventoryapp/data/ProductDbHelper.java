package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Database helper for InventoryApp. Creates database and provides version management.
 */
class ProductDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ProductDbHelper}.
     *
     * @param context of the app
     */
    public ProductDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    /**
     * Creates the construction of the database.
     */
    @Override
    public void onCreate(SQLiteDatabase base) {
        // Create a String that contains the SQL statement to create the books's table
        String dbQuery = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_BOOK_TITLE + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_BOOK_PRICE + " REAL NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PHONE_NUMBER + " TEXT, "
                + InventoryEntry.COLUMN_IMAGE + " TEXT);";

        // Execute the SQL statement
        base.execSQL(dbQuery);

        Log.i(LOG_TAG, "dbQuery log data" + dbQuery);

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase base, int oldVersion, int newVersion) {
        // So far there is no new version of the database and there is no
        // reason to execute onUpgrade method
    }
}
