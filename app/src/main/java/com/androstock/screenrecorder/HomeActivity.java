package com.androstock.screenrecorder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1000;
    RecyclerView videos_list;
    private MediaProjectionManager mProjectionManager;
    private static final int REQUEST_PERMISSION_KEY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                        REQUEST_CODE);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setVideoList();
    }

    public void setVideoList() {
        videos_list = (RecyclerView) findViewById(R.id.videos_list);

        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };
        if (!Function.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        videos_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        VideoItemLayoutAdapter videoItemLayoutAdapter = new VideoItemLayoutAdapter(RecordedVideoModel.listAll(RecordedVideoModel.class), getApplicationContext());
        videos_list.setAdapter(videoItemLayoutAdapter);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_videos) {
            // Handle the camera action
        } else if (id == R.id.nav_screenshot) {

        } else if (id == R.id.nav_settings) {
            SettingsActivity.startActivity(HomeActivity.this);
        } else if (id == R.id.nav_trim_video) {
            TrimVideoActivity.startActivity(HomeActivity.this,0);
        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_share_app) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, ScreenRecordService.class).putExtra(ScreenRecordService.EXTRA_RESULT_CODE, resultCode).putExtra(ScreenRecordService.EXTRA_RESULT_INTENT, data);
        startService(i);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_KEY: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                            REQUEST_CODE);
                } else {

                    Snackbar.make(findViewById(android.R.id.content), "Please enable Microphone and Storage permissions.",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
        }
    }


}
