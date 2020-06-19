package com.gameofcoding.spy.spys;

import java.io.File;

public abstract class Spy {
    public abstract boolean hasPermissions();

    public abstract Spy load();

    public abstract boolean save(File file) throws Exception;
}
