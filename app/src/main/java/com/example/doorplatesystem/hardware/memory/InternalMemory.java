package com.example.doorplatesystem.hardware.memory;

import android.os.Environment;

public class InternalMemory extends AbstractMemory{
    public static String getMemoryPath (String path){
        return Environment.getExternalStorageDirectory().getPath() + "/" +path;
    }
}
