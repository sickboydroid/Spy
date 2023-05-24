package com.tangledbytes.sw.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * @param root    Directory whose files you want to scan
     * @param scanner Interface called on each file
     */
    public static void scanFiles(File root, FileScanner scanner) {
        File[] files = root.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                scanFiles(file, scanner);
                continue;
            }
            scanner.scan(file);
        }
    }

    /**
     * Deletes given directory or file recursively
     */
    public static boolean delete(File fileToDelete) {
        if (fileToDelete.isFile()) {
            if (!fileToDelete.delete()) {
                Log.v(TAG, "delete(File): Could not delete, " + fileToDelete);
                return false;
            }
            return true;
        }
        File[] files = fileToDelete.listFiles();
        if (files == null) return true;
        for (File file : files) {
            if (file.isDirectory()) {
                if (!delete(file)) return false;
            } else if (!file.delete()) {
                Log.d(TAG, "Could not delete file, " + file);
                return false;
            }
        }

        // Delete root folder
        if (!fileToDelete.delete()) {
            Log.d(TAG, "Could not delete dir, " + fileToDelete);
            return false;
        }
        return true;
    }

    public static void write(String destFile, String data) throws IOException {
        write(new File(destFile), data);
    }

    public static void write(File destFile, String data) throws IOException {
        write(destFile, data, false);
    }

    public static void write(String destFile, String data, boolean append) throws IOException {
        write(new File(destFile), data, append);
    }

    public static void write(File destFile, String data, boolean append) throws IOException {
        FileWriter fw = new FileWriter(destFile, append);
        fw.write(data);
        fw.close();
    }

    public static String read(String filePath) throws IOException {
        return read(new File(filePath));
    }

    public static String read(File file) throws IOException {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String data = "";
        String line = null;
        while ((line = br.readLine()) != null) data += "\n" + line;
        br.close();
        fr.close();

        // Remove the extra line that was added in first iteration of loop
        if (!data.isEmpty()) data = data.substring(1);
        return data;
    }


    @FunctionalInterface
    public interface FileScanner {
        void scan(File file);
    }
}
