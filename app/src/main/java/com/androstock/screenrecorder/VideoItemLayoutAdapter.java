package com.androstock.screenrecorder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by advanz101 on 13/10/17.
 */

public class VideoItemLayoutAdapter extends RecyclerView.Adapter<VideoItemLayoutAdapter.ViewHolder>{

    List<RecordedVideoModel> recordedVideoModelList;
    Context context;
    private String[] e = new String[]{"_display_name", "duration", "resolution", "_size"};
    public VideoItemLayoutAdapter(List<RecordedVideoModel> recordedVideoModelList, Context context) {
        this.recordedVideoModelList = recordedVideoModelList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
     initializeViews(recordedVideoModelList.get(position),holder);
    }

    private void initializeViews(RecordedVideoModel recordedVideoModel, ViewHolder holder) {
        File file=new File(recordedVideoModel.getVideoLinkSdCard());
        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
        holder.videoThumbnail.setImageBitmap(bMap);
        holder.txtRecordTime.setText(AppConstant.getFormattedDateTime(Long.parseLong(recordedVideoModel.getVideoDate()),AppConstant.DISPLAY_PATTERN));
        Uri videoUri = FileProvider.getUriForFile(context, AppConstant.FILE_PROVIDER, file);
        Cursor query = MediaStore.Video.query(context.getContentResolver(),videoUri,e);
        if (query == null || query.getCount() <= 0) {

            return;
        }
        if (query.moveToFirst()) {

           //holder.txt.setText(query.getString(query.getColumnIndex(this.e[0])));
            holder.txtVideoDuration.setText("Duration".concat(DurationUtill.b(query.getLong(query.getColumnIndex(this.e[1])))));
            holder.txtVideoResolution.setText("Resolution".concat(query.getString(query.getColumnIndex(this.e[2]))));
            holder.txtVideoSize.setText("Size".concat(String.valueOf(query.getLong(query.getColumnIndex(this.e[3])))));
        }
        query.close();
        //TODO implement
    }




    @Override
    public int getItemCount() {
        return recordedVideoModelList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView videoThumbnail;
        private LinearLayout videoInfo;
        private TextView txtRecordTime;
        private TextView txtVideoDuration;
        private TextView txtVideoResolution;
        private TextView txtVideoSize;
        private ImageButton btnPopupMenu;

        public ViewHolder(View view) {
            super(view);

            videoThumbnail = (ImageView) view.findViewById(R.id.videoThumbnail);
            videoInfo = (LinearLayout) view.findViewById(R.id.videoInfo);
            txtRecordTime = (TextView) view.findViewById(R.id.txtRecordTime);
            txtVideoDuration = (TextView) view.findViewById(R.id.txtVideoDuration);
            txtVideoResolution = (TextView) view.findViewById(R.id.txtVideoResolution);
            txtVideoSize = (TextView) view.findViewById(R.id.txtVideoSize);
            btnPopupMenu = (ImageButton) view.findViewById(R.id.btnPopupMenu);
        }
    }
}
