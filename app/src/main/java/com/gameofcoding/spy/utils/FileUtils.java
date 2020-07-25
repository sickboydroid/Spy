package com.gameofcoding.spy.utils;

import java.io.File;

public class FileUtils {
    public static boolean deleteForcefully(File fileToDelete) {
	if(fileToDelete.isFile())
	    return fileToDelete.delete();
	for(File file : fileToDelete.listFiles()) {
	    if(file.isDirectory()) {
		if(!deleteForcefully(file))
		    return false;
	    } else if(!file.delete())
		return false;
	}
	return fileToDelete.delete();
    }
}
