package com.gameofcoding.spy.server.files;

import java.io.File;

/**
 * Interface for DataFiles and ServerFiles.
 * <b>DataFiles: It provides the directories where we actually save data.</b>
 * <b>ServerFiles: It is the repository directory. During upload data is moved
 * DataFiles into this directory.</b>
*/
public interface Files {
    public abstract File getRootDir();
}
