package com.vietpt.RCTCamera;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RCTCameraUtils {
    public static String getTimeStampFile(String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return timeStamp + "." + extension;
    }

    public static String getStoragePath(String filename) {
        if(filename != null && !filename.isEmpty()) {
            return Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/" + filename;
        }

        return null;
    }

    public static Bitmap rotatePhoto(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.postRotate(orientationDegree);
        try {
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
