package com.androstock.screenrecorder;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by advanz101 on 13/10/17.
 */

public class DurationUtill {
    public static float a(float f, Context context) {
        return (((float) context.getResources().getDisplayMetrics().densityDpi) * f) / 160.0f;
    }

    public static String a(long j) {
        return String.format(Locale.US, "%.2f", new Object[]{Float.valueOf((((float) j) / 1024.0f) / 1024.0f)}) + "MB";
    }

    public static void a(Context context) {
        context.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    public static void a(View view, float f) {
        view.animate().alpha(0.01f * f).start();
    }

    public static String b(long j) {
        long j2 = j / 1000;
        long j3 = (j2 / 60) / 60;
        j2 = (j2 - (60 * ((j2 - (j3 * 3600)) / 60))) - (j3 * 3600);
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", new Object[]{Long.valueOf(j3), Long.valueOf(r4), Long.valueOf(j2)});
    }

    public static String c(long j) {
        return String.format(Locale.getDefault(), "%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(j)), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(j)))});
    }
}
