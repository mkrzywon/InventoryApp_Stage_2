package com.example.android.inventoryapp;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {

    public InfoFragment() {
        // Required empty public constructor
    }

    public static class InfoDialog extends DialogFragment {

        private static final String KEY_URI = "uri";
        private static final String KEY_DETAILS = "details";

        @BindView(R.id.book_edit)
        ImageButton editor;
        @BindView(R.id.book_details)
        ImageButton details;
        Uri newUri;
        MediaPlayer mMediaPlayer;

        /**
         * Initial creation of the dialog fragment window with proper styling
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setStyle(DialogFragment.STYLE_NORMAL, R.style.InfoDialogFragment);

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

                mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.weapgone);
                mMediaPlayer.start();

            }
        }

        /**
         * Creates th view of the dialog fragment window.
         */
        @Override
        public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

            getDialog().setCanceledOnTouchOutside(true);

            View rootView = inflater.inflate(R.layout.info_dialog, container, false);

            ButterKnife.bind(this, rootView);

            //Defining the initial dialog window animation
            if (getDialog().getWindow() != null) {

                getDialog().getWindow().getAttributes().windowAnimations = (R.style.info_dialog_animation_fade);

            }

            // Getting the data passed from list
            Bundle mBundle = getArguments();

            if (mBundle != null) {

                newUri = mBundle.getParcelable(KEY_URI);

            }

            // Hiding the fab button
            if (getActivity() != null) {

                ((MainActivity) getActivity()).hideFab();

            }

            // Defining the behavior of details button
            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    getDialog().dismiss();

                    Bundle bundle = new Bundle();

                    bundle.putParcelable(KEY_URI, newUri);

                    FragmentManager fm = getFragmentManager();

                    if (fm != null) {

                        DetailsFragment detailsFragment = new DetailsFragment();
                        detailsFragment.setArguments(bundle);
                        detailsFragment.show(fm, KEY_DETAILS);

                    }
                }
            });

            // Defining the behavior of editor button
            editor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    getDialog().dismiss();

                    Intent intent = new Intent(getContext(), EditorActivity.class);

                    intent.setData(newUri);

                    startActivity(intent);

                }
            });

            return rootView;

        }
    }
}
