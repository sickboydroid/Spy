package com.gameofcoding.spy.io;

import java.io.File;

public interface FilePaths {
    public static final String DIR_USER_DATA = "user_data";
    public static final String DIR_COMMANDS = "cmds";
    public static final String DIR_OTHERS = "others";

    public abstract File getRootDir();
}
