package com.androstock.screenrecorder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by advanz101 on 13/10/17.
 */

public class VideoItemLayoutAdapter extends RecyclerView.Adapter<VideoItemLayoutAdapter.ViewHolder> {

    List<RecordedVideoModel> recordedVideoModelList;
    Context context;
    private String[] e = new String[]{"_display_name", "_size", "width","height"};

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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        initializeViews(recordedVideoModelList.get(position), holder);
    }

    private void initializeViews(final RecordedVideoModel recordedVideoModel, final ViewHolder holder) {
        File file = new File(recordedVideoModel.getVideoLinkSdCard());
        Bitmap bMap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
        holder.videoThumbnail.setImageBitmap(bMap);
        holder.txtRecordTime.setText(AppConstant.getFormattedDateTime(Long.parseLong(recordedVideoModel.getVideoDate()), AppConstant.DISPLAY_PATTERN));
        holder.txtVideoResolution.setText(recordedVideoModel.getVideoComment());
        Uri videoUri = FileProvider.getUriForFile(context, AppConstant.FILE_PROVIDER, file);
        Cursor query = MediaStore.Video.query(context.getContentResolver(), videoUri, e);
        if (query == null || query.getCount() <= 0) {

            return;
        }
        if (query.moveToFirst()) {
            Log.e("Column Name", query.getColumnName(0) + " ," + query.getColumnName(1));
            holder.txtVideoDuration.setText(query.getString(query.getColumnIndex(this.e[0])));
            holder.txtVideoSize.setText("Size " + readableFileSize(query.getLong(query.getColumnIndex(this.e[1]))));
        }
        query.close();

        holder.btnPopupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.itemView, recordedVideoModel);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Uri uri = FileProvider.getUriForFile(context, AppConstant.FILE_PROVIDER, new File(recordedVideoModel.getVideoLinkSdCard()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(uri, "video/mp4");
                context.startActivity(intent);*/
               Intent intent=new Intent(context,TrimVideoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("selected_video",recordedVideoModel.getId());
                context.startActivity(intent);
            }
        });
        //TODO implement
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, RecordedVideoModel position) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_video, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        RecordedVideoModel position;

        public MyMenuItemClickListener(RecordedVideoModel position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.share_image:
                    Toast.makeText(context, "TEST", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.set_as_wallpaper:

                    return true;
                default:
            }
            return false;
        }
    }


    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    @Override
    public int getItemCount() {
        return recordedVideoModelList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
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
