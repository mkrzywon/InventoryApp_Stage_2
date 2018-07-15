package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Allows to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String KEY_IMAGE = "IMAGE_KEY";
    private static final String KEY_INTENT_IMAGE = "image/*";
    private static final String KEY_REGEX = "\\.00";
    private static final String LOG_TAG = EditorActivity.class.getName();

    private static final int BOOK_EDITOR_LOADER = 0;
    private static final int SELECT_IMAGE = 0;
    private final String noImage = InventoryEntry.RES_URI.toString() + R.drawable.no_image;
    @BindView(R.id.book_title_field)
    EditText bookTitle;
    @BindView(R.id.supplier_spinner)
    Spinner supplierNameSpinner;
    @BindView(R.id.phone_number_field)
    EditText phoneNumber;
    @BindView(R.id.quantity_field)
    EditText editQuantity;
    @BindView(R.id.price_field)
    EditText editPrice;
    @BindView(R.id.book_image)
    ImageView bookImage;
    @BindView(R.id.arrow)
    ImageView arrow;
    @BindView(R.id.image_change_text)
    TextView imageChangeText;
    private String supplierName = InventoryEntry.SUPPLIER_ONE;
    private String imageString;
    private Uri imageUri;
    private Bitmap imageBitmap;
    private String bookTitleString;
    private String quantityString;
    private String priceString;

    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;
    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    private boolean mBookHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mBookHasChanged boolean to true.
     */
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mBookHasChanged = true;
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }

            return false;
        }
    };

    private TextView text;

    private View layout;

    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ButterKnife.bind(this);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new book or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // If the intent DOES NOT contain a book content URI, then we know that we are
        // creating a new book.
        if (mCurrentBookUri == null) {

            // This is a new book, so change the app bar to say "Add a new Book"
            setTitle(getBaseContext().getResources().getString(R.string.editor_activity_add_new_book));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();

        } else {

            // Otherwise this is an existing book, so change app bar to say "Edit Book"
            setTitle(getBaseContext().getResources().getString(R.string.editor_activity_edit_book));

            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(BOOK_EDITOR_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        phoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        // Setting the image pointing arrow with animation
        setArrowAnimation(arrow);

        // Setting the information for the pointing arrow
        imageChangeText.setText(getBaseContext().getResources().getString(R.string.change_image_text));

        // Defining the no image picture for the new book events.
        if (mCurrentBookUri == null) {

            Uri noImageUri = Uri.parse(InventoryEntry.RES_URI.toString() + R.drawable.no_image);
            Bitmap noImageBitmap = getBitmapFromUri(noImageUri, 200);
            bookImage.setImageBitmap(noImageBitmap);

        }

        // We can change the image by entering the gallery
        bookImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();

                if (Build.VERSION.SDK_INT < 19) {

                    intent.setAction(Intent.ACTION_GET_CONTENT);

                } else {

                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                }

                intent.setType(KEY_INTENT_IMAGE);
                startActivityForResult(Intent.createChooser(intent, getBaseContext().getResources().getString(R.string.editor_new_image)), SELECT_IMAGE);

            }
        });

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        bookTitle.setOnTouchListener(mTouchListener);
        phoneNumber.setOnTouchListener(mTouchListener);
        editQuantity.setOnTouchListener(mTouchListener);
        editPrice.setOnTouchListener(mTouchListener);

        supplierNameSpinner.setOnTouchListener(mTouchListener);

        // Settings the spinner
        setSpinner();

        // Settings the custom toast layout
        LayoutInflater toastInflater = getLayoutInflater();
        layout = toastInflater.inflate(R.layout.toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        text = layout.findViewById(R.id.text);
    }

    /**
     * Saving the imageString in case of changing the portrait and landscape mode.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_IMAGE, imageString);

        Log.i(LOG_TAG, getBaseContext().getResources().getString(R.string.editor_put_string) + imageString);

    }

    /**
     * Restoring the imageString in case of changing the portrait and landscape mode.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        imageString = savedInstanceState.getString(KEY_IMAGE);

        setImage();

        Log.i(LOG_TAG, getBaseContext().getResources().getString(R.string.editor_get_string) + imageString);

    }

    /**
     * Getting the new image from the gallery
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Here we need to check if the activity that was triggers was the Image Gallery.
        // If it is the requestCode will match the LOAD_IMAGE_RESULTS value.
        // If the resultCode is RESULT_OK and there is some data we know that an image was picked.
        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {

            imageUri = data.getData();

            if (imageUri != null) {

                imageString = imageUri.toString();

            }

            imageBitmap = getBitmapFromUri(imageUri, 260);
            bookImage.setImageBitmap(imageBitmap);

            Log.i(LOG_TAG, getBaseContext().getResources().getString(R.string.editor_image_string_received) + imageString);

        }
    }

    /**
     * Dropdown spinner that allows the user to select the supplier name.
     */
    private void setSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierListSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_name, android.R.layout.simple_spinner_item);

        // Setting the dropdown layout style
        supplierListSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);

        // Apply the adapter to the spinner
        supplierNameSpinner.setAdapter(supplierListSpinnerAdapter);

        // Set the integer mSelected to the constant values
        supplierNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getResources().getString(R.string.supplier2))) {
                        supplierName = InventoryEntry.SUPPLIER_TWO;
                    } else if (selection.equals(getResources().getString(R.string.supplier3))) {
                        supplierName = InventoryEntry.SUPPLIER_THREE;
                    } else if (selection.equals(getResources().getString(R.string.supplier4))) {
                        supplierName = InventoryEntry.SUPPLIER_FOUR;
                    } else if (selection.equals(getResources().getString(R.string.supplier5))) {
                        supplierName = InventoryEntry.SUPPLIER_FIVE;
                    } else if (selection.equals(getResources().getString(R.string.supplier6))) {
                        supplierName = InventoryEntry.SUPPLIER_SIX;
                    } else if (selection.equals(getResources().getString(R.string.supplier7))) {
                        supplierName = InventoryEntry.SUPPLIER_SEVEN;
                    } else {
                        supplierName = InventoryEntry.SUPPLIER_ONE;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                supplierName = InventoryEntry.SUPPLIER_ONE;
            }
        });
    }

    /**
     * Getting the information from the editor and inserting it into database
     */
    private void insertBook() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        bookTitleString = bookTitle.getText().toString().trim();
        quantityString = editQuantity.getText().toString().trim();
        priceString = editPrice.getText().toString().trim();
        String phoneNumberString = phoneNumber.getText().toString().trim();

        // Conditions in case of empty entry fields
        setEmptyFields();

        // If all required fields are filled a database entry can be made
        if (!TextUtils.isEmpty(bookTitleString) &&
                !TextUtils.isEmpty(quantityString) &&
                !TextUtils.isEmpty(priceString)) {

            int quantityInt = Integer.parseInt(quantityString);
            Double priceDouble = Double.parseDouble(priceString);

            // Create a ContentValues object where column names are represented by the keys,
            // and book attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_BOOK_TITLE, bookTitleString);
            values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, supplierName);
            values.put(InventoryEntry.COLUMN_PHONE_NUMBER, phoneNumberString);
            values.put(InventoryEntry.COLUMN_QUANTITY, quantityInt);
            values.put(InventoryEntry.COLUMN_BOOK_PRICE, priceDouble);
            values.put(InventoryEntry.COLUMN_IMAGE, imageString);

            Log.i(LOG_TAG, getBaseContext().getResources().getString(R.string.editor_image_string_inserted) + imageString);

            // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
            if (mCurrentBookUri == null) {

                // This is a new book, so insert a new book into the provider,
                // returning the content URI for the new book.
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {

                    // If the new content URI is null, then there was an error with insertion.
                    setCustomToast(text, getBaseContext().getResources().getString(R.string.editor_error_while_saving_book));

                } else {

                    // Otherwise, the insertion was successful and we can display a toast.
                    setCustomToast(text, getBaseContext().getResources().getString(R.string.editor_successful_book_insertion));
                }

                // Exit activity
                finish();

                // Navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(EditorActivity.this);

            } else {

                // Otherwise this is an existing book, so update the book with content URI: mCurrentBookUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentBookUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {

                    // If no rows were affected, then there was an error with the update.
                    setCustomToast(text, getBaseContext().getResources().getString(R.string.editor_error_while_updating_book));

                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    setCustomToast(text, getBaseContext().getResources().getString(R.string.editor_successful_book_update));
                }

                // Exit activity
                finish();

                // Navigate back to parent activity (MainActivity)
                NavUtils.navigateUpFromSameTask(EditorActivity.this);

            }
        }
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

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,   // Provider content URI to query
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

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {

            // Find the columns of pet attributes that we're interested in
            int titleColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_BOOK_TITLE);
            int supplierColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PHONE_NUMBER);
            int quantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_BOOK_PRICE);
            int imageColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String title = data.getString(titleColumnIndex);
            String supplier = data.getString(supplierColumnIndex);
            String supplierPhone = data.getString(supplierPhoneColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);

            Double price = data.getDouble(priceColumnIndex);
            String formattedBookPrice = NumberFormat.getNumberInstance().format(price);
            formattedBookPrice = formattedBookPrice.replaceAll(KEY_REGEX, "");

            imageString = data.getString(imageColumnIndex);

            setImage();

            Log.i(LOG_TAG, getBaseContext().getResources().getString(R.string.editor_image_string_from_loader) + imageString);

            // Update the views on the screen with the values from the database
            bookTitle.setText(title);
            phoneNumber.setText(supplierPhone);
            editQuantity.setText(String.valueOf(quantity));
            editPrice.setText(formattedBookPrice);

            // Supplier is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Supplier One, 1 is Supplier Two, 2 is Supplier Three,
            // 3 is Supplier Four, 4 is Supplier Five, 5 is Supplier Six and 6 is Supplier Seven).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (supplier) {

                case InventoryEntry.SUPPLIER_ONE:
                    supplierNameSpinner.setSelection(0);
                    break;
                case InventoryEntry.SUPPLIER_TWO:
                    supplierNameSpinner.setSelection(1);
                    break;
                case InventoryEntry.SUPPLIER_THREE:
                    supplierNameSpinner.setSelection(2);
                    break;
                case InventoryEntry.SUPPLIER_FOUR:
                    supplierNameSpinner.setSelection(3);
                    break;
                case InventoryEntry.SUPPLIER_FIVE:
                    supplierNameSpinner.setSelection(4);
                    break;
                case InventoryEntry.SUPPLIER_SIX:
                    supplierNameSpinner.setSelection(5);
                    break;
                case InventoryEntry.SUPPLIER_SEVEN:
                    supplierNameSpinner.setSelection(6);
                    break;
                default:
                    supplierNameSpinner.setSelection(0);
                    break;
            }
        }
    }

    /**
     * Providing the instructions for the moment when the loader is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        loader.reset();
    }

    /**
     * Menu methods
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        // Respond to a click on the "Save" menu option
        if (item.getItemId() == R.id.action_save) {

            // Saving book to database
            insertBook();
            return true;

        }

        // User clicked on a menu option in the app bar overflow menu
        // Respond to a click on the "Save" menu option
        if (item.getItemId() == R.id.action_delete) {

            showDeleteConfirmationDialog();

            return true;

        }

        // User clicked on a menu option in the app bar overflow menu
        // Respond to a click on the "Save" menu option
        if (item.getItemId() == R.id.home) {

            if (!mBookHasChanged) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;

            } else {

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);

                return true;

            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {

        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.poof_of_smoke);
        mMediaPlayer.start();
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setMessage(R.string.editor_alert_dialog_message);
        builder.setPositiveButton(R.string.editor_alert_dialog_positive_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.editor_alert_dialog_negative_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
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

        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.poof_of_smoke);
        mMediaPlayer.start();

        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setMessage(R.string.editor_unsaved_changes_message);
        builder.setPositiveButton(R.string.editor_unsaved_positive_button, discardButtonClickListener);
        builder.setNegativeButton(R.string.editor_unsaved_negative_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
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
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {

        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {

            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                setCustomToast(text, getBaseContext().getResources().getString(R.string.editor_unsuccessful_book_deletion));

            } else {
                // Otherwise, the delete was successful and we can display a toast.
                setCustomToast(text, getBaseContext().getResources().getString(R.string.editor_successful_book_deletion));
            }
        }

        // Close the activity
        finish();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {

        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();

        } else {

            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, close the current activity.
                            finish();
                        }
                    };

            // Show dialog that there are unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

    /**
     * This method sets the image
     */
    private void setImage() {

        if (imageString != null) {

            // using the Uri of an image, generate a Bitmap
            imageUri = Uri.parse(imageString);
            imageBitmap = getBitmapFromUri(imageUri, 260);
            bookImage.setImageBitmap(imageBitmap);

        } else {

            imageUri = Uri.parse(noImage);
            imageBitmap = getBitmapFromUri(imageUri, 200);
            bookImage.setImageBitmap(imageBitmap);

        }
    }

    /**
     * This method sets the conditions with toast messages for insertBook()
     */
    private void setEmptyFields() {

        if ((TextUtils.isEmpty(bookTitleString) && TextUtils.isEmpty(quantityString)) ||
                (TextUtils.isEmpty(bookTitleString) && TextUtils.isEmpty(priceString)) ||
                (TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString)) ||
                (TextUtils.isEmpty(bookTitleString) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString))) {

            setCustomToast(text, getBaseContext().getResources().getString(R.string.unfilled_fields));

        } else if (TextUtils.isEmpty(quantityString) &&
                !TextUtils.isEmpty(priceString) && !TextUtils.isEmpty(bookTitleString)) {

            setCustomToast(text, getBaseContext().getResources().getString(R.string.unfilled_quantity));

        } else if (TextUtils.isEmpty(bookTitleString) &&
                !TextUtils.isEmpty(priceString) && !TextUtils.isEmpty(quantityString)) {

            setCustomToast(text, getBaseContext().getResources().getString(R.string.unfilled_title));

        } else if (TextUtils.isEmpty(priceString) &&
                !TextUtils.isEmpty(quantityString) && !TextUtils.isEmpty(bookTitleString)) {

            setCustomToast(text, getBaseContext().getResources().getString(R.string.unfilled_price));
        }
    }

    /**
     * This method sets the animation for arrow
     */
    private void setArrowAnimation(ImageView imageView) {

        TranslateAnimation ta = new TranslateAnimation(10, -20, 0, 0);
        ta.setInterpolator(new BounceInterpolator());
        ta.setStartOffset(3000);
        ta.setDuration(400);
        ta.setRepeatMode(TranslateAnimation.RESTART);
        ta.setRepeatCount(TranslateAnimation.INFINITE);
        imageView.setAnimation(ta);

    }

    /**
     * This method sets the behavior of the custom toast message with sound
     */
    private void setCustomToast(TextView textView, String string) {

        textView.setText(string);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hint);
        mMediaPlayer.start();

    }

    /**
     * This helper method defines the bitmap
     */
    private Bitmap getBitmapFromUri(Uri uri, int targetH) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = 200;

        InputStream input = null;
        try {

            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);

            if (input != null) {

                input.close();

            }

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);

            if (input != null) {

                input.close();

            }

            return bitmap;

        } catch (FileNotFoundException fne) {

            Log.e(LOG_TAG, getBaseContext().getResources().getString(R.string.failed_to_load_image), fne);
            return null;

        } catch (Exception e) {

            Log.e(LOG_TAG, getBaseContext().getResources().getString(R.string.failed_to_load_image), e);
            return null;

        } finally {
            try {

                if (input != null) {

                    input.close();

                }

            } catch (IOException ioe) {

                // Do nothing

            }
        }
    }
}
