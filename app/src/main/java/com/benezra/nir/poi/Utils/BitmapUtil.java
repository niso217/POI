package com.benezra.nir.poi.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by nir on 08/10/2017.
 */

public class BitmapUtil {

    public static String encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

    }


    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    public static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {

        if (image==null) return null;

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap UriToBitmap(Context context, Uri uri){
        if (uri==null) return null;
        try {
            InputStream ims = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap =  BitmapFactory.decodeStream(ims);
            return getResizeBitmap(bitmap);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static Bitmap  getResizeBitmap(Bitmap b) {

        Bitmap b2 = null;
        int origWidth = b.getWidth();
        int origHeight = b.getHeight();

        final int destWidth = 500;//or the width you need

        if (origWidth > destWidth) {
            // picture is wider than we want it, we calculate its target height
            int destHeight = origHeight / (origWidth / destWidth);
            // we create an scaled bitmap so it reduces the image, not just trim it
             b2 = Bitmap.createScaledBitmap(b, destWidth, destHeight, false);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            // compress to the format you want, JPEG, PNG...
            // 70 is the 0-100 quality percentage
            b2.compress(Bitmap.CompressFormat.JPEG,50, outStream);
            // we save the file, at least until we have made use of it
//            File f = new File(Environment.getExternalStorageDirectory()
//                    + File.separator + "test.jpg");
//
//            f.getAbsolutePath();
//            try {
//                f.createNewFile();
//                //write the bytes in file
//                FileOutputStream fo = new FileOutputStream(f);
//                fo.write(outStream.toByteArray());
//                // remember close de FileOutput
//                fo.close();
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }

        }
        return b2;

    }
}
