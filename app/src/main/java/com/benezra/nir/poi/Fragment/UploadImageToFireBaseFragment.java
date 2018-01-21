package com.benezra.nir.poi.Fragment;

/**
 * Created by nir on 09/10/2017.
 * <p>
 * Created by nirb on 19/09/2017.
 * <p>
 * Created by nirb on 19/09/2017.
 * <p>
 * Created by nirb on 19/09/2017.
 * <p>
 * Created by nirb on 19/09/2017.
 * <p>
 * Created by nirb on 19/09/2017.
 */


/**
 * Created by nirb on 19/09/2017.
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.benezra.nir.poi.R;
import com.benezra.nir.poi.Utils.BitmapUtil;
import com.benezra.nir.poi.Objects.EventPhotos;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static com.benezra.nir.poi.Interface.Constants.ID;
import static com.benezra.nir.poi.Interface.Constants.URI;


/**
 * this class helps to handle dialog popup response from the user
 */
public class UploadImageToFireBaseFragment extends DialogFragment {

    private ProgressDialog mProgressDialog;
    private Handler mHandler;
    private DatabaseReference mFirebaseEventPicReference;
    private DatabaseReference mFirebaseEventReference;

    private static final int MAX = 100;
    private Context mContext;
    final static String TAG = UploadImageToFireBaseFragment.class.getSimpleName();
    private UploadListener mListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof UploadImageToFireBaseFragment.UploadListener) {
            mListener = (UploadImageToFireBaseFragment.UploadListener) context;
        }
    }

    public UploadImageToFireBaseFragment() {
        // Empty constructor required for DialogFragment
    }

    public static UploadImageToFireBaseFragment newInstance(Uri uri, String id) {
        UploadImageToFireBaseFragment frag = new UploadImageToFireBaseFragment();
        Bundle args = new Bundle();
        args.putParcelable(URI, uri);
        args.putString(ID, id);

        frag.setArguments(args);
        return frag;
    }


    // 1. Defines the listener interface with a method passing back data result.
    public interface UploadListener {
        void onFinishDialog(String image);

        void onErrorDialog(String error);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        setCancelable(false);
        mProgressDialog = new ProgressDialog(getActivity(), getTheme());
        mProgressDialog.setTitle(getString(R.string.image_uploaded_title));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Progress Dialog Style Spinner
        mProgressDialog.setMax(MAX); // Progress Dialog Max Value
        initHandler();
        initProgressUpdate();

        return mProgressDialog;


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        String id = getArguments().getString(ID);
        Uri uri = getArguments().getParcelable(URI);
        mFirebaseEventReference = FirebaseDatabase.getInstance().getReference("events");
        mFirebaseEventPicReference = FirebaseDatabase.getInstance().getReference("events").child(id).child("pictures");
        uploadBytes(uri, id);


    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
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


    private void uploadBytes(Uri picUri, final String id) {


        if (picUri != null) {

            Bitmap bitmap = BitmapUtil.UriToBitmap(getContext(), picUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            try {
                BitmapUtil.rotateImageIfRequired(bitmap,getContext(),picUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] data = baos.toByteArray();
            final String pic_id = UUID.randomUUID().toString() + ".jpg";


            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("images").child(id).child(pic_id);
            fileRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //mProgressDialogFragment.dismiss();

                            Log.i(TAG, "Uri: " + taskSnapshot.getDownloadUrl());
                            Log.i(TAG, "Name: " + taskSnapshot.getMetadata().getName());
                            isEventExist(id, pic_id, taskSnapshot.getDownloadUrl().toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            dismiss();
                            mListener.onErrorDialog(exception.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Log.d(TAG, "addOnProgressListener " + progress + "");
                            // percentage in progress dialog
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putInt("prg", (int) progress);
                            message.setData(bundle);
                            setProgress(message);
                        }
                    })
                    .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("Upload is paused!");
                        }
                    });
        } else {
            mListener.onErrorDialog("No file");

        }
    }

    private void isEventExist(final String id, final String title, final String url) {
        mFirebaseEventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(id))
                    writeNewImageInfoToDB(title, url);
                else
                    mListener.onFinishDialog("");


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void writeNewImageInfoToDB(String title, String url) {
        String key = mFirebaseEventPicReference.push().getKey();
        mFirebaseEventPicReference.child(key).setValue(new EventPhotos(url, title));
        mListener.onFinishDialog(url);
    }

}
