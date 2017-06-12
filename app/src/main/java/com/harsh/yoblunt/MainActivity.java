package com.harsh.yoblunt;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private MediaPlayer mMediaPlayer;
    private Uri mUri = Uri.parse("http://playertest.longtailvideo.com/adaptive/bbbfull/bbbfull.m3u8");
    private Uri[] mUris = {mUri, Uri.parse("http://devimages.apple.com/samplecode/adDemo/ad.m3u8"), mUri, mUri, mUri, mUri, mUri, mUri, mUri, mUri};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaPlayer = new MediaPlayer(this);
        mMediaPlayer.initializePlayer();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        VideoListAdapter adapter = new VideoListAdapter(mRecyclerView, mMediaPlayer, mUris);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(Build.VERSION.SDK_INT <= 23){
            mMediaPlayer.releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(Build.VERSION.SDK_INT > 23){
            mMediaPlayer.releasePlayer();
        }
    }
}
