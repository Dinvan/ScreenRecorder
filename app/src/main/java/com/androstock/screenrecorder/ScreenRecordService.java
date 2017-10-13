package com.androstock.screenrecorder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class ScreenRecordService extends Service {

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    boolean isRecording;
    private int mScreenDensity;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static final String EXTRA_RESULT_CODE = "resultCode";
    static final String EXTRA_RESULT_INTENT = "resultIntent";
    WindowManager windowManager;
    Intent intentData;
    int resultCode;

    public ScreenRecordService() {
    }

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.


        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setBroadCastReceiver();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mMediaRecorder = new MediaRecorder();
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        resultCode = i.getIntExtra(EXTRA_RESULT_CODE, -1);
        intentData = i.getParcelableExtra(EXTRA_RESULT_INTENT);
        showRecordingNotification();
        return (START_NOT_STICKY);
    }

    public void initMediaProjection() {
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, intentData);
        mMediaProjectionCallback = new MediaProjectionCallback();
    }


    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (isRecording) {
                isRecording = false;

                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            mMediaProjection = null;

        }
    }

    public void onToggleScreenShare() {
        if (!isRecording) {
            startShareScreen();
        } else {
            stopScreenRecording();
        }
    }

    public void stopScreenRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            stopScreenSharing();
        }
    }


    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("ScreenRecordService", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mMediaRecorder.getSurface(), null, null);
    }
    File destination;
    private void initRecorder() {
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //THREE_GPP
            destination  = AppConstant.createVideoFile(this);
            //Uri cameraImageUri = FileProvider.getUriForFile(this, AppConstant.FILE_PROVIDER, destination);

            mMediaRecorder.setOutputFile(destination.getAbsolutePath());
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(16); // 30
            mMediaRecorder.setVideoEncodingBitRate(3000000);

            Display display = windowManager.getDefaultDisplay();
            int rotation = display.getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e("Exesp", e.toString());
        }
    }


    private void startShareScreen() {
        initMediaProjection();
        initRecorder();
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        isRecording = true;
        Toast.makeText(getApplicationContext(), "Screen Recording Started.", Toast.LENGTH_SHORT).show();
        mBuilder.mActions.clear();
        Intent yesReceive2 = new Intent();
        yesReceive2.setAction(AppConstant.START_ACTION);
        PendingIntent pendingIntentYes2 = PendingIntent.getBroadcast(this, 12345, yesReceive2, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(android.R.drawable.ic_media_pause, "Stop Recording", pendingIntentYes2);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        destroyMediaProjection();

        RecordedVideoModel recordedVideoModel=new RecordedVideoModel();
        recordedVideoModel.setVideoComment("Video Recorded");
        recordedVideoModel.setVideoDate(System.currentTimeMillis()+"");
        recordedVideoModel.setVideoLink("");
        recordedVideoModel.setVideoLinkSdCard(destination.getAbsolutePath());
        recordedVideoModel.save();

        isRecording = false;
        Toast.makeText(getApplicationContext(), "Screen Recording Stopped.", Toast.LENGTH_SHORT).show();
        mBuilder.mActions.clear();
        Intent yesReceive2 = new Intent();
        yesReceive2.setAction(AppConstant.START_ACTION);
        PendingIntent pendingIntentYes2 = PendingIntent.getBroadcast(this, 12345, yesReceive2, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(android.R.drawable.ic_media_play, "Start Recording", pendingIntentYes2);
        notificationManager.notify(notificationId, mBuilder.build());
    }


    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i("test", "MediaProjection Stopped");
    }

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    public void setBroadCastReceiver() {
        mReceiver = new NotificationReceiver(this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(AppConstant.STOP_ACTION);
        mIntentFilter.addAction(AppConstant.NOTIFICATION_CLEAR);
        mIntentFilter.addAction(AppConstant.START_ACTION);
        registerReceiver(mReceiver, mIntentFilter);
    }

    NotificationCompat.Builder mBuilder;
    int notificationId = 10;
    NotificationManager notificationManager;

    public void showRecordingNotification() // paste in activity
    {

        String CHANNEL_ID = "my_channel_01";
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.presence_video_away)
                        .setContentTitle("Screen Recording..")
                        .setContentText("Recording your mobile screen.");

        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(path);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent yesReceive2 = new Intent();
        yesReceive2.setAction(AppConstant.START_ACTION);
        PendingIntent pendingIntentYes2 = PendingIntent.getBroadcast(this, 12345, yesReceive2, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(android.R.drawable.ic_media_play, "Start Recording", pendingIntentYes2);
        mBuilder.setDeleteIntent(getDeleteIntent());
        notificationManager.notify(notificationId, mBuilder.build());

    }

    protected PendingIntent getDeleteIntent() {
        Intent intent = new Intent();
        intent.setAction(AppConstant.NOTIFICATION_CLEAR);
        return PendingIntent.getBroadcast(this, 12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void actions() {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        destroyMediaProjection();
    }

    WindowManager getWindowManager() {
        return (windowManager);
    }


}
