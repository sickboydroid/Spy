package com.gameofcoding.spy.utils;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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

    public static boolean write(File destFile, String data) throws IOException {
	 FileWriter fw = new FileWriter(destFile);
	 BufferedWriter bw = new BufferedWriter(fw);
	 bw.write(data);
	 bw.close();
	 fw.close();
	 return true;
    }
    
    public static boolean write(String destFile, String data) throws IOException {
	return write(new File(destFile), data);
    }
}
