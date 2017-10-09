package com.benezra.nir.poi;

/**
 * Created by nir on 09/10/2017.
 */


/**
 * Created by nirb on 19/09/2017.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.benezra.nir.poi.Helper.Constants.MESSAGE;
import static com.benezra.nir.poi.Helper.Constants.OPTIONS;
import static com.benezra.nir.poi.Helper.Constants.TITLE;


/**
 * this class helps to handle dialog popup response from the user
 */
public class AlertDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {



    public AlertDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static AlertDialogFragment newInstance(String title, String message, int [] options) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        args.putIntArray(OPTIONS,options);
        frag.setArguments(args);
        return frag;
    }




    // 1. Defines the listener interface with a method passing back data result.
    public interface DialogListener {
        void onFinishDialog(int state);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE);
        String message = getArguments().getString(MESSAGE);
        int [] options = getArguments().getIntArray(OPTIONS);
        setCancelable(false);


        if (options!=null){


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message);

            for (int i = 0; i < options.length; i++) {
                switch(options[i])
                {
                    case BUTTON_POSITIVE:
                        alertDialogBuilder.setPositiveButton(getString(R.string.retry),this);
                        break;
                    case BUTTON_NEGATIVE:
                        break;
                    case BUTTON_NEUTRAL:
                        alertDialogBuilder.setNeutralButton(getString(R.string.leave), this);
                        break;
                }

            }

            return alertDialogBuilder.create();

        }
        ProgressDialog progressDialog = new ProgressDialog(getActivity(), getTheme());
        progressDialog.setTitle(getString(R.string.loading_data_title));
        progressDialog.setMessage(getString(R.string.please_wait_message));
        return progressDialog;

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        DialogListener listener = (DialogListener) getActivity();
        switch (which)
        {
            case BUTTON_POSITIVE:
                listener.onFinishDialog(BUTTON_POSITIVE);
                break;
            case BUTTON_NEGATIVE:
                listener.onFinishDialog(BUTTON_NEGATIVE);
                break;
            case BUTTON_NEUTRAL:
                listener.onFinishDialog(BUTTON_NEUTRAL);
                break;
        }

    }
}
