package com.tangledbytes.sw.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.tangledbytes.sw.guides.ImageCollectorGuide;

import java.io.File;
import java.io.FileOutputStream;

public class ImageCompressor {
    public static final String TAG = "ImageCompressor";
    private final File imageSrc;
    private final File imageDest;
    private Bitmap mScaledBitmap;
    private final BitmapFactory.Options mOptions;
    private final ImageCollectorGuide guide;

    public ImageCompressor(File imageSrc, File imageDest, ImageCollectorGuide guide) {
        this.imageSrc = imageSrc;
        this.imageDest = imageDest;
        this.guide = guide;
        mOptions = new BitmapFactory.Options();
        mOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageSrc.toString(), mOptions);
    }

    public static void compress(File imageSrc, File imageDest, ImageCollectorGuide guide) {
        try {
            ImageCompressor compressor = new ImageCompressor(imageSrc, imageDest, guide);
            compressor.compress();
            compressor.writeImage();
        } catch (Exception e) {
            Log.e(TAG, "Failed to compress an image: " + e.getMessage());
        }
    }


    private int calculateInSampleSize(int reqWidth, int reqHeight) {
        final int height = mOptions.outHeight;
        final int width = mOptions.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void compress() {
        String imagePath = imageSrc.toString();
        int actualHeight = mOptions.outHeight;
        int actualWidth = mOptions.outWidth;

        // max Height and width values of the compressed image
        float maxHeight = guide.getImageMinHeight();
        float maxWidth = guide.getImageMinWidth();
        float imgRatio = (float) actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        // setting inSampleSize value allows to load a scaled down version of the original image
        mOptions.inSampleSize = calculateInSampleSize(actualWidth, actualHeight);

        // inJustDecodeBounds set to false to load the actual bitmap
        mOptions.inJustDecodeBounds = false;
        Bitmap bitmapImage = null;
        try {
            // load the bitmap from its path
            bitmapImage = BitmapFactory.decodeFile(imagePath, mOptions);
            mScaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Exception occurred while loading bitmap in memory", e);
        }

        float ratioX = actualWidth / (float) mOptions.outWidth;
        float ratioY = actualHeight / (float) mOptions.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(mScaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmapImage,
                middleX - (float) bitmapImage.getWidth() / 2,
                middleY - (float) bitmapImage.getHeight() / 2,
                new Paint(Paint.FILTER_BITMAP_FLAG));
        mScaledBitmap = Bitmap.createBitmap(mScaledBitmap, 0, 0,
                mScaledBitmap.getWidth(),
                mScaledBitmap.getHeight(),
                null, true);
    }

    public void writeImage() {
        try {
            FileOutputStream fos = new FileOutputStream(imageDest);
            mScaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Error occurred while storing image", e);
        }
    }
}
