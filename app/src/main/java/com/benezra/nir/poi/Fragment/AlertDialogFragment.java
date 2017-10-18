package com.benezra.nir.poi.Fragment;

/**
 * Created by nir on 09/10/2017.
 */


/**
 * Created by nirb on 19/09/2017.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.benezra.nir.poi.R;

import java.util.HashMap;
import java.util.Map;

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


    private Context mContext;
    private DialogListenerCallback mListener;

    public AlertDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static AlertDialogFragment newInstance(String title, String message, HashMap<Integer,String> options) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        args.putSerializable(OPTIONS,options);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof DialogListenerCallback) {
            mListener = (DialogListenerCallback) context;
        }
    }



    // 1. Defines the listener interface with a method passing back data result.
    public interface DialogListenerCallback {
        void onFinishDialog(int state);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE);
        String message = getArguments().getString(MESSAGE);
        HashMap<Integer,String> map = (HashMap<Integer, String>) getArguments().getSerializable(OPTIONS);
        setCancelable(false);



            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message);


        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

            int option =  entry.getKey();
            String button = entry.getValue();

            switch(option)
            {
                case BUTTON_POSITIVE:
                    alertDialogBuilder.setPositiveButton(button,this);
                    break;
                case BUTTON_NEGATIVE:
                    alertDialogBuilder.setNeutralButton(button, this);
                    break;
                case BUTTON_NEUTRAL:
                    alertDialogBuilder.setNeutralButton(button, this);
                    break;
            }
        }

            return alertDialogBuilder.create();

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which)
        {
            case BUTTON_POSITIVE:
                mListener.onFinishDialog(BUTTON_POSITIVE);
                break;
            case BUTTON_NEGATIVE:
                mListener.onFinishDialog(BUTTON_NEGATIVE);
                break;
            case BUTTON_NEUTRAL:
                mListener.onFinishDialog(BUTTON_NEUTRAL);
                break;
        }
        dismiss();

    }
}