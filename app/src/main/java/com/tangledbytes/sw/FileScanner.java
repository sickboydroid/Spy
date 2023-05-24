package com.tangledbytes.sw;

import java.io.File;

@FunctionalInterface
public interface FileScanner {
    void scan(File file);
}
