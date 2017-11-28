package com.benezra.nir.poi.Fragment;

/**
 * Created by nir on 09/10/2017.
 * <p>
 * Created by nirb on 19/09/2017.
 */


/**
 * Created by nirb on 19/09/2017.
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.benezra.nir.poi.Interface.Constants.MESSAGE;
import static com.benezra.nir.poi.Interface.Constants.OPTIONS;
import static com.benezra.nir.poi.Interface.Constants.TITLE;


/**
 * this class helps to handle dialog popup response from the user
 */
public class ProgressDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private ProgressDialog mProgressDialog;
    private Handler mHandler;
    private static final int MAX = 100;



    public ProgressDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static ProgressDialogFragment newInstance(String title, String message, int type) {
        ProgressDialogFragment frag = new ProgressDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        args.putInt(OPTIONS, type);
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
        int type = getArguments().getInt(OPTIONS);
        setCancelable(false);

        mProgressDialog = new ProgressDialog(getActivity(), getTheme());
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);

        switch (type) {
            case ProgressDialog.STYLE_SPINNER:
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner

                break;

            case ProgressDialog.STYLE_HORIZONTAL:
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Progress Dialog Style Spinner
                mProgressDialog.setMax(MAX); // Progress Dialog Max Value
                initHandler();
                initProgressUpdate();
                break;

        }


        return mProgressDialog;


    }

    private void initHandler() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int i = msg.getData().getInt("prg");
                mProgressDialog.setProgress(i); // Incremented By Value 2
            }
        };
    }

    private void initProgressUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mProgressDialog.getProgress() <= mProgressDialog.getMax()) {
                        Thread.sleep(200);
                        if (mProgressDialog.getProgress() == mProgressDialog.getMax()) {
                            dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setProgress(Message msg) {
        mHandler.sendMessage(msg);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        DialogListener listener = (DialogListener) getActivity();
        switch (which) {
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
