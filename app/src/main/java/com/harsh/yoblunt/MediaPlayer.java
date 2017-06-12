package com.harsh.yoblunt;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.TextureView;

import com.google.android.exoplayer2.C;
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

/**
 * Created by Anjan on 6/11/2017.
 */

public class MediaPlayer {
    private final Context mContext;
    private SimpleExoPlayer mPlayer;
    private Handler mHandler;
    private DataSource.Factory mDataSourceFactory;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private DefaultTrackSelector mTrackSelector;
    private MediaSource mMediaSource;
    public boolean isPlaying = false;

    public MediaPlayer(Context context) {
        mContext = context;
        mDataSourceFactory = buildDataSourceFactory(BANDWIDTH_METER);
        mHandler = new Handler();
    }

    public void addEventListener(ExoPlayer.EventListener eventListener) {
        mPlayer.addListener(eventListener);
    }

    public void initializePlayer() {
        boolean needNewPlayer = mPlayer == null;
        if (needNewPlayer) {
            TrackSelection.Factory mAdaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            mTrackSelector = new DefaultTrackSelector(mAdaptiveTrackSelectionFactory);
            DefaultRenderersFactory mDefaultRendererFactory = new DefaultRenderersFactory(mContext,
                    null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
            mPlayer = ExoPlayerFactory.newSimpleInstance(mDefaultRendererFactory, mTrackSelector);
            mPlayer.addListener(new EventListener() {
                @Override
                public void onLoadingChanged(boolean isLoading) {
                    super.onLoadingChanged(isLoading);
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    super.onPlayerStateChanged(playWhenReady, playbackState);
                    switch (playbackState) {
                        case ExoPlayer.STATE_IDLE:
                        case ExoPlayer.STATE_ENDED:
                            isPlaying = false;
                            break;
                        case ExoPlayer.STATE_BUFFERING:
                        case ExoPlayer.STATE_READY:
                            isPlaying = true;
                            break;
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    super.onPlayerError(error);
                    isPlaying = false;
                }
            });
            mPlayer.setPlayWhenReady(true);
        }
    }

    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter mDefaultBandwidthMeter) {
        return new DefaultDataSourceFactory(mContext, mDefaultBandwidthMeter, buildHttpDataSourceFactory(mDefaultBandwidthMeter));
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter mDefaultBandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(mContext, "YoBlunt"));
    }

    public void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mTrackSelector = null;
        }
        isPlaying = false;
    }


    public SimpleExoPlayer getPlayer() {
        return mPlayer;
    }

    public void play(Uri uri, WeakReference<TextureView> refTextureView, PlaybackState playbackState) {
        if (playbackState.getResumeWindow() != C.INDEX_UNSET &&
                playbackState.getResumePosition() != C.TIME_UNSET) {
            mPlayer.seekTo(playbackState.getResumeWindow(), playbackState.getResumePosition());
        }
        mPlayer.setVideoTextureView(refTextureView.get());
        mPlayer.prepare(buildHLSMediaSource(uri), true, false);
        isPlaying = true;
    }

    private MediaSource buildHLSMediaSource(Uri uri) {
        return new HlsMediaSource(uri, mDataSourceFactory, mHandler, null);
    }

    public void stopPlayer() {
        if (isPlaying) {
            mPlayer.stop();
            isPlaying = false;
        }
    }
}
