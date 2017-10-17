package com.androstock.screenrecorder;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by advanz101 on 13/10/17.
 */

public class AppConstant {
    public static final String IMAGE_FOLDER = "ScreenRecorder";
    public static final String FILE_PROVIDER = "com.androstock.screenrecorder.fileprovider";
    public static final String START_ACTION = "START_ACTION";
    public static final String STOP_ACTION = "STOP_ACTION";
    public static final String NOTIFICATION_CLEAR = "notification_cancelled";
    public static String DISPLAY_PATTERN = "dd MMM, yyyy";

    public static File createVideoFile(Context context) throws IOException {
        // Create an image file name

        String imageFileName = "RECORD_" + System.currentTimeMillis() + "_";
        File storageDir = context.getExternalFilesDir(IMAGE_FOLDER);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        // mCurrentPhotoPath = image.getAbsolutePath();
        Log.e("newImage", image.getAbsolutePath());
        return image;
    }

    public static File createVideoFile(String prefix,Context context) throws IOException {
        // Create an image file name

        String imageFileName = prefix+ System.currentTimeMillis() + "_";
        File storageDir = context.getExternalFilesDir(IMAGE_FOLDER);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        // mCurrentPhotoPath = image.getAbsolutePath();
        Log.e("newImage", image.getAbsolutePath());
        return image;
    }

    public static String getFormattedDateTime(long time, String pattern) {
        SimpleDateFormat sdfDate = new SimpleDateFormat(pattern, Locale.ENGLISH);
        Date date = new Date();
        date.setTime(time);
        return sdfDate.format(date);
    }

}
