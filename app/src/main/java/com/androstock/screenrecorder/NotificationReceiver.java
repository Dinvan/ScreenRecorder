package com.androstock.screenrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    private ScreenRecordService mActivity;

    public NotificationReceiver(ScreenRecordService mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String action = intent.getAction();

        if (AppConstant.START_ACTION.equals(action)) {
            mActivity.onToggleScreenShare();
        }else if(AppConstant.NOTIFICATION_CLEAR.equals(action)){
            if(mActivity.isRecording){
                mActivity.onToggleScreenShare();
            }
        }
    }
}
