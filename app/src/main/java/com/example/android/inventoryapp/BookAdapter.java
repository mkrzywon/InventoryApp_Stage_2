package com.example.android.inventoryapp;

import android.animation.ObjectAnimator;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryProvider;

import java.text.NumberFormat;

/**
 * {@link BookAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of books data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
class BookAdapter extends CursorAdapter {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * A mMediaPlayer variable
     */
    private MediaPlayer mMediaPlayer;

    /**
     * Constructs a new {@link BookAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookAdapter(Context context, Cursor c) {
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

        // Inflate a view using the layout specified in list_item.xml
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        // Defining the ViewHolder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {

        final ViewHolder holder = (ViewHolder) view.getTag();

        // Find the columns of book attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_BOOK_TITLE);
        int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
        final int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_BOOK_PRICE);
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);

        // Read the pet attributes from the Cursor for the current book
        final String bookTitle = cursor.getString(titleColumnIndex);
        final String supplier = cursor.getString(supplierColumnIndex);
        final int bookQuantity = cursor.getInt(quantityColumnIndex);
        final String id = cursor.getString(idColumnIndex);

        // Formatting the book price so that it displays currency symbol and two values after a dot.
        Double bookPrice = cursor.getDouble(priceColumnIndex);
        String formattedBookPrice = NumberFormat.getCurrencyInstance().format(bookPrice);
        formattedBookPrice = formattedBookPrice.replaceAll("\\.00", "");

        // Update the TextViews with the attributes for the current book
        holder.title.setText(bookTitle);

        String quantityText = context.getString(R.string.quantityText);
        String temp1 = quantityText + String.valueOf(bookQuantity);
        holder.quantity.setText(Html.fromHtml(temp1));

        String priceText = context.getString(R.string.priceText);
        String temp2 = priceText + String.valueOf(formattedBookPrice);
        holder.price.setText(Html.fromHtml(temp2));

        // Defining the behavior of the sale button with it's animation.
        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bookQuantity > 0) {

                    TranslateAnimation ta1 = new TranslateAnimation(0, 300, 0, 0);
                    ta1.setDuration(500);
                    ta1.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                            holder.saleButton.setImageResource(R.drawable.shopping_cart_pulled);
                            setTextViewRotateAnimation(holder.title);

                            mMediaPlayer = MediaPlayer.create(context, R.raw.cash_register);
                            mMediaPlayer.start();

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            TranslateAnimation ta2 = new TranslateAnimation(500, 0, 0, 0);
                            ta2.setDuration(500);

                            holder.saleButton.setImageResource(R.drawable.shopping_trolley);
                            holder.saleButton.setAnimation(ta2);

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                            // Do nothing

                        }
                    });

                    holder.saleButton.setAnimation(ta1);

                    Uri newUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, Long.parseLong(id));

                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_QUANTITY, bookQuantity - 1);
                    context.getContentResolver().update(newUri, values, null, null);

                } else {

                    // Custom toast for the out of stock message.
                    LayoutInflater toastInflater = ((MainActivity) context).getLayoutInflater();
                    View layout = toastInflater.inflate(R.layout.toast,
                            (ViewGroup) view.findViewById(R.id.custom_toast_container));

                    TextView text = layout.findViewById(R.id.text);

                    text.setText((context.getResources().getString(R.string.out_of_stock_message) + supplier));

                    Toast toast = new Toast(context.getApplicationContext());
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();

                    mMediaPlayer = MediaPlayer.create(context, R.raw.hint);
                    mMediaPlayer.start();

                }

            }
        });

        holder.saleButton.setFocusable(false);

        Log.i(LOG_TAG, context.getString(R.string.log_quantity) + bookQuantity + idColumnIndex + id);
    }

    /**
     * This method sets the rotate animation for text fields
     */
    private void setTextViewRotateAnimation(TextView textView) {

        ObjectAnimator oa = ObjectAnimator.ofFloat(textView, "rotationX", 0, 360);
        oa.setInterpolator(new AccelerateDecelerateInterpolator());
        oa.setDuration(1000);
        oa.start();

    }

    /**
     * Cache of the children views for a list item.
     */
    private static class ViewHolder {
        private final TextView title;
        private final TextView quantity;
        private final TextView price;
        private final ImageButton saleButton;

        // Find individual views that we want to modify in the list item layout
        private ViewHolder(View view) {
            title = view.findViewById(R.id.book_title);
            quantity = view.findViewById(R.id.quantity);
            price = view.findViewById(R.id.price);
            saleButton = view.findViewById(R.id.sale_button);
        }
    }
}
