package com.gameofcoding.spy.server.files;

import java.io.File;

public interface Files {
    public static final String DIR_USER_DATA = "user_data";
    public static final String DIR_OTHERS = "others";

    public abstract File getRootDir();
}
