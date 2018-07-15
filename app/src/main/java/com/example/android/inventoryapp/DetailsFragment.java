package com.example.android.inventoryapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryProvider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String KEY_DETAILS = "details";
    private static final String KEY_URI = "uri";

    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int DETAILS_LOADER = 1;

    private final String noImage = InventoryEntry.RES_URI.toString() + R.drawable.no_image;

    @BindView(R.id.book_title)
    TextView bookTitle;
    @BindView(R.id.supplier_name)
    TextView supplierName;
    @BindView(R.id.supplier_phone)
    TextView supplierPhone;
    @BindView(R.id.book_quantity)
    TextView bookQuantity;
    @BindView(R.id.price)
    TextView bookPrice;
    @BindView(R.id.subtraction)
    ImageButton quantitySubtraction;
    @BindView(R.id.addition)
    ImageButton quantityAddition;
    @BindView(R.id.delete_book)
    ImageButton deleteBook;
    @BindView(R.id.order_book)
    ImageButton bookPhoneOrder;
    @BindView(R.id.image)
    ImageView bookImage;

    private int quantity;

    private String id;

    private String title;

    private String phoneNumber;

    private Uri newUri;

    private View layout;

    private MediaPlayer mMediaPlayer;

    private Uri imageUri;

    private Bitmap imageBitmap;

    private TextView text;

    public DetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Signals to the fragment that its activity has completed its own onCreate().
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        if (getActivity() != null) {

            LoaderManager lm = getActivity().getLoaderManager();
            lm.restartLoader(DETAILS_LOADER, null, this);

            ((MainActivity) getActivity()).hideFab();

        }

        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Initial creation of the dialog fragment window with proper styling and sound
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragment);

        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Plays the sound after 500ms
                mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.whoosh);
                mMediaPlayer.start();

            }
        }, 500);
    }

    /**
     * Defining the behavior of the dialog fragment in case of dismiss() method.
     */
    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }

        if (getActivity() != null) {

            ((MainActivity) getActivity()).showFab();

            mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.teleport);
            mMediaPlayer.start();

        }
    }

    /**
     * Creates th view of the dialog fragment window.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle(KEY_DETAILS);
        getDialog().setCanceledOnTouchOutside(true);

        View rootView = inflater.inflate(R.layout.item_details, container, false);

        ButterKnife.bind(this, rootView);

        //Defining the initial dialog window animation
        if (getDialog().getWindow() != null) {

            getDialog().getWindow().getAttributes().windowAnimations = (R.style.details_dialog_animation_fade);

        }

        // Getting the data passed from list
        Bundle mBundle = getArguments();

        if (mBundle != null) {

            newUri = mBundle.getParcelable(KEY_URI);

        }

        if (getActivity() != null) {

            Log.i(LOG_TAG, getActivity().getResources().getString(R.string.new_uri_details) + newUri + rootView + mBundle);

            LoaderManager lm = getActivity().getLoaderManager();
            lm.initLoader(DETAILS_LOADER, null, this);

            // Layout for custom toast messages
            LayoutInflater toastInflater = getActivity().getLayoutInflater();
            layout = toastInflater.inflate(R.layout.toast,
                    (ViewGroup) getActivity().findViewById(R.id.custom_toast_container));

            text = layout.findViewById(R.id.text);

        }

        return rootView;
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
        return new CursorLoader(getActivity(), // Parent activity context
                newUri,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    /**
     * Providing the instructions for the moment when the loading is finished.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {

            // Find the columns of book attributes that we're interested in
            int titleColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_BOOK_TITLE);
            int supplierColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PHONE_NUMBER);
            int quantityColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_BOOK_PRICE);
            int imageColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_IMAGE);
            int idColumnIndex = data.getColumnIndex(InventoryEntry._ID);

            // Extract out the value from the Cursor for the given column index
            title = data.getString(titleColumnIndex);
            String supplier = data.getString(supplierColumnIndex);
            phoneNumber = data.getString(supplierPhoneColumnIndex);
            quantity = data.getInt(quantityColumnIndex);

            Double price = data.getDouble(priceColumnIndex);
            String formattedBookPrice = NumberFormat.getCurrencyInstance().format(price);
            formattedBookPrice = formattedBookPrice.replaceAll("\\.00", "");

            String imageString = data.getString(imageColumnIndex);
            id = data.getString(idColumnIndex);

            // using the Uri of an image, generate a Bitmap
            if (imageString != null) {

                imageUri = Uri.parse(imageString);

                setImageSize();

                bookImage.setImageBitmap(imageBitmap);

            } else {

                imageUri = Uri.parse(noImage);

                setNoImageSize();

                bookImage.setImageBitmap(imageBitmap);

            }

            if (getActivity() != null) {

                Log.i(LOG_TAG, getActivity().getResources().getString(R.string.image_on_load_finished) + imageString + imageUri);
            }

            bookTitle.setText(title);

            // Defining the behavior of title field depending on the title's length.
            int lineCounter = bookTitle.getLineCount();

            switch (lineCounter) {

                case 4:
                    bookTitle.setLines(4);
                    break;
                case 3:
                    bookTitle.setLines(3);
                    break;
                case 2:
                    bookTitle.setLines(2);
                    break;
                case 1:
                    bookTitle.setLines(1);
                    break;
                default:
                    bookTitle.setLines(1);
            }

            supplierName.setText(supplier);
            supplierPhone.setText(phoneNumber);
            bookQuantity.setText(String.valueOf(quantity));
            bookPrice.setText(formattedBookPrice);

            setPlusMinusButtons();
            setDeleteBook();
            setPhoneCall();

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
     * This method defines the behavior of plus and minus buttons.
     */
    private void setPlusMinusButtons() {

        if (getActivity() != null) {

            quantityAddition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    newUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, Long.parseLong(id));

                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_QUANTITY, quantity + 1);
                    getActivity().getContentResolver().update(newUri, values, null, null);

                    mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.pop_sound);
                    mMediaPlayer.start();

                }
            });

            quantitySubtraction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (quantity > 0) {

                        newUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, Long.parseLong(id));

                        ContentValues values = new ContentValues();
                        values.put(InventoryEntry.COLUMN_QUANTITY, quantity - 1);
                        getActivity().getContentResolver().update(newUri, values, null, null);

                        mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.pop_sound);
                        mMediaPlayer.start();

                    } else {

                        setCustomToast(text, getActivity().getResources().getString(R.string.no_more) + title + getString(R.string.books_in_store));

                    }
                }
            });
        }
    }

    /**
     * This method defines the behavior of the delete button.
     */
    private void setDeleteBook() {

        if (getActivity() != null) {

            deleteBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.poof_of_smoke);
                    mMediaPlayer.start();

                    // Create an AlertDialog.Builder and set the message, and click listeners
                    // for the positive and negative buttons on the dialog.
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.editor_alert_dialog_message);
                    builder.setPositiveButton(R.string.editor_alert_dialog_positive_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.crumple);
                            mMediaPlayer.start();

                            // User clicked the "Delete" button, so delete the book.
                            int rowsDeleted = getActivity().getContentResolver().delete(newUri, null, null);

                            getDialog().dismiss();

                            // Show a toast message depending on whether or not the delete was successful.
                            if (rowsDeleted == 0) {

                                // If no rows were deleted, then there was an error with the delete.
                                setCustomToast(text, getActivity().getResources().getString(R.string.editor_unsuccessful_book_deletion));

                            } else {

                                // Otherwise, the delete was successful and we can display a toast.
                                setCustomToast(text, getActivity().getResources().getString(R.string.editor_successful_book_deletion));

                            }
                        }
                    });

                    builder.setNegativeButton(R.string.editor_alert_dialog_negative_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked the "Cancel" button, so dismiss the dialog
                            // and continue editing the pet.
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    });

                    // Create and show the AlertDialog
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
            });
        }
    }

    /**
     * This method defines the behavior of the phone call button.
     */
    private void setPhoneCall() {

        if (getActivity() != null) {

            bookPhoneOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!TextUtils.isEmpty(phoneNumber)) {

                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(getResources().getString(R.string.tel_prefix) + phoneNumber));
                        startActivity(intent);

                        mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.pop_sound);
                        mMediaPlayer.start();

                    } else {

                        setCustomToast(text, getActivity().getResources().getString(R.string.no_phone_toast));

                    }
                }
            });
        }
    }

    /**
     * This helper method defines the bitmap
     */
    private Bitmap getBitmapFromUri(Uri uri, int targetW, int targetH) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        InputStream input = null;

        try {

            if (getActivity() != null) {

                input = getActivity().getBaseContext().getContentResolver().openInputStream(uri);

            }

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

            input = getActivity().getBaseContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);


            if (input != null) {

                input.close();

            }
            return bitmap;

        } catch (FileNotFoundException fne) {

            Log.e(LOG_TAG, getActivity().getResources().getString(R.string.failed_to_load_image), fne);
            return null;

        } catch (Exception e) {

            Log.e(LOG_TAG, getActivity().getResources().getString(R.string.failed_to_load_image), e);
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

    /**
     * This method defines the dimensions of the existing image depending on the phone's density parameter.
     */
    private void setImageSize() {

        if (getActivity() != null) {

            if ((getActivity().getResources().getDisplayMetrics().density == 2)) {

                imageBitmap = getBitmapFromUri(imageUri, 250, 325);

            } else {

                imageBitmap = getBitmapFromUri(imageUri, 300, 375);

            }
        }
    }

    /**
     * This method defines the dimensions of the 'no image' picture depending on the phone's density parameter.
     */
    private void setNoImageSize() {

        if (getActivity() != null) {

            if ((getActivity().getResources().getDisplayMetrics().density == 2)) {

                imageBitmap = getBitmapFromUri(imageUri, 300, 300);

            } else {

                imageBitmap = getBitmapFromUri(imageUri, 400, 400);

            }

        }
    }

    /**
     * This method defines the custom toast message with sound.
     */
    private void setCustomToast(TextView textView, String string) {

        if (getActivity() != null) {

            textView.setText(string);

            Toast toast = new Toast(getActivity().getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

            mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.hint);
            mMediaPlayer.start();

        }
    }
}
