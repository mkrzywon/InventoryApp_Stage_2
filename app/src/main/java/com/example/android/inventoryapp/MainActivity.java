package com.example.android.inventoryapp;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Displays our list of books entered into the database
 */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String KEY_INFO = "info";
    private static final String KEY_URI = "uri";

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Identifier for the book data loader
     */
    private static final int BOOK_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    private BookAdapter mCursorAdapter;

    /**
     * FloatingActionButton for the ListView
     */
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ListView which will be populated with the book data
        ListView bookList = findViewById(R.id.book_list);

        // Setup an Adapter to create a list item for each row of book data in the Cursor.
        // There is no pet book yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new BookAdapter(this, null);
        bookList.setAdapter(mCursorAdapter);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        final View emptyView = findViewById(R.id.empty_view);
        bookList.setEmptyView(emptyView);

        // Setup the item click listener
        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Uri currentBookUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                Bundle bundle = new Bundle();

                bundle.putParcelable(KEY_URI, currentBookUri);

                FragmentManager fm = getSupportFragmentManager();
                final InfoFragment.InfoDialog infoDialog = new InfoFragment.InfoDialog();
                infoDialog.setArguments(bundle);

                infoDialog.show(fm, KEY_INFO);

                MediaPlayer mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.drum);
                mMediaPlayer.start();

                Log.i(LOG_TAG, getString(R.string.log_current_book_uri) + currentBookUri.toString());

                hideFab();

            }
        });

        // Setup FAB to open EditorActivity
        fab = findViewById(R.id.fab);
        setFabAnimation(fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);

                MediaPlayer mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell);
                mMediaPlayer.start();
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    /**
     * Method for showing the fab button
     */
    public void showFab() {
        fab.show();
    }

    /**
     * Method for hiding the fab button
     */
    public void hideFab() {
        fab.hide();
    }

    /**
     * This method inserts example data into database
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void insertExampleData() {

        ArrayList<ContentValues> valuesArrayList = new ArrayList<>();

        // Create a ContentValues object where column names are the keys,
        // and example book attributes represent the values
        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_BOOK_TITLE, getResources().getString(R.string.example_title1));
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, InventoryEntry.SUPPLIER_SEVEN);
        values.put(InventoryEntry.COLUMN_PHONE_NUMBER, getResources().getString(R.string.example_phone_number1));
        values.put(InventoryEntry.COLUMN_QUANTITY, 10);
        values.put(InventoryEntry.COLUMN_BOOK_PRICE, 27.00);
        values.put(InventoryEntry.COLUMN_IMAGE, InventoryEntry.RES_URI.toString() + R.drawable.android_desing_patterns_book);
        valuesArrayList.add(values);

        values = new ContentValues();
        values.put(InventoryEntry.COLUMN_BOOK_TITLE, getResources().getString(R.string.example_title2));
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, InventoryEntry.SUPPLIER_ONE);
        values.put(InventoryEntry.COLUMN_PHONE_NUMBER, getResources().getString(R.string.example_phone_number2));
        values.put(InventoryEntry.COLUMN_QUANTITY, 5);
        values.put(InventoryEntry.COLUMN_BOOK_PRICE, 25.00);
        values.put(InventoryEntry.COLUMN_IMAGE, InventoryEntry.RES_URI.toString() + R.drawable.android_application_development_book);
        valuesArrayList.add(values);

        values = new ContentValues();
        values.put(InventoryEntry.COLUMN_BOOK_TITLE, getResources().getString(R.string.example_title3));
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, InventoryEntry.SUPPLIER_FIVE);
        values.put(InventoryEntry.COLUMN_PHONE_NUMBER, getResources().getString(R.string.example_phone_number3));
        values.put(InventoryEntry.COLUMN_QUANTITY, 14);
        values.put(InventoryEntry.COLUMN_BOOK_PRICE, 31.00);
        values.put(InventoryEntry.COLUMN_IMAGE, InventoryEntry.RES_URI.toString() + R.drawable.java_deep_learning_book);
        valuesArrayList.add(values);

        values = new ContentValues();
        values.put(InventoryEntry.COLUMN_BOOK_TITLE, getResources().getString(R.string.example_title4));
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, InventoryEntry.SUPPLIER_SIX);
        values.put(InventoryEntry.COLUMN_PHONE_NUMBER, getResources().getString(R.string.example_phone_number4));
        values.put(InventoryEntry.COLUMN_QUANTITY, 7);
        values.put(InventoryEntry.COLUMN_BOOK_PRICE, 28.00);
        values.put(InventoryEntry.COLUMN_IMAGE, InventoryEntry.RES_URI.toString() + R.drawable.object_oriented_javascript);
        valuesArrayList.add(values);

        values = new ContentValues();
        values.put(InventoryEntry.COLUMN_BOOK_TITLE, getResources().getString(R.string.example_title5));
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, InventoryEntry.SUPPLIER_THREE);
        values.put(InventoryEntry.COLUMN_PHONE_NUMBER, getResources().getString(R.string.example_phone_number5));
        values.put(InventoryEntry.COLUMN_QUANTITY, 9);
        values.put(InventoryEntry.COLUMN_BOOK_PRICE, 32.00);
        values.put(InventoryEntry.COLUMN_IMAGE, InventoryEntry.RES_URI.toString() + R.drawable.programming_kotlin);
        valuesArrayList.add(values);

        values = new ContentValues();
        values.put(InventoryEntry.COLUMN_BOOK_TITLE, getResources().getString(R.string.example_title6));
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, InventoryEntry.SUPPLIER_TWO);
        values.put(InventoryEntry.COLUMN_PHONE_NUMBER, getResources().getString(R.string.example_phone_number6));
        values.put(InventoryEntry.COLUMN_QUANTITY, 8);
        values.put(InventoryEntry.COLUMN_BOOK_PRICE, 24.00);
        values.put(InventoryEntry.COLUMN_IMAGE, InventoryEntry.RES_URI.toString() + R.drawable.learning_python);
        valuesArrayList.add(values);

        values = new ContentValues();
        values.put(InventoryEntry.COLUMN_BOOK_TITLE, getResources().getString(R.string.example_title7));
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, InventoryEntry.SUPPLIER_SEVEN);
        values.put(InventoryEntry.COLUMN_PHONE_NUMBER, getResources().getString(R.string.example_phone_number7));
        values.put(InventoryEntry.COLUMN_QUANTITY, 21);
        values.put(InventoryEntry.COLUMN_BOOK_PRICE, 29.00);
        values.put(InventoryEntry.COLUMN_IMAGE, InventoryEntry.RES_URI.toString() + R.drawable.php_7_programming);
        valuesArrayList.add(values);

        ContentValues[] valuesArray = new ContentValues[valuesArrayList.size()];

        valuesArrayList.toArray(valuesArray);

        // Insert a new rows into the provider using the ContentResolver.
        // Use the {@link InventoryEntry#CONTENT_URI} to indicate that we want to insert
        // into the books database table.
        // Receive the new content URI that will allow us to access books data in the future.
        getContentResolver().bulkInsert(InventoryEntry.CONTENT_URI, valuesArray);

        String newRow = Objects.toString(valuesArrayList);
        String newRow1 = Objects.toString(valuesArray);
        Log.i(LOG_TAG, getBaseContext().getResources().getString(R.string.example_data) + values + newRow + newRow1);

    }

    /**
     * This method deletes all rows from the database
     */
    private void deleteRows() {

        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + getBaseContext().getResources().getString(R.string.rows_deleted));

    }

    /**
     * This method sets the animation for floating button
     */
    private void setFabAnimation(FloatingActionButton floatingButton) {

        final TranslateAnimation ta1 = new TranslateAnimation(-10, 0, 0, 0);
        ta1.setInterpolator(new CycleInterpolator(1));
        ta1.setStartOffset(0);
        ta1.setDuration(1500);
        ta1.setRepeatMode(TranslateAnimation.RESTART);
        ta1.setRepeatCount(TranslateAnimation.INFINITE);
        floatingButton.setAnimation(ta1);

    }

    /**
     * Menu methods
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Menu "Insert example data"
        if (item.getItemId() == R.id.action_insert_example_data) {

            insertExampleData();

        }

        // Menu "Delete all entries"
        if (item.getItemId() == R.id.action_delete_all_products) {

            deleteRows();

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Instantiating the loader.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_BOOK_TITLE,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PHONE_NUMBER,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_BOOK_PRICE,
                InventoryEntry.COLUMN_IMAGE};

        Log.i(LOG_TAG, getBaseContext().getResources().getString(R.string.projection_string) + InventoryEntry.CONTENT_URI.toString());

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                InventoryEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order

    }

    /**
     * Providing the instructions for the moment when the loading is finished.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);

    }

    /**
     * Providing the instructions for the moment when the loader is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}