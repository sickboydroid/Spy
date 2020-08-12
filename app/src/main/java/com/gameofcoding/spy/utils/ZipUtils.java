package com.gameofcoding.spy.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This utility compresses a list of files to standard ZIP format file.
 */
public class ZipUtils {
    private static final String TAG = "ZipUtils";
    private static final int BUFFER_SIZE = 4096;

    public void zip(File[] files, File destZipFile) throws FileNotFoundException, IOException {
	zip(files, destZipFile, false);
    }
    /**
     * Compresses files represented in an array of paths
     */
    public void zip(File[] files, File destZipFile, boolean deleteZipped) throws FileNotFoundException,
										 IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile));
        for (File file : files) {
            if (file.isDirectory()) {
                zipDirectory(file, file.getName(), zos);
		if(deleteZipped)
		    if(!FileUtils.delete(file))
			XLog.w(TAG, "Added dir could not be deleted, dir=" + file);
            } else {
                zipFile(file, zos);
		if(deleteZipped)
		    if(!file.delete())
			XLog.w(TAG, "Added file could not be deleted, file=" + file);
            }
        }
        zos.flush();
        zos.close();
    }
    /**
     * Adds a directory to the current zip output stream
     */
    private void zipDirectory(File folder, String parentFolder,
            ZipOutputStream zos) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
            }
	    bis.close();
            zos.closeEntry();
        }
    }
    /**
     * Adds a file to the current zip output stream
     */
    private void zipFile(File file, ZipOutputStream zos)
            throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
        }
	bis.close();
        zos.closeEntry();
    }
}
