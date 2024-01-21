package org.example.utils;

import net.lingala.zip4j.ZipFile;

import java.io.IOException;

public class ZipUtil {

    public static void zip(String fileName) {
        try {
            new ZipFile("zip.zip").addFile(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
