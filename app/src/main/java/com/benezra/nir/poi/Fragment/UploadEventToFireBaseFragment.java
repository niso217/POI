package com.benezra.nir.poi.Fragment;


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

import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Objects.EventPhotos;
import com.benezra.nir.poi.Objects.User;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.Utils.BitmapUtil;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.benezra.nir.poi.Interface.Constants.ADDRESS;
import static com.benezra.nir.poi.Interface.Constants.CREATE;
import static com.benezra.nir.poi.Interface.Constants.DETAILS;
import static com.benezra.nir.poi.Interface.Constants.END;
import static com.benezra.nir.poi.Interface.Constants.EVENT;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LIST;
import static com.benezra.nir.poi.Interface.Constants.ID;
import static com.benezra.nir.poi.Interface.Constants.IMAGE;
import static com.benezra.nir.poi.Interface.Constants.INTEREST;
import static com.benezra.nir.poi.Interface.Constants.LATITUDE;
import static com.benezra.nir.poi.Interface.Constants.LONGITUDE;
import static com.benezra.nir.poi.Interface.Constants.MODE;
import static com.benezra.nir.poi.Interface.Constants.OPERATION;
import static com.benezra.nir.poi.Interface.Constants.OWNER;
import static com.benezra.nir.poi.Interface.Constants.PARTICIPATES;
import static com.benezra.nir.poi.Interface.Constants.REMOVE;
import static com.benezra.nir.poi.Interface.Constants.START;
import static com.benezra.nir.poi.Interface.Constants.STATUS;
import static com.benezra.nir.poi.Interface.Constants.TITLE;
import static com.benezra.nir.poi.Interface.Constants.UPDATE;
import static com.benezra.nir.poi.Interface.Constants.URI;


/**
 * this class helps to handle dialog popup response from the user
 */
public class UploadEventToFireBaseFragment extends DialogFragment {

    private ProgressDialog mProgressDialog;
    private Handler mHandler;
    private DatabaseReference mFirebaseEventReference;
    private static final int MAX = 100;
    private Context mContext;
    final static String TAG = UploadEventToFireBaseFragment.class.getSimpleName();
    private UploadEventListener mListener;
    private  int mOperation;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof UploadEventToFireBaseFragment.UploadEventListener) {
            mListener = (UploadEventToFireBaseFragment.UploadEventListener) context;
        }
    }

    public UploadEventToFireBaseFragment() {
        // Empty constructor required for DialogFragment
    }

    public static UploadEventToFireBaseFragment newInstance(Event event,int operation) {
        UploadEventToFireBaseFragment frag = new UploadEventToFireBaseFragment();
        Bundle args = new Bundle();
        args.putParcelable(EVENT, event);
        args.putInt(OPERATION, operation);
        frag.setArguments(args);
        return frag;
    }


    // 1. Defines the listener interface with a method passing back data result.
    public interface UploadEventListener {
        void onEventFinishDialog(int operation);
        void onEventErrorDialog(String error);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        mProgressDialog = new ProgressDialog(getActivity(), getTheme());
        mProgressDialog.setMessage(getString(R.string.please_wait));

        switch (mOperation)
        {

            case CREATE:
                mProgressDialog.setTitle(getString(R.string.creating_event));
                break;
            case UPDATE:
                mProgressDialog.setTitle(getString(R.string.updating_event));
                break;
            case REMOVE:
                mProgressDialog.setTitle(getString(R.string.delete_event));
                break;


        }

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
        Event event = getArguments().getParcelable(EVENT);
        mOperation = getArguments().getInt(OPERATION);
        uploadBytes(event);


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


    private void uploadBytes(Event event) {

        DatabaseReference eventReference = FirebaseDatabase.getInstance().getReference("events").child(event.getId());

        if (mOperation==REMOVE)
        {
            eventReference.removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("prg", (int) 100);
                    message.setData(bundle);
                    setProgress(message);
                    mListener.onEventFinishDialog(mOperation);
                }
            });
        }

        else{


            GeoHash geoHash = new GeoHash(new GeoLocation(event.getLatitude(), event.getLongitude()));
            Map<String, Object> updates = new HashMap<>();

            if (mOperation==CREATE) {
                Map<String, User> map = setOwnerAsParticipate();
                updates.put(PARTICIPATES, map);
            }

            updates.put(ID, event.getId());
            updates.put(DETAILS, event.getDetails());
            updates.put(START, event.getStart());
            updates.put(END, event.getEnd());
            updates.put(LATITUDE, event.getLatitude());
            updates.put(LONGITUDE, event.getLongitude());
            updates.put(TITLE, event.getTitle());
            updates.put(INTEREST, event.getInterest());
            updates.put(ADDRESS, event.getAddress());
            updates.put(OWNER, event.getOwner());
            updates.put(STATUS, true);
            updates.put(IMAGE, event.getImage());


            updates.put("/g", geoHash.getGeoHashString());
            updates.put("/l", Arrays.asList(event.getLatitude(), event.getLongitude()));
            eventReference.updateChildren(updates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("prg", (int) 100);
                    message.setData(bundle);
                    setProgress(message);
                    mListener.onEventFinishDialog(mOperation);
                }



            });
        }

    }

    private Map<String, User> setOwnerAsParticipate() {
        User owner = new User();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        owner.setName(user.getDisplayName());
        owner.setEmail(user.getEmail());
        owner.setAvatar(user.getPhotoUrl().toString());
        HashMap<String, User> map = new HashMap<>();
        map.put(user.getUid(), owner);
        return map;
    }



}
