package com.ed;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by shicongyu01_91 on 2015/3/10.
 */
public class EmailGen {
    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\shicongyu01_91\\Desktop\\email.txt");
        for (int i = 0; i < 100; i++) {
            FileUtils.write(file, "goodboy1a2" + i + "\r\n", true);
        }
    }
}