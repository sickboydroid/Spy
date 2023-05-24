package com.tangledbytes.sw.collectors;

import com.tangledbytes.sw.FileScanner;

import java.io.File;

public class Files {

    public static void scanFiles(File root, FileScanner scanner) {
        if(root.getName().equals("images"))
            return;
        File[] files = root.listFiles();
        if(files == null) return;
        for (File file : files) {
            if (file.isDirectory())
                scanFiles(file, scanner);
            else
                scanner.scan(file);
        }
    }
}

