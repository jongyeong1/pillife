package com.crontiers.pillife.Camera;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class FileManager {
    public static void writeFrame(String fileName, byte[] data) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
            bos.write(data);
            bos.flush();
            bos.close();
//            Log.e(TAG, "" + data.length + " bytes have been written to " + filesDir + fileName + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

