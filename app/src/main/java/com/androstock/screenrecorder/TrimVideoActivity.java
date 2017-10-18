package com.androstock.screenrecorder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;

public class TrimVideoActivity extends AppCompatActivity {
    VideoView videoView;
    RangeSeekBar<Integer> rangeSeekBar;
    Uri videoUri;
    int duration;
    int seekTo;
    int start;
    long selectedVideoId;
    String path;
    Button trimVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim_video);

        selectedVideoId = getIntent().getExtras().getLong("selected_video");
        initializeViews();
    }

    public static  void startActivity(Context context){
        Intent intent=new Intent(context,SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void initializeViews() {
        getSupportActionBar().setTitle("Trim Video");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        videoView = (VideoView) findViewById(R.id.videoView);
        trimVideo = (Button) findViewById(R.id.trimVideo);
        rangeSeekBar = (RangeSeekBar<Integer>) findViewById(R.id.rangeSeekBar);
        if(selectedVideoId!=0){
            path = RecordedVideoModel.findById(RecordedVideoModel.class, selectedVideoId).getVideoLinkSdCard();
            videoUri = FileProvider.getUriForFile(TrimVideoActivity.this, AppConstant.FILE_PROVIDER, new File(path));
            setVideoData();
        }


    }

    public void setVideoData() {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(this.videoView);
        this.videoView.setMediaController(mediaController);
        this.videoView.setVideoURI(videoUri);
        this.rangeSeekBar.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        this.rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Integer minValue, Integer maxValue) {
                if (start != ((Integer) rangeSeekBar.getSelectedMinValue()).intValue()) {
                    start = ((Integer) rangeSeekBar.getSelectedMinValue()).intValue();
                    videoView.seekTo(start * 1000);
                }
                if (seekTo != ((Integer) rangeSeekBar.getSelectedMaxValue()).intValue()) {
                    seekTo = ((Integer) rangeSeekBar.getSelectedMaxValue()).intValue();
                    videoView.seekTo(seekTo * 1000);
                }
            }
        });
        this.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                duration = videoView.getDuration();
                start = 0;
                seekTo = duration / 1000;
                rangeSeekBar.setRangeValues(start, seekTo);
                rangeSeekBar.setSelectedMinValue(Integer.valueOf(start));
                rangeSeekBar.setSelectedMaxValue(Integer.valueOf(seekTo));
                videoView.seekTo(start);
            }
        });

        trimVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(path!=null&&videoUri!=null){
                    trimVideo();
                }else {
                    Toast.makeText(TrimVideoActivity.this, "Please Select Video to Trim..", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public void trimVideo() {

        TrimmVideo trimmVideo = new TrimmVideo(path, start, seekTo);
        trimmVideo.execute();
    }


    private class TrimmVideo extends AsyncTask<Void, Void, Void> {
        private String mediaPath;
        private double startTime;
        private double endTime;
        private int length;
        private ProgressDialog progressDialog;

        private TrimmVideo(String mediaPath, int startTime, int length) {
            this.mediaPath = mediaPath;
            this.startTime = startTime;
            this.length = length;
            this.endTime = this.startTime + this.length;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(TrimVideoActivity.this,
                    "Trimming videos", "Please wait...", true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            trimVideo();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            Toast.makeText(TrimVideoActivity.this, "Video Trimmed Successfully...", Toast.LENGTH_SHORT).show();
            finish();
            super.onPostExecute(result);
        }

        private void trimVideo() {
            try {
                File file = new File(mediaPath);
                FileInputStream fis = new FileInputStream(file);
                FileChannel in = fis.getChannel();
                Movie movie = MovieCreator.build(in);

                List<Track> tracks = movie.getTracks();
                movie.setTracks(new LinkedList<Track>());

                boolean timeCorrected = false;

                // Here we try to find a track that has sync samples. Since we can only start decoding
                // at such a sample we SHOULD make sure that the start of the new fragment is exactly
                // such a frame
                for (Track track : tracks) {
                    if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                        if (timeCorrected) {
                            // This exception here could be a false positive in case we have multiple tracks
                            // with sync samples at exactly the same positions. E.g. a single movie containing
                            // multiple qualities of the same video (Microsoft Smooth Streaming file)

                            //throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                        } else {
                            startTime = correctTimeToNextSyncSample(track, startTime);
                            timeCorrected = true;
                        }
                    }
                }

                for (Track track : tracks) {
                    long currentSample = 0;
                    double currentTime = 0;
                    long startSample = -1;
                    long endSample = -1;

                    for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
                        TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
                        for (int j = 0; j < entry.getCount(); j++) {
                            // entry.getDelta() is the amount of time the current sample covers.

                            if (currentTime <= startTime) {
                                // current sample is still before the new starttime
                                startSample = currentSample;
                            } else if (currentTime <= endTime) {
                                // current sample is after the new start time and still before the new endtime
                                endSample = currentSample;
                            } else {
                                // current sample is after the end of the cropped video
                                break;
                            }
                            currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                            currentSample++;
                        }
                    }
                    movie.addTrack(new CroppedTrack(track, startSample, endSample));
                }
                //if(startTime==length)
                //throw new Exception("times are equal, something went bad in the conversion");

                IsoFile out = new DefaultMp4Builder().build(movie);

                File myMovie = AppConstant.createVideoFile("Trim_", getApplicationContext()); //new File(storagePath, String.format("output-%s-%f-%d.mp4", timestampS, startTime*1000, length*1000));

                FileOutputStream fos = new FileOutputStream(myMovie);
                FileChannel fc = fos.getChannel();
                out.getBox(fc);

                fc.close();
                fos.close();
                fis.close();
                in.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private double correctTimeToNextSyncSample(Track track, double cutHere) {
            double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
            long currentSample = 0;
            double currentTime = 0;
            for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
                TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
                for (int j = 0; j < entry.getCount(); j++) {
                    if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                        // samples always start with 1 but we start with zero therefore +1
                        timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
                    }
                    currentTime += (double) entry.getDelta() / (double) track.getTrackMetaData().getTimescale();
                    currentSample++;
                }
            }
            for (double timeOfSyncSample : timeOfSyncSamples) {
                if (timeOfSyncSample > cutHere) {
                    return timeOfSyncSample;
                }
            }
            return timeOfSyncSamples[timeOfSyncSamples.length - 1];
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trim_video, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (itemId == R.id.action_trim_video) {
            trimVideo();
        } else if (itemId == R.id.action_add_video) {

            Intent intent = new Intent("android.intent.action.PICK", MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
            startActivityForResult(intent, 1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            videoUri =data.getData();
            try {
                path=PathUtil.getPath(getApplicationContext(),videoUri);
                setVideoData();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static  void startActivity(Context context,long selectedVideoId){
        Intent intent=new Intent(context,TrimVideoActivity.class);
        intent.putExtra("selected_video",selectedVideoId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
