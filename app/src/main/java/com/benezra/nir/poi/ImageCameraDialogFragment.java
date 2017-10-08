package com.benezra.nir.poi;

/**
 * Created by nir on 08/10/2017.
 */

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.benezra.nir.poi.Bitmap.BitmapUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ImageCameraDialogFragment extends DialogFragment implements View.OnClickListener {

    private ImageCameraDialogCallback mListener;
    private EditText mDialogTitle;
    private Button mDialogFinish;
    private ImageView mDialogImageView;
    private Context mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof ImageCameraDialogFragment.ImageCameraDialogCallback) {
            mListener = (ImageCameraDialogFragment.ImageCameraDialogCallback) context;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", mDialogTitle.getText().toString());
        outState.putParcelable("image", getBitmap());

    }

    private Bitmap getBitmap() {
        return ((BitmapDrawable) mDialogImageView.getDrawable()).getBitmap();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_login, container, false);

        mDialogTitle = (EditText) view.findViewById(R.id.et_dialogtitle);
        mDialogFinish = (Button) view.findViewById(R.id.btn_dialogfinish);
        mDialogImageView = (ImageView) view.findViewById(R.id.iv_dialogimage);
        mDialogImageView.setOnClickListener(this);
        mDialogFinish.setOnClickListener(this);

        if (savedInstanceState != null) {
            String title = savedInstanceState.getString("title");
            Bitmap bitmap = savedInstanceState.getParcelable("image");
            if (title != null)
                mDialogTitle.setText(title);
            if (bitmap != null) {
                mDialogImageView.setImageBitmap(bitmap);
            }

        }


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_dialogimage:
                startActivityForResult(getPickImageChooserIntent(), 200);
                break;
            case R.id.btn_dialogfinish:
                mListener.DialogResults(mDialogTitle.getText().toString(), getBitmap());
                dismiss();
                break;

        }
    }

    public interface ImageCameraDialogCallback {
        void DialogResults(String title, Bitmap bitmap);
    }

    /**
     * Create a chooser intent to select the source to get mDialogImageView from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera mDialogImageView to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getActivity().getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    /**
     * Get URI to mDialogImageView received from capture by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getActivity().getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected mDialogImageView from {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera and gallery mDialogImageView.
     *
     * @param data the returned data of the activity result
     */
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Bitmap bitmap;
        if (resultCode == Activity.RESULT_OK) {

            if (getPickImageResultUri(data) != null) {
                Uri PicUri = getPickImageResultUri(data);

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), PicUri);
                    //mDialogBitmap = rotateImageIfRequired(mDialogBitmap, mPicUri);
                    bitmap = BitmapUtil.getResizedBitmap(bitmap, 500);

                    mDialogImageView.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else {


                bitmap = (Bitmap) data.getExtras().get("data");
                mDialogImageView.setImageBitmap(bitmap);

            }


        }

    }

}
